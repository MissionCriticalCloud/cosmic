package com.cloud.usage;

import com.cloud.alert.AlertManager;
import com.cloud.event.dao.UsageEventDao;
import com.cloud.usage.UsageManagerTestConfiguration.Library;
import com.cloud.usage.dao.UsageDaoImpl;
import com.cloud.usage.dao.UsageIPAddressDaoImpl;
import com.cloud.usage.dao.UsageJobDaoImpl;
import com.cloud.usage.dao.UsageLoadBalancerPolicyDaoImpl;
import com.cloud.usage.dao.UsageNetworkDaoImpl;
import com.cloud.usage.dao.UsageNetworkOfferingDaoImpl;
import com.cloud.usage.dao.UsagePortForwardingRuleDaoImpl;
import com.cloud.usage.dao.UsageSecurityGroupDaoImpl;
import com.cloud.usage.dao.UsageStorageDaoImpl;
import com.cloud.usage.dao.UsageVMInstanceDaoImpl;
import com.cloud.usage.dao.UsageVPNUserDaoImpl;
import com.cloud.usage.dao.UsageVmDiskDaoImpl;
import com.cloud.usage.dao.UsageVolumeDaoImpl;
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
import com.cloud.user.dao.AccountDaoImpl;
import com.cloud.user.dao.UserStatisticsDaoImpl;
import org.apache.cloudstack.framework.config.dao.ConfigurationDaoImpl;
import org.apache.cloudstack.test.utils.SpringUtils;

import java.io.IOException;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

@Configuration
@ComponentScan(basePackageClasses = {AccountDaoImpl.class, UsageDaoImpl.class, UsageJobDaoImpl.class, UsageVMInstanceDaoImpl.class, UsageIPAddressDaoImpl.class,
        UsageNetworkDaoImpl.class, UsageVolumeDaoImpl.class, UsageStorageDaoImpl.class, UsageLoadBalancerPolicyDaoImpl.class,
        UsagePortForwardingRuleDaoImpl.class, UsageNetworkOfferingDaoImpl.class, UsageVPNUserDaoImpl.class, UsageVmDiskDaoImpl.class,
        UsageSecurityGroupDaoImpl.class, ConfigurationDaoImpl.class, UsageManagerImpl.class, VMInstanceUsageParser.class, IPAddressUsageParser.class,
        LoadBalancerUsageParser.class, NetworkOfferingUsageParser.class, NetworkUsageParser.class, PortForwardingUsageParser.class,
        SecurityGroupUsageParser.class, StorageUsageParser.class, VmDiskUsageParser.class, VolumeUsageParser.class, VPNUserUsageParser.class,
        UserStatisticsDaoImpl.class},
        includeFilters = {@Filter(value = Library.class, type = FilterType.CUSTOM)},
        useDefaultFilters = false)
public class UsageManagerTestConfiguration {

    @Bean
    public AlertManager alertManager() {
        return Mockito.mock(AlertManager.class);
    }

    @Bean
    public UsageEventDao usageEventDao() {
        return Mockito.mock(UsageEventDao.class);
    }

    public static class Library implements TypeFilter {

        @Override
        public boolean match(final MetadataReader mdr, final MetadataReaderFactory arg1) throws IOException {
            mdr.getClassMetadata().getClassName();
            final ComponentScan cs = UsageManagerTestConfiguration.class.getAnnotation(ComponentScan.class);
            return SpringUtils.includedInBasePackageClasses(mdr.getClassMetadata().getClassName(), cs);
        }
    }
}
