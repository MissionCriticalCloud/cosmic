package com.cloud.vpc;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/VpcTestContext.xml")
public class Site2SiteVpnTest {
    private final static Logger s_logger = LoggerFactory.getLogger(Site2SiteVpnTest.class);

    //    private static void addDaos(MockComponentLocator locator) {
    //        locator.addDao("AccountDao", AccountDaoImpl.class);
    //        locator.addDao("Site2SiteCustomerGatewayDao", Site2SiteCustomerGatewayDaoImpl.class);
    //        locator.addDao("Site2SiteVpnGatewayDao", Site2SiteVpnGatewayDaoImpl.class);
    //        locator.addDao("Site2SiteVpnConnectionDao", Site2SiteVpnConnectionDaoImpl.class);
    //
    //        locator.addDao("IPAddressDao", IPAddressDaoImpl.class);
    //        locator.addDao("VpcDao", VpcDaoImpl.class);
    //        locator.addDao("ConfiguratioDao", MockConfigurationDaoImpl.class);
    //
    //    }
    //
    //    private static void addManagers(MockComponentLocator locator) {
    //        locator.addManager("AccountManager", MockAccountManagerImpl.class);
    //        locator.addManager("VpcManager", MockVpcManagerImpl.class);
    //    }

    @Before
    public void setUp() {
        //        locator = new MockComponentLocator("management-server");
        //        addDaos(locator);
        //        addManagers(locator);
        //        s_logger.info("Finished setUp");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testInjected() throws Exception {
        //        List<Pair<String, Class<? extends Site2SiteVpnServiceProvider>>> list =
        //                new ArrayList<Pair<String, Class<? extends Site2SiteVpnServiceProvider>>>();
        //        list.add(new Pair<String, Class<? extends Site2SiteVpnServiceProvider>>("Site2SiteVpnServiceProvider", MockSite2SiteVpnServiceProvider.class));
        //        locator.addAdapterChain(Site2SiteVpnServiceProvider.class, list);
        //        s_logger.info("Finished add adapter");
        //        locator.makeActive(new DefaultInterceptorLibrary());
        //        s_logger.info("Finished make active");
        //        Site2SiteVpnManagerImpl vpnMgr = ComponentLocator.inject(Site2SiteVpnManagerImpl.class);
        //        s_logger.info("Finished inject");
        //        Assert.assertTrue(vpnMgr.configure("Site2SiteVpnMgr",new HashMap<String, Object>()) );
        //        Assert.assertTrue(vpnMgr.start());

    }
}
