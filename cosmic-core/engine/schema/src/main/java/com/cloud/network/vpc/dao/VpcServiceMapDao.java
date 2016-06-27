package com.cloud.network.vpc.dao;

import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.dao.NetworkServiceMapVO;
import com.cloud.network.vpc.VpcServiceMapVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

/**
 * VpcServiceMapDao deals with searches and operations done on the
 * vpc_service_map table.
 */
public interface VpcServiceMapDao extends GenericDao<VpcServiceMapVO, Long> {
    boolean areServicesSupportedInVpc(long vpcId, Service... services);

    boolean canProviderSupportServiceInVpc(long vpcId, Service service, Provider provider);

    List<NetworkServiceMapVO> getServicesInVpc(long vpcId);

    String getProviderForServiceInVpc(long vpcId, Service service);

    void deleteByVpcId(long vpcId);

    List<String> getDistinctProviders(long vpcId);

    String isProviderForVpc(long vpcId, Provider provider);
}
