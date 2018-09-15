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
import javax.json.JsonValue;
import java.io.InputStream;
import java.net.URL;

/**
 * @author Clebert Suconic
 */

public class RestList {

   public String jira;
   public String url;

   public String getJiraLookup() {
      return jira;
   }

   public RestList setJiraLookup(String jira) {
      this.jira = jira;
      return this;
   }

   public String getUrl() {
      return url;
   }

   public RestList setUrl(String url) {
      this.url = url;
      return this;
   }

   public void lookup() throws Exception {
      int start = 0;
      int total = -1;

      while (true) {
         URL resturl = new URL(url);

         InputStream stream = resturl.openStream();

         JsonObject object = Json.createReader(stream).readObject();

         if (total == -1) {
            total = object.getInt("total");
         }


         JsonArray issues = object.getJsonArray("issues");

         System.out.println("Object: " + object + ", issues=" + issues);

         for (int i = 0; i < issues.size(); i++) {
            JsonObject item = (JsonObject)issues.get(i);
            System.out.println("Key=" + item.get("key"));

            JsonObject fields = item.getJsonObject("fields");

            for (int f = 0; f < fields.size(); f++) {
               JsonObject field = (JsonObject)fields.get(f);
               //System.ou
            }


         }

      }

   }
}
