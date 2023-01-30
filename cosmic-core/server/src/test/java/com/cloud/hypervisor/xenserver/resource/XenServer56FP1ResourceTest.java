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
public class XenServer56FP1ResourceTest extends CitrixResourceBaseTest {

    private final XenServer56FP1Resource xenServer56FP1Resource = new XenServer56FP1Resource();

    @Test
    public void testPatchFilePath() {
        final String patchFilePath = xenServer56FP1Resource.getPatchFilePath();
        final String patch = "scripts/vm/hypervisor/xenserver/xenserver56fp1/patch";

        Assert.assertEquals(patch, patchFilePath);
    }

    @Test(expected = CloudRuntimeException.class)
    @PrepareForTest(Script.class)
    public void testGetFiles() {
        testGetPathFilesExeption(xenServer56FP1Resource);
    }

    @Test
    @PrepareForTest(Script.class)
    public void testGetFilesListReturned() {
        testGetPathFilesListReturned(xenServer56FP1Resource);
    }
}
