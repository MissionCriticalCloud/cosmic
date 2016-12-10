package com.cloud.resourcedetail.dao;

import com.cloud.resourcedetail.AutoScaleVmProfileDetailVO;
import com.cloud.resourcedetail.ResourceDetailsDaoBase;

import org.springframework.stereotype.Component;

@Component
public class AutoScaleVmProfileDetailsDaoImpl extends ResourceDetailsDaoBase<AutoScaleVmProfileDetailVO> implements AutoScaleVmProfileDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new AutoScaleVmProfileDetailVO(resourceId, key, value, display));
    }
}
