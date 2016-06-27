package com.cloud.network.dao;

import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface Site2SiteCustomerGatewayDao extends GenericDao<Site2SiteCustomerGatewayVO, Long> {
    Site2SiteCustomerGatewayVO findByGatewayIpAndAccountId(String ip, long accountId);

    Site2SiteCustomerGatewayVO findByNameAndAccountId(String name, long accountId);

    List<Site2SiteCustomerGatewayVO> listByAccountId(long accountId);
}
