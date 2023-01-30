package com.cloud.hypervisor.xenserver.resource;

import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.utils.script.Script;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class XenServer56SP2ResourceTest extends CitrixResourceBaseTest {

    private final XenServer56SP2Resource xenServer56SP2Resource = new XenServer56SP2Resource();

    @Test
    public void testPatchFilePath() {
        final String patchFilePath = xenServer56SP2Resource.getPatchFilePath();
        final String patch = "scripts/vm/hypervisor/xenserver/xenserver56fp1/patch";

        Assert.assertEquals(patch, patchFilePath);
    }

    @Test(expected = CloudRuntimeException.class)
    @PrepareForTest(Script.class)
    public void testGetFiles() {
        testGetPathFilesExeption(xenServer56SP2Resource);
    }

    @Test
    @PrepareForTest(Script.class)
    public void testGetFilesListReturned() {
        testGetPathFilesListReturned(xenServer56SP2Resource);
    }
}
