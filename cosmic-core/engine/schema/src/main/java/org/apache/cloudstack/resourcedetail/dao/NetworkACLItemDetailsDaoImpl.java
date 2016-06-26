package org.apache.cloudstack.resourcedetail.dao;

import org.apache.cloudstack.resourcedetail.NetworkACLItemDetailVO;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDaoBase;

import org.springframework.stereotype.Component;

@Component
public class NetworkACLItemDetailsDaoImpl extends ResourceDetailsDaoBase<NetworkACLItemDetailVO> implements NetworkACLItemDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new NetworkACLItemDetailVO(resourceId, key, value, display));
    }
}
