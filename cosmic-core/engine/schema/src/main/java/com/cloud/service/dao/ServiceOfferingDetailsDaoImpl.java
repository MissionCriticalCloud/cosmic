package com.cloud.service.dao;

import com.cloud.resourcedetail.ResourceDetailsDaoBase;
import com.cloud.service.ServiceOfferingDetailsVO;

import org.springframework.stereotype.Component;

@Component
public class ServiceOfferingDetailsDaoImpl extends ResourceDetailsDaoBase<ServiceOfferingDetailsVO> implements ServiceOfferingDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new ServiceOfferingDetailsVO(resourceId, key, value, display));
    }
}
