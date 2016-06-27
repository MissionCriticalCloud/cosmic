package com.cloud.usage;

import com.cloud.usage.parser.IPAddressUsageParser;
import com.cloud.usage.parser.LoadBalancerUsageParser;
import com.cloud.usage.parser.NetworkOfferingUsageParser;
import com.cloud.usage.parser.NetworkUsageParser;
import com.cloud.usage.parser.PortForwardingUsageParser;
import com.cloud.usage.parser.SecurityGroupUsageParser;
import com.cloud.usage.parser.StorageUsageParser;
import com.cloud.usage.parser.VMInstanceUsageParser;
import com.cloud.usage.parser.VPNUserUsageParser;
import com.cloud.usage.parser.VmDiskUsageParser;
import com.cloud.usage.parser.VolumeUsageParser;
import com.cloud.user.AccountVO;
import com.cloud.utils.component.ComponentContext;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.Date;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/UsageManagerTestContext.xml")
public class UsageManagerTest extends TestCase {
    @Inject
    UsageManagerImpl _usageMgr = null;
    @Inject
    VMInstanceUsageParser vmParser = null;
    @Inject
    IPAddressUsageParser ipParser = null;
    @Inject
    LoadBalancerUsageParser lbParser = null;
    @Inject
    NetworkOfferingUsageParser noParser = null;
    @Inject
    NetworkUsageParser netParser = null;
    @Inject
    VmDiskUsageParser vmdiskParser = null;
    @Inject
    PortForwardingUsageParser pfParser = null;
    @Inject
    SecurityGroupUsageParser sgParser = null;
    @Inject
    StorageUsageParser stParser = null;
    @Inject
    VolumeUsageParser volParser = null;
    @Inject
    VPNUserUsageParser vpnParser = null;

    Date startDate = null;
    Date endDate = null;

    @Before
    public void setup() throws Exception {
        System.setProperty("pid", "5678");
        ComponentContext.initComponentsLifeCycle();
        startDate = new Date();
        endDate = new Date(100000L + System.currentTimeMillis());
    }

    @Test
    public void testParse() throws ConfigurationException {
        final UsageJobVO job = new UsageJobVO();
        _usageMgr.parse(job, System.currentTimeMillis(), 100000L + System.currentTimeMillis());
    }

    @Test
    public void testSchedule() throws ConfigurationException {
        _usageMgr.scheduleParse();
    }

    @Test
    public void testParsers() throws ConfigurationException {
        final AccountVO account = new AccountVO();
        account.setId(2L);
        VMInstanceUsageParser.parse(account, startDate, endDate);
        IPAddressUsageParser.parse(account, startDate, endDate);
        LoadBalancerUsageParser.parse(account, startDate, endDate);
        NetworkOfferingUsageParser.parse(account, startDate, endDate);
        NetworkUsageParser.parse(account, startDate, endDate);
        VmDiskUsageParser.parse(account, startDate, endDate);
        PortForwardingUsageParser.parse(account, startDate, endDate);
        SecurityGroupUsageParser.parse(account, startDate, endDate);
        StorageUsageParser.parse(account, startDate, endDate);
        VolumeUsageParser.parse(account, startDate, endDate);
        VPNUserUsageParser.parse(account, startDate, endDate);
    }
}
