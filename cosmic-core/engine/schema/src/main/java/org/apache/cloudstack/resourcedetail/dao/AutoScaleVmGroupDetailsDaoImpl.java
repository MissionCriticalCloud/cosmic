package org.apache.cloudstack.resourcedetail.dao;

import org.apache.cloudstack.resourcedetail.AutoScaleVmGroupDetailVO;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDaoBase;

import org.springframework.stereotype.Component;

@Component
public class AutoScaleVmGroupDetailsDaoImpl extends ResourceDetailsDaoBase<AutoScaleVmGroupDetailVO> implements AutoScaleVmGroupDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new AutoScaleVmGroupDetailVO(resourceId, key, value, display));
    }
}
