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

import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class ListJIRAsTest {

   @Test
   public void doList() throws Exception {
      RestList list = new RestList().setJiraLookup("ARTEMIS-").setQueryUrl("https://issues.jboss.org/rest/api/latest/search?jql=project=%22ENTMQBR%22&fields=*all&maxResults=100").setBaseURL("https://issues.jboss.org/rest/api/latest/issue/");

      list.lookup();

      Set<Map.Entry<String, String>> entries = list.mapJiras.entrySet();

      System.out.println("Result::");
      for (Map.Entry<String, String> entry : entries) {
         System.out.println(entry.getKey() + "=" + entry.getValue());
      }
   }
}
