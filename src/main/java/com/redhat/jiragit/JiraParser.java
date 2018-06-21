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
import javax.json.JsonObject;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;

import static com.redhat.jiragit.LinkUtility.makeALink;

/**
 * @author Clebert Suconic
 */

public class JiraParser {

   String jira;
   String jiraBrowseURI;

   String restLocation;

   // JQL used to list all JIRAs here
   String sampleJQL;

   String[] currentJiras;


   JsonObject lastJIRAObject;
   String lastJIRA;

   final HashSet<String> totalJiras = new HashSet<>();

   public JiraParser() {
   }

   public String getJira() {
      return jira;
   }

   public JiraParser setJira(String jira) {
      this.jira = jira;
      return this;
   }

   public String getJiraBrowseURI() {
      return jiraBrowseURI;
   }

   public JiraParser setJiraBrowseURI(String jiraBrowseURI) {
      this.jiraBrowseURI = jiraBrowseURI;
      return this;
   }

   public String getRestLocation() {
      return restLocation;
   }

   public JiraParser setRestLocation(String restLocation) {
      this.restLocation = restLocation;
      return this;
   }

   public String getSampleJQL() {
      return sampleJQL;
   }

   public JiraParser setSampleJQL(String sampleJQL) {
      this.sampleJQL = sampleJQL;
      return this;
   }

   public String prettyCommitMessage(String message) {
      currentJiras = extractJIRAs(message);
      for (int i = 0; i < currentJiras.length; i++) {
         totalJiras.add(currentJiras[i]);
         message = message.replace(currentJiras[i], makeALink(currentJiras[i], jiraBrowseURI + currentJiras[i]));
      }

      return message;
   }


   public String getJIRAStatus() throws Exception {
      StringBuffer bufferJIRA = new StringBuffer();
      if (currentJiras != null) {
         for (int i = 0; i < currentJiras.length; i++) {

            String jiraIteration = currentJiras[i];
            JsonObject object = null;
            if (restLocation != null) {
               object = restJIRA(jiraIteration);
            }
            // it could happen the object is returning null for security or something else
            if (object != null) {
               String issuetype = getField(object, "issuetype");
               String status = getField(object, "status");
               String resolution = getField(object, "resolution");
               String priority = getField(object, "priority");
               bufferJIRA.append(makeALink(priority + "/" + issuetype + "/" + resolution + "/" + status, jiraBrowseURI + jiraIteration));
            } else {
               bufferJIRA.append(makeALink(jiraIteration, jiraBrowseURI + jiraIteration));
            }

            if (i < currentJiras.length -1) {
               bufferJIRA.append(",");
            }
         }
      }
      return bufferJIRA.toString();
   }



   private JsonObject restJIRA(String JIRA) throws Exception {

      System.out.println("Inspecting " + JIRA);
      if (lastJIRA != null && lastJIRA.equals(JIRA)) {
         return lastJIRAObject;
      }
      if (restLocation != null) {
         try {
            URL url = new URL(restLocation + JIRA);
            InputStream stream = url.openStream();

            lastJIRA = JIRA;
            lastJIRAObject = Json.createReader(stream).readObject();
         } catch (Throwable e) {
            e.printStackTrace();
            lastJIRAObject = null;
            lastJIRA = null;
         }
         return lastJIRAObject;
      }
      return null;
   }



   private String getField(JsonObject object, String name) {
      try {
         return object.getJsonObject("fields").getJsonObject(name).getString("name");
      } catch (Throwable e) {
         return " ";
      }
   }



   public String[] extractJIRAs(String message) {
      HashSet list = new HashSet(1);
      for (int jiraIndex = message.indexOf(jira); jiraIndex >= 0; jiraIndex = message.indexOf(jira, jiraIndex)) {
         StringBuffer jiraID = new StringBuffer(jira);

         for (int i = jiraIndex + jira.length(); i < message.length(); i++) {
            char charAt = message.charAt(i);
            if (charAt >= '0' && charAt <= '9') {
               jiraID.append(charAt);
            } else {
               break;
            }
         }
         list.add(jiraID.toString());
         jiraIndex++;
      }

      return (String[]) list.toArray(new String[list.size()]);
   }


   public void generateSQL(PrintStream output) {

      if (sampleJQL != null && !totalJiras.isEmpty()) {
         output.println("<br><h2>");
         output.print("<a href='" + sampleJQL + "(");

         Iterator<String> jiraIterator = totalJiras.iterator();
         StringBuffer bufferJiras = new StringBuffer();

         do {
            bufferJiras.append(jiraIterator.next());
            if (jiraIterator.hasNext()) {
               bufferJiras.append("%2C");
            }
         } while (jiraIterator.hasNext());

         output.print(bufferJiras.toString());
         output.println(")'>" + totalJiras.size() + " JIRAS on this Report</a></h2>");
      }

   }


}
