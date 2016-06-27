package com.cloud.network.vpc.dao;

import com.cloud.network.vpc.VpcGateway;
import com.cloud.network.vpc.VpcGatewayVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface VpcGatewayDao extends GenericDao<VpcGatewayVO, Long> {
    VpcGatewayVO getPrivateGatewayForVpc(long vpcId);

    Long getNetworkAclIdForPrivateIp(long vpcId, long networkId, String ipaddr);

    List<VpcGatewayVO> listByVpcIdAndType(long vpcId, VpcGateway.Type type);

    List<VpcGatewayVO> listByAclIdAndType(long aclId, VpcGateway.Type type);
}
