package com.cloud.vpc;

import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Site2SiteVpnConnection;
import com.cloud.network.element.Site2SiteVpnServiceProvider;
import com.cloud.utils.component.ManagerBase;

import javax.naming.ConfigurationException;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class MockSite2SiteVpnServiceProvider extends ManagerBase implements Site2SiteVpnServiceProvider {

    /* (non-Javadoc)
     * @see com.cloud.utils.component.Adapter#getName()
     */
    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "Site2SiteVpnServiceProvider";
    }

    /* (non-Javadoc)
     * @see com.cloud.utils.component.Adapter#configure(java.lang.String, java.util.Map)
     */
    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        // TODO Auto-generated method stub
        return true;
    }

    /* (non-Javadoc)
     * @see com.cloud.utils.component.Adapter#start()
     */
    @Override
    public boolean start() {
        // TODO Auto-generated method stub
        return true;
    }

    /* (non-Javadoc)
     * @see com.cloud.utils.component.Adapter#stop()
     */
    @Override
    public boolean stop() {
        // TODO Auto-generated method stub
        return true;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.element.Site2SiteVpnServiceProvider#startSite2SiteVpn(com.cloud.network.Site2SiteVpnConnection)
     */
    @Override
    public boolean startSite2SiteVpn(final Site2SiteVpnConnection conn) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return true;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.element.Site2SiteVpnServiceProvider#stopSite2SiteVpn(com.cloud.network.Site2SiteVpnConnection)
     */
    @Override
    public boolean stopSite2SiteVpn(final Site2SiteVpnConnection conn) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return true;
    }
}
