package com.cloud.resourcedetail.dao;

import com.cloud.resourcedetail.RemoteAccessVpnDetailVO;
import com.cloud.resourcedetail.ResourceDetailsDaoBase;

import org.springframework.stereotype.Component;

@Component
public class RemoteAccessVpnDetailsDaoImpl extends ResourceDetailsDaoBase<RemoteAccessVpnDetailVO> implements RemoteAccessVpnDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new RemoteAccessVpnDetailVO(resourceId, key, value, display));
    }
}
