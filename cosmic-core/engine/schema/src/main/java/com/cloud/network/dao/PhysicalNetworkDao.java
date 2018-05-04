package com.cloud.network.dao;

import com.cloud.model.enumeration.TrafficType;
import com.cloud.network.PhysicalNetwork.IsolationMethod;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface PhysicalNetworkDao extends GenericDao<PhysicalNetworkVO, Long> {
    List<PhysicalNetworkVO> listByZone(long zoneId);

    List<PhysicalNetworkVO> listByZoneIncludingRemoved(long zoneId);

    List<PhysicalNetworkVO> listByZoneAndTrafficType(long dataCenterId, TrafficType trafficType);

    List<PhysicalNetworkVO> listByZoneAndTrafficTypeAndIsolationMethod(long dataCenterId, TrafficType trafficType, IsolationMethod isolationMethod);
}
