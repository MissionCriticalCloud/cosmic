package com.cloud.network.dao;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Site2SiteCustomerGatewayDaoImpl extends GenericDaoBase<Site2SiteCustomerGatewayVO, Long> implements Site2SiteCustomerGatewayDao {
    private static final Logger s_logger = LoggerFactory.getLogger(Site2SiteCustomerGatewayDaoImpl.class);

    private final SearchBuilder<Site2SiteCustomerGatewayVO> AllFieldsSearch;

    protected Site2SiteCustomerGatewayDaoImpl() {
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("gatewayIp", AllFieldsSearch.entity().getGatewayIp(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("name", AllFieldsSearch.entity().getName(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("accountId", AllFieldsSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.done();
    }

    @Override
    public Site2SiteCustomerGatewayVO findByGatewayIpAndAccountId(final String ip, final long accountId) {
        final SearchCriteria<Site2SiteCustomerGatewayVO> sc = AllFieldsSearch.create();
        sc.setParameters("gatewayIp", ip);
        sc.setParameters("accountId", accountId);
        return findOneBy(sc);
    }

    @Override
    public Site2SiteCustomerGatewayVO findByNameAndAccountId(final String name, final long accountId) {
        final SearchCriteria<Site2SiteCustomerGatewayVO> sc = AllFieldsSearch.create();
        sc.setParameters("name", name);
        sc.setParameters("accountId", accountId);
        return findOneBy(sc);
    }

    @Override
    public List<Site2SiteCustomerGatewayVO> listByAccountId(final long accountId) {
        final SearchCriteria<Site2SiteCustomerGatewayVO> sc = AllFieldsSearch.create();
        sc.setParameters("accountId", accountId);
        return listBy(sc, null);
    }
}
