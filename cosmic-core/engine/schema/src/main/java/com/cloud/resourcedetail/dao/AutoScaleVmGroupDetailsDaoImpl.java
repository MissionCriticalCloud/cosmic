package com.cloud.resourcedetail.dao;

import com.cloud.resourcedetail.AutoScaleVmGroupDetailVO;
import com.cloud.resourcedetail.ResourceDetailsDaoBase;

import org.springframework.stereotype.Component;

@Component
public class AutoScaleVmGroupDetailsDaoImpl extends ResourceDetailsDaoBase<AutoScaleVmGroupDetailVO> implements AutoScaleVmGroupDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new AutoScaleVmGroupDetailVO(resourceId, key, value, display));
    }
}
