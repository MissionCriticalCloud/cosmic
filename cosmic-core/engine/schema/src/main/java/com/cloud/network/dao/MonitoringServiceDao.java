package com.cloud.network.dao;

import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface MonitoringServiceDao extends GenericDao<MonitoringServiceVO, Long> {

    List<MonitoringServiceVO> listAllServices();

    List<MonitoringServiceVO> listDefaultServices(boolean isDefault);

    MonitoringServiceVO getServiceByName(String service);
}
