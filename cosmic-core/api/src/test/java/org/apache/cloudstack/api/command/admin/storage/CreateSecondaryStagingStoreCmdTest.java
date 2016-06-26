package org.apache.cloudstack.api.command.admin.storage;

import org.apache.cloudstack.api.ApiCmdTestUtil;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class CreateSecondaryStagingStoreCmdTest {

    @Test
    public void getDetails() throws IllegalArgumentException,
            IllegalAccessException {
        final CreateSecondaryStagingStoreCmd cmd = new CreateSecondaryStagingStoreCmd();
        final HashMap<String, Map<String, String>> details = new HashMap<>();
        final HashMap<String, String> kv = new HashMap<>();
        kv.put("key", "TEST-KEY");
        kv.put("value", "TEST-VALUE");
        details.put("does not matter", kv);
        ApiCmdTestUtil.set(cmd, "details", details);
        final Map<String, String> detailsMap = cmd.getDetails();
        Assert.assertNotNull(detailsMap);
        Assert.assertEquals(1, detailsMap.size());
        Assert.assertTrue(detailsMap.containsKey("TEST-KEY"));
        Assert.assertEquals("TEST-VALUE", detailsMap.get("TEST-KEY"));
    }

    @Test
    public void getDetailsEmpty() throws IllegalArgumentException,
            IllegalAccessException {
        final CreateSecondaryStagingStoreCmd cmd = new CreateSecondaryStagingStoreCmd();
        ApiCmdTestUtil.set(cmd, "details", new HashMap<String, Map<String, String>>());
        Assert.assertNull(cmd.getDetails());
    }

    @Test
    public void getDetailsNull() throws IllegalArgumentException,
            IllegalAccessException {
        final CreateSecondaryStagingStoreCmd cmd = new CreateSecondaryStagingStoreCmd();
        ApiCmdTestUtil.set(cmd, "details", null);
        Assert.assertNull(cmd.getDetails());
    }
}
