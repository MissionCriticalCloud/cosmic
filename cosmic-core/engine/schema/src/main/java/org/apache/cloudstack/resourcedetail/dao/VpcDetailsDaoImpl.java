package org.apache.cloudstack.resourcedetail.dao;

import org.apache.cloudstack.resourcedetail.ResourceDetailsDaoBase;
import org.apache.cloudstack.resourcedetail.VpcDetailVO;

import org.springframework.stereotype.Component;

@Component
public class VpcDetailsDaoImpl extends ResourceDetailsDaoBase<VpcDetailVO> implements VpcDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new VpcDetailVO(resourceId, key, value, display));
    }
}
