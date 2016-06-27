package com.cloud.network.dao;

import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface Site2SiteVpnConnectionDao extends GenericDao<Site2SiteVpnConnectionVO, Long> {
    List<Site2SiteVpnConnectionVO> listByCustomerGatewayId(long id);

    List<Site2SiteVpnConnectionVO> listByVpnGatewayId(long id);

    List<Site2SiteVpnConnectionVO> listByVpcId(long vpcId);

    Site2SiteVpnConnectionVO findByVpnGatewayIdAndCustomerGatewayId(long vpnId, long customerId);

    Site2SiteVpnConnectionVO findByCustomerGatewayId(long customerId);
}
