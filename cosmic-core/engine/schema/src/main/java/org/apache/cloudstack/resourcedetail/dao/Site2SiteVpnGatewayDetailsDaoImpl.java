package org.apache.cloudstack.resourcedetail.dao;

import org.apache.cloudstack.resourcedetail.ResourceDetailsDaoBase;
import org.apache.cloudstack.resourcedetail.Site2SiteVpnGatewayDetailVO;

import org.springframework.stereotype.Component;

@Component
public class Site2SiteVpnGatewayDetailsDaoImpl extends ResourceDetailsDaoBase<Site2SiteVpnGatewayDetailVO> implements Site2SiteVpnGatewayDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new Site2SiteVpnGatewayDetailVO(resourceId, key, value, display));
    }
}
