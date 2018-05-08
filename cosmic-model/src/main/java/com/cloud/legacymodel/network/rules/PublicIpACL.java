package com.cloud.legacymodel.network.rules;

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
        return this.macAddress;
    }

    public void setMacAddress(final String macAddress) {
        this.macAddress = macAddress;
    }

    public String getNicIp() {
        return this.nicIp;
    }

    public void setNicIp(final String nicIp) {
        this.nicIp = nicIp;
    }

    public String getNicNetmask() {
        return this.nicNetmask;
    }

    public void setNicNetmask(final String nicNetmask) {
        this.nicNetmask = nicNetmask;
    }

    public String getPublicIp() {
        return this.publicIp;
    }

    public void setPublicIp(final String publicIp) {
        this.publicIp = publicIp;
    }

    public AclRule[] getIngressRules() {
        return this.ingressRules;
    }

    public void setIngressRules(final AclRule[] ingressRules) {
        this.ingressRules = ingressRules;
    }

    public AclRule[] getEgressRules() {
        return this.egressRules;
    }

    public void setEgressRules(final AclRule[] egressRules) {
        this.egressRules = egressRules;
    }
}
