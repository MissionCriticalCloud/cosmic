package com.cloud.network.dao;

import com.cloud.network.Networks.TrafficType;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface PhysicalNetworkDao extends GenericDao<PhysicalNetworkVO, Long> {
    List<PhysicalNetworkVO> listByZone(long zoneId);

    List<PhysicalNetworkVO> listByZoneIncludingRemoved(long zoneId);

    List<PhysicalNetworkVO> listByZoneAndTrafficType(long dataCenterId, TrafficType trafficType);
}
