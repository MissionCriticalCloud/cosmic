package com.cloud.network.vpn;

import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.RemoteAccessVpn;
import com.cloud.network.VpnUser;
import com.cloud.network.element.RemoteAccessVPNServiceProvider;
import com.cloud.utils.component.ManagerBase;

import javax.naming.ConfigurationException;
import java.util.List;
import java.util.Map;

public class MockRemoteAccessVPNServiceProvider extends ManagerBase implements RemoteAccessVPNServiceProvider {

    @Override
    public String getName() {
        return "MockRemoteAccessVPNServiceProvider";
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String[] applyVpnUsers(final RemoteAccessVpn vpn, final List<? extends VpnUser> users) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean startVpn(final RemoteAccessVpn vpn) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean stopVpn(final RemoteAccessVpn vpn) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }
}
