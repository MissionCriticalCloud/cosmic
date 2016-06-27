package org.apache.cloudstack.resourcedetail.dao;

import org.apache.cloudstack.resourcedetail.NetworkACLListDetailVO;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDaoBase;

import org.springframework.stereotype.Component;

@Component
public class NetworkACLListDetailsDaoImpl extends ResourceDetailsDaoBase<NetworkACLListDetailVO> implements NetworkACLListDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new NetworkACLListDetailVO(resourceId, key, value, display));
    }
}
