package com.cloud.network.element;

import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.RemoteAccessVpn;
import com.cloud.network.VpnUser;
import com.cloud.utils.component.Adapter;

import java.util.List;

public interface RemoteAccessVPNServiceProvider extends Adapter {
    String[] applyVpnUsers(RemoteAccessVpn vpn, List<? extends VpnUser> users) throws ResourceUnavailableException;

    boolean startVpn(RemoteAccessVpn vpn) throws ResourceUnavailableException;

    boolean stopVpn(RemoteAccessVpn vpn) throws ResourceUnavailableException;
}
