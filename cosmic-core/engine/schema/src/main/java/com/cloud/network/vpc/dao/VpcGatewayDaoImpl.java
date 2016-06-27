package com.cloud.network.vpc.dao;

import com.cloud.network.vpc.VpcGateway;
import com.cloud.network.vpc.VpcGatewayVO;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
@DB()
public class VpcGatewayDaoImpl extends GenericDaoBase<VpcGatewayVO, Long> implements VpcGatewayDao {
    protected final SearchBuilder<VpcGatewayVO> AllFieldsSearch;

    protected VpcGatewayDaoImpl() {
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("vpcId", AllFieldsSearch.entity().getVpcId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("type", AllFieldsSearch.entity().getType(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("networkid", AllFieldsSearch.entity().getNetworkId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("ipaddress", AllFieldsSearch.entity().getIp4Address(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("aclId", AllFieldsSearch.entity().getNetworkACLId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.done();
    }

    @Override
    public VpcGatewayVO getPrivateGatewayForVpc(final long vpcId) {
        final SearchCriteria<VpcGatewayVO> sc = AllFieldsSearch.create();
        sc.setParameters("vpcId", vpcId);
        sc.setParameters("type", VpcGateway.Type.Private);

        return findOneBy(sc);
    }

    @Override
    public Long getNetworkAclIdForPrivateIp(final long vpcId, final long networkId, final String ipaddr) {
        final SearchCriteria<VpcGatewayVO> sc = AllFieldsSearch.create();
        sc.setParameters("vpcId", vpcId);
        sc.setParameters("networkid", networkId);
        sc.setParameters("ipaddress", ipaddr);

        final VpcGateway vpcGateway = findOneBy(sc);
        if (vpcGateway != null) {
            return vpcGateway.getNetworkACLId();
        } else {
            return null;
        }
    }

    @Override
    public List<VpcGatewayVO> listByVpcIdAndType(final long vpcId, final VpcGateway.Type type) {
        final SearchCriteria<VpcGatewayVO> sc = AllFieldsSearch.create();
        sc.setParameters("vpcId", vpcId);
        sc.setParameters("type", type);
        return listBy(sc);
    }

    @Override
    public List<VpcGatewayVO> listByAclIdAndType(final long aclId, final VpcGateway.Type type) {
        final SearchCriteria<VpcGatewayVO> sc = AllFieldsSearch.create();
        sc.setParameters("aclId", aclId);
        sc.setParameters("type", type);
        return listBy(sc);
    }
}
