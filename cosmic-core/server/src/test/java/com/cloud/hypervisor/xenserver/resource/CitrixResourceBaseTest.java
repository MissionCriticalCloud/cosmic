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
