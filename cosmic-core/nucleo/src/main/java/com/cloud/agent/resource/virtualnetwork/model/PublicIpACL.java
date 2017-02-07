package com.cloud.agent.resource.virtualnetwork.model;

public class PublicIpACL extends ConfigBase {
    private String publicIp;
    private AclRule[] ingressRules;
    private AclRule[] egressRules;

    public PublicIpACL() {
        super(ConfigBase.NETWORK_ACL);
    }

    public PublicIpACL(final String publicIp, final AclRule[] ingressRules, final AclRule[] egressRules) {
        super(ConfigBase.PUBLIC_IP_ACL);

        this.publicIp = publicIp;
        this.ingressRules = ingressRules;
        this.egressRules = egressRules;
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
