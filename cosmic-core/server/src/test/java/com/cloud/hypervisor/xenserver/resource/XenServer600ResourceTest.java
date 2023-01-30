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
public class XenServer600ResourceTest extends CitrixResourceBaseTest {

    private final XenServer600Resource xenServer600Resource = new XenServer600Resource();

    @Test
    public void testPatchFilePath() {
        final String patchFilePath = xenServer600Resource.getPatchFilePath();
        final String patch = "scripts/vm/hypervisor/xenserver/xenserver60/patch";

        Assert.assertEquals(patch, patchFilePath);
    }

    @Test(expected = CloudRuntimeException.class)
    @PrepareForTest(Script.class)
    public void testGetFiles() {
        testGetPathFilesExeption(xenServer600Resource);
    }

    @Test
    @PrepareForTest(Script.class)
    public void testGetFilesListReturned() {
        testGetPathFilesListReturned(xenServer600Resource);
    }
}
