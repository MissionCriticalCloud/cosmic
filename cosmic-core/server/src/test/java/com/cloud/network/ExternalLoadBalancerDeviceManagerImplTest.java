package com.cloud.network;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.routing.HealthCheckLBConfigAnswer;
import com.cloud.agent.api.to.LoadBalancerTO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.dc.dao.VlanDao;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.host.dao.HostDetailsDao;
import com.cloud.network.dao.ExternalFirewallDeviceDao;
import com.cloud.network.dao.ExternalLoadBalancerDeviceDao;
import com.cloud.network.dao.ExternalLoadBalancerDeviceVO;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.InlineLoadBalancerNicMapDao;
import com.cloud.network.dao.LoadBalancerDao;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkExternalFirewallDao;
import com.cloud.network.dao.NetworkExternalLoadBalancerDao;
import com.cloud.network.dao.NetworkExternalLoadBalancerVO;
import com.cloud.network.dao.NetworkServiceMapDao;
import com.cloud.network.dao.PhysicalNetworkDao;
import com.cloud.network.dao.PhysicalNetworkServiceProviderDao;
import com.cloud.network.lb.LoadBalancingRule;
import com.cloud.network.rules.dao.PortForwardingRulesDao;
import com.cloud.offerings.dao.NetworkOfferingDao;
import com.cloud.resource.ResourceManager;
import com.cloud.user.AccountManager;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserStatisticsDao;
import com.cloud.utils.net.Ip;
import com.cloud.vm.dao.DomainRouterDao;
import com.cloud.vm.dao.NicDao;
import org.apache.cloudstack.engine.orchestration.service.NetworkOrchestrationService;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExternalLoadBalancerDeviceManagerImplTest {

    @Mock
    protected HostPodDao _podDao = null;
    @Mock
    NetworkExternalLoadBalancerDao _networkExternalLBDao;
    @Mock
    ExternalLoadBalancerDeviceDao _externalLoadBalancerDeviceDao;
    @Mock
    HostDao _hostDao;
    @Mock
    DataCenterDao _dcDao;
    @Mock
    NetworkModel _networkModel;
    @Mock
    NetworkOrchestrationService _networkMgr;
    @Mock
    InlineLoadBalancerNicMapDao _inlineLoadBalancerNicMapDao;
    @Mock
    NicDao _nicDao;
    @Mock
    AgentManager _agentMgr;
    @Mock
    ResourceManager _resourceMgr;
    @Mock
    IPAddressDao _ipAddressDao;
    @Mock
    VlanDao _vlanDao;
    @Mock
    NetworkOfferingDao _networkOfferingDao;
    @Mock
    AccountDao _accountDao;
    @Mock
    PhysicalNetworkDao _physicalNetworkDao;
    @Mock
    PhysicalNetworkServiceProviderDao _physicalNetworkServiceProviderDao;
    @Mock
    AccountManager _accountMgr;
    @Mock
    UserStatisticsDao _userStatsDao;
    @Mock
    NetworkDao _networkDao;
    @Mock
    DomainRouterDao _routerDao;
    @Mock
    LoadBalancerDao _loadBalancerDao;
    @Mock
    PortForwardingRulesDao _portForwardingRulesDao;
    @Mock
    ConfigurationDao _configDao;
    @Mock
    HostDetailsDao _hostDetailDao;
    @Mock
    NetworkExternalLoadBalancerDao _networkLBDao;
    @Mock
    NetworkServiceMapDao _ntwkSrvcProviderDao;
    @Mock
    NetworkExternalFirewallDao _networkExternalFirewallDao;
    @Mock
    ExternalFirewallDeviceDao _externalFirewallDeviceDao;
    @Mock
    IpAddressManager _ipAddrMgr;

    @Mock
    Network network;

    @Mock
    LoadBalancingRule rule;

    ExternalLoadBalancerDeviceManagerImpl externalLoadBalancerDeviceManager;

    @Before
    public void setup() throws IllegalArgumentException,
            IllegalAccessException, NoSuchFieldException, SecurityException {
        externalLoadBalancerDeviceManager = new ExternalLoadBalancerDeviceManagerImpl() {
        };
        for (final Field fieldToInject : ExternalLoadBalancerDeviceManagerImpl.class
                .getDeclaredFields()) {
            if (fieldToInject.isAnnotationPresent(Inject.class)) {
                fieldToInject.setAccessible(true);
                fieldToInject.set(externalLoadBalancerDeviceManager, this
                        .getClass().getDeclaredField(fieldToInject.getName())
                        .get(this));
            }
        }
    }

    @Test
    public void getLBHealthChecks() throws ResourceUnavailableException,
            URISyntaxException {
        setupLBHealthChecksMocks();

        final HealthCheckLBConfigAnswer answer = Mockito
                .mock(HealthCheckLBConfigAnswer.class);
        Mockito.when(answer.getLoadBalancers()).thenReturn(
                Collections.<LoadBalancerTO>emptyList());
        Mockito.when(
                _agentMgr.easySend(Mockito.anyLong(),
                        Mockito.any(Command.class))).thenReturn(answer);

        Assert.assertNotNull(externalLoadBalancerDeviceManager
                .getLBHealthChecks(network, Arrays.asList(rule)));
    }

    private void setupLBHealthChecksMocks() throws URISyntaxException {
        Mockito.when(network.getId()).thenReturn(42l);
        Mockito.when(network.getBroadcastUri()).thenReturn(new URI("vlan://1"));
        final NetworkExternalLoadBalancerVO externalLb = Mockito
                .mock(NetworkExternalLoadBalancerVO.class);
        Mockito.when(externalLb.getExternalLBDeviceId()).thenReturn(66l);
        Mockito.when(_networkExternalLBDao.findByNetworkId(42)).thenReturn(
                externalLb);
        final ExternalLoadBalancerDeviceVO lbDevice = Mockito
                .mock(ExternalLoadBalancerDeviceVO.class);
        Mockito.when(_externalLoadBalancerDeviceDao.findById(66l)).thenReturn(
                lbDevice);
        Mockito.when(rule.getAlgorithm()).thenReturn("TEST");
        Mockito.when(rule.getProtocol()).thenReturn("TEST");
        Mockito.when(rule.getSourceIp()).thenReturn(new Ip(1l));
        Mockito.when(lbDevice.getHostId()).thenReturn(99l);
        final HostVO hostVo = Mockito.mock(HostVO.class);
        Mockito.when(_hostDao.findById(Mockito.anyLong())).thenReturn(hostVo);
    }

    @Test
    public void getLBHealthChecksNullAnswer() throws ResourceUnavailableException,
            URISyntaxException {
        setupLBHealthChecksMocks();

        Mockito.when(
                _agentMgr.easySend(Mockito.anyLong(),
                        Mockito.any(Command.class))).thenReturn(null);

        Assert.assertNull(externalLoadBalancerDeviceManager
                .getLBHealthChecks(network, Arrays.asList(rule)));
    }

    @Test
    public void testUsageTask() {
        final ExternalDeviceUsageManagerImpl.ExternalDeviceNetworkUsageTask usageTask = Mockito
                .mock(ExternalDeviceUsageManagerImpl.ExternalDeviceNetworkUsageTask.class);
        Mockito.when(_hostDao.listByType(Host.Type.ExternalFirewall)).thenReturn(new ArrayList<>());
        Mockito.when(_hostDao.listByType(Host.Type.ExternalLoadBalancer)).thenReturn(new ArrayList<>());
        usageTask.runInContext();
        Mockito.verify(usageTask, Mockito.times(0)).runExternalDeviceNetworkUsageTask();
    }
}
