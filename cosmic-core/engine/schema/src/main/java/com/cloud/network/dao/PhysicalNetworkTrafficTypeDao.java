package com.cloud.network.dao;

import com.cloud.legacymodel.utils.Pair;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.model.enumeration.TrafficType;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface PhysicalNetworkTrafficTypeDao extends GenericDao<PhysicalNetworkTrafficTypeVO, Long> {
    Pair<List<PhysicalNetworkTrafficTypeVO>, Integer> listAndCountBy(long physicalNetworkId);

    boolean isTrafficTypeSupported(long physicalNetworkId, TrafficType trafficType);

    String getNetworkTag(long physicalNetworkId, TrafficType trafficType, HypervisorType hType);

    PhysicalNetworkTrafficTypeVO findBy(long physicalNetworkId, TrafficType trafficType);

    void deleteTrafficTypes(long physicalNetworkId);
}
