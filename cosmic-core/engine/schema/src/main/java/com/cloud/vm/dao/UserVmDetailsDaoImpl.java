package com.cloud.vm.dao;

import com.cloud.vm.UserVmDetailVO;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDaoBase;

import org.springframework.stereotype.Component;

@Component
public class UserVmDetailsDaoImpl extends ResourceDetailsDaoBase<UserVmDetailVO> implements UserVmDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new UserVmDetailVO(resourceId, key, value, display));
    }
}
