package org.apache.cloudstack.privategw;

import com.cloud.configuration.ConfigurationManager;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.VlanDao;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.NetworkModel;
import com.cloud.network.NetworkService;
import com.cloud.network.dao.FirewallRulesDao;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.PhysicalNetworkDao;
import com.cloud.network.dao.Site2SiteVpnGatewayDao;
import com.cloud.network.vpc.VpcManagerImpl;
import com.cloud.network.vpc.VpcService;
import com.cloud.network.vpc.dao.NetworkACLDao;
import com.cloud.network.vpc.dao.PrivateIpDao;
import com.cloud.network.vpc.dao.StaticRouteDao;
import com.cloud.network.vpc.dao.VpcDao;
import com.cloud.network.vpc.dao.VpcGatewayDao;
import com.cloud.network.vpc.dao.VpcOfferingDao;
import com.cloud.network.vpc.dao.VpcOfferingServiceMapDao;
import com.cloud.network.vpc.dao.VpcServiceMapDao;
import com.cloud.network.vpn.Site2SiteVpnManager;
import com.cloud.offerings.dao.NetworkOfferingServiceMapDao;
import com.cloud.server.ConfigurationServer;
import com.cloud.tags.dao.ResourceTagDao;
import com.cloud.user.AccountManager;
import com.cloud.user.ResourceLimitService;
import com.cloud.vm.dao.DomainRouterDao;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.admin.vpc.CreatePrivateGatewayCmd;
import org.apache.cloudstack.engine.orchestration.service.NetworkOrchestrationService;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.test.utils.SpringUtils;

import javax.naming.ConfigurationException;
import java.io.IOException;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class AclOnPrivateGwTest {

    private CreatePrivateGatewayCmd createPrivateGwCmd;

    @Before
    public void setUp() throws ConfigurationException {

        createPrivateGwCmd = new CreatePrivateGatewayCmd() {
            @Override
            public Long getEntityId() {
                return 2L;
            }
        };
    }

    @Test
    public void testExecuteSuccess() {

        final VpcService _vpcService = Mockito.mock(VpcService.class);

        try {
            _vpcService.applyVpcPrivateGateway(Matchers.anyLong(), Matchers.anyBoolean());
        } catch (final ResourceUnavailableException e) {
            e.printStackTrace();
        } catch (final ConcurrentOperationException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testExecuteFail() {
        final VpcService vpcService = Mockito.mock(VpcService.class);
        createPrivateGwCmd._vpcService = vpcService;

        try {
            Mockito.when(vpcService.applyVpcPrivateGateway(Matchers.anyLong(), Matchers.anyBoolean())).thenReturn(null);
        } catch (final ResourceUnavailableException e) {
            e.printStackTrace();
        } catch (final ConcurrentOperationException e) {
            e.printStackTrace();
        }

        try {
            createPrivateGwCmd.execute();
        } catch (final ServerApiException exception) {
            Assert.assertEquals("Failed to create private gateway", exception.getDescription());
        } catch (final ResourceAllocationException e) {
            e.printStackTrace();
        } catch (final InsufficientCapacityException e) {
            e.printStackTrace();
        } catch (final ConcurrentOperationException e) {
            e.printStackTrace();
        } catch (final ResourceUnavailableException e) {
            e.printStackTrace();
        }
    }

    @Configuration
    @ComponentScan(basePackageClasses = {VpcManagerImpl.class},
            includeFilters = {@ComponentScan.Filter(value = TestConfiguration.Library.class, type = FilterType.CUSTOM)},
            useDefaultFilters = false)
    public static class TestConfiguration extends SpringUtils.CloudStackTestConfiguration {
        @Bean
        public VpcOfferingDao vpcOfferingDao() {
            return Mockito.mock(VpcOfferingDao.class);
        }

        @Bean
        public VpcOfferingServiceMapDao vpcOfferingServiceMapDao() {
            return Mockito.mock(VpcOfferingServiceMapDao.class);
        }

        @Bean
        public VpcDao vpcDao() {
            return Mockito.mock(VpcDao.class);
        }

        @Bean
        public ConfigurationDao configurationDao() {
            return Mockito.mock(ConfigurationDao.class);
        }

        @Bean
        public ConfigurationManager configurationManager() {
            return Mockito.mock(ConfigurationManager.class);
        }

        @Bean
        public AccountManager accountManager() {
            return Mockito.mock(AccountManager.class);
        }

        @Bean
        public NetworkDao networkDao() {
            return Mockito.mock(NetworkDao.class);
        }

        @Bean
        public NetworkOrchestrationService networkManager() {
            return Mockito.mock(NetworkOrchestrationService.class);
        }

        @Bean
        public NetworkModel networkModel() {
            return Mockito.mock(NetworkModel.class);
        }

        @Bean
        public NetworkService networkService() {
            return Mockito.mock(NetworkService.class);
        }

        @Bean
        public IPAddressDao iPAddressDao() {
            return Mockito.mock(IPAddressDao.class);
        }

        @Bean
        public DomainRouterDao domainRouterDao() {
            return Mockito.mock(DomainRouterDao.class);
        }

        @Bean
        public VpcGatewayDao vpcGatewayDao() {
            return Mockito.mock(VpcGatewayDao.class);
        }

        @Bean
        public PrivateIpDao privateIpDao() {
            return Mockito.mock(PrivateIpDao.class);
        }

        @Bean
        public StaticRouteDao staticRouteDao() {
            return Mockito.mock(StaticRouteDao.class);
        }

        @Bean
        public NetworkOfferingServiceMapDao networkOfferingServiceMapDao() {
            return Mockito.mock(NetworkOfferingServiceMapDao.class);
        }

        @Bean
        public PhysicalNetworkDao physicalNetworkDao() {
            return Mockito.mock(PhysicalNetworkDao.class);
        }

        @Bean
        public ResourceTagDao resourceTagDao() {
            return Mockito.mock(ResourceTagDao.class);
        }

        @Bean
        public FirewallRulesDao firewallRulesDao() {
            return Mockito.mock(FirewallRulesDao.class);
        }

        @Bean
        public Site2SiteVpnGatewayDao site2SiteVpnGatewayDao() {
            return Mockito.mock(Site2SiteVpnGatewayDao.class);
        }

        @Bean
        public Site2SiteVpnManager site2SiteVpnManager() {
            return Mockito.mock(Site2SiteVpnManager.class);
        }

        @Bean
        public VlanDao vlanDao() {
            return Mockito.mock(VlanDao.class);
        }

        @Bean
        public ResourceLimitService resourceLimitService() {
            return Mockito.mock(ResourceLimitService.class);
        }

        @Bean
        public VpcServiceMapDao vpcServiceMapDao() {
            return Mockito.mock(VpcServiceMapDao.class);
        }

        @Bean
        public DataCenterDao dataCenterDao() {
            return Mockito.mock(DataCenterDao.class);
        }

        @Bean
        public ConfigurationServer configurationServer() {
            return Mockito.mock(ConfigurationServer.class);
        }

        @Bean
        public NetworkACLDao networkACLDao() {
            return Mockito.mock(NetworkACLDao.class);
        }

        public static class Library implements TypeFilter {

            @Override
            public boolean match(final MetadataReader mdr, final MetadataReaderFactory arg1) throws IOException {
                final ComponentScan cs = TestConfiguration.class.getAnnotation(ComponentScan.class);
                return SpringUtils.includedInBasePackageClasses(mdr.getClassMetadata().getClassName(), cs);
            }
        }
    }
}
