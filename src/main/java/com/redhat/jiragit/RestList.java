/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.redhat.jiragit;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;

public class RestList {

   public String userPassProperty;
   public String jira;
   public String queryURL;
   public String baseURL;

   public Set<Pair<String, String>> setJiras = new HashSet<>();

   public String getJiraLookup() {
      return jira;
   }

   public RestList setJiraLookup(String jira) {
      this.jira = jira;
      return this;
   }

   public String getUserPassProperty() {
      return userPassProperty;
   }

   public RestList setUserPassProperty(String userPassProperty) {
      this.userPassProperty = userPassProperty;
      return this;
   }

   public String getBaseURL() {
      return baseURL;
   }

   public RestList setBaseURL(String baseURL) {
      this.baseURL = baseURL;
      return this;
   }

   public String getQueryURL() {
      return queryURL;
   }

   public RestList setQueryUrl(String url) {
      this.queryURL = url;
      return this;
   }

   public InputStream openStream(URL url) throws Exception {
      URLConnection urlConnection = url.openConnection();

      // userPass should be user:password
      String userPass = System.getProperty(userPassProperty);

      if (userPass != null) {

         System.out.println("Using user/pass");
         String authString = userPass;
         String authStringEnc = new String(Base64.encodeBase64(userPass.getBytes()));
         urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);

      }

      return urlConnection.getInputStream();
   }

   public void lookup() throws Exception {
      int start = 0;
      int total = 0;

      do {
         System.out.println("Querying for JIRAs, starting at " + start);
         URL resturl = new URL(queryURL + "&startAt=" + start);

         InputStream stream = openStream(resturl);

         JsonObject object = Json.createReader(stream).readObject();

         stream.close();

         total = object.getInt("total");
         int maxResults = object.getInt("maxResults");

         JsonArray issues = object.getJsonArray("issues");

         // System.out.println("Object: " + object + ", issues=" + issues);

         for (int i = 0; i < issues.size(); i++) {
            JsonObject item = (JsonObject) issues.get(i);

            JsonString jirakey = (JsonString) item.get("key");

            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            writer.println(item.toString());

            HashSet<String> jiras = new HashSet<>();
            JiraParser.extractJIRAs(stringWriter.toString(), jira, jiras);

            if (!jiras.isEmpty()) {
               //System.out.println("Found " + jiras.size());
               for (String jiraFound : jiras) {
                  //System.out.println("jira::" + jiraFound);
                  System.out.println(jirakey.getString() + "=" + jiraFound);
                  setJiras.add(new Pair<>(jirakey.getString(), jiraFound));
               }
            }

            //
            //            JsonObject fields = item.getJsonObject("fields");
            //
            //            Iterator<Map.Entry<String, JsonValue>> iterator = fields.entrySet().iterator();
            //
            //            while (iterator.hasNext()) {
            //               Map.Entry<String, JsonValue> entry = iterator.next();
            //               System.out.println("Key::" + entry.getKey() + " == " + entry.getValue());
            //            }
            //
            //
         }

         start = start + maxResults;

      } while (start < total);

   }

   private void remoteQuery(String jirakey, String queryRemotes) throws Exception {
      URL urlRemote = new URL(queryRemotes);
      InputStream inputStream = new BufferedInputStream(openStream(urlRemote));

      ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();

      byte[] bytes = new byte[1024];

      while (true) {
         int size = inputStream.read(bytes);
         arrayOutputStream.write(bytes, 0, size);

         if (size < 1024)
            break;
      }

      String result = new String(arrayOutputStream.toByteArray());

      HashSet<String> jiras = new HashSet<>();
      JiraParser.extractJIRAs(result, jira, jiras);
      if (!jiras.isEmpty()) {
         //System.out.println("Found " + jiras.size());
         for (String jiraFound : jiras) {
            //System.out.println("jira::" + jiraFound);
            System.out.println("remote found-> " + jirakey + "=" + jiraFound);
            setJiras.add(new Pair<>(jirakey, jiraFound));
         }
      }

      inputStream.close();
   }
}
