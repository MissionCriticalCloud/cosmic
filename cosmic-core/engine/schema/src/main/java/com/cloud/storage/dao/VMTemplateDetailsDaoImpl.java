package com.cloud.storage.dao;

import com.cloud.storage.VMTemplateDetailVO;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDaoBase;

import org.springframework.stereotype.Component;

@Component
public class VMTemplateDetailsDaoImpl extends ResourceDetailsDaoBase<VMTemplateDetailVO> implements VMTemplateDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new VMTemplateDetailVO(resourceId, key, value, display));
    }
}
