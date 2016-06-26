package org.apache.cloudstack.resourcedetail.dao;

import org.apache.cloudstack.resourcedetail.RemoteAccessVpnDetailVO;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDaoBase;

import org.springframework.stereotype.Component;

@Component
public class RemoteAccessVpnDetailsDaoImpl extends ResourceDetailsDaoBase<RemoteAccessVpnDetailVO> implements RemoteAccessVpnDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new RemoteAccessVpnDetailVO(resourceId, key, value, display));
    }
}
