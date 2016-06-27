/*
 * Copyright 2015 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloud.hypervisor.xenserver.resource;

import com.cloud.utils.script.Script;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

public abstract class CitrixResourceBaseTest {

    public void testGetPathFilesExeption(final CitrixResourceBase citrixResourceBase) {
        final String patch = citrixResourceBase.getPatchFilePath();

        PowerMockito.mockStatic(Script.class);
        Mockito.when(Script.findScript("", patch)).thenReturn(null);

        citrixResourceBase.getPatchFiles();
    }

    public void testGetPathFilesListReturned(final CitrixResourceBase citrixResourceBase) {
        final String patch = citrixResourceBase.getPatchFilePath();

        PowerMockito.mockStatic(Script.class);
        Mockito.when(Script.findScript("", patch)).thenReturn(patch);

        final File expected = new File(patch);
        final String pathExpected = expected.getAbsolutePath();

        final List<File> files = citrixResourceBase.getPatchFiles();
        final String receivedPath = files.get(0).getAbsolutePath();
        Assert.assertEquals(receivedPath, pathExpected);
    }
}
