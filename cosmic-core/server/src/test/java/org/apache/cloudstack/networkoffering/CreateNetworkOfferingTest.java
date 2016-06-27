package org.apache.cloudstack.networkoffering;

import com.cloud.configuration.ConfigurationManager;
import com.cloud.event.dao.UsageEventDao;
import com.cloud.event.dao.UsageEventDetailsDao;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.network.Network;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.vpc.VpcManager;
import com.cloud.offering.NetworkOffering.Availability;
import com.cloud.offerings.NetworkOfferingServiceMapVO;
import com.cloud.offerings.NetworkOfferingVO;
import com.cloud.offerings.dao.NetworkOfferingDao;
import com.cloud.offerings.dao.NetworkOfferingServiceMapDao;
import com.cloud.user.AccountManager;
import com.cloud.user.AccountVO;
import com.cloud.user.UserVO;
import com.cloud.utils.component.ComponentContext;
import com.cloud.vm.dao.UserVmDetailsDao;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.framework.config.impl.ConfigurationVO;
import org.apache.cloudstack.resourcedetail.dao.UserIpAddressDetailsDao;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/createNetworkOffering.xml")
public class CreateNetworkOfferingTest extends TestCase {

    @Inject
    ConfigurationManager configMgr;

    @Inject
    ConfigurationDao configDao;

    @Inject
    NetworkOfferingDao offDao;

    @Inject
    NetworkOfferingServiceMapDao mapDao;

    @Inject
    AccountManager accountMgr;

    @Inject
    VpcManager vpcMgr;

    @Inject
    UserVmDetailsDao userVmDetailsDao;

    @Inject
    UsageEventDao UsageEventDao;

    @Inject
    UsageEventDetailsDao usageEventDetailsDao;

    @Inject
    UserIpAddressDetailsDao userIpAddressDetailsDao;

    @Override
    @Before
    public void setUp() {
        ComponentContext.initComponentsLifeCycle();

        final ConfigurationVO configVO = new ConfigurationVO("200", "200", "200", "200", "200", "200");
        Mockito.when(configDao.findByName(Matchers.anyString())).thenReturn(configVO);

        Mockito.when(offDao.persist(Matchers.any(NetworkOfferingVO.class))).thenReturn(new NetworkOfferingVO());
        Mockito.when(offDao.persist(Matchers.any(NetworkOfferingVO.class), Matchers.anyMap())).thenReturn(new NetworkOfferingVO());
        Mockito.when(mapDao.persist(Matchers.any(NetworkOfferingServiceMapVO.class))).thenReturn(new NetworkOfferingServiceMapVO());
        Mockito.when(accountMgr.getSystemUser()).thenReturn(new UserVO(1));
        Mockito.when(accountMgr.getSystemAccount()).thenReturn(new AccountVO(2));

        CallContext.register(accountMgr.getSystemUser(), accountMgr.getSystemAccount());
    }

    @Override
    @After
    public void tearDown() {
        CallContext.unregister();
    }

    //Test Shared network offerings
    @Test
    public void createSharedNtwkOffWithVlan() {
        final NetworkOfferingVO off =
                configMgr.createNetworkOffering("shared", "shared", TrafficType.Guest, null, true, Availability.Optional, 200, null, false, Network.GuestType.Shared, false,
                        null, false, null, true, false, null, false, null, true);
        assertNotNull("Shared network offering with specifyVlan=true failed to create ", off);
    }

    @Test(expected = InvalidParameterValueException.class)
    public void createSharedNtwkOffWithNoVlan() {
        final NetworkOfferingVO off =
                configMgr.createNetworkOffering("shared", "shared", TrafficType.Guest, null, false, Availability.Optional, 200, null, false, Network.GuestType.Shared,
                        false, null, false, null, true, false, null, false, null, true);
        assertNull("Shared network offering with specifyVlan=false was created", off);
    }

    @Test
    public void createSharedNtwkOffWithSpecifyIpRanges() {
        final NetworkOfferingVO off =
                configMgr.createNetworkOffering("shared", "shared", TrafficType.Guest, null, true, Availability.Optional, 200, null, false, Network.GuestType.Shared, false,
                        null, false, null, true, false, null, false, null, true);

        assertNotNull("Shared network offering with specifyIpRanges=true failed to create ", off);
    }

    @Test(expected = InvalidParameterValueException.class)
    public void createSharedNtwkOffWithoutSpecifyIpRanges() {
        final NetworkOfferingVO off =
                configMgr.createNetworkOffering("shared", "shared", TrafficType.Guest, null, true, Availability.Optional, 200, null, false, Network.GuestType.Shared,
                        false, null, false, null, false, false, null, false, null, true);
        assertNull("Shared network offering with specifyIpRanges=false was created", off);
    }

    //Test Isolated network offerings
    @Test
    public void createIsolatedNtwkOffWithNoVlan() {
        final Map<Service, Set<Provider>> serviceProviderMap = new HashMap<>();
        final Set<Network.Provider> vrProvider = new HashSet<>();
        vrProvider.add(Provider.VirtualRouter);
        serviceProviderMap.put(Network.Service.SourceNat, vrProvider);
        final NetworkOfferingVO off =
                configMgr.createNetworkOffering("isolated", "isolated", TrafficType.Guest, null, false, Availability.Optional, 200, serviceProviderMap, false,
                        Network.GuestType.Isolated, false, null, false, null, false, false, null, false, null, true);

        assertNotNull("Isolated network offering with specifyIpRanges=false failed to create ", off);
    }

    @Test
    public void createIsolatedNtwkOffWithVlan() {
        final Map<Service, Set<Provider>> serviceProviderMap = new HashMap<>();
        final Set<Network.Provider> vrProvider = new HashSet<>();
        vrProvider.add(Provider.VirtualRouter);
        serviceProviderMap.put(Network.Service.SourceNat, vrProvider);
        final NetworkOfferingVO off =
                configMgr.createNetworkOffering("isolated", "isolated", TrafficType.Guest, null, true, Availability.Optional, 200, serviceProviderMap, false,
                        Network.GuestType.Isolated, false, null, false, null, false, false, null, false, null, true);
        assertNotNull("Isolated network offering with specifyVlan=true wasn't created", off);
    }

    @Test(expected = InvalidParameterValueException.class)
    public void createIsolatedNtwkOffWithSpecifyIpRangesAndSourceNat() {
        final Map<Service, Set<Provider>> serviceProviderMap = new HashMap<>();
        final Set<Network.Provider> vrProvider = new HashSet<>();
        vrProvider.add(Provider.VirtualRouter);
        serviceProviderMap.put(Network.Service.SourceNat, vrProvider);
        final NetworkOfferingVO off =
                configMgr.createNetworkOffering("isolated", "isolated", TrafficType.Guest, null, false, Availability.Optional, 200, serviceProviderMap, false,
                        Network.GuestType.Isolated, false, null, false, null, true, false, null, false, null, true);
        assertNull("Isolated network offering with specifyIpRanges=true and source nat service enabled, was created", off);
    }

    @Test
    public void createIsolatedNtwkOffWithSpecifyIpRangesAndNoSourceNat() {

        final Map<Service, Set<Provider>> serviceProviderMap = new HashMap<>();
        final Set<Network.Provider> vrProvider = new HashSet<>();
        final NetworkOfferingVO off =
                configMgr.createNetworkOffering("isolated", "isolated", TrafficType.Guest, null, false, Availability.Optional, 200, serviceProviderMap, false,
                        Network.GuestType.Isolated, false, null, false, null, true, false, null, false, null, true);
        assertNotNull("Isolated network offering with specifyIpRanges=true and with no sourceNatService, failed to create", off);
    }

    @Test
    public void createVpcNtwkOff() {
        final Map<Service, Set<Provider>> serviceProviderMap = new HashMap<>();
        final Set<Network.Provider> vrProvider = new HashSet<>();
        vrProvider.add(Provider.VPCVirtualRouter);
        serviceProviderMap.put(Network.Service.Dhcp, vrProvider);
        serviceProviderMap.put(Network.Service.Dns, vrProvider);
        serviceProviderMap.put(Network.Service.Lb, vrProvider);
        serviceProviderMap.put(Network.Service.SourceNat, vrProvider);
        serviceProviderMap.put(Network.Service.Gateway, vrProvider);
        serviceProviderMap.put(Network.Service.Lb, vrProvider);
        final NetworkOfferingVO off =
                configMgr.createNetworkOffering("isolated", "isolated", TrafficType.Guest, null, true, Availability.Optional, 200, serviceProviderMap, false,
                        Network.GuestType.Isolated, false, null, false, null, false, false, null, false, null, true);
        // System.out.println("Creating Vpc Network Offering");
        assertNotNull("Vpc Isolated network offering with Vpc provider ", off);
    }
}
