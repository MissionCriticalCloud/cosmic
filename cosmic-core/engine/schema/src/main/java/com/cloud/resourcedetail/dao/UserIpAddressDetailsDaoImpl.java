package com.cloud.resourcedetail.dao;

import com.cloud.resourcedetail.ResourceDetailsDaoBase;
import com.cloud.resourcedetail.UserIpAddressDetailVO;

import org.springframework.stereotype.Component;

@Component
public class UserIpAddressDetailsDaoImpl extends ResourceDetailsDaoBase<UserIpAddressDetailVO> implements UserIpAddressDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new UserIpAddressDetailVO(resourceId, key, value, display));
    }
}
