//
// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//

package com.cloud.utils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

public class PropertiesUtilsTest {
    @Test
    public void findConfigFile() {
        final File configFile = PropertiesUtil.findConfigFile("notexistingresource");
        Assert.assertNull(configFile);
    }

    @Test
    public void loadFromFile() throws IOException {
        final File file = File.createTempFile("test", ".properties");
        FileUtils.writeStringToFile(file, "a=b\nc=d\n");
        final Properties properties = new Properties();
        PropertiesUtil.loadFromFile(properties, file);
        Assert.assertEquals("b", properties.get("a"));
    }

    @Test
    public void loadPropertiesFromFile() throws IOException {
        final File file = File.createTempFile("test", ".properties");
        FileUtils.writeStringToFile(file, "a=b\nc=d\n");
        final Properties properties = PropertiesUtil.loadFromFile(file);
        Assert.assertEquals("b", properties.get("a"));
    }

    @Test
    public void processConfigFile() throws IOException {
        final File tempFile = File.createTempFile("temp", ".properties");
        FileUtils.writeStringToFile(tempFile, "a=b\nc=d\n");
        final Map<String, String> config = PropertiesUtil.processConfigFile(new String[]{tempFile.getAbsolutePath()});
        Assert.assertEquals("b", config.get("a"));
        Assert.assertEquals("d", config.get("c"));
        tempFile.delete();
    }
}
