package org.apache.cloudstack.resourcedetail.dao;

import org.apache.cloudstack.resourcedetail.DiskOfferingDetailVO;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDaoBase;

import org.springframework.stereotype.Component;

@Component
public class DiskOfferingDetailsDaoImpl extends ResourceDetailsDaoBase<DiskOfferingDetailVO> implements DiskOfferingDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new DiskOfferingDetailVO(resourceId, key, value, display));
    }
}
