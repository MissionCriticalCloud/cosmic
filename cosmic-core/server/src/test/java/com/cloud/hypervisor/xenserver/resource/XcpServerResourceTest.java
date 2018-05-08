package com.cloud.hypervisor.xenserver.resource;

import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.utils.script.Script;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class XcpServerResourceTest extends CitrixResourceBaseTest {

    private final XcpServerResource xcpServerResource = new XcpServerResource();

    @Test
    public void testPatchFilePath() {
        final String patchFilePath = xcpServerResource.getPatchFilePath();
        final String patch = "scripts/vm/hypervisor/xenserver/xcpserver/patch";

        Assert.assertEquals(patch, patchFilePath);
    }

    @Test(expected = CloudRuntimeException.class)
    @PrepareForTest(Script.class)
    public void testGetFilesExeption() {
        testGetPathFilesExeption(xcpServerResource);
    }

    @Test
    @PrepareForTest(Script.class)
    public void testGetFilesListReturned() {
        testGetPathFilesListReturned(xcpServerResource);
    }
}
