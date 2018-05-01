package com.cloud.network.vpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
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

    protected Map<String, String> createFakeCapabilityInputMap() {
        final Map<String, String> map = new HashMap<>();
        map.put(VpcManagerImpl.CAPABILITYVALUE, VpcManagerImpl.TRUE_VALUE);
        map.put(VpcManagerImpl.CAPABILITYTYPE, Network.Capability.SupportedProtocols.getName());
        map.put(VpcManagerImpl.SERVICE, "");
        return map;
    }

    @Test(expected = InvalidParameterValueException.class)
    public void testCheckCapabilityPerServiceProviderFail() {
        // Prepare
        final Map<Capability, String> capabilities = new HashMap<>();
        final Set<Network.Provider> providers = this.prepareVpcManagerForCheckingCapabilityPerService(Service.Connectivity, capabilities);

        // Execute
        this.manager.checkCapabilityPerServiceProvider(providers, Capability.RedundantRouter, Service.SourceNat);
    }

    protected Set<Network.Provider> prepareVpcManagerForCheckingCapabilityPerService(final Service service, final Map<Capability, String> capabilities) {
        final Set<Network.Provider> providers = new HashSet<>();
        providers.add(Provider.VPCVirtualRouter);
        final NetworkElement nwElement1 = mock(NetworkElement.class);
        this.manager._ntwkModel = mock(NetworkModel.class);
        when(this.manager._ntwkModel.getElementImplementingProvider(Provider.VPCVirtualRouter.getName()))
                .thenReturn(nwElement1);
        final Map<Service, Map<Network.Capability, String>> capabilitiesService1 = new HashMap<>();
        when(nwElement1.getCapabilities()).thenReturn(capabilitiesService1);
        capabilitiesService1.put(service, capabilities);

        return providers;
    }
}
