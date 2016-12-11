package com.cloud.engine.provisioning.test;

import com.cloud.engine.datacenter.entity.api.db.dao.EngineClusterDao;
import com.cloud.engine.datacenter.entity.api.db.dao.EngineDataCenterDao;
import com.cloud.engine.datacenter.entity.api.db.dao.EngineHostDao;
import com.cloud.engine.datacenter.entity.api.db.dao.EngineHostPodDao;

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
