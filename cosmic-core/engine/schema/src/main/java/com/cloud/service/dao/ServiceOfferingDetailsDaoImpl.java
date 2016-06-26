package com.cloud.service.dao;

import com.cloud.service.ServiceOfferingDetailsVO;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDaoBase;

import org.springframework.stereotype.Component;

@Component
public class ServiceOfferingDetailsDaoImpl extends ResourceDetailsDaoBase<ServiceOfferingDetailsVO> implements ServiceOfferingDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new ServiceOfferingDetailsVO(resourceId, key, value, display));
    }
}
