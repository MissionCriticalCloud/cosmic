package com.cloud.network.vpc.dao;

import com.cloud.network.vpc.Vpc;
import com.cloud.network.vpc.VpcVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;
import java.util.Map;

public interface VpcDao extends GenericDao<VpcVO, Long> {

    /**
     * @param offId
     * @return
     */
    int getVpcCountByOfferingId(long offId);

    Vpc getActiveVpcById(long vpcId);

    List<? extends Vpc> listByAccountId(long accountId);

    List<VpcVO> listInactiveVpcs();

    long countByAccountId(long accountId);

    VpcVO persist(VpcVO vpc, Map<String, List<String>> serviceProviderMap);

    void persistVpcServiceProviders(long vpcId, Map<String, List<String>> serviceProviderMap);
}
