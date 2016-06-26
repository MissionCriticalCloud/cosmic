package org.apache.cloudstack.resourcedetail.dao;

import org.apache.cloudstack.resourcedetail.ResourceDetailsDaoBase;
import org.apache.cloudstack.resourcedetail.UserDetailVO;

import org.springframework.stereotype.Component;

@Component
public class UserDetailsDaoImpl extends ResourceDetailsDaoBase<UserDetailVO> implements UserDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new UserDetailVO(resourceId, key, value));
    }
}
