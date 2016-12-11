package com.cloud.framework.config;

import com.cloud.utils.exception.CloudRuntimeException;

import com.google.common.testing.EqualsTester;
import org.junit.Assert;
import org.junit.Test;

public class ConfigKeyTest {
    @Test
    public void testEquals() {
        new EqualsTester()
                .addEqualityGroup(new ConfigKey("cat", String.class, "naam", "nick", "bijnaam", true, ConfigKey.Scope.Cluster),
                        new ConfigKey("hond", Boolean.class, "naam", "truus", "thrown name", false),
                        new ConfigKey(Long.class, "naam", "vis", "goud", "zwemt", true, ConfigKey.Scope.Account, 3L)
                )
                .testEquals();
    }

    @Test
    public void testIsSameKeyAs() {
        final ConfigKey key = new ConfigKey("cat", String.class, "naam", "nick", "bijnaam", true, ConfigKey.Scope.Cluster);
        Assert.assertTrue("1 and one should be considdered the same address", key.isSameKeyAs("naam"));
    }

    @Test(expected = CloudRuntimeException.class)
    public void testIsSameKeyAsThrowingCloudRuntimeException() {
        final ConfigKey key = new ConfigKey("hond", Boolean.class, "naam", "truus", "thrown name", false);
        Assert.assertFalse("zero and 0L should be considdered the same address", key.isSameKeyAs(0L));
    }
}
