package com.cloud.network.vpc.dao;

import com.cloud.network.vpc.VpcOfferingVO;
import com.cloud.utils.db.GenericDao;

public interface VpcOfferingDao extends GenericDao<VpcOfferingVO, Long> {
    /**
     * Returns the VPC offering that matches the unique name.
     *
     * @param uniqueName name
     * @return VpcOfferingVO
     */
    VpcOfferingVO findByUniqueName(String uniqueName);
}
