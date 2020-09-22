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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.redhat.jiragit.LinkUtility.makeALink;

/**
 * @author Clebert Suconic
 */

public class JiraParser {

   private final String title;

   String jira;
   String jiraBrowseURI;

   String restLocation;

   boolean requireCherryPick;
   String labelException;

   // JQL used to list all JIRAs here
   String sampleJQL;

   String[] currentJiras;


   JsonObject lastJIRAObject;
   String lastJIRA;


   String upstreamJIRA;
   File upstreamFile;
   Map<String, Set<String>> upstreamList;
   Map<String, Set<String>> labelsList;

   final HashSet<String> totalJiras = new HashSet<>();

   public JiraParser(String title) {
      this.title = title;
   }

   public String getTitle() {
      return title;
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

   public void scanJIRAS(String fullMessage) {
      currentJiras = extractJIRAs(fullMessage);
   }

   public String prettyCommitMessage(String message) {
      for (int i = 0; i < currentJiras.length; i++) {
         totalJiras.add(currentJiras[i]);
         message = message.replace(currentJiras[i], makeALink(currentJiras[i], jiraBrowseURI + currentJiras[i]));
      }

      return message;
   }

   public boolean isRequireCherryPick() {
      return requireCherryPick;
   }

   public JiraParser setRequireCherryPick(boolean requireCherryPick) {
      this.requireCherryPick = requireCherryPick;
      return this;
   }

   public String getLabelException() {
      return labelException;
   }

   public JiraParser setLabelException(String labelException) {
      this.labelException = labelException;
      return this;
   }

   boolean isCherryPickRequired() {
      if (requireCherryPick && currentJiras != null && currentJiras.length > 0) {
         if (labelException != null && labelsList != null) {
            for (String jira : currentJiras) {
               Set<String> ignoreJIRA = labelsList.get(labelException);
               if (ignoreJIRA != null) {
                  if (ignoreJIRA.contains(jira)) {
                     return false;
                  }
               }
            }
         }

         return true;
      }

      return false;
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

               // if you make changes here, be careful with the closing paragraph at the end
               bufferJIRA.append("<p>" + makeALink(priority + "/" + issuetype + "/" + resolution + "/" + status + " (" + jiraIteration + ")", jiraBrowseURI + jiraIteration));
            } else {
               bufferJIRA.append(makeALink(jiraIteration, jiraBrowseURI + jiraIteration));
            }

            if (i < currentJiras.length -1) {
               // jira is opening a paragraph
               bufferJIRA.append(",</p>");
            } else {
               bufferJIRA.append("</p>");
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


   public void setUpstream(String upstreamJIRA, File upstreamFile) throws Exception {
      this.upstreamJIRA = upstreamJIRA;
      this.upstreamFile = upstreamFile;
      this.upstreamList = loadValues(upstreamFile, false);
   }

   public JiraParser setLabels(File labelsFile) throws Exception {
      this.labelsList = loadValues(labelsFile, true);
      return this;
   }


   public String[] extractJIRAs(String message) {
      HashSet locallist = new HashSet(1);
      extractJIRAs(message, jira, locallist);
      if (upstreamJIRA != null) {
         HashSet<String> upstreamLocalList = new HashSet();
         extractJIRAs(message, upstreamJIRA, upstreamLocalList);
         for (String upstreamItem : upstreamLocalList) {
            Set<String> value = upstreamList.get(upstreamItem);
            if (value != null) {
               for (String v : value) {
                  locallist.add(v);
               }
            }
         }
      }
      return (String[]) locallist.toArray(new String[locallist.size()]);
   }

   public static void extractJIRAs(String message, String jira, HashSet list) {
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
         output.println(")'>" + totalJiras.size() + " JIRAS on " + title + "</a></h2>");
      }

   }

   public static HashMap<String, Set<String>> loadValues(File stream, boolean valueAsKey) throws Exception {

      int keyLocation;
      int valueLocation;

      if (valueAsKey) {
         keyLocation = 1;
         valueLocation = 0;
      } else {
         keyLocation = 0;
         valueLocation = 1;
      }

      HashMap<String, Set<String>> returnValue = new HashMap<>();
      BufferedReader inputStream = new BufferedReader(new InputStreamReader(new FileInputStream(stream)));
      while (true) {
         String line = inputStream.readLine();
         if (line == null) {
            break;
         }

         String keys[] = line.split("=");

         Set<String> values = returnValue.get(keys[keyLocation]);

         if (values == null) {
            values = new LinkedHashSet<>();
            returnValue.put(keys[keyLocation], values);
         }
         values.add(keys[valueLocation]);

      }

      return returnValue;

   }


}
