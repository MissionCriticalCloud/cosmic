package com.cloud.network.vpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

import com.cloud.context.CallContext;
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.legacymodel.network.Network;
import com.cloud.legacymodel.network.Network.Capability;
import com.cloud.legacymodel.network.Network.Provider;
import com.cloud.legacymodel.network.Network.Service;
import com.cloud.legacymodel.network.vpc.Vpc;
import com.cloud.legacymodel.user.Account;
import com.cloud.legacymodel.user.User;
import com.cloud.legacymodel.vm.VirtualMachine;
import com.cloud.model.enumeration.ComplianceStatus;
import com.cloud.network.NetworkModel;
import com.cloud.network.element.NetworkElement;
import com.cloud.network.router.VpcVirtualNetworkApplianceManager;
import com.cloud.network.vpc.dao.VpcDao;
import com.cloud.network.vpc.dao.VpcOfferingServiceMapDao;
import com.cloud.user.AccountManager;
import com.cloud.user.AccountVO;
import com.cloud.user.UserVO;
import com.cloud.vm.DomainRouterVO;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.dao.DomainRouterDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class VpcManagerImplTest {

    final long vpcId = 1L;
    @Mock
    VpcOfferingServiceMapDao vpcOffSvcMapDao;
    @Mock
    Vpc vpc;
    @Mock
    VpcDao vpcDao;
    @Mock
    VpcVO vpcVO;
    @Mock
    DomainRouterDao routerDao;
    @Mock
    VpcVirtualNetworkApplianceManager routerMgr;
    @Mock
    AccountManager accountMgr;
    VpcManagerImpl manager;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        manager = spy(new VpcManagerImpl());
        manager._accountMgr = accountMgr;
        manager._routerDao = routerDao;
        manager._routerMgr = routerMgr;
        manager._vpcDao = vpcDao;
        manager._vpcOffSvcMapDao = vpcOffSvcMapDao;

        final AccountVO account = new AccountVO("admin", 5L, "networkDomain", Account.ACCOUNT_TYPE_NORMAL, "uuid");
        final UserVO user = new UserVO(1, "testuser", "password", "firstname", "lastName", "email", "timezone", UUID.randomUUID().toString(), User.Source.UNKNOWN);
        CallContext.register(user, account);
        when(accountMgr.getActiveUser(anyLong())).thenReturn(user);

        when(vpc.getId()).thenReturn(vpcId);
        doReturn(vpc).when(this.manager).getActiveVpc(vpcId);
        when(vpcDao.findById(vpcId)).thenReturn(vpcVO);
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

    @Test
    public void testRestartVpcWithoutCleanUp() throws Exception {
        doReturn(true).when(this.manager).startVpc(vpcId, false);
        final boolean result = manager.restartVpc(vpcId, false);

        assertTrue(result);
    }

    @Test
    public void testRestartVpcWithCleanUp() throws Exception {
        // Setup routers
        final DomainRouterVO stoppedRouter = mock(DomainRouterVO.class);
        when(stoppedRouter.getId()).thenReturn(1L);
        when(stoppedRouter.getState()).thenReturn(VirtualMachine.State.Stopped);

        final DomainRouterVO runningRouter = mock(DomainRouterVO.class);
        when(runningRouter.getState()).thenReturn(VirtualMachine.State.Running);
        when(runningRouter.getId()).thenReturn(2L);

        final List<DomainRouterVO> routerList = new ArrayList<>();
        routerList.add(stoppedRouter);
        routerList.add(runningRouter);

        when(manager._routerDao.listByVpcId(vpcId)).thenReturn(routerList);

        doReturn(true).when(manager).rollingRestartVpc(eq(vpc), eq(routerList), any(ReservationContext.class));
        doReturn(true).when(manager).startVpc(vpcId, false);

        final boolean result = manager.restartVpc(vpcId, true);

        assertTrue(result);
    }

    @Test
    public void testRestartVpcResetsCompliance() throws Exception {
        when(manager._routerDao.listByVpcId(vpcId)).thenReturn(new ArrayList<>());

        doReturn(true).when(manager).startVpc(vpcId, false);
        when(vpc.getComplianceStatus()).thenReturn(ComplianceStatus.VPCNeedsRestart);

        final boolean result = manager.restartVpc(vpcId, true);

        verify(vpcVO).setComplianceStatus(ComplianceStatus.Compliant);
        assertTrue(result);
    }

    @Test(expected = InvalidParameterValueException.class)
    public void testRestartVpcFailsOnEmptyVPC() throws Exception {
        doReturn(null).when(this.manager).getActiveVpc(vpcId);

        manager.restartVpc(vpcId, false);
    }

    @Test
    public void testRestartVpcFailedRollingRestart() throws Exception {
        final List<DomainRouterVO> routerList = new ArrayList<>();
        routerList.add(mock(DomainRouterVO.class));

        when(manager._routerDao.listByVpcId(vpcId)).thenReturn(routerList);

        doReturn(false).when(manager).rollingRestartVpc(eq(vpc), eq(routerList), any(ReservationContext.class));

        final boolean result = manager.restartVpc(vpcId, true);
        assertFalse(result);
    }

    @Test
    public void testRestartVpcFailedStart() throws Exception {
        doReturn(false).when(this.manager).startVpc(vpcId, false);
        final boolean result = manager.restartVpc(vpcId, false);

        assertFalse(result);
    }

    @After
    public void tearDown() {
        CallContext.unregister();
    }
}
