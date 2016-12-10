package com.cloud.api.query.dao;

import com.cloud.api.query.vo.DiskOfferingJoinVO;
import com.cloud.api.response.DiskOfferingResponse;
import com.cloud.offering.DiskOffering;
import com.cloud.utils.db.GenericDao;

public interface DiskOfferingJoinDao extends GenericDao<DiskOfferingJoinVO, Long> {

    DiskOfferingResponse newDiskOfferingResponse(DiskOfferingJoinVO dof);

    DiskOfferingJoinVO newDiskOfferingView(DiskOffering dof);
}
