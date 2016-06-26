package com.cloud.network.element;

import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Site2SiteVpnConnection;
import com.cloud.utils.component.Adapter;

public interface Site2SiteVpnServiceProvider extends Adapter {
    boolean startSite2SiteVpn(Site2SiteVpnConnection conn) throws ResourceUnavailableException;

    boolean stopSite2SiteVpn(Site2SiteVpnConnection conn) throws ResourceUnavailableException;
}
