package com.cloud.network.vpc.dao;

import com.cloud.network.Network.Service;
import com.cloud.network.vpc.VpcOfferingServiceMapVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface VpcOfferingServiceMapDao extends GenericDao<VpcOfferingServiceMapVO, Long> {

    List<VpcOfferingServiceMapVO> listByVpcOffId(long vpcOffId);

    /**
     * @param networkOfferingId
     * @param services
     * @return
     */
    boolean areServicesSupportedByNetworkOffering(long networkOfferingId, Service[] services);

    List<String> listServicesForVpcOffering(long vpcOfferingId);

    VpcOfferingServiceMapVO findByServiceProviderAndOfferingId(String service, String provider, long vpcOfferingId);
}
