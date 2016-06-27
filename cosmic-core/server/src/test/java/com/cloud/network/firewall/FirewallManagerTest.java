package com.cloud.network.firewall;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.IpAddressManager;
import com.cloud.network.Network;
import com.cloud.network.NetworkModel;
import com.cloud.network.NetworkRuleApplier;
import com.cloud.network.dao.FirewallRulesDao;
import com.cloud.network.element.FirewallServiceProvider;
import com.cloud.network.element.VirtualRouterElement;
import com.cloud.network.element.VpcVirtualRouterElement;
import com.cloud.network.rules.FirewallManager;
import com.cloud.network.rules.FirewallRule;
import com.cloud.network.rules.FirewallRule.Purpose;
import com.cloud.network.rules.FirewallRuleVO;
import com.cloud.network.vpc.VpcManager;
import com.cloud.user.AccountManager;
import com.cloud.user.DomainManager;
import com.cloud.utils.component.ComponentContext;
import org.apache.cloudstack.engine.orchestration.service.NetworkOrchestrationService;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@Ignore("Requires database to be set up")
@RunWith(MockitoJUnitRunner.class)
//@ContextConfiguration(locations = "classpath:/testContext.xml")
//@ComponentSetup(managerName="management-server", setupXml="network-mgr-component.xml")
public class FirewallManagerTest {
    private static final Logger s_logger = LoggerFactory.getLogger(FirewallManagerTest.class);

    //    @Before
    //    public void setUp() {
    //        Logger daoLogger = LoggerFactory.getLogger(GenericDaoBase.class);
    //        Logger cloudLogger = LoggerFactory.getLogger("com.cloud");
    //
    //        componentlogger.setLevel(Level.WARN);
    //        daoLogger.setLevel(Level.ERROR);
    //        cloudLogger.setLevel(Level.ERROR);
    //        s_logger.setLevel(Level.INFO);
    //        super.setUp();
    //    }
    @Mock
    AccountManager _accountMgr;
    @Mock
    NetworkOrchestrationService _networkMgr;
    @Mock
    NetworkModel _networkModel;
    @Mock
    DomainManager _domainMgr;
    @Mock
    VpcManager _vpcMgr;
    @Mock
    IpAddressManager _ipAddrMgr;
    @Mock
    FirewallRulesDao _firewallDao;
    @InjectMocks
    FirewallManager _firewallMgr = new FirewallManagerImpl();

    @Ignore("Requires database to be set up")
    @Test
    public void testInjected() {

        //        FirewallManagerImpl firewallMgr = (FirewallManagerImpl)ComponentLocator.getCurrentLocator().getManager(FirewallManager.class);
        //        Assert.assertTrue(firewallMgr._firewallElements.enumeration().hasMoreElements());
        //        Assert.assertTrue(firewallMgr._pfElements.enumeration().hasMoreElements());
        //        Assert.assertTrue(firewallMgr._staticNatElements.enumeration().hasMoreElements());
        //        Assert.assertTrue(firewallMgr._networkAclElements.enumeration().hasMoreElements());
        //        Assert.assertNotNull(firewallMgr._networkModel);
        //
        //        Assert.assertNotNull(firewallMgr._firewallElements.get("VirtualRouter"));
        //        Assert.assertNotNull(firewallMgr._firewallElements.get("VpcVirtualRouter"));
        //        Assert.assertNotNull(firewallMgr._pfElements.get("VirtualRouter"));
        //        Assert.assertNotNull(firewallMgr._pfElements.get("VpcVirtualRouter"));
        //        Assert.assertNotNull(firewallMgr._staticNatElements.get("VirtualRouter"));
        //        Assert.assertNotNull(firewallMgr._staticNatElements.get("VpcVirtualRouter"));
        //        Assert.assertNotNull(firewallMgr._networkAclElements.get("VpcVirtualRouter"));
        //        Assert.assertNull(firewallMgr._networkAclElements.get("VirtualRouter"));
        //
        //
        //        Assert.assertTrue(firewallMgr._firewallElements.get("VirtualRouter") instanceof FirewallServiceProvider);
        //        Assert.assertTrue(firewallMgr._pfElements.get("VirtualRouter") instanceof PortForwardingServiceProvider);
        //        Assert.assertTrue(firewallMgr._staticNatElements.get("VirtualRouter") instanceof StaticNatServiceProvider);
        //        Assert.assertTrue(firewallMgr._networkAclElements.get("VpcVirtualRouter") instanceof NetworkACLServiceProvider);

        s_logger.info("Done testing injection of service elements into firewall manager");
    }

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Ignore("Requires database to be set up")
    @Test
    public void testApplyRules() {
        final List<FirewallRuleVO> ruleList = new ArrayList<>();
        final FirewallRuleVO rule = new FirewallRuleVO("rule1", 1, 80, "TCP", 1, 2, 1, FirewallRule.Purpose.Firewall, null, null, null, null);
        ruleList.add(rule);
        final FirewallManagerImpl firewallMgr = (FirewallManagerImpl) _firewallMgr;

        final NetworkOrchestrationService netMgr = mock(NetworkOrchestrationService.class);
        final IpAddressManager addrMgr = mock(IpAddressManager.class);
        firewallMgr._networkMgr = netMgr;

        try {
            firewallMgr.applyRules(ruleList, false, false);
            verify(addrMgr).applyRules(any(List.class), any(FirewallRule.Purpose.class), any(NetworkRuleApplier.class), anyBoolean());
        } catch (final ResourceUnavailableException e) {
            Assert.fail("Unreachable code");
        }
    }

    @Ignore("Requires database to be set up")
    @Test
    public void testApplyFWRules() {
        final List<FirewallRuleVO> ruleList = new ArrayList<>();
        final FirewallRuleVO rule = new FirewallRuleVO("rule1", 1, 80, "TCP", 1, 2, 1, FirewallRule.Purpose.Firewall, null, null, null, null);
        ruleList.add(rule);
        final FirewallManagerImpl firewallMgr = (FirewallManagerImpl) _firewallMgr;
        final VirtualRouterElement virtualRouter = mock(VirtualRouterElement.class);
        final VpcVirtualRouterElement vpcVirtualRouter = mock(VpcVirtualRouterElement.class);

        final List<FirewallServiceProvider> fwElements = new ArrayList<>();
        fwElements.add(ComponentContext.inject(VirtualRouterElement.class));
        fwElements.add(ComponentContext.inject(VpcVirtualRouterElement.class));

        firewallMgr._firewallElements = fwElements;

        try {
            when(virtualRouter.applyFWRules(any(Network.class), any(List.class))).thenReturn(false);
            when(vpcVirtualRouter.applyFWRules(any(Network.class), any(List.class))).thenReturn(true);
            //Network network, Purpose purpose, List<? extends FirewallRule> rules
            firewallMgr.applyRules(mock(Network.class), Purpose.Firewall, ruleList);
            verify(vpcVirtualRouter).applyFWRules(any(Network.class), any(List.class));
            verify(virtualRouter).applyFWRules(any(Network.class), any(List.class));
        } catch (final ResourceUnavailableException e) {
            Assert.fail("Unreachable code");
        }
    }

    @Test
    public void testDetectRulesConflict() {
        final List<FirewallRuleVO> ruleList = new ArrayList<>();
        final FirewallRuleVO rule1 = spy(new FirewallRuleVO("rule1", 3, 500, "UDP", 1, 2, 1, Purpose.Vpn, null, null, null, null));
        final FirewallRuleVO rule2 = spy(new FirewallRuleVO("rule2", 3, 1701, "UDP", 1, 2, 1, Purpose.Vpn, null, null, null, null));
        final FirewallRuleVO rule3 = spy(new FirewallRuleVO("rule3", 3, 4500, "UDP", 1, 2, 1, Purpose.Vpn, null, null, null, null));

        ruleList.add(rule1);
        ruleList.add(rule2);
        ruleList.add(rule3);

        final FirewallManagerImpl firewallMgr = (FirewallManagerImpl) _firewallMgr;

        when(firewallMgr._firewallDao.listByIpAndPurposeAndNotRevoked(3, null)).thenReturn(ruleList);
        when(rule1.getId()).thenReturn(1L);
        when(rule2.getId()).thenReturn(2L);
        when(rule3.getId()).thenReturn(3L);

        final FirewallRule newRule1 = new FirewallRuleVO("newRule1", 3, 500, "TCP", 1, 2, 1, Purpose.PortForwarding, null, null, null, null);
        final FirewallRule newRule2 = new FirewallRuleVO("newRule2", 3, 1701, "TCP", 1, 2, 1, Purpose.PortForwarding, null, null, null, null);
        final FirewallRule newRule3 = new FirewallRuleVO("newRule3", 3, 4500, "TCP", 1, 2, 1, Purpose.PortForwarding, null, null, null, null);

        try {
            firewallMgr.detectRulesConflict(newRule1);
            firewallMgr.detectRulesConflict(newRule2);
            firewallMgr.detectRulesConflict(newRule3);
        } catch (final NetworkRuleConflictException ex) {
            Assert.fail();
        }
    }
}
