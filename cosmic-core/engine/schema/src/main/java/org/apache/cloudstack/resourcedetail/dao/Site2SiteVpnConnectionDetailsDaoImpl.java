package org.apache.cloudstack.resourcedetail.dao;

import org.apache.cloudstack.resourcedetail.ResourceDetailsDaoBase;
import org.apache.cloudstack.resourcedetail.Site2SiteVpnConnectionDetailVO;

import org.springframework.stereotype.Component;

@Component
public class Site2SiteVpnConnectionDetailsDaoImpl extends ResourceDetailsDaoBase<Site2SiteVpnConnectionDetailVO> implements Site2SiteVpnConnectionDetailsDao {

    @Override
    public void addDetail(final long resourceId, final String key, final String value, final boolean display) {
        super.addDetail(new Site2SiteVpnConnectionDetailVO(resourceId, key, value, display));
    }
}
