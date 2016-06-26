//

//

package com.cloud.agent.api.routing;

public class RemoteAccessVpnCfgCommand extends NetworkElementCommand {

    boolean create;
    String vpnServerIp;
    String ipRange;
    String presharedKey;
    String localIp;
    private boolean vpcEnabled;
    private String localCidr;
    private String publicInterface;

    protected RemoteAccessVpnCfgCommand() {
        this.create = false;
    }

    public RemoteAccessVpnCfgCommand(final boolean create, final String vpnServerAddress, final String localIp, final String ipRange, final String ipsecPresharedKey, final
    boolean vpcEnabled) {
        this.vpnServerIp = vpnServerAddress;
        this.ipRange = ipRange;
        this.presharedKey = ipsecPresharedKey;
        this.localIp = localIp;
        this.create = create;
        this.vpcEnabled = vpcEnabled;
        if (vpcEnabled) {
            this.setPublicInterface("eth1");
        } else {
            this.setPublicInterface("eth2");
        }
    }

    public boolean isCreate() {
        return create;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }

    public String getVpnServerIp() {
        return vpnServerIp;
    }

    public void setVpnServerIp(final String vpnServerIp) {
        this.vpnServerIp = vpnServerIp;
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

    public String getLocalIp() {
        return localIp;
    }

    public boolean isVpcEnabled() {
        return vpcEnabled;
    }

    public void setVpcEnabled(final boolean vpcEnabled) {
        this.vpcEnabled = vpcEnabled;
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
