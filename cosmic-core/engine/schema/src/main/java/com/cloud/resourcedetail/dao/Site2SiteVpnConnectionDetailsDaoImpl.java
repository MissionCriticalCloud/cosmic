package com.cloud.resourcedetail.dao;

import com.cloud.resourcedetail.ResourceDetailsDaoBase;
import com.cloud.resourcedetail.Site2SiteVpnConnectionDetailVO;

import org.springframework.stereotype.Component;

@Component
public class Site2SiteVpnConnectionDetailsDaoImpl extends ResourceDetailsDaoBase<Site2SiteVpnConnectionDetailVO> implements Site2SiteVpnConnectionDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new Site2SiteVpnConnectionDetailVO(resourceId, key, value, display));
    }
}
