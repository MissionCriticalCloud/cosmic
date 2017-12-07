package com.cloud.network.dao;

import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface PhysicalNetworkIsolationMethodDao extends GenericDao<PhysicalNetworkIsolationMethodVO, Long> {
    public List<String> getAllIsolationMethod(final long physicalNetworkId);

    public String getIsolationMethod(final long physicalNetworkId);

    public int clearIsolationMethods(final long physicalNetworkId);
}
