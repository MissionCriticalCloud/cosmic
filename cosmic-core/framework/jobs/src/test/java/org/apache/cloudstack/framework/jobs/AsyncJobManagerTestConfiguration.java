package org.apache.cloudstack.framework.jobs;

import com.cloud.storage.dao.StoragePoolDetailsDaoImpl;
import org.apache.cloudstack.framework.config.ConfigDepot;
import org.apache.cloudstack.framework.config.ScopedConfigStorage;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.framework.config.dao.ConfigurationDaoImpl;
import org.apache.cloudstack.framework.config.impl.ConfigDepotImpl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AsyncJobManagerTestConfiguration {

    @Bean
    public ConfigDepot configDepot() {
        return new ConfigDepotImpl();
    }

    @Bean
    public ConfigurationDao configDao() {
        return new ConfigurationDaoImpl();
    }

    @Bean
    public ScopedConfigStorage scopedConfigStorage() {
        return new StoragePoolDetailsDaoImpl();
    }

    @Bean
    public AsyncJobTestDashboard testDashboard() {
        return new AsyncJobTestDashboard();
    }
}
