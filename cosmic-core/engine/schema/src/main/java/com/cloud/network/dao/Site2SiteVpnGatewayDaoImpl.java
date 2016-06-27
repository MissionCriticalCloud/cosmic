package com.cloud.network.dao;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Site2SiteVpnGatewayDaoImpl extends GenericDaoBase<Site2SiteVpnGatewayVO, Long> implements Site2SiteVpnGatewayDao {
    private static final Logger s_logger = LoggerFactory.getLogger(Site2SiteVpnGatewayDaoImpl.class);
    private final SearchBuilder<Site2SiteVpnGatewayVO> AllFieldsSearch;
    @Inject
    protected IPAddressDao _addrDao;

    protected Site2SiteVpnGatewayDaoImpl() {
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("vpcId", AllFieldsSearch.entity().getVpcId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.done();
    }

    @Override
    public Site2SiteVpnGatewayVO findByVpcId(final long vpcId) {
        final SearchCriteria<Site2SiteVpnGatewayVO> sc = AllFieldsSearch.create();
        sc.setParameters("vpcId", vpcId);
        return findOneBy(sc);
    }
}
