package com.cloud.resourcedetail.dao;

import com.cloud.resourcedetail.NetworkACLItemDetailVO;
import com.cloud.resourcedetail.ResourceDetailsDaoBase;

import org.springframework.stereotype.Component;

@Component
public class NetworkACLItemDetailsDaoImpl extends ResourceDetailsDaoBase<NetworkACLItemDetailVO> implements NetworkACLItemDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new NetworkACLItemDetailVO(resourceId, key, value, display));
    }
}
