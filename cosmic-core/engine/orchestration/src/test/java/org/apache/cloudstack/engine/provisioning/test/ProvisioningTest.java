/**
 *
 */
package org.apache.cloudstack.engine.provisioning.test;

import com.cloud.dc.DataCenter.NetworkType;
import org.apache.cloudstack.engine.datacenter.entity.api.ClusterEntity;
import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceEntity.State;
import org.apache.cloudstack.engine.datacenter.entity.api.HostEntity;
import org.apache.cloudstack.engine.datacenter.entity.api.PodEntity;
import org.apache.cloudstack.engine.datacenter.entity.api.ZoneEntity;
import org.apache.cloudstack.engine.datacenter.entity.api.db.EngineClusterVO;
import org.apache.cloudstack.engine.datacenter.entity.api.db.EngineDataCenterVO;
import org.apache.cloudstack.engine.datacenter.entity.api.db.EngineHostPodVO;
import org.apache.cloudstack.engine.datacenter.entity.api.db.EngineHostVO;
import org.apache.cloudstack.engine.datacenter.entity.api.db.dao.EngineClusterDao;
import org.apache.cloudstack.engine.datacenter.entity.api.db.dao.EngineDataCenterDao;
import org.apache.cloudstack.engine.datacenter.entity.api.db.dao.EngineHostDao;
import org.apache.cloudstack.engine.datacenter.entity.api.db.dao.EngineHostPodDao;
import org.apache.cloudstack.engine.service.api.ProvisioningService;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.UUID;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/resource/provisioningContext.xml")
public class ProvisioningTest extends TestCase {

    @Inject
    ProvisioningService service;

    @Inject
    EngineDataCenterDao dcDao;

    @Inject
    EngineHostPodDao _podDao;

    @Inject
    EngineClusterDao _clusterDao;

    @Inject
    EngineHostDao _hostDao;

    @Override
    @Before
    public void setUp() {
        final EngineDataCenterVO dc =
                new EngineDataCenterVO(UUID.randomUUID().toString(), "test", "8.8.8.8", null, "10.0.0.1", null, "10.0.0.1/24", null, null, NetworkType.Basic, null, null,
                        true, true, null, null);
        Mockito.when(dcDao.findByUuid(Matchers.anyString())).thenReturn(dc);
        Mockito.when(dcDao.persist((EngineDataCenterVO) Matchers.anyObject())).thenReturn(dc);

        final EngineHostPodVO pod = new EngineHostPodVO("lab", 123, "10.0.0.1", "10.0.0.1", 24, "test");
        Mockito.when(_podDao.findByUuid(Matchers.anyString())).thenReturn(pod);
        Mockito.when(_podDao.persist((EngineHostPodVO) Matchers.anyObject())).thenReturn(pod);

        final EngineClusterVO cluster = new EngineClusterVO();
        Mockito.when(_clusterDao.findByUuid(Matchers.anyString())).thenReturn(cluster);
        Mockito.when(_clusterDao.persist((EngineClusterVO) Matchers.anyObject())).thenReturn(cluster);

        final EngineHostVO host = new EngineHostVO("68765876598");
        Mockito.when(_hostDao.findByUuid(Matchers.anyString())).thenReturn(host);
        Mockito.when(_hostDao.persist((EngineHostVO) Matchers.anyObject())).thenReturn(host);
    }

    @Test
    public void testProvisioning() {
        registerAndEnableZone();
        registerAndEnablePod();
        registerAndEnableCluster();
        registerAndEnableHost();
    }

    private void registerAndEnableZone() {
        final ZoneEntity zone = service.registerZone("47547648", "lab", "owner", null, new HashMap<>());
        final State state = zone.getState();
        System.out.println("state:" + state);
        final boolean result = zone.enable();
        System.out.println("result:" + result);
    }

    private void registerAndEnablePod() {
        final PodEntity pod = service.registerPod("47547648", "lab", "owner", "8709874074", null, new HashMap<>());
        final State state = pod.getState();
        System.out.println("state:" + state);
        final boolean result = pod.enable();
        System.out.println("result:" + result);
    }

    private void registerAndEnableCluster() {
        final ClusterEntity cluster = service.registerCluster("1265476542", "lab", "owner", null, new HashMap<>());
        final State state = cluster.getState();
        System.out.println("state:" + state);
        final boolean result = cluster.enable();
        System.out.println("result:" + result);
    }

    private void registerAndEnableHost() {
        final HostEntity host = service.registerHost("1265476542", "lab", "owner", null, new HashMap<>());
        final State state = host.getState();
        System.out.println("state:" + state);
        final boolean result = host.enable();
        System.out.println("result:" + result);
    }
}
