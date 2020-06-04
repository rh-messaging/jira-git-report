/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.redhat.jiragit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class LoadValuesTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();


    @Test
    public void testLoad() throws Exception {
        File file = temporaryFolder.newFile("upload");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        PrintStream printStream = new PrintStream(fileOutputStream);
        printStream.println("a=b");
        printStream.println("a=c");
        printStream.println("a=d");
        printStream.println("e=f");
        printStream.close();
        fileOutputStream.close();
        Map<String, String[]> values = ProjectParser.loadValues(file);

        String[] valuesA = values.get("a");
        Assert.assertEquals(3, valuesA.length);
        Assert.assertEquals("b", valuesA[0]);
        Assert.assertEquals("c", valuesA[1]);
        Assert.assertEquals("d", valuesA[2]);

        String[] valuesE = values.get("e");
        Assert.assertEquals(1, valuesE.length);
        Assert.assertEquals("f", valuesE[0]);

    }
}
