package org.apache.cloudstack.resourcedetail.dao;

import org.apache.cloudstack.resourcedetail.ResourceDetailsDaoBase;
import org.apache.cloudstack.resourcedetail.UserIpAddressDetailVO;

import org.springframework.stereotype.Component;

@Component
public class UserIpAddressDetailsDaoImpl extends ResourceDetailsDaoBase<UserIpAddressDetailVO> implements UserIpAddressDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new UserIpAddressDetailVO(resourceId, key, value, display));
    }
}
