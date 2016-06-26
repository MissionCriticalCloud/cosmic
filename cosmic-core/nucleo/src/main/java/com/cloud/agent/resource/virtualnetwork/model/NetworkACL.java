//

//

package com.cloud.agent.resource.virtualnetwork.model;

public class NetworkACL extends ConfigBase {
    private String device;
    private String macAddress;
    private boolean privateGatewayAcl;
    private String nicIp;
    private String nicNetmask;
    private AclRule[] ingressRules;
    private AclRule[] egressRules;

    public NetworkACL() {
        super(ConfigBase.NETWORK_ACL);
    }

    public NetworkACL(final String device, final String macAddress, final boolean privateGatewayAcl, final String nicIp, final String nicNetmask, final AclRule[] ingressRules,
                      final AclRule[] egressRules) {
        super(ConfigBase.NETWORK_ACL);
        this.device = device;
        this.macAddress = macAddress;
        this.privateGatewayAcl = privateGatewayAcl;
        this.nicIp = nicIp;
        this.nicNetmask = nicNetmask;
        this.ingressRules = ingressRules;
        this.egressRules = egressRules;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(final String device) {
        this.device = device;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(final String macAddress) {
        this.macAddress = macAddress;
    }

    public boolean isPrivateGatewayAcl() {
        return privateGatewayAcl;
    }

    public void setPrivateGatewayAcl(final boolean privateGatewayAcl) {
        this.privateGatewayAcl = privateGatewayAcl;
    }

    public String getNicIp() {
        return nicIp;
    }

    public void setNicIp(final String nicIp) {
        this.nicIp = nicIp;
    }

    public String getNicNetmask() {
        return nicNetmask;
    }

    public void setNicNetmask(final String nicNetmask) {
        this.nicNetmask = nicNetmask;
    }

    public AclRule[] getIngressRules() {
        return ingressRules;
    }

    public void setIngressRules(final AclRule[] ingressRules) {
        this.ingressRules = ingressRules;
    }

    public AclRule[] getEgressRules() {
        return egressRules;
    }

    public void setEgressRules(final AclRule[] egressRules) {
        this.egressRules = egressRules;
    }
}
