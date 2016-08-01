package com.cloud.network.firewall;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.network.IpAddressManager;
import com.cloud.network.NetworkModel;
import com.cloud.network.dao.FirewallRulesDao;
import com.cloud.network.rules.FirewallManager;
import com.cloud.network.rules.FirewallRule;
import com.cloud.network.rules.FirewallRule.Purpose;
import com.cloud.network.rules.FirewallRuleVO;
import com.cloud.network.vpc.VpcManager;
import com.cloud.user.AccountManager;
import com.cloud.user.DomainManager;
import org.apache.cloudstack.engine.orchestration.service.NetworkOrchestrationService;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import org.junit.Before;
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

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
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
