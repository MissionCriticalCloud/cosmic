package org.apache.cloudstack.resourcedetail.dao;

import org.apache.cloudstack.resourcedetail.ResourceDetailsDaoBase;
import org.apache.cloudstack.resourcedetail.Site2SiteCustomerGatewayDetailVO;

import org.springframework.stereotype.Component;

@Component
public class Site2SiteCustomerGatewayDetailsDaoImpl extends ResourceDetailsDaoBase<Site2SiteCustomerGatewayDetailVO> implements Site2SiteCustomerGatewayDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new Site2SiteCustomerGatewayDetailVO(resourceId, key, value, display));
    }
}
