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

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.*;

/**
 * @author Clebert Suconic
 */

public class ProjectParser {

   public static void main(String arg[]) {
      try {
         if (arg.length < 6) {
            printSyntax();
            System.exit(-1);
         }
         String otherBranches[] = new String[0];
         if (arg.length > 6) {
            otherBranches = new String[arg.length - 6];
            int c = 0;
            for (int i = 6; i < arg.length; i++) {
               otherBranches[c++] = arg[i];
            }


            if (otherBranches.length % 2 != 0) {
               System.out.println("Usage on other branches:... branch origin-on-that-branch....");
               System.exit(-1);
            }
         }
         switch (arg[0]) {
            case "artemis":
               artemisProcess(arg[1], arg[2], arg[3], arg[4], Boolean.parseBoolean(arg[5]), otherBranches);
               break;
            case "amq":
               amqProcess(arg[1], arg[2], arg[3], arg[4], Boolean.parseBoolean(arg[5]), otherBranches, false);
               break;
            case "amqcherry":
               amqProcess(arg[1], arg[2], arg[3], arg[4], Boolean.parseBoolean(arg[5]), otherBranches, true);
               break;
            case "wildfly":
               wildflyProcess(arg[1], arg[2], arg[3], arg[4], Boolean.parseBoolean(arg[5]), otherBranches);
               break;
            case "qpid-jms":
               qpidJMSProcess(arg[1], arg[2], arg[3], arg[4], Boolean.parseBoolean(arg[5]), otherBranches);
               break;
            case "qpid-dispatch":
               qpidDispatchProcess(arg[1], arg[2], arg[3], arg[4], Boolean.parseBoolean(arg[5]), otherBranches);
               break;
            default:
               System.err.println("Invalid Argument: " + arg[0]);
               printSyntax();
               System.exit(-1);
         }

      } catch (Exception e) {
         e.printStackTrace();
         System.exit(-1);
      }
   }

   private static void printSyntax() {
      System.err.println("use Parser <project> <repository> <reportOutput> <from> <to> <rest : true|false> cherry-pick-branch1 cherry-pick-source1 cherry-pick-branch2 cherry-pick-source2...cherry-pickbranchN cherry-pick-sourceN");
      System.err.println("    valid projects: artemis, amq, amqcherry, wildfly, qpid-jms, qpid-dispatch");
   }

   private static void wildflyProcess(String clone, String output, String tag1, String tag2, boolean rest, String[] otherBranches) throws Exception {

      JiraParser jiraParser = new JiraParser("WildFly JIRAs");
      jiraParser.setJira("WFLY-").setJiraBrowseURI("https://issues.redhat.com/browse/").
         setSampleJQL("https://issues.redhat.com/issues/?jql=project%20%3D%20WildFly%20AND%20KEY%20IN");

      if (rest) {
         jiraParser.setRestLocation("https://issues.redhat.com/rest/api/2/issue/");
      }

      GitParser parser = new GitParser(new File(clone), "https://github.com/wildfly/wildfly/").
         setSourceSuffix(".java", ".md", ".c", ".sh", ".groovy", ".adoc");
      parser.addJIRA(jiraParser);

      parser.addInterestingfolder("test").addInterestingfolder("docs/");
      File file = new File(output);
      parser.parse(file,tag1, tag2);


   }

   private static void artemisProcess(String clone, String output, String tag1, String tag2, boolean rest, String[] otherBranches) throws Exception {


      JiraParser jiraParser = new JiraParser("ARTEMIS JIRAs");

      jiraParser.setJira("ARTEMIS-").setJiraBrowseURI("https://issues.apache.org/jira/browse/").
         setSampleJQL("https://issues.apache.org/jira/issues/?jql=project%20%3D%20ARTEMIS%20AND%20key%20in%20");

      if (rest) {
         jiraParser.setRestLocation("https://issues.apache.org/jira/rest/api/2/issue/");
      }

      GitParser parser = new GitParser(new File(clone), "https://github.com/apache/activemq-artemis/").
         setSourceSuffix(".java", ".md", ".c", ".sh", ".groovy");
      parser.addJIRA(jiraParser);

      parser.addBranches(otherBranches);

      parser.addInterestingfolder("test").addInterestingfolder("docs/").addInterestingfolder("examples/");
      File file = new File(output);
      parser.parse(file,tag1, tag2);
   }

   private static void qpidJMSProcess(String clone, String output, String tag1, String tag2, boolean rest, String[] otherBranches) throws Exception {

      JiraParser jiraParser = new JiraParser("QPID JIRAs");
      jiraParser.setJira("QPIDJMS-").setJiraBrowseURI("https://issues.apache.org/jira/browse/").
         setSampleJQL("https://issues.apache.org/jira/issues/?jql=project%20%3D%20QPIDJMS%20AND%20key%20in%20");

      if (rest) {
         jiraParser.setRestLocation("https://issues.apache.org/jira/rest/api/2/issue/");
      }

      GitParser parser = new GitParser(new File(clone), "https://github.com/apache/qpid-jms/").
         setSourceSuffix(".java", ".md", ".c", ".sh", ".groovy");
      parser.addJIRA(jiraParser);

      parser.addBranches(otherBranches);

      parser.addInterestingfolder("test").addInterestingfolder("docs/").addInterestingfolder("examples/");
      File file = new File(output);
      parser.parse(file,tag1, tag2);
   }

   private static void qpidDispatchProcess(String clone, String output, String tag1, String tag2, boolean rest, String[] otherBranches) throws Exception {

      JiraParser jiraParser = new JiraParser("Dispatch JIRAs");
      jiraParser.setJira("DISPATCH-").setJiraBrowseURI("https://issues.apache.org/browse/").
         setSampleJQL("https://issues.apache.org/jira/issues/?jql=project%20%3D%20DISPATCH%20AND%20key%20in%20");

      if (rest) {
         jiraParser.setRestLocation("https://issues.apache.org/jira/rest/api/2/issue/");
      }

      GitParser parser = new GitParser(new File(clone), "https://github.com/apache/qpid-dispatch/").
         setSourceSuffix(".java", ".md", ".c", ".sh", ".groovy", ".py", ".h");
      parser.addJIRA(jiraParser);

      parser.addBranches(otherBranches);

      parser.addInterestingfolder("tests/").addInterestingfolder("doc/");
      File file = new File(output);
      parser.parse(file,tag1, tag2);
   }

   private static int getJIRACode(String jira) {
      if (jira.contains("-")) {
         String value = jira.substring(jira.lastIndexOf('-') + 1);
         return Integer.valueOf(value);
      } else {
         return 0;
      }
   }

   private static void amqProcess(String clone, String output, String tag1, String tag2, boolean rest, String[] otherBranches, boolean treatCherryPick) throws Exception {


      GitParser parser = new GitParser(new File(clone), "https://github.com/rh-messaging/activemq-artemis/").
         setSourceSuffix(".java", ".md", ".c", ".sh", ".groovy");

      JiraParser artemisJIRA = new JiraParser("ARTEMIS");
      artemisJIRA.setJira("ARTEMIS-").setJiraBrowseURI("https://issues.apache.org/jira/browse/").
         setSampleJQL("https://issues.apache.org/jira/issues/?jql=project%20%3D%20ARTEMIS%20AND%20key%20in%20");

      if (rest) {
         artemisJIRA.setRestLocation("https://issues.apache.org/jira/rest/api/2/issue/");
      }
      parser.addJIRA(artemisJIRA);

      parser.addBranches(otherBranches);

      JiraParser entmqbrJIRA = new JiraParser("ENTMQBR");
      entmqbrJIRA.setJira("ENTMQBR-").setJiraBrowseURI("https://issues.redhat.com/jira/browse/").
         setSampleJQL("https://issues.redhat.com/issues/?jql=project%20%3D%20ENTMQBR%20AND%20KEY%20IN");

      if (rest) {
         entmqbrJIRA.setRestLocation("https://issues.redhat.com/rest/api/2/issue/");
      }

      File upstream = new File("entmqbr.properties");
      File entmqbrLabels = new File("entmqbr-labels.properties");
      File entmqbrPRs = new File("entmqbr-PRs.properties");

      if (!upstream.exists() || !entmqbrLabels.exists() || !entmqbrPRs.exists()) {
         if (upstream.exists()) {
            upstream.delete();
         }
         if (entmqbrPRs.exists()) {
            entmqbrPRs.delete();
         }
         if (entmqbrLabels.exists()) {
            entmqbrLabels.delete();
         }
         try {

            final PrintStream pullRequestsStream = new PrintStream(entmqbrPRs);

            final RestList list = new RestList().setJiraLookup("ARTEMIS-").addInterestLabel("NO-BACKPORT-NEEDED").addInterestLabel("pr-sent").setQueryUrl("https://issues.redhat.com/rest/api/latest/search?jql=project=%22ENTMQBR%22&fields=*all&maxResults=250").setBaseURL("https://issues.redhat.com/rest/api/latest/issue/").setUserPassProperty("ENTMQPASS");
            list.lookup(new RestList.RestIntercept() {
               @Override
               // if the issue is not a bug, we will infer the NO-BACKPORT-NEEDED
               public void intercept(JsonObject jira, JsonString jirakey, JsonObject fields) {
                  JsonObject issueType = (JsonObject)fields.get("issuetype");
                  JsonString issueTypeName = issueType.getJsonString("name");
                  if (!issueTypeName.getString().equals("Bug")) {
                     list.interestingLabels.get("NO-BACKPORT-NEEDED").add(jirakey.getString());
                  }
                  JsonValue githubPR = fields.get("customfield_12310220");
                  if (githubPR != null && githubPR instanceof JsonArray) {
                     JsonArray githubPRArray = (JsonArray)githubPR;
                     for (int i = 0; i < githubPRArray.size(); i++) {
                        pullRequestsStream.println(jirakey.getString() + "=" + githubPRArray.getString(i));
                     }
                  }
               }
            });

            PrintStream stream = new PrintStream(upstream);
            Set<Pair<String, String>> entries = list.setJiras;
            for (Pair<String, String> pair : entries) {
               stream.println(pair.getB() + "=" + pair.getA());
            }
            stream.close();
            stream = new PrintStream(entmqbrLabels);
            for (Map.Entry<String, Set<String>> entry : list.interestingLabels.entrySet()) {
               Iterator<String> jirasIterator = entry.getValue().iterator();
               while (jirasIterator.hasNext()) {
                  stream.println(jirasIterator.next() + "=" + entry.getKey());
               }
            }
            stream.close();
         } catch (Exception e) {
            e.printStackTrace();
         }
      }

      entmqbrJIRA.setUpstream("ARTEMIS-", upstream);
      entmqbrJIRA.setLabels(entmqbrLabels).setRequireCherryPick(treatCherryPick).setLabelException("NO-BACKPORT-NEEDED").setLabelPRSent("pr-sent");
      entmqbrJIRA.setPRs(entmqbrPRs);

      parser.addJIRA(entmqbrJIRA);

      parser.addInterestingfolder("test").addInterestingfolder("docs/").addInterestingfolder("examples/");
      File file = new File(output);
      parser.parse(file,tag1, tag2);
   }
}
