//

//

package com.cloud.agent.resource.virtualnetwork.model;

public class RemoteAccessVpn extends ConfigBase {

    public boolean create;
    public String ipRange, presharedKey, vpnServerIp, localIp, localCidr, publicInterface;

    public RemoteAccessVpn() {
        super(ConfigBase.REMOTEACCESSVPN);
    }

    public RemoteAccessVpn(final boolean create, final String ipRange, final String presharedKey, final String vpnServerIp, final String localIp, final String localCidr, final
    String publicInterface) {
        super(ConfigBase.REMOTEACCESSVPN);
        this.create = create;
        this.ipRange = ipRange;
        this.presharedKey = presharedKey;
        this.vpnServerIp = vpnServerIp;
        this.localIp = localIp;
        this.localCidr = localCidr;
        this.publicInterface = publicInterface;
    }

    public boolean isCreate() {
        return create;
    }

    public void setCreate(final boolean create) {
        this.create = create;
    }

    public String getIpRange() {
        return ipRange;
    }

    public void setIpRange(final String ipRange) {
        this.ipRange = ipRange;
    }

    public String getPresharedKey() {
        return presharedKey;
    }

    public void setPresharedKey(final String presharedKey) {
        this.presharedKey = presharedKey;
    }

    public String getVpnServerIp() {
        return vpnServerIp;
    }

    public void setVpnServerIp(final String vpnServerIp) {
        this.vpnServerIp = vpnServerIp;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(final String localIp) {
        this.localIp = localIp;
    }

    public String getLocalCidr() {
        return localCidr;
    }

    public void setLocalCidr(final String localCidr) {
        this.localCidr = localCidr;
    }

    public String getPublicInterface() {
        return publicInterface;
    }

    public void setPublicInterface(final String publicInterface) {
        this.publicInterface = publicInterface;
    }
}
