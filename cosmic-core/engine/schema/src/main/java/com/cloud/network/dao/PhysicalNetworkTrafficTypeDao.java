package com.cloud.network.dao;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.network.Networks.TrafficType;
import com.cloud.utils.Pair;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface PhysicalNetworkTrafficTypeDao extends GenericDao<PhysicalNetworkTrafficTypeVO, Long> {
    Pair<List<PhysicalNetworkTrafficTypeVO>, Integer> listAndCountBy(long physicalNetworkId);

    boolean isTrafficTypeSupported(long physicalNetworkId, TrafficType trafficType);

    String getNetworkTag(long physicalNetworkId, TrafficType trafficType, HypervisorType hType);

    PhysicalNetworkTrafficTypeVO findBy(long physicalNetworkId, TrafficType trafficType);

    void deleteTrafficTypes(long physicalNetworkId);
}
