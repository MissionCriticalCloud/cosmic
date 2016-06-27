package com.cloud.network.vpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import com.cloud.exception.InvalidParameterValueException;
import com.cloud.network.Network;
import com.cloud.network.Network.Capability;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.NetworkModel;
import com.cloud.network.element.NetworkElement;
import com.cloud.network.vpc.dao.VpcOfferingServiceMapDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;

public class VpcManagerImplTest {

    @Mock
    VpcOfferingServiceMapDao vpcOffSvcMapDao;
    VpcManagerImpl manager;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        manager = new VpcManagerImpl();
        manager._vpcOffSvcMapDao = vpcOffSvcMapDao;
    }

    @Test
    public void getVpcOffSvcProvidersMapForEmptyServiceTest() {
        final long vpcOffId = 1L;
        final List<VpcOfferingServiceMapVO> list = new ArrayList<>();
        list.add(mock(VpcOfferingServiceMapVO.class));
        when(manager._vpcOffSvcMapDao.listByVpcOffId(vpcOffId)).thenReturn(list);

        final Map<Service, Set<Provider>> map = manager.getVpcOffSvcProvidersMap(vpcOffId);

        assertNotNull(map);
        assertEquals(map.size(), 1);
    }

    @Test
    public void testIsVpcOfferingForRegionLevelVpc() throws Exception {
        // Prepare
        final Map<String, Map<String, String>> serviceCapabilitystList = new HashMap<>();
        // First add some other capabilities to be ignored
        serviceCapabilitystList.put("", createFakeCapabilityInputMap());

        final Map<String, String> servicePair = new HashMap<>();
        servicePair.put(VpcManagerImpl.SERVICE, Service.Connectivity.getName());
        servicePair.put(VpcManagerImpl.CAPABILITYTYPE, Network.Capability.RegionLevelVpc.getName());
        servicePair.put(VpcManagerImpl.CAPABILITYVALUE, VpcManagerImpl.TRUE_VALUE);
        serviceCapabilitystList.put("", servicePair);

        // Execute
        final boolean result = Whitebox.invokeMethod(this.manager, "isVpcOfferingForRegionLevelVpc",
                serviceCapabilitystList); //, Network.Capability.RedundantRouter.getName(), Service.SourceNat);

        // Assert
        assertEquals("VpcOffering should be created for Region Level Vpc", true, result);
    }

    protected Map<String, String> createFakeCapabilityInputMap() {
        final Map<String, String> map = new HashMap<>();
        map.put(VpcManagerImpl.CAPABILITYVALUE, VpcManagerImpl.TRUE_VALUE);
        map.put(VpcManagerImpl.CAPABILITYTYPE, Network.Capability.SupportedProtocols.getName());
        map.put(VpcManagerImpl.SERVICE, "");
        return map;
    }

    @Test
    public void testIsVpcOfferingForRegionLevelVpcFalse() throws Exception {
        // Prepare
        final Map<String, Map<String, String>> serviceCapabilitystList = new HashMap<>();
        // First add some other capabilities to be ignored
        serviceCapabilitystList.put("", createFakeCapabilityInputMap());
        serviceCapabilitystList.put("", createFakeCapabilityInputMap());

        // Execute
        final boolean result = Whitebox.invokeMethod(this.manager, "isVpcOfferingForRegionLevelVpc",
                serviceCapabilitystList);

        // Assert
        assertEquals("VpcOffering should be created NOT for Region Level Vpc", false, result);
    }

    @Test(expected = InvalidParameterValueException.class)
    public void testCheckCapabilityPerServiceProviderFail() {
        // Prepare
        final Map<Capability, String> capabilities = new HashMap<>();
        capabilities.put(Capability.RegionLevelVpc, "");
        capabilities.put(Capability.DistributedRouter, "");
        final Set<Network.Provider> providers = this.prepareVpcManagerForCheckingCapabilityPerService(Service.Connectivity, capabilities);

        // Execute
        this.manager.checkCapabilityPerServiceProvider(providers, Capability.RedundantRouter, Service.SourceNat);
    }

    protected Set<Network.Provider> prepareVpcManagerForCheckingCapabilityPerService(final Service service, final Map<Capability, String> capabilities) {
        final Set<Network.Provider> providers = new HashSet<>();
        providers.add(Provider.VPCVirtualRouter);
        final Network.Capability capability = Capability.DistributedRouter;
        final boolean regionLevel = true;
        final boolean distributedRouter = true;
        final NetworkElement nwElement1 = mock(NetworkElement.class);
        this.manager._ntwkModel = mock(NetworkModel.class);
        when(this.manager._ntwkModel.getElementImplementingProvider(Provider.VPCVirtualRouter.getName()))
                .thenReturn(nwElement1);
        final Map<Service, Map<Network.Capability, String>> capabilitiesService1 = new HashMap<>();
        when(nwElement1.getCapabilities()).thenReturn(capabilitiesService1);
        capabilities.put(Capability.RegionLevelVpc, "");
        capabilities.put(Capability.DistributedRouter, "");
        capabilitiesService1.put(service, capabilities);

        return providers;
    }

    @Test
    public void testCheckCapabilityPerServiceProvider() {
        // Prepare
        final Map<Capability, String> capabilities = new HashMap<>();
        capabilities.put(Capability.RegionLevelVpc, "");
        capabilities.put(Capability.DistributedRouter, "");
        final Set<Network.Provider> providers = this.prepareVpcManagerForCheckingCapabilityPerService(Service.Connectivity, capabilities);

        // Execute
        this.manager.checkCapabilityPerServiceProvider(providers, Capability.DistributedRouter, Service.Connectivity);
        this.manager.checkCapabilityPerServiceProvider(providers, Capability.RegionLevelVpc, Service.Connectivity);
    }
}
