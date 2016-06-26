package com.cloud.cluster;

import static org.junit.Assert.assertTrue;

import com.cloud.utils.component.ComponentLifecycle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ClusterServiceServletAdapterTest {

    ClusterServiceServletAdapter clusterServiceServletAdapter;
    ClusterManagerImpl clusterManagerImpl;

    @Before
    public void setup() throws IllegalArgumentException,
            IllegalAccessException, NoSuchFieldException, SecurityException {
        clusterServiceServletAdapter = new ClusterServiceServletAdapter();
        clusterManagerImpl = new ClusterManagerImpl();
    }

    @Test
    public void testRunLevel() {
        final int runLevel = clusterServiceServletAdapter.getRunLevel();
        assertTrue(runLevel == ComponentLifecycle.RUN_LEVEL_FRAMEWORK);
        assertTrue(runLevel == clusterManagerImpl.getRunLevel());
    }
}
