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

import java.io.File;

/**
 * @author Clebert Suconic
 */

public class ProjectParser {

   public static void main(String arg[]) {
      try {
         if (arg.length != 6) {
            System.err.println("use Parser <project> <repository> <reportOutput> <from> <to> <rest : true|false>");
            System.err.println("    valid projects: artemis, amq, wildfly");
            System.exit(-1);
         }
         switch (arg[0]) {
            case "artemis":
               artemisProcess(arg);
               break;
            case "amq":
               amqProcess(arg);
               break;
            case "wildfly":
               wildflyProcess(arg);
               break;
            default:
               System.err.println("Invalid Argument: " + arg[0]);
               System.exit(-1);
         }

      } catch (Exception e) {
         e.printStackTrace();
         System.exit(-1);
      }
   }


   private static void wildflyProcess(String arg[]) throws Exception {
      /* GitParser parser = new GitParser(new File(arg[1]), "WFLY-", "https://issues.jboss.org/browse/", "https://github.com/wildfly/wildfly/").
         setSourceSuffix(".java", ".md", ".c", ".sh", ".groovy", ".adoc").
         setSampleJQL("https://issues.jboss.org/issues/?jql=project%20%3D%20WildFly%20AND%20KEY%20IN");
      parser.addInterestingfolder("test").addInterestingfolder("docs/");

      File file = new File(arg[2]);

      parser.setRestLocation("https://issues.jboss.org/rest/api/2/issue/");

      parser.parse(file, arg[3], arg[4]); */

   }

   private static void artemisProcess(String[] arg) throws Exception {
      boolean rest = Boolean.parseBoolean(arg[5]);

      JiraParser jiraParser = new JiraParser("ARTEMIS JIRAs");
      jiraParser.setJira("ARTEMIS-").setJiraBrowseURI("https://issues.apache.org/jira/browse/").
         setSampleJQL("https://issues.apache.org/jira/issues/?jql=project%20%3D%20ARTEMIS%20AND%20key%20in%20");

      if (rest) {
         jiraParser.setRestLocation("https://issues.apache.org/jira/rest/api/2/issue/");
      }

      GitParser parser = new GitParser(new File(arg[1]), "https://github.com/apache/activemq-artemis/").
         setSourceSuffix(".java", ".md", ".c", ".sh", ".groovy");
      parser.addJIRA(jiraParser);

      parser.addInterestingfolder("test").addInterestingfolder("docs/").addInterestingfolder("examples/");
      File file = new File(arg[2]);
      parser.parse(file, arg[3], arg[4]);
   }

   private static void amqProcess(String[] arg) throws Exception {
      boolean rest = Boolean.parseBoolean(arg[5]);


      GitParser parser = new GitParser(new File(arg[1]), "https://github.com/apache/activemq-artemis/").
         setSourceSuffix(".java", ".md", ".c", ".sh", ".groovy");

      JiraParser artemisJIRA = new JiraParser("ARTEMIS");
      artemisJIRA.setJira("ARTEMIS-").setJiraBrowseURI("https://issues.apache.org/jira/browse/").
         setSampleJQL("https://issues.apache.org/jira/issues/?jql=project%20%3D%20ARTEMIS%20AND%20key%20in%20");

      if (rest) {
         artemisJIRA.setRestLocation("https://issues.apache.org/jira/rest/api/2/issue/");
      }
      parser.addJIRA(artemisJIRA);

      JiraParser entmqbrJIRA = new JiraParser("ENTMQBR");
      entmqbrJIRA.setJira("ENTMQBR-").setJiraBrowseURI("https://issues.apache.org/jira/browse/").
         setSampleJQL("https://issues.jboss.org/issues/?jql=project%20%3D%20ENTMQBR%20AND%20KEY%20IN");

      if (rest) {
         entmqbrJIRA.setRestLocation("https://issues.jboss.org/rest/api/2/issue/");
      }

      parser.addJIRA(entmqbrJIRA);

      parser.addInterestingfolder("test").addInterestingfolder("docs/").addInterestingfolder("examples/");
      File file = new File(arg[2]);
      parser.parse(file, arg[3], arg[4]);
   }
}
