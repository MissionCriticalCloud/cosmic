package com.cloud.configuration;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.cloud.configuration.Resource.ResourceType;
import com.cloud.dc.AccountVlanMapVO;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.DataCenter.NetworkType;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.Vlan;
import com.cloud.dc.VlanVO;
import com.cloud.dc.dao.AccountVlanMapDao;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.DataCenterIpAddressDao;
import com.cloud.dc.dao.DomainVlanMapDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.dc.dao.VlanDao;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.network.IpAddressManager;
import com.cloud.network.Network;
import com.cloud.network.Network.Capability;
import com.cloud.network.NetworkModel;
import com.cloud.network.dao.FirewallRulesDao;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.IPAddressVO;
import com.cloud.network.dao.PhysicalNetworkDao;
import com.cloud.network.dao.PhysicalNetworkVO;
import com.cloud.projects.ProjectManager;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.AccountVO;
import com.cloud.user.ResourceLimitService;
import com.cloud.user.User;
import com.cloud.user.UserVO;
import com.cloud.user.dao.AccountDao;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.Ip;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.dao.VMInstanceDao;
import org.apache.cloudstack.api.command.admin.vlan.DedicatePublicIpRangeCmd;
import org.apache.cloudstack.api.command.admin.vlan.ReleasePublicIpRangeCmd;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.engine.orchestration.service.NetworkOrchestrationService;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationManagerTest {

    private static final Logger s_logger = LoggerFactory.getLogger(ConfigurationManagerTest.class);

    ConfigurationManagerImpl configurationMgr = new ConfigurationManagerImpl();

    DedicatePublicIpRangeCmd dedicatePublicIpRangesCmd = new DedicatePublicIpRangeCmdExtn();
    Class<?> _dedicatePublicIpRangeClass = dedicatePublicIpRangesCmd.getClass().getSuperclass();

    ReleasePublicIpRangeCmd releasePublicIpRangesCmd = new ReleasePublicIpRangeCmdExtn();
    Class<?> _releasePublicIpRangeClass = releasePublicIpRangesCmd.getClass().getSuperclass();

    @Mock
    AccountManager _accountMgr;
    @Mock
    ProjectManager _projectMgr;
    @Mock
    ResourceLimitService _resourceLimitMgr;
    @Mock
    NetworkOrchestrationService _networkMgr;
    @Mock
    AccountDao _accountDao;
    @Mock
    VlanDao _vlanDao;
    @Mock
    AccountVlanMapDao _accountVlanMapDao;
    @Mock
    DomainVlanMapDao _domainVlanMapDao;
    @Mock
    IPAddressDao _publicIpAddressDao;
    @Mock
    DataCenterDao _zoneDao;
    @Mock
    FirewallRulesDao _firewallDao;
    @Mock
    IpAddressManager _ipAddrMgr;
    @Mock
    NetworkModel _networkModel;
    @Mock
    DataCenterIpAddressDao _privateIpAddressDao;
    @Mock
    VolumeDao _volumeDao;
    @Mock
    HostDao _hostDao;
    @Mock
    VMInstanceDao _vmInstanceDao;
    @Mock
    ClusterDao _clusterDao;
    @Mock
    HostPodDao _podDao;
    @Mock
    PhysicalNetworkDao _physicalNetworkDao;

    VlanVO vlan = new VlanVO(Vlan.VlanType.VirtualNetwork, "vlantag", "vlangateway", "vlannetmask", 1L, "iprange", 1L, 1L, null, null, null);

    @Mock
    Network network;
    @Mock
    Account account;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        configurationMgr._accountMgr = _accountMgr;
        configurationMgr._projectMgr = _projectMgr;
        configurationMgr._resourceLimitMgr = _resourceLimitMgr;
        configurationMgr._networkMgr = _networkMgr;
        configurationMgr._accountDao = _accountDao;
        configurationMgr._vlanDao = _vlanDao;
        configurationMgr._accountVlanMapDao = _accountVlanMapDao;
        configurationMgr._domainVlanMapDao = _domainVlanMapDao;
        configurationMgr._publicIpAddressDao = _publicIpAddressDao;
        configurationMgr._zoneDao = _zoneDao;
        configurationMgr._firewallDao = _firewallDao;
        configurationMgr._ipAddrMgr = _ipAddrMgr;
        configurationMgr._networkModel = _networkModel;
        configurationMgr._privateIpAddressDao = _privateIpAddressDao;
        configurationMgr._volumeDao = _volumeDao;
        configurationMgr._hostDao = _hostDao;
        configurationMgr._vmInstanceDao = _vmInstanceDao;
        configurationMgr._clusterDao = _clusterDao;
        configurationMgr._podDao = _podDao;
        configurationMgr._physicalNetworkDao = _physicalNetworkDao;

        final Account account = new AccountVO("testaccount", 1, "networkdomain", (short) 0, UUID.randomUUID().toString());
        when(configurationMgr._accountMgr.getAccount(anyLong())).thenReturn(account);
        when(configurationMgr._accountDao.findActiveAccount(anyString(), anyLong())).thenReturn(account);
        when(configurationMgr._accountMgr.getActiveAccountById(anyLong())).thenReturn(account);

        final UserVO user = new UserVO(1, "testuser", "password", "firstname", "lastName", "email", "timezone", UUID.randomUUID().toString(), User.Source.UNKNOWN);
        CallContext.register(user, account);

        when(configurationMgr._publicIpAddressDao.countIPs(anyLong(), anyLong(), anyBoolean())).thenReturn(1);

        doNothing().when(configurationMgr._resourceLimitMgr).checkResourceLimit(any(Account.class), any(ResourceType.class), anyLong());

        when(configurationMgr._accountVlanMapDao.persist(any(AccountVlanMapVO.class))).thenReturn(new AccountVlanMapVO());

        when(configurationMgr._vlanDao.acquireInLockTable(anyLong(), anyInt())).thenReturn(vlan);

        final Field dedicateIdField = _dedicatePublicIpRangeClass.getDeclaredField("id");
        dedicateIdField.setAccessible(true);
        dedicateIdField.set(dedicatePublicIpRangesCmd, 1L);

        final Field accountNameField = _dedicatePublicIpRangeClass.getDeclaredField("accountName");
        accountNameField.setAccessible(true);
        accountNameField.set(dedicatePublicIpRangesCmd, "accountname");

        final Field projectIdField = _dedicatePublicIpRangeClass.getDeclaredField("projectId");
        projectIdField.setAccessible(true);
        projectIdField.set(dedicatePublicIpRangesCmd, null);

        final Field domainIdField = _dedicatePublicIpRangeClass.getDeclaredField("domainId");
        domainIdField.setAccessible(true);
        domainIdField.set(dedicatePublicIpRangesCmd, 1L);

        final Field releaseIdField = _releasePublicIpRangeClass.getDeclaredField("id");
        releaseIdField.setAccessible(true);
        releaseIdField.set(releasePublicIpRangesCmd, 1L);
    }

    @After
    public void tearDown() {
        CallContext.unregister();
    }

    @Test
    public void testDedicatePublicIpRange() throws Exception {

        s_logger.info("Running tests for DedicatePublicIpRange API");

        /*
         * TEST 1: given valid parameters DedicatePublicIpRange should succeed
         */
        runDedicatePublicIpRangePostiveTest();

        /*
         * TEST 2: given invalid public ip range DedicatePublicIpRange should fail
         */
        runDedicatePublicIpRangeInvalidRange();
        /*
        * TEST 3: given public IP range that is already dedicated to a different account DedicatePublicIpRange should fail
        */
        runDedicatePublicIpRangeDedicatedRange();

        /*
        * TEST 4: given zone is of type Basic DedicatePublicIpRange should fail
        */
        runDedicatePublicIpRangeInvalidZone();

        /*
         * TEST 5: given range is already allocated to a different account DedicatePublicIpRange should fail
         */
        runDedicatePublicIpRangeIPAdressAllocated();
    }

    void runDedicatePublicIpRangePostiveTest() throws Exception {
        final TransactionLegacy txn = TransactionLegacy.open("runDedicatePublicIpRangePostiveTest");

        when(configurationMgr._vlanDao.findById(anyLong())).thenReturn(vlan);

        when(configurationMgr._accountVlanMapDao.listAccountVlanMapsByAccount(anyLong())).thenReturn(null);

        final DataCenterVO dc =
                new DataCenterVO(UUID.randomUUID().toString(), "test", "8.8.8.8", null, "10.0.0.1", null, "10.0.0.1/24", null, null, NetworkType.Advanced, null, null, true,
                        true, null, null);
        when(configurationMgr._zoneDao.findById(anyLong())).thenReturn(dc);

        final List<IPAddressVO> ipAddressList = new ArrayList<>();
        final IPAddressVO ipAddress = new IPAddressVO(new Ip("75.75.75.75"), 1, 0xaabbccddeeffL, 10, false);
        ipAddressList.add(ipAddress);
        when(configurationMgr._publicIpAddressDao.listByVlanId(anyLong())).thenReturn(ipAddressList);

        try {
            final Vlan result = configurationMgr.dedicatePublicIpRange(dedicatePublicIpRangesCmd);
            Assert.assertNotNull(result);
        } catch (final Exception e) {
            s_logger.info("exception in testing runDedicatePublicIpRangePostiveTest message: " + e.toString());
        } finally {
            txn.close("runDedicatePublicIpRangePostiveTest");
        }
    }

    void runDedicatePublicIpRangeInvalidRange() throws Exception {
        final TransactionLegacy txn = TransactionLegacy.open("runDedicatePublicIpRangeInvalidRange");

        when(configurationMgr._vlanDao.findById(anyLong())).thenReturn(null);
        try {
            configurationMgr.dedicatePublicIpRange(dedicatePublicIpRangesCmd);
        } catch (final Exception e) {
            Assert.assertTrue(e.getMessage().contains("Unable to find vlan by id"));
        } finally {
            txn.close("runDedicatePublicIpRangeInvalidRange");
        }
    }

    void runDedicatePublicIpRangeDedicatedRange() throws Exception {
        final TransactionLegacy txn = TransactionLegacy.open("runDedicatePublicIpRangeDedicatedRange");

        when(configurationMgr._vlanDao.findById(anyLong())).thenReturn(vlan);

        // public ip range is already dedicated
        final List<AccountVlanMapVO> accountVlanMaps = new ArrayList<>();
        final AccountVlanMapVO accountVlanMap = new AccountVlanMapVO(1, 1);
        accountVlanMaps.add(accountVlanMap);
        when(configurationMgr._accountVlanMapDao.listAccountVlanMapsByVlan(anyLong())).thenReturn(accountVlanMaps);

        final DataCenterVO dc =
                new DataCenterVO(UUID.randomUUID().toString(), "test", "8.8.8.8", null, "10.0.0.1", null, "10.0.0.1/24", null, null, NetworkType.Advanced, null, null, true,
                        true, null, null);
        when(configurationMgr._zoneDao.findById(anyLong())).thenReturn(dc);

        final List<IPAddressVO> ipAddressList = new ArrayList<>();
        final IPAddressVO ipAddress = new IPAddressVO(new Ip("75.75.75.75"), 1, 0xaabbccddeeffL, 10, false);
        ipAddressList.add(ipAddress);
        when(configurationMgr._publicIpAddressDao.listByVlanId(anyLong())).thenReturn(ipAddressList);

        try {
            configurationMgr.dedicatePublicIpRange(dedicatePublicIpRangesCmd);
        } catch (final Exception e) {
            Assert.assertTrue(e.getMessage().contains("Public IP range has already been dedicated"));
        } finally {
            txn.close("runDedicatePublicIpRangePublicIpRangeDedicated");
        }
    }

    void runDedicatePublicIpRangeInvalidZone() throws Exception {
        final TransactionLegacy txn = TransactionLegacy.open("runDedicatePublicIpRangeInvalidZone");

        when(configurationMgr._vlanDao.findById(anyLong())).thenReturn(vlan);

        when(configurationMgr._accountVlanMapDao.listAccountVlanMapsByVlan(anyLong())).thenReturn(null);

        // public ip range belongs to zone of type basic
        final DataCenterVO dc =
                new DataCenterVO(UUID.randomUUID().toString(), "test", "8.8.8.8", null, "10.0.0.1", null, "10.0.0.1/24", null, null, NetworkType.Basic, null, null, true,
                        true, null, null);
        when(configurationMgr._zoneDao.findById(anyLong())).thenReturn(dc);

        final List<IPAddressVO> ipAddressList = new ArrayList<>();
        final IPAddressVO ipAddress = new IPAddressVO(new Ip("75.75.75.75"), 1, 0xaabbccddeeffL, 10, false);
        ipAddressList.add(ipAddress);
        when(configurationMgr._publicIpAddressDao.listByVlanId(anyLong())).thenReturn(ipAddressList);

        try {
            configurationMgr.dedicatePublicIpRange(dedicatePublicIpRangesCmd);
        } catch (final Exception e) {
            Assert.assertTrue(e.getMessage().contains("Public IP range can be dedicated to an account only in the zone of type Advanced"));
        } finally {
            txn.close("runDedicatePublicIpRangeInvalidZone");
        }
    }

    void runDedicatePublicIpRangeIPAdressAllocated() throws Exception {
        final TransactionLegacy txn = TransactionLegacy.open("runDedicatePublicIpRangeIPAdressAllocated");

        when(configurationMgr._vlanDao.findById(anyLong())).thenReturn(vlan);

        when(configurationMgr._accountVlanMapDao.listAccountVlanMapsByAccount(anyLong())).thenReturn(null);

        final DataCenterVO dc =
                new DataCenterVO(UUID.randomUUID().toString(), "test", "8.8.8.8", null, "10.0.0.1", null, "10.0.0.1/24", null, null, NetworkType.Advanced, null, null, true,
                        true, null, null);
        when(configurationMgr._zoneDao.findById(anyLong())).thenReturn(dc);

        // one of the ip addresses of the range is allocated to different account
        final List<IPAddressVO> ipAddressList = new ArrayList<>();
        final IPAddressVO ipAddress = new IPAddressVO(new Ip("75.75.75.75"), 1, 0xaabbccddeeffL, 10, false);
        ipAddress.setAllocatedToAccountId(1L);
        ipAddressList.add(ipAddress);
        when(configurationMgr._publicIpAddressDao.listByVlanId(anyLong())).thenReturn(ipAddressList);

        try {
            configurationMgr.dedicatePublicIpRange(dedicatePublicIpRangesCmd);
        } catch (final Exception e) {
            Assert.assertTrue(e.getMessage().contains("Public IP address in range is allocated to another account"));
        } finally {
            txn.close("runDedicatePublicIpRangeIPAdressAllocated");
        }
    }

    @Test
    public void testReleasePublicIpRange() throws Exception {

        s_logger.info("Running tests for DedicatePublicIpRange API");

        /*
         * TEST 1: given valid parameters and no allocated public ip's in the range ReleasePublicIpRange should succeed
         */
        runReleasePublicIpRangePostiveTest1();

        /*
         * TEST 2: given valid parameters ReleasePublicIpRange should succeed
         */
        runReleasePublicIpRangePostiveTest2();

        /*
         * TEST 3: given range doesn't exist
         */
        runReleasePublicIpRangeInvalidIpRange();

        /*
         * TEST 4: given range is not dedicated to any account
         */
        runReleaseNonDedicatedPublicIpRange();
    }

    void runReleasePublicIpRangePostiveTest1() throws Exception {
        final TransactionLegacy txn = TransactionLegacy.open("runReleasePublicIpRangePostiveTest1");

        when(configurationMgr._vlanDao.findById(anyLong())).thenReturn(vlan);

        final List<AccountVlanMapVO> accountVlanMaps = new ArrayList<>();
        final AccountVlanMapVO accountVlanMap = new AccountVlanMapVO(1, 1);
        accountVlanMaps.add(accountVlanMap);
        when(configurationMgr._accountVlanMapDao.listAccountVlanMapsByVlan(anyLong())).thenReturn(accountVlanMaps);

        // no allocated ip's
        when(configurationMgr._publicIpAddressDao.countIPs(anyLong(), anyLong(), anyBoolean())).thenReturn(0);

        when(configurationMgr._accountVlanMapDao.remove(anyLong())).thenReturn(true);
        try {
            final Boolean result = configurationMgr.releasePublicIpRange(releasePublicIpRangesCmd);
            Assert.assertTrue(result);
        } catch (final Exception e) {
            s_logger.info("exception in testing runReleasePublicIpRangePostiveTest1 message: " + e.toString());
        } finally {
            txn.close("runReleasePublicIpRangePostiveTest1");
        }
    }

    void runReleasePublicIpRangePostiveTest2() throws Exception {
        final TransactionLegacy txn = TransactionLegacy.open("runReleasePublicIpRangePostiveTest2");

        when(configurationMgr._vlanDao.findById(anyLong())).thenReturn(vlan);

        final List<AccountVlanMapVO> accountVlanMaps = new ArrayList<>();
        final AccountVlanMapVO accountVlanMap = new AccountVlanMapVO(1, 1);
        accountVlanMaps.add(accountVlanMap);
        when(configurationMgr._accountVlanMapDao.listAccountVlanMapsByVlan(anyLong())).thenReturn(accountVlanMaps);

        when(configurationMgr._publicIpAddressDao.countIPs(anyLong(), anyLong(), anyBoolean())).thenReturn(1);

        final List<IPAddressVO> ipAddressList = new ArrayList<>();
        final IPAddressVO ipAddress = new IPAddressVO(new Ip("75.75.75.75"), 1, 0xaabbccddeeffL, 10, false);
        ipAddressList.add(ipAddress);
        when(configurationMgr._publicIpAddressDao.listByVlanId(anyLong())).thenReturn(ipAddressList);

        when(configurationMgr._firewallDao.countRulesByIpId(anyLong())).thenReturn(0L);

        when(configurationMgr._ipAddrMgr.disassociatePublicIpAddress(anyLong(), anyLong(), any(Account.class))).thenReturn(true);

        when(configurationMgr._vlanDao.releaseFromLockTable(anyLong())).thenReturn(true);

        when(configurationMgr._accountVlanMapDao.remove(anyLong())).thenReturn(true);
        try {
            final Boolean result = configurationMgr.releasePublicIpRange(releasePublicIpRangesCmd);
            Assert.assertTrue(result);
        } catch (final Exception e) {
            s_logger.info("exception in testing runReleasePublicIpRangePostiveTest2 message: " + e.toString());
        } finally {
            txn.close("runReleasePublicIpRangePostiveTest2");
        }
    }

    void runReleasePublicIpRangeInvalidIpRange() throws Exception {
        final TransactionLegacy txn = TransactionLegacy.open("runReleasePublicIpRangeInvalidIpRange");

        when(configurationMgr._vlanDao.findById(anyLong())).thenReturn(null);
        try {
            configurationMgr.releasePublicIpRange(releasePublicIpRangesCmd);
        } catch (final Exception e) {
            Assert.assertTrue(e.getMessage().contains("Please specify a valid IP range id"));
        } finally {
            txn.close("runReleasePublicIpRangeInvalidIpRange");
        }
    }

    void runReleaseNonDedicatedPublicIpRange() throws Exception {
        final TransactionLegacy txn = TransactionLegacy.open("runReleaseNonDedicatedPublicIpRange");

        when(configurationMgr._vlanDao.findById(anyLong())).thenReturn(vlan);

        when(configurationMgr._accountVlanMapDao.listAccountVlanMapsByVlan(anyLong())).thenReturn(null);
        when(configurationMgr._domainVlanMapDao.listDomainVlanMapsByVlan(anyLong())).thenReturn(null);
        try {
            configurationMgr.releasePublicIpRange(releasePublicIpRangesCmd);
        } catch (final Exception e) {
            Assert.assertTrue(e.getMessage().contains("as it not dedicated to any domain and any account"));
        } finally {
            txn.close("runReleaseNonDedicatedPublicIpRange");
        }
    }

    @Test
    public void validateEmptyStaticNatServiceCapablitiesTest() {
        final Map<Capability, String> staticNatServiceCapabilityMap = new HashMap<>();

        configurationMgr.validateStaticNatServiceCapablities(staticNatServiceCapabilityMap);
    }

    @Test
    public void validateInvalidStaticNatServiceCapablitiesTest() {
        final Map<Capability, String> staticNatServiceCapabilityMap = new HashMap<>();
        staticNatServiceCapabilityMap.put(Capability.AssociatePublicIP, "Frue and Talse");

        boolean caught = false;
        try {
            configurationMgr.validateStaticNatServiceCapablities(staticNatServiceCapabilityMap);
        } catch (final InvalidParameterValueException e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().contains("(frue and talse)"));
            caught = true;
        }
        Assert.assertTrue("should not be accepted", caught);
    }

    @Test
    public void validateTTStaticNatServiceCapablitiesTest() {
        final Map<Capability, String> staticNatServiceCapabilityMap = new HashMap<>();
        staticNatServiceCapabilityMap.put(Capability.AssociatePublicIP, "true and Talse");
        staticNatServiceCapabilityMap.put(Capability.ElasticIp, "True");

        configurationMgr.validateStaticNatServiceCapablities(staticNatServiceCapabilityMap);
    }

    @Test
    public void validateFTStaticNatServiceCapablitiesTest() {
        final Map<Capability, String> staticNatServiceCapabilityMap = new HashMap<>();
        staticNatServiceCapabilityMap.put(Capability.AssociatePublicIP, "false");
        staticNatServiceCapabilityMap.put(Capability.ElasticIp, "True");

        configurationMgr.validateStaticNatServiceCapablities(staticNatServiceCapabilityMap);
    }

    @Test
    public void validateTFStaticNatServiceCapablitiesTest() {
        final Map<Capability, String> staticNatServiceCapabilityMap = new HashMap<>();
        staticNatServiceCapabilityMap.put(Capability.AssociatePublicIP, "true and Talse");
        staticNatServiceCapabilityMap.put(Capability.ElasticIp, "false");

        boolean caught = false;
        try {
            configurationMgr.validateStaticNatServiceCapablities(staticNatServiceCapabilityMap);
        } catch (final InvalidParameterValueException e) {
            Assert.assertTrue(
                    e.getMessage(),
                    e.getMessage().contains(
                            "Capability " + Capability.AssociatePublicIP.getName() + " can only be set when capability " + Capability.ElasticIp.getName() + " is true"));
            caught = true;
        }
        Assert.assertTrue("should not be accepted", caught);
    }

    @Test
    public void validateFFStaticNatServiceCapablitiesTest() {
        final Map<Capability, String> staticNatServiceCapabilityMap = new HashMap<>();
        staticNatServiceCapabilityMap.put(Capability.AssociatePublicIP, "false");
        staticNatServiceCapabilityMap.put(Capability.ElasticIp, "False");

        configurationMgr.validateStaticNatServiceCapablities(staticNatServiceCapabilityMap);
    }

    @Test
    public void checkIfPodIsDeletableSuccessTest() {
        final HostPodVO hostPodVO = Mockito.mock(HostPodVO.class);
        Mockito.when(hostPodVO.getDataCenterId()).thenReturn(new Random().nextLong());
        Mockito.when(_podDao.findById(anyLong())).thenReturn(hostPodVO);

        Mockito.when(_privateIpAddressDao.countIPs(anyLong(), anyLong(), anyBoolean())).thenReturn(0);
        Mockito.when(_volumeDao.findByPod(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_hostDao.findByPodId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_vmInstanceDao.listByPodId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_clusterDao.listByPodId(anyLong())).thenReturn(new ArrayList<>());

        configurationMgr.checkIfPodIsDeletable(new Random().nextLong());
    }

    @Test(expected = CloudRuntimeException.class)
    public void checkIfPodIsDeletableFailureOnPrivateIpAddressTest() {
        final HostPodVO hostPodVO = Mockito.mock(HostPodVO.class);
        Mockito.when(hostPodVO.getDataCenterId()).thenReturn(new Random().nextLong());
        Mockito.when(_podDao.findById(anyLong())).thenReturn(hostPodVO);

        Mockito.when(_privateIpAddressDao.countIPs(anyLong(), anyLong(), anyBoolean())).thenReturn(1);
        Mockito.when(_volumeDao.findByPod(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_hostDao.findByPodId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_vmInstanceDao.listByPodId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_clusterDao.listByPodId(anyLong())).thenReturn(new ArrayList<>());

        configurationMgr.checkIfPodIsDeletable(new Random().nextLong());
    }

    @Test(expected = CloudRuntimeException.class)
    public void checkIfPodIsDeletableFailureOnVolumeTest() {
        final HostPodVO hostPodVO = Mockito.mock(HostPodVO.class);
        Mockito.when(hostPodVO.getDataCenterId()).thenReturn(new Random().nextLong());
        Mockito.when(_podDao.findById(anyLong())).thenReturn(hostPodVO);

        final VolumeVO volumeVO = Mockito.mock(VolumeVO.class);
        final ArrayList<VolumeVO> arrayList = new ArrayList<>();
        arrayList.add(volumeVO);
        Mockito.when(_privateIpAddressDao.countIPs(anyLong(), anyLong(), anyBoolean())).thenReturn(0);
        Mockito.when(_volumeDao.findByPod(anyLong())).thenReturn(arrayList);
        Mockito.when(_hostDao.findByPodId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_vmInstanceDao.listByPodId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_clusterDao.listByPodId(anyLong())).thenReturn(new ArrayList<>());

        configurationMgr.checkIfPodIsDeletable(new Random().nextLong());
    }

    @Test(expected = CloudRuntimeException.class)
    public void checkIfPodIsDeletableFailureOnHostTest() {
        final HostPodVO hostPodVO = Mockito.mock(HostPodVO.class);
        Mockito.when(hostPodVO.getDataCenterId()).thenReturn(new Random().nextLong());
        Mockito.when(_podDao.findById(anyLong())).thenReturn(hostPodVO);

        final HostVO hostVO = Mockito.mock(HostVO.class);
        final ArrayList<HostVO> arrayList = new ArrayList<>();
        arrayList.add(hostVO);
        Mockito.when(_privateIpAddressDao.countIPs(anyLong(), anyLong(), anyBoolean())).thenReturn(0);
        Mockito.when(_volumeDao.findByPod(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_hostDao.findByPodId(anyLong())).thenReturn(arrayList);
        Mockito.when(_vmInstanceDao.listByPodId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_clusterDao.listByPodId(anyLong())).thenReturn(new ArrayList<>());

        configurationMgr.checkIfPodIsDeletable(new Random().nextLong());
    }

    @Test(expected = CloudRuntimeException.class)
    public void checkIfPodIsDeletableFailureOnVmInstanceTest() {
        final HostPodVO hostPodVO = Mockito.mock(HostPodVO.class);
        Mockito.when(hostPodVO.getDataCenterId()).thenReturn(new Random().nextLong());
        Mockito.when(_podDao.findById(anyLong())).thenReturn(hostPodVO);

        final VMInstanceVO vMInstanceVO = Mockito.mock(VMInstanceVO.class);
        final ArrayList<VMInstanceVO> arrayList = new ArrayList<>();
        arrayList.add(vMInstanceVO);
        Mockito.when(_privateIpAddressDao.countIPs(anyLong(), anyLong(), anyBoolean())).thenReturn(0);
        Mockito.when(_volumeDao.findByPod(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_hostDao.findByPodId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_vmInstanceDao.listByPodId(anyLong())).thenReturn(arrayList);
        Mockito.when(_clusterDao.listByPodId(anyLong())).thenReturn(new ArrayList<>());

        configurationMgr.checkIfPodIsDeletable(new Random().nextLong());
    }

    @Test(expected = CloudRuntimeException.class)
    public void checkIfPodIsDeletableFailureOnClusterTest() {
        final HostPodVO hostPodVO = Mockito.mock(HostPodVO.class);
        Mockito.when(hostPodVO.getDataCenterId()).thenReturn(new Random().nextLong());
        Mockito.when(_podDao.findById(anyLong())).thenReturn(hostPodVO);

        final ClusterVO clusterVO = Mockito.mock(ClusterVO.class);
        final ArrayList<ClusterVO> arrayList = new ArrayList<>();
        arrayList.add(clusterVO);
        Mockito.when(_privateIpAddressDao.countIPs(anyLong(), anyLong(), anyBoolean())).thenReturn(0);
        Mockito.when(_volumeDao.findByPod(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_hostDao.findByPodId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_vmInstanceDao.listByPodId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_clusterDao.listByPodId(anyLong())).thenReturn(arrayList);

        configurationMgr.checkIfPodIsDeletable(new Random().nextLong());
    }

    @Test
    public void checkIfZoneIsDeletableSuccessTest() {
        Mockito.when(_hostDao.listByDataCenterId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_podDao.listByDataCenterId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_privateIpAddressDao.countIPs(anyLong(), anyBoolean())).thenReturn(0);
        Mockito.when(_publicIpAddressDao.countIPs(anyLong(), anyBoolean())).thenReturn(0);
        Mockito.when(_vmInstanceDao.listByZoneId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_volumeDao.findByDc(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_physicalNetworkDao.listByZone(anyLong())).thenReturn(new ArrayList<>());

        configurationMgr.checkIfZoneIsDeletable(new Random().nextLong());
    }

    @Test(expected = CloudRuntimeException.class)
    public void checkIfZoneIsDeletableFailureOnHostTest() {
        final HostVO hostVO = Mockito.mock(HostVO.class);
        final ArrayList<HostVO> arrayList = new ArrayList<>();
        arrayList.add(hostVO);

        Mockito.when(_hostDao.listByDataCenterId(anyLong())).thenReturn(arrayList);
        Mockito.when(_podDao.listByDataCenterId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_privateIpAddressDao.countIPs(anyLong(), anyBoolean())).thenReturn(0);
        Mockito.when(_publicIpAddressDao.countIPs(anyLong(), anyBoolean())).thenReturn(0);
        Mockito.when(_vmInstanceDao.listByZoneId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_volumeDao.findByDc(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_physicalNetworkDao.listByZone(anyLong())).thenReturn(new ArrayList<>());

        configurationMgr.checkIfZoneIsDeletable(new Random().nextLong());
    }

    @Test(expected = CloudRuntimeException.class)
    public void checkIfZoneIsDeletableFailureOnPodTest() {
        final HostPodVO hostPodVO = Mockito.mock(HostPodVO.class);
        final ArrayList<HostPodVO> arrayList = new ArrayList<>();
        arrayList.add(hostPodVO);

        Mockito.when(_hostDao.listByDataCenterId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_podDao.listByDataCenterId(anyLong())).thenReturn(arrayList);
        Mockito.when(_privateIpAddressDao.countIPs(anyLong(), anyBoolean())).thenReturn(0);
        Mockito.when(_publicIpAddressDao.countIPs(anyLong(), anyBoolean())).thenReturn(0);
        Mockito.when(_vmInstanceDao.listByZoneId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_volumeDao.findByDc(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_physicalNetworkDao.listByZone(anyLong())).thenReturn(new ArrayList<>());

        configurationMgr.checkIfZoneIsDeletable(new Random().nextLong());
    }

    @Test(expected = CloudRuntimeException.class)
    public void checkIfZoneIsDeletableFailureOnPrivateIpAddressTest() {
        Mockito.when(_hostDao.listByDataCenterId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_podDao.listByDataCenterId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_privateIpAddressDao.countIPs(anyLong(), anyBoolean())).thenReturn(1);
        Mockito.when(_publicIpAddressDao.countIPs(anyLong(), anyBoolean())).thenReturn(0);
        Mockito.when(_vmInstanceDao.listByZoneId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_volumeDao.findByDc(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_physicalNetworkDao.listByZone(anyLong())).thenReturn(new ArrayList<>());

        configurationMgr.checkIfZoneIsDeletable(new Random().nextLong());
    }

    @Test(expected = CloudRuntimeException.class)
    public void checkIfZoneIsDeletableFailureOnPublicIpAddressTest() {
        Mockito.when(_hostDao.listByDataCenterId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_podDao.listByDataCenterId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_privateIpAddressDao.countIPs(anyLong(), anyBoolean())).thenReturn(0);
        Mockito.when(_publicIpAddressDao.countIPs(anyLong(), anyBoolean())).thenReturn(1);
        Mockito.when(_vmInstanceDao.listByZoneId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_volumeDao.findByDc(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_physicalNetworkDao.listByZone(anyLong())).thenReturn(new ArrayList<>());

        configurationMgr.checkIfZoneIsDeletable(new Random().nextLong());
    }

    @Test(expected = CloudRuntimeException.class)
    public void checkIfZoneIsDeletableFailureOnVmInstanceTest() {
        final VMInstanceVO vMInstanceVO = Mockito.mock(VMInstanceVO.class);
        final ArrayList<VMInstanceVO> arrayList = new ArrayList<>();
        arrayList.add(vMInstanceVO);

        Mockito.when(_hostDao.listByDataCenterId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_podDao.listByDataCenterId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_privateIpAddressDao.countIPs(anyLong(), anyBoolean())).thenReturn(0);
        Mockito.when(_publicIpAddressDao.countIPs(anyLong(), anyBoolean())).thenReturn(0);
        Mockito.when(_vmInstanceDao.listByZoneId(anyLong())).thenReturn(arrayList);
        Mockito.when(_volumeDao.findByDc(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_physicalNetworkDao.listByZone(anyLong())).thenReturn(new ArrayList<>());

        configurationMgr.checkIfZoneIsDeletable(new Random().nextLong());
    }

    @Test(expected = CloudRuntimeException.class)
    public void checkIfZoneIsDeletableFailureOnVolumeTest() {
        final VolumeVO volumeVO = Mockito.mock(VolumeVO.class);
        final ArrayList<VolumeVO> arrayList = new ArrayList<>();
        arrayList.add(volumeVO);

        Mockito.when(_hostDao.listByDataCenterId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_podDao.listByDataCenterId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_privateIpAddressDao.countIPs(anyLong(), anyBoolean())).thenReturn(0);
        Mockito.when(_publicIpAddressDao.countIPs(anyLong(), anyBoolean())).thenReturn(0);
        Mockito.when(_vmInstanceDao.listByZoneId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_volumeDao.findByDc(anyLong())).thenReturn(arrayList);
        Mockito.when(_physicalNetworkDao.listByZone(anyLong())).thenReturn(new ArrayList<>());

        configurationMgr.checkIfZoneIsDeletable(new Random().nextLong());
    }

    @Test(expected = CloudRuntimeException.class)
    public void checkIfZoneIsDeletableFailureOnPhysicalNetworkTest() {
        final PhysicalNetworkVO physicalNetworkVO = Mockito.mock(PhysicalNetworkVO.class);
        final ArrayList<PhysicalNetworkVO> arrayList = new ArrayList<>();
        arrayList.add(physicalNetworkVO);

        Mockito.when(_hostDao.listByDataCenterId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_podDao.listByDataCenterId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_privateIpAddressDao.countIPs(anyLong(), anyBoolean())).thenReturn(0);
        Mockito.when(_publicIpAddressDao.countIPs(anyLong(), anyBoolean())).thenReturn(0);
        Mockito.when(_vmInstanceDao.listByZoneId(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_volumeDao.findByDc(anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(_physicalNetworkDao.listByZone(anyLong())).thenReturn(arrayList);

        configurationMgr.checkIfZoneIsDeletable(new Random().nextLong());
    }

    public class DedicatePublicIpRangeCmdExtn extends DedicatePublicIpRangeCmd {
        @Override
        public long getEntityOwnerId() {
            return 1;
        }
    }

    public class ReleasePublicIpRangeCmdExtn extends ReleasePublicIpRangeCmd {
        @Override
        public long getEntityOwnerId() {
            return 1;
        }
    }
}
