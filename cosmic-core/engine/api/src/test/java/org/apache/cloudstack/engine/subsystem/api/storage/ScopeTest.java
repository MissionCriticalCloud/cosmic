package org.apache.cloudstack.engine.subsystem.api.storage;

import junit.framework.Assert;
import org.junit.Test;

public class ScopeTest {

    @Test
    public void testZoneScope() {
        final ZoneScope zoneScope = new ZoneScope(1L);
        final ZoneScope zoneScope2 = new ZoneScope(1L);
        Assert.assertTrue(zoneScope.isSameScope(zoneScope2));

        final ZoneScope zoneScope3 = new ZoneScope(2L);
        Assert.assertFalse(zoneScope.isSameScope(zoneScope3));
    }

    @Test
    public void testClusterScope() {
        final ClusterScope clusterScope = new ClusterScope(1L, 1L, 1L);
        final ClusterScope clusterScope2 = new ClusterScope(1L, 1L, 1L);

        Assert.assertTrue(clusterScope.isSameScope(clusterScope2));

        final ClusterScope clusterScope3 = new ClusterScope(2L, 2L, 1L);
        Assert.assertFalse(clusterScope.isSameScope(clusterScope3));
    }

    @Test
    public void testHostScope() {
        final HostScope hostScope = new HostScope(1L, 1L, 1L);
        final HostScope hostScope2 = new HostScope(1L, 1L, 1L);
        final HostScope hostScope3 = new HostScope(2L, 1L, 1L);

        Assert.assertTrue(hostScope.isSameScope(hostScope2));
        Assert.assertFalse(hostScope.isSameScope(hostScope3));
    }
}
