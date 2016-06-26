package com.cloud.api.query.dao;

import com.cloud.api.query.vo.ServiceOfferingJoinVO;
import com.cloud.offering.ServiceOffering;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.api.response.ServiceOfferingResponse;

public interface ServiceOfferingJoinDao extends GenericDao<ServiceOfferingJoinVO, Long> {

    ServiceOfferingResponse newServiceOfferingResponse(ServiceOfferingJoinVO offering);

    ServiceOfferingJoinVO newServiceOfferingView(ServiceOffering offering);
}
