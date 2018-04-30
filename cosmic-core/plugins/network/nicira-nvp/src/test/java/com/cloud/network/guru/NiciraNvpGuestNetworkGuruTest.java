package com.cloud.network.guru;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.CreateLogicalSwitchAnswer;
import com.cloud.agent.api.DeleteLogicalSwitchAnswer;
import com.cloud.db.model.Zone;
import com.cloud.db.repository.ZoneRepository;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.deploy.DeployDestination;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.domain.Domain;
import com.cloud.engine.orchestration.service.NetworkOrchestrationService;
import com.cloud.exception.InsufficientVirtualNetworkCapacityException;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.model.enumeration.NetworkType;
import com.cloud.network.Network;
import com.cloud.network.Network.GuestType;
import com.cloud.network.Network.Service;
import com.cloud.network.NetworkModel;
import com.cloud.network.NetworkProfile;
import com.cloud.network.Networks.BroadcastDomainType;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.NiciraNvpDeviceVO;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkVO;
import com.cloud.network.dao.NiciraNvpDao;
import com.cloud.network.dao.PhysicalNetworkDao;
import com.cloud.network.dao.PhysicalNetworkVO;
import com.cloud.offering.NetworkOffering;
import com.cloud.offerings.dao.NetworkOfferingServiceMapDao;
import com.cloud.user.Account;
import com.cloud.vm.ReservationContext;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

public class NiciraNvpGuestNetworkGuruTest {
    private static final long NETWORK_ID = 42L;
    PhysicalNetworkDao physnetdao = mock(PhysicalNetworkDao.class);
    NiciraNvpDao nvpdao = mock(NiciraNvpDao.class);
    DataCenterDao dcdao = mock(DataCenterDao.class);
    NetworkOfferingServiceMapDao nosd = mock(NetworkOfferingServiceMapDao.class);
    AgentManager agentmgr = mock(AgentManager.class);
    NetworkOrchestrationService netmgr = mock(NetworkOrchestrationService.class);
    NetworkModel netmodel = mock(NetworkModel.class);

    HostDao hostdao = mock(HostDao.class);
    NetworkDao netdao = mock(NetworkDao.class);
    NiciraNvpGuestNetworkGuru guru;

    ZoneRepository zoneRepository = mock(ZoneRepository.class);

    @Before
    public void setUp() {
        guru = new NiciraNvpGuestNetworkGuru();
        ((GuestNetworkGuru) guru)._physicalNetworkDao = physnetdao;
        guru.physicalNetworkDao = physnetdao;
        guru.niciraNvpDao = nvpdao;
        guru._dcDao = dcdao;
        guru.zoneRepository = zoneRepository;
        guru.ntwkOfferingSrvcDao = nosd;
        guru.networkModel = netmodel;
        guru.hostDao = hostdao;
        guru.agentMgr = agentmgr;
        guru.networkDao = netdao;

        final DataCenterVO dc = mock(DataCenterVO.class);
        when(dc.getNetworkType()).thenReturn(NetworkType.Advanced);
        when(dc.getGuestNetworkCidr()).thenReturn("10.1.1.1/24");
        when(dcdao.findById((Long) any())).thenReturn(dc);

        final Zone zone = mock(Zone.class);
        when(zone.getNetworkType()).thenReturn(NetworkType.Advanced);
        when(zone.getGuestNetworkCidr()).thenReturn("10.1.1.1/24");
        when(zoneRepository.findById(anyLong())).thenReturn(Optional.of(zone));
    }

    @Test
    public void testCanHandle() {
        final NetworkOffering offering = mock(NetworkOffering.class);
        when(offering.getId()).thenReturn(NETWORK_ID);
        when(offering.getTrafficType()).thenReturn(TrafficType.Guest);
        when(offering.getGuestType()).thenReturn(GuestType.Isolated);

        final PhysicalNetworkVO physnet = mock(PhysicalNetworkVO.class);
        when(physnet.getIsolationMethods()).thenReturn(Arrays.asList(new String[]{"STT", "VXLAN"}));
        when(physnet.getId()).thenReturn(NETWORK_ID);

        when(nosd.areServicesSupportedByNetworkOffering(NETWORK_ID, Service.Connectivity)).thenReturn(true);

        assertTrue(guru.canHandle(offering, NetworkType.Advanced, physnet) == true);

        // Supported: IsolationMethod == VXLAN
        when(physnet.getIsolationMethods()).thenReturn(Arrays.asList(new String[]{"VXLAN"}));
        assertTrue(guru.canHandle(offering, NetworkType.Advanced, physnet) == true);

        // Not supported TrafficType != Guest
        when(offering.getTrafficType()).thenReturn(TrafficType.Management);
        assertFalse(guru.canHandle(offering, NetworkType.Advanced, physnet) == true);

        // Not supported: GuestType Shared
        when(offering.getTrafficType()).thenReturn(TrafficType.Guest);
        when(offering.getGuestType()).thenReturn(GuestType.Shared);
        assertFalse(guru.canHandle(offering, NetworkType.Advanced, physnet) == true);

        // Not supported: Basic networking
        when(offering.getGuestType()).thenReturn(GuestType.Isolated);
        assertFalse(guru.canHandle(offering, NetworkType.Basic, physnet) == true);

        // Not supported: IsolationMethod != STT, VXLAN
        when(physnet.getIsolationMethods()).thenReturn(Arrays.asList(new String[]{"VLAN"}));
        assertFalse(guru.canHandle(offering, NetworkType.Advanced, physnet) == true);
    }

    @Test
    public void testDesign() {
        final PhysicalNetworkVO physnet = mock(PhysicalNetworkVO.class);
        when(physnetdao.findById((Long) any())).thenReturn(physnet);
        when(physnet.getIsolationMethods()).thenReturn(Arrays.asList(new String[]{"STT", "VXLAN"}));
        when(physnet.getId()).thenReturn(NETWORK_ID);

        final NiciraNvpDeviceVO device = mock(NiciraNvpDeviceVO.class);
        when(nvpdao.listByPhysicalNetwork(NETWORK_ID)).thenReturn(Arrays.asList(new NiciraNvpDeviceVO[]{device}));
        when(device.getId()).thenReturn(1L);

        final NetworkOffering offering = mock(NetworkOffering.class);
        when(offering.getId()).thenReturn(NETWORK_ID);
        when(offering.getTrafficType()).thenReturn(TrafficType.Guest);
        when(offering.getGuestType()).thenReturn(GuestType.Isolated);

        when(nosd.areServicesSupportedByNetworkOffering(NETWORK_ID, Service.Connectivity)).thenReturn(true);

        final DeploymentPlan plan = mock(DeploymentPlan.class);
        final Network network = mock(Network.class);
        final Account account = mock(Account.class);

        final Network designednetwork = guru.design(offering, plan, network, account);
        assertTrue(designednetwork != null);
        assertTrue(designednetwork.getBroadcastDomainType() == BroadcastDomainType.Lswitch);
    }

    @Test
    public void testDesignNoElementOnPhysicalNetwork() {
        final PhysicalNetworkVO physnet = mock(PhysicalNetworkVO.class);
        when(physnetdao.findById((Long) any())).thenReturn(physnet);
        when(physnet.getIsolationMethods()).thenReturn(Arrays.asList(new String[]{"STT", "VXLAN"}));
        when(physnet.getId()).thenReturn(NETWORK_ID);

        mock(NiciraNvpDeviceVO.class);
        when(nvpdao.listByPhysicalNetwork(NETWORK_ID)).thenReturn(Collections.<NiciraNvpDeviceVO>emptyList());

        final NetworkOffering offering = mock(NetworkOffering.class);
        when(offering.getId()).thenReturn(NETWORK_ID);
        when(offering.getTrafficType()).thenReturn(TrafficType.Guest);
        when(offering.getGuestType()).thenReturn(GuestType.Isolated);

        final DeploymentPlan plan = mock(DeploymentPlan.class);
        final Network network = mock(Network.class);
        final Account account = mock(Account.class);

        final Network designednetwork = guru.design(offering, plan, network, account);
        assertTrue(designednetwork == null);
    }

    @Test
    public void testDesignNoIsolationMethodSTT() {
        final PhysicalNetworkVO physnet = mock(PhysicalNetworkVO.class);
        when(physnetdao.findById((Long) any())).thenReturn(physnet);
        when(physnet.getIsolationMethods()).thenReturn(Arrays.asList(new String[]{"VLAN"}));
        when(physnet.getId()).thenReturn(NETWORK_ID);

        mock(NiciraNvpDeviceVO.class);
        when(nvpdao.listByPhysicalNetwork(NETWORK_ID)).thenReturn(Collections.<NiciraNvpDeviceVO>emptyList());

        final NetworkOffering offering = mock(NetworkOffering.class);
        when(offering.getId()).thenReturn(NETWORK_ID);
        when(offering.getTrafficType()).thenReturn(TrafficType.Guest);
        when(offering.getGuestType()).thenReturn(GuestType.Isolated);

        final DeploymentPlan plan = mock(DeploymentPlan.class);
        final Network network = mock(Network.class);
        final Account account = mock(Account.class);

        final Network designednetwork = guru.design(offering, plan, network, account);
        assertTrue(designednetwork == null);
    }

    @Test
    public void testDesignNoConnectivityInOffering() {
        final PhysicalNetworkVO physnet = mock(PhysicalNetworkVO.class);
        when(physnetdao.findById((Long) any())).thenReturn(physnet);
        when(physnet.getIsolationMethods()).thenReturn(Arrays.asList(new String[]{"STT", "VXLAN"}));
        when(physnet.getId()).thenReturn(NETWORK_ID);

        final NiciraNvpDeviceVO device = mock(NiciraNvpDeviceVO.class);
        when(nvpdao.listByPhysicalNetwork(NETWORK_ID)).thenReturn(Arrays.asList(new NiciraNvpDeviceVO[]{device}));
        when(device.getId()).thenReturn(1L);

        final NetworkOffering offering = mock(NetworkOffering.class);
        when(offering.getId()).thenReturn(NETWORK_ID);
        when(offering.getTrafficType()).thenReturn(TrafficType.Guest);
        when(offering.getGuestType()).thenReturn(GuestType.Isolated);

        when(nosd.areServicesSupportedByNetworkOffering(NETWORK_ID, Service.Connectivity)).thenReturn(false);

        final DeploymentPlan plan = mock(DeploymentPlan.class);
        final Network network = mock(Network.class);
        final Account account = mock(Account.class);

        final Network designednetwork = guru.design(offering, plan, network, account);
        assertTrue(designednetwork == null);
    }

    @Test
    public void testImplement() throws InsufficientVirtualNetworkCapacityException {
        final PhysicalNetworkVO physnet = mock(PhysicalNetworkVO.class);
        when(physnetdao.findById((Long) any())).thenReturn(physnet);
        when(physnet.getIsolationMethods()).thenReturn(Arrays.asList(new String[]{"STT", "VXLAN"}));
        when(physnet.getId()).thenReturn(NETWORK_ID);

        final NiciraNvpDeviceVO device = mock(NiciraNvpDeviceVO.class);
        when(nvpdao.listByPhysicalNetwork(NETWORK_ID)).thenReturn(Arrays.asList(new NiciraNvpDeviceVO[]{device}));
        when(device.getId()).thenReturn(1L);

        final NetworkOffering offering = mock(NetworkOffering.class);
        when(offering.getId()).thenReturn(NETWORK_ID);
        when(offering.getTrafficType()).thenReturn(TrafficType.Guest);
        when(offering.getGuestType()).thenReturn(GuestType.Isolated);

        when(nosd.areServicesSupportedByNetworkOffering(NETWORK_ID, Service.Connectivity)).thenReturn(false);

        mock(DeploymentPlan.class);

        final NetworkVO network = mock(NetworkVO.class);
        when(network.getName()).thenReturn("testnetwork");
        when(network.getState()).thenReturn(Network.State.Implementing);
        when(network.getPhysicalNetworkId()).thenReturn(NETWORK_ID);

        final DeployDestination dest = mock(DeployDestination.class);

        final Zone zone = mock(Zone.class);
        when(dest.getZone()).thenReturn(zone);

        final HostVO niciraHost = mock(HostVO.class);
        when(hostdao.findById(anyLong())).thenReturn(niciraHost);
        when(niciraHost.getDetail("transportzoneuuid")).thenReturn("aaaa");
        when(niciraHost.getDetail("transportzoneisotype")).thenReturn("stt");
        when(niciraHost.getId()).thenReturn(NETWORK_ID);

        when(netmodel.findPhysicalNetworkId(anyLong(), (String) any(), (TrafficType) any())).thenReturn(NETWORK_ID);
        final Domain dom = mock(Domain.class);
        when(dom.getName()).thenReturn("domain");
        final Account acc = mock(Account.class);
        when(acc.getAccountName()).thenReturn("accountname");
        final ReservationContext res = mock(ReservationContext.class);
        when(res.getDomain()).thenReturn(dom);
        when(res.getAccount()).thenReturn(acc);

        final CreateLogicalSwitchAnswer answer = mock(CreateLogicalSwitchAnswer.class);
        when(answer.getResult()).thenReturn(true);
        when(answer.getLogicalSwitchUuid()).thenReturn("aaaaa");
        when(agentmgr.easySend(eq(NETWORK_ID), any())).thenReturn(answer);

        final Network implementednetwork = guru.implement(network, offering, dest, res);
        assertTrue(implementednetwork != null);
        verify(agentmgr, times(1)).easySend(eq(NETWORK_ID), any());
    }

    @Test
    public void testImplementWithCidr() throws InsufficientVirtualNetworkCapacityException {
        final PhysicalNetworkVO physnet = mock(PhysicalNetworkVO.class);
        when(physnetdao.findById((Long) any())).thenReturn(physnet);
        when(physnet.getIsolationMethods()).thenReturn(Arrays.asList(new String[]{"STT"}));
        when(physnet.getId()).thenReturn(NETWORK_ID);

        final NiciraNvpDeviceVO device = mock(NiciraNvpDeviceVO.class);
        when(nvpdao.listByPhysicalNetwork(NETWORK_ID)).thenReturn(Arrays.asList(new NiciraNvpDeviceVO[]{device}));
        when(device.getId()).thenReturn(1L);

        final NetworkOffering offering = mock(NetworkOffering.class);
        when(offering.getId()).thenReturn(NETWORK_ID);
        when(offering.getTrafficType()).thenReturn(TrafficType.Guest);
        when(offering.getGuestType()).thenReturn(GuestType.Isolated);

        when(nosd.areServicesSupportedByNetworkOffering(NETWORK_ID, Service.Connectivity)).thenReturn(false);

        mock(DeploymentPlan.class);

        final NetworkVO network = mock(NetworkVO.class);
        when(network.getName()).thenReturn("testnetwork");
        when(network.getState()).thenReturn(Network.State.Implementing);
        when(network.getGateway()).thenReturn("10.1.1.1");
        when(network.getCidr()).thenReturn("10.1.1.0/24");
        when(network.getPhysicalNetworkId()).thenReturn(NETWORK_ID);

        final DeployDestination dest = mock(DeployDestination.class);

        final Zone zone = mock(Zone.class);
        when(dest.getZone()).thenReturn(zone);

        final HostVO niciraHost = mock(HostVO.class);
        when(hostdao.findById(anyLong())).thenReturn(niciraHost);
        when(niciraHost.getDetail("transportzoneuuid")).thenReturn("aaaa");
        when(niciraHost.getDetail("transportzoneisotype")).thenReturn("stt");
        when(niciraHost.getId()).thenReturn(NETWORK_ID);

        when(netmodel.findPhysicalNetworkId(anyLong(), (String) any(), (TrafficType) any())).thenReturn(NETWORK_ID);
        final Domain dom = mock(Domain.class);
        when(dom.getName()).thenReturn("domain");
        final Account acc = mock(Account.class);
        when(acc.getAccountName()).thenReturn("accountname");
        final ReservationContext res = mock(ReservationContext.class);
        when(res.getDomain()).thenReturn(dom);
        when(res.getAccount()).thenReturn(acc);

        final CreateLogicalSwitchAnswer answer = mock(CreateLogicalSwitchAnswer.class);
        when(answer.getResult()).thenReturn(true);
        when(answer.getLogicalSwitchUuid()).thenReturn("aaaaa");
        when(agentmgr.easySend(eq(NETWORK_ID), (Command) any())).thenReturn(answer);

        final Network implementednetwork = guru.implement(network, offering, dest, res);
        assertTrue(implementednetwork != null);
        assertTrue(implementednetwork.getCidr().equals("10.1.1.0/24"));
        assertTrue(implementednetwork.getGateway().equals("10.1.1.1"));
        verify(agentmgr, times(1)).easySend(eq(NETWORK_ID), (Command) any());
    }

    @Test
    public void testImplementURIException() throws InsufficientVirtualNetworkCapacityException {
        final PhysicalNetworkVO physnet = mock(PhysicalNetworkVO.class);
        when(physnetdao.findById((Long) any())).thenReturn(physnet);
        when(physnet.getIsolationMethods()).thenReturn(Arrays.asList(new String[]{"STT"}));
        when(physnet.getId()).thenReturn(NETWORK_ID);

        final NiciraNvpDeviceVO device = mock(NiciraNvpDeviceVO.class);
        when(nvpdao.listByPhysicalNetwork(NETWORK_ID)).thenReturn(Arrays.asList(new NiciraNvpDeviceVO[]{device}));
        when(device.getId()).thenReturn(1L);

        final NetworkOffering offering = mock(NetworkOffering.class);
        when(offering.getId()).thenReturn(NETWORK_ID);
        when(offering.getTrafficType()).thenReturn(TrafficType.Guest);
        when(offering.getGuestType()).thenReturn(GuestType.Isolated);

        when(nosd.areServicesSupportedByNetworkOffering(NETWORK_ID, Service.Connectivity)).thenReturn(false);

        mock(DeploymentPlan.class);

        final NetworkVO network = mock(NetworkVO.class);
        when(network.getName()).thenReturn("testnetwork");
        when(network.getState()).thenReturn(Network.State.Implementing);
        when(network.getPhysicalNetworkId()).thenReturn(NETWORK_ID);

        final DeployDestination dest = mock(DeployDestination.class);

        final Zone zone = mock(Zone.class);
        when(dest.getZone()).thenReturn(zone);

        final HostVO niciraHost = mock(HostVO.class);
        when(hostdao.findById(anyLong())).thenReturn(niciraHost);
        when(niciraHost.getDetail("transportzoneuuid")).thenReturn("aaaa");
        when(niciraHost.getDetail("transportzoneisotype")).thenReturn("stt");
        when(niciraHost.getId()).thenReturn(NETWORK_ID);

        when(netmodel.findPhysicalNetworkId(anyLong(), (String) any(), (TrafficType) any())).thenReturn(NETWORK_ID);
        final Domain dom = mock(Domain.class);
        when(dom.getName()).thenReturn("domain");
        final Account acc = mock(Account.class);
        when(acc.getAccountName()).thenReturn("accountname");
        final ReservationContext res = mock(ReservationContext.class);
        when(res.getDomain()).thenReturn(dom);
        when(res.getAccount()).thenReturn(acc);

        final CreateLogicalSwitchAnswer answer = mock(CreateLogicalSwitchAnswer.class);
        when(answer.getResult()).thenReturn(true);
        //when(answer.getLogicalSwitchUuid()).thenReturn("aaaaa");
        when(agentmgr.easySend(eq(NETWORK_ID), (Command) any())).thenReturn(answer);

        final Network implementednetwork = guru.implement(network, offering, dest, res);
        assertTrue(implementednetwork == null);
        verify(agentmgr, times(1)).easySend(eq(NETWORK_ID), (Command) any());
    }

    @Test
    public void testShutdown() throws InsufficientVirtualNetworkCapacityException, URISyntaxException {
        final PhysicalNetworkVO physnet = mock(PhysicalNetworkVO.class);
        when(physnetdao.findById((Long) any())).thenReturn(physnet);
        when(physnet.getIsolationMethods()).thenReturn(Arrays.asList(new String[]{"STT", "VXLAN"}));
        when(physnet.getId()).thenReturn(NETWORK_ID);

        final NiciraNvpDeviceVO device = mock(NiciraNvpDeviceVO.class);
        when(nvpdao.listByPhysicalNetwork(NETWORK_ID)).thenReturn(Arrays.asList(new NiciraNvpDeviceVO[]{device}));
        when(device.getId()).thenReturn(1L);

        final NetworkOffering offering = mock(NetworkOffering.class);
        when(offering.getId()).thenReturn(NETWORK_ID);
        when(offering.getTrafficType()).thenReturn(TrafficType.Guest);
        when(offering.getGuestType()).thenReturn(GuestType.Isolated);

        when(nosd.areServicesSupportedByNetworkOffering(NETWORK_ID, Service.Connectivity)).thenReturn(false);

        mock(DeploymentPlan.class);

        final NetworkVO network = mock(NetworkVO.class);
        when(network.getName()).thenReturn("testnetwork");
        when(network.getState()).thenReturn(Network.State.Implementing);
        when(network.getBroadcastDomainType()).thenReturn(BroadcastDomainType.Lswitch);
        when(network.getBroadcastUri()).thenReturn(new URI("lswitch:aaaaa"));
        when(network.getPhysicalNetworkId()).thenReturn(NETWORK_ID);
        when(netdao.findById(NETWORK_ID)).thenReturn(network);

        final DeployDestination dest = mock(DeployDestination.class);

        final Zone zone = mock(Zone.class);
        when(dest.getZone()).thenReturn(zone);

        final HostVO niciraHost = mock(HostVO.class);
        when(hostdao.findById(anyLong())).thenReturn(niciraHost);
        when(niciraHost.getDetail("transportzoneuuid")).thenReturn("aaaa");
        when(niciraHost.getDetail("transportzoneisotype")).thenReturn("stt");
        when(niciraHost.getId()).thenReturn(NETWORK_ID);

        when(netmodel.findPhysicalNetworkId(anyLong(), (String) any(), (TrafficType) any())).thenReturn(NETWORK_ID);
        final Domain dom = mock(Domain.class);
        when(dom.getName()).thenReturn("domain");
        final Account acc = mock(Account.class);
        when(acc.getAccountName()).thenReturn("accountname");
        final ReservationContext res = mock(ReservationContext.class);
        when(res.getDomain()).thenReturn(dom);
        when(res.getAccount()).thenReturn(acc);

        final DeleteLogicalSwitchAnswer answer = mock(DeleteLogicalSwitchAnswer.class);
        when(answer.getResult()).thenReturn(true);
        when(agentmgr.easySend(eq(NETWORK_ID), (Command) any())).thenReturn(answer);

        final NetworkProfile implementednetwork = mock(NetworkProfile.class);
        when(implementednetwork.getId()).thenReturn(NETWORK_ID);
        when(implementednetwork.getBroadcastUri()).thenReturn(new URI("lswitch:aaaa"));
        when(offering.getSpecifyVlan()).thenReturn(false);

        guru.shutdown(implementednetwork, offering);
        verify(agentmgr, times(1)).easySend(eq(NETWORK_ID), (Command) any());
        verify(implementednetwork, times(1)).setBroadcastUri(null);
    }
}
