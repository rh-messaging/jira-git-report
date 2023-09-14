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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class RestList {

   public String jira;
   public String authStringProperty;
   public HashMap<String, Set<String>> interestingLabels = new HashMap<>();
   public String nonRequiredCherryPickLabel;
   public String queryURL;
   public String baseURL;

   public Set<Pair<String, String>> setJiras = new HashSet<>();

   public String getJiraLookup() {
      return jira;
   }

   public RestList addInterestLabel(String interestedLabel) {
      interestingLabels.put(interestedLabel, new HashSet<String>());
      return this;
   }

   public RestList setJiraLookup(String jira) {
      this.jira = jira;
      return this;
   }

   public String getAuthStringProperty() {
      return authStringProperty;
   }

   public RestList setAuthStringProperty(String authStringProperty) {
      this.authStringProperty = authStringProperty;
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

      String authString = System.getProperty(authStringProperty);

      System.out.println("URL: " + url);

      if (authString != null) {
         System.out.println("AuthStringProperty: " + authStringProperty);
         urlConnection.setRequestProperty("Authorization", authString);
      }

      return urlConnection.getInputStream();
   }

   public interface RestIntercept {
      public void intercept(JsonObject jira, JsonString jirakey, JsonObject fields);
   }

   public void lookup() throws Exception {
      lookup(null);
   }

   public void lookup(RestIntercept intercept) throws Exception {
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

            JsonObject fields = (JsonObject) item.get("fields");

            if (intercept != null) {
               intercept.intercept(item, jirakey, fields);
            }

            JsonArray labels = (JsonArray) fields.get("labels");

            if (labels != null) {
               for (int l = 0; l < labels.size(); l++) {
                  String labelUsed = labels.getString(l, null);
                  if (labelUsed != null) {
                     Set<String> jirasOnLabel = interestingLabels.get(labelUsed);

                     if (jirasOnLabel != null) {
                        jirasOnLabel.add(jirakey.getString());
                     }
                  }
               }
            }

            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            writer.println(item.toString());

            HashSet<String> jiras = new HashSet<>();
            JiraParser.extractJIRAs(stringWriter.toString(), jira, jiras);

            if (!jiras.isEmpty()) {
               for (String jiraFound : jiras) {
                  System.out.println(jirakey.getString() + "=" + jiraFound);
                  setJiras.add(new Pair<>(jirakey.getString(), jiraFound));
               }
            }
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
