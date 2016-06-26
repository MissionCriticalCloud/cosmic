package com.cloud.network.dao;

import org.apache.cloudstack.resourcedetail.ResourceDetailsDaoBase;

import org.springframework.stereotype.Component;

@Component
public class NetworkDetailsDaoImpl extends ResourceDetailsDaoBase<NetworkDetailVO> implements NetworkDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new NetworkDetailVO(resourceId, key, value, display));
    }
}
