package com.cloud.network.element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloud.dao.EntityManager;
import com.cloud.db.model.Zone;
import com.cloud.db.repository.ZoneRepository;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.RemoteAccessVpn;
import com.cloud.network.VpnUser;
import com.cloud.network.router.VpcVirtualNetworkApplianceManagerImpl;
import com.cloud.network.topology.AdvancedNetworkTopology;
import com.cloud.network.topology.BasicNetworkTopology;
import com.cloud.network.topology.NetworkTopologyContext;
import com.cloud.network.vpc.Vpc;
import com.cloud.vm.DomainRouterVO;
import com.cloud.vm.dao.DomainRouterDao;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VpcVirtualRouterElementTest {
    @Mock
    EntityManager _entityMgr;
    @Mock
    NetworkTopologyContext networkTopologyContext;
    @InjectMocks
    VpcVirtualNetworkApplianceManagerImpl _vpcRouterMgr;
    @InjectMocks
    VpcVirtualRouterElement vpcVirtualRouterElement;
    @Mock
    private DomainRouterDao _routerDao;
    @Mock
    ZoneRepository zoneRepository;

    @Test
    public void testApplyVpnUsers() {
        vpcVirtualRouterElement._vpcRouterMgr = _vpcRouterMgr;

        final AdvancedNetworkTopology advancedNetworkTopology = Mockito.mock(AdvancedNetworkTopology.class);
        final BasicNetworkTopology basicNetworkTopology = Mockito.mock(BasicNetworkTopology.class);

        networkTopologyContext.setAdvancedNetworkTopology(advancedNetworkTopology);
        networkTopologyContext.setBasicNetworkTopology(basicNetworkTopology);
        networkTopologyContext.init();

        final Vpc vpc = Mockito.mock(Vpc.class);
        final Zone zone = Mockito.mock(Zone.class);
        final RemoteAccessVpn remoteAccessVpn = Mockito.mock(RemoteAccessVpn.class);
        final DomainRouterVO domainRouterVO1 = Mockito.mock(DomainRouterVO.class);
        final DomainRouterVO domainRouterVO2 = Mockito.mock(DomainRouterVO.class);
        final VpnUser vpnUser1 = Mockito.mock(VpnUser.class);
        final VpnUser vpnUser2 = Mockito.mock(VpnUser.class);

        final List<VpnUser> users = new ArrayList<>();
        users.add(vpnUser1);
        users.add(vpnUser2);

        final List<DomainRouterVO> routers = new ArrayList<>();
        routers.add(domainRouterVO1);
        routers.add(domainRouterVO2);

        final Long vpcId = new Long(1l);
        final Long zoneId = new Long(1l);

        when(remoteAccessVpn.getVpcId()).thenReturn(vpcId);
        when(_vpcRouterMgr.getVpcRouters(vpcId)).thenReturn(routers);
        when(_entityMgr.findById(Vpc.class, vpcId)).thenReturn(vpc);
        when(vpc.getZoneId()).thenReturn(zoneId);
        when(zoneRepository.findOne(zoneId)).thenReturn(zone);
        when(networkTopologyContext.retrieveNetworkTopology(zone)).thenReturn(advancedNetworkTopology);

        try {
            when(advancedNetworkTopology.applyVpnUsers(remoteAccessVpn, users, domainRouterVO1)).thenReturn(new String[]{"user1", "user2"});
            when(advancedNetworkTopology.applyVpnUsers(remoteAccessVpn, users, domainRouterVO2)).thenReturn(new String[]{"user3", "user4"});
        } catch (final ResourceUnavailableException e) {
            fail(e.getMessage());
        }

        try {
            final String[] results = vpcVirtualRouterElement.applyVpnUsers(remoteAccessVpn, users);

            assertNotNull(results);
            assertEquals(results[0], "user1");
            assertEquals(results[1], "user2");
            assertEquals(results[2], "user3");
            assertEquals(results[3], "user4");
        } catch (final ResourceUnavailableException e) {
            fail(e.getMessage());
        }

        verify(remoteAccessVpn, times(1)).getVpcId();
        verify(vpc, times(1)).getZoneId();
        verify(zoneRepository, times(1)).findOne(zoneId);
        verify(networkTopologyContext, times(1)).retrieveNetworkTopology(zone);
    }

    @Test
    public void testApplyVpnUsersException1() {
        vpcVirtualRouterElement._vpcRouterMgr = _vpcRouterMgr;

        final AdvancedNetworkTopology advancedNetworkTopology = Mockito.mock(AdvancedNetworkTopology.class);
        final BasicNetworkTopology basicNetworkTopology = Mockito.mock(BasicNetworkTopology.class);

        networkTopologyContext.setAdvancedNetworkTopology(advancedNetworkTopology);
        networkTopologyContext.setBasicNetworkTopology(basicNetworkTopology);
        networkTopologyContext.init();

        final RemoteAccessVpn remoteAccessVpn = Mockito.mock(RemoteAccessVpn.class);
        final List<VpnUser> users = new ArrayList<>();

        when(remoteAccessVpn.getVpcId()).thenReturn(null);

        try {
            final String[] results = vpcVirtualRouterElement.applyVpnUsers(remoteAccessVpn, users);
            assertNull(results);
        } catch (final ResourceUnavailableException e) {
            fail(e.getMessage());
        }

        verify(remoteAccessVpn, times(1)).getVpcId();
    }

    @Test
    public void testApplyVpnUsersException2() {
        vpcVirtualRouterElement._vpcRouterMgr = _vpcRouterMgr;

        final AdvancedNetworkTopology advancedNetworkTopology = Mockito.mock(AdvancedNetworkTopology.class);
        final BasicNetworkTopology basicNetworkTopology = Mockito.mock(BasicNetworkTopology.class);

        networkTopologyContext.setAdvancedNetworkTopology(advancedNetworkTopology);
        networkTopologyContext.setBasicNetworkTopology(basicNetworkTopology);
        networkTopologyContext.init();

        final RemoteAccessVpn remoteAccessVpn = Mockito.mock(RemoteAccessVpn.class);

        final List<VpnUser> users = new ArrayList<>();

        final Long vpcId = new Long(1l);

        when(remoteAccessVpn.getVpcId()).thenReturn(vpcId);
        when(_vpcRouterMgr.getVpcRouters(vpcId)).thenReturn(null);

        try {
            final String[] results = vpcVirtualRouterElement.applyVpnUsers(remoteAccessVpn, users);

            assertNull(results);
        } catch (final ResourceUnavailableException e) {
            fail(e.getMessage());
        }

        verify(remoteAccessVpn, times(1)).getVpcId();
    }
}
