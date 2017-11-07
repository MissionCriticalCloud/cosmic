package com.cloud.agent.resource.virtualnetwork.model;

public class PublicIpACL extends ConfigBase {
    private String macAddress;
    private String nicIp;
    private String nicNetmask;
    private String publicIp;
    private AclRule[] ingressRules;
    private AclRule[] egressRules;

    public PublicIpACL() {
        super(ConfigBase.PUBLIC_IP_ACL);
    }

    public PublicIpACL(final String macAddress, final String nicIp, final String nicNetmask, final String publicIp, final AclRule[] ingressRules,
                       final AclRule[] egressRules) {
        super(ConfigBase.PUBLIC_IP_ACL);

        this.macAddress = macAddress;
        this.nicIp = nicIp;
        this.nicNetmask = nicNetmask;
        this.publicIp = publicIp;
        this.ingressRules = ingressRules;
        this.egressRules = egressRules;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(final String macAddress) {
        this.macAddress = macAddress;
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

    public String getPublicIp() {
        return publicIp;
    }

    public void setPublicIp(final String publicIp) {
        this.publicIp = publicIp;
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
