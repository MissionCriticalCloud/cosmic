package com.cloud.vpc.dao;

import com.cloud.network.Network.Service;
import com.cloud.network.vpc.VpcOfferingServiceMapVO;
import com.cloud.network.vpc.dao.VpcOfferingServiceMapDao;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;

import java.util.List;

@DB()
public class MockVpcOfferingServiceMapDaoImpl extends GenericDaoBase<VpcOfferingServiceMapVO, Long> implements VpcOfferingServiceMapDao {

    /* (non-Javadoc)
     * @see com.cloud.network.vpc.Dao.VpcOfferingServiceMapDao#listByVpcOffId(long)
     */
    @Override
    public List<VpcOfferingServiceMapVO> listByVpcOffId(final long vpcOffId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.vpc.Dao.VpcOfferingServiceMapDao#areServicesSupportedByNetworkOffering(long, com.cloud.network.Network.Service[])
     */
    @Override
    public boolean areServicesSupportedByNetworkOffering(final long networkOfferingId, final Service[] services) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.vpc.Dao.VpcOfferingServiceMapDao#listServicesForVpcOffering(long)
     */
    @Override
    public List<String> listServicesForVpcOffering(final long vpcOfferingId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.vpc.Dao.VpcOfferingServiceMapDao#findByServiceProviderAndOfferingId(java.lang.String, java.lang.String, long)
     */
    @Override
    public VpcOfferingServiceMapVO findByServiceProviderAndOfferingId(final String service, final String provider, final long vpcOfferingId) {
        return new VpcOfferingServiceMapVO();
    }

    @Override
    public VpcOfferingServiceMapVO persist(final VpcOfferingServiceMapVO vo) {
        return vo;
    }
}
