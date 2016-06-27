package org.apache.cloudstack.region;

import com.cloud.exception.InvalidParameterValueException;
import org.apache.cloudstack.region.dao.RegionDao;

import javax.naming.ConfigurationException;
import java.util.HashMap;

import junit.framework.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class RegionManagerTest {

    @Test
    public void testUniqueName() {
        final RegionManagerImpl regionMgr = new RegionManagerImpl();
        final RegionDao regionDao = Mockito.mock(RegionDao.class);
        final RegionVO region = new RegionVO(2, "APAC", "");
        Mockito.when(regionDao.findByName(Matchers.anyString())).thenReturn(region);
        regionMgr._regionDao = regionDao;
        try {
            regionMgr.addRegion(2, "APAC", "");
        } catch (final InvalidParameterValueException e) {
            Assert.assertEquals("Region with name: APAC already exists", e.getMessage());
        }
    }

    @Test
    public void configure() throws ConfigurationException {
        final RegionManagerImpl regionManager = new RegionManagerImpl();
        regionManager.configure("foo", new HashMap<>());
        Assert.assertTrue(regionManager.getId() != 0);
    }
}
