package com.cloud.network.dao;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.JoinBuilder.JoinType;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Site2SiteVpnConnectionDaoImpl extends GenericDaoBase<Site2SiteVpnConnectionVO, Long> implements Site2SiteVpnConnectionDao {
    private static final Logger s_logger = LoggerFactory.getLogger(Site2SiteVpnConnectionDaoImpl.class);

    @Inject
    protected IPAddressDao _addrDao;
    @Inject
    protected Site2SiteVpnGatewayDao _vpnGatewayDao;

    private SearchBuilder<Site2SiteVpnConnectionVO> AllFieldsSearch;
    private SearchBuilder<Site2SiteVpnConnectionVO> VpcSearch;
    private SearchBuilder<Site2SiteVpnGatewayVO> VpnGatewaySearch;

    public Site2SiteVpnConnectionDaoImpl() {
    }

    @PostConstruct
    protected void init() {
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("customerGatewayId", AllFieldsSearch.entity().getCustomerGatewayId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("vpnGatewayId", AllFieldsSearch.entity().getVpnGatewayId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.done();

        VpcSearch = createSearchBuilder();
        VpnGatewaySearch = _vpnGatewayDao.createSearchBuilder();
        VpnGatewaySearch.and("vpcId", VpnGatewaySearch.entity().getVpcId(), SearchCriteria.Op.EQ);
        VpcSearch.join("vpnGatewaySearch", VpnGatewaySearch, VpnGatewaySearch.entity().getId(), VpcSearch.entity().getVpnGatewayId(), JoinType.INNER);
        VpcSearch.done();
    }

    @Override
    public List<Site2SiteVpnConnectionVO> listByCustomerGatewayId(final long id) {
        final SearchCriteria<Site2SiteVpnConnectionVO> sc = AllFieldsSearch.create();
        sc.setParameters("customerGatewayId", id);
        return listBy(sc);
    }

    @Override
    public List<Site2SiteVpnConnectionVO> listByVpnGatewayId(final long id) {
        final SearchCriteria<Site2SiteVpnConnectionVO> sc = AllFieldsSearch.create();
        sc.setParameters("vpnGatewayId", id);
        return listBy(sc);
    }

    @Override
    public List<Site2SiteVpnConnectionVO> listByVpcId(final long vpcId) {
        final SearchCriteria<Site2SiteVpnConnectionVO> sc = VpcSearch.create();
        sc.setJoinParameters("vpnGatewaySearch", "vpcId", vpcId);
        return listBy(sc);
    }

    @Override
    public Site2SiteVpnConnectionVO findByVpnGatewayIdAndCustomerGatewayId(final long vpnId, final long customerId) {
        final SearchCriteria<Site2SiteVpnConnectionVO> sc = AllFieldsSearch.create();
        sc.setParameters("vpnGatewayId", vpnId);
        sc.setParameters("customerGatewayId", customerId);
        return findOneBy(sc);
    }

    @Override
    public Site2SiteVpnConnectionVO findByCustomerGatewayId(final long customerId) {
        final SearchCriteria<Site2SiteVpnConnectionVO> sc = AllFieldsSearch.create();
        sc.setParameters("customerGatewayId", customerId);
        return findOneBy(sc);
    }
}
