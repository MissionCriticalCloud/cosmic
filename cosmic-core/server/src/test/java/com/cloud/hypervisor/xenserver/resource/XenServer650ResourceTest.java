package com.cloud.hypervisor.xenserver.resource;

import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.utils.script.Script;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class XenServer650ResourceTest extends CitrixResourceBaseTest {

    private final XenServer650Resource xenServer650Resource = new XenServer650Resource();

    @Test
    public void testPatchFilePath() {
        final String patchFilePath = xenServer650Resource.getPatchFilePath();
        final String patch = "scripts/vm/hypervisor/xenserver/xenserver65/patch";

        Assert.assertEquals(patch, patchFilePath);
    }

    @Test(expected = CloudRuntimeException.class)
    @PrepareForTest(Script.class)
    public void testGetFiles() {
        testGetPathFilesExeption(xenServer650Resource);
    }

    @Test
    @PrepareForTest(Script.class)
    public void testGetFilesListReturned() {
        testGetPathFilesListReturned(xenServer650Resource);
    }
}
