package com.cloud.vpc.dao;

import com.cloud.network.vpc.VpcOfferingVO;
import com.cloud.network.vpc.dao.VpcOfferingDao;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;

@DB()
public class MockVpcOfferingDaoImpl extends GenericDaoBase<VpcOfferingVO, Long> implements VpcOfferingDao {

    /* (non-Javadoc)
     * @see com.cloud.network.vpc.Dao.VpcOfferingDao#findByUniqueName(java.lang.String)
     */
    @Override
    public VpcOfferingVO findByUniqueName(final String uniqueName) {
        return new VpcOfferingVO();
    }

    @Override
    public VpcOfferingVO persist(final VpcOfferingVO vo) {
        return vo;
    }
}
