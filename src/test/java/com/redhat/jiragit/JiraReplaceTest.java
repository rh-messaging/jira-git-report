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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Clebert Suconic
 */

public class JiraReplaceTest {

   JiraParser jiraParser;


   @Before
   public void init() {
      jiraParser = new JiraParser("Artemis");
      jiraParser.setJira("ARTEMIS-").setJiraBrowseURI("https://issues.apache.org/jira/browse/").
         setSampleJQL("https://issues.apache.org/jira/issues/?jql=project%20%3D%20ARTEMIS%20AND%20key%20in%20");
   }

   @Test
   public void jiraReplaceMultiple() throws Exception {
      String[] jiras = jiraParser.extractJIRAs("[ARTEMIS-110] [ARTEMIS-1264]");

      Assert.assertEquals(2, jiras.length);
      Assert.assertEquals("ARTEMIS-110", jiras[0]);
      Assert.assertEquals("ARTEMIS-1264", jiras[1]);
   }

   @Test
   public void replaceSimple() {
      String jiras[] = jiraParser.extractJIRAs("ARTEMIS-1537 broker was less strict while reloading configuration");
      Assert.assertEquals(1, jiras.length);
      Assert.assertEquals("ARTEMIS-1537", jiras[0]);
   }
}
