package org.apache.cloudstack.engine.provisioning.test;

import org.apache.cloudstack.engine.datacenter.entity.api.db.dao.EngineClusterDao;
import org.apache.cloudstack.engine.datacenter.entity.api.db.dao.EngineDataCenterDao;
import org.apache.cloudstack.engine.datacenter.entity.api.db.dao.EngineHostDao;
import org.apache.cloudstack.engine.datacenter.entity.api.db.dao.EngineHostPodDao;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;

public class ChildTestConfiguration {

    @Bean
    public EngineDataCenterDao dataCenterDao() {
        return Mockito.mock(EngineDataCenterDao.class);
    }

    @Bean
    public EngineHostPodDao hostPodDao() {
        return Mockito.mock(EngineHostPodDao.class);
    }

    @Bean
    public EngineClusterDao clusterDao() {
        return Mockito.mock(EngineClusterDao.class);
    }

    @Bean
    public EngineHostDao hostDao() {
        return Mockito.mock(EngineHostDao.class);
    }
}
