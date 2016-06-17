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

package com.cloud.storage.template;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.io.File;

import org.junit.Test;

public class LocalTemplateDownloaderTest {

  @Test
  public void localTemplateDownloaderTest() throws Exception {
    final String url = new File("pom.xml").toURI().toURL().toString();
    final long defaultMaxTemplateSizeInBytes = TemplateDownloader.DEFAULT_MAX_TEMPLATE_SIZE_IN_BYTES;
    final String tempDir = System.getProperty("java.io.tmpdir");
    final TemplateDownloader td = new LocalTemplateDownloader(url, tempDir, defaultMaxTemplateSizeInBytes);

    final long bytes = td.download(true, null);

    assertThat(bytes, greaterThan(0L));
  }

}
