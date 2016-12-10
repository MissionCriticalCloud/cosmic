package com.cloud.resourcedetail.dao;

import com.cloud.resourcedetail.ResourceDetailsDaoBase;
import com.cloud.resourcedetail.Site2SiteVpnGatewayDetailVO;

import org.springframework.stereotype.Component;

@Component
public class Site2SiteVpnGatewayDetailsDaoImpl extends ResourceDetailsDaoBase<Site2SiteVpnGatewayDetailVO> implements Site2SiteVpnGatewayDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new Site2SiteVpnGatewayDetailVO(resourceId, key, value, display));
    }
}
