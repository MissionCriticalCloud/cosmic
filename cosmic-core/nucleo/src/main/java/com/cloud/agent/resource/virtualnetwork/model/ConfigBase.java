package com.cloud.agent.resource.virtualnetwork.model;

public abstract class ConfigBase {
    public final static String UNKNOWN = "unknown";
    public static final String NETWORK_ACL = "networkacl";
    public static final String PUBLIC_IP_ACL = "publicipacl";
    public static final String VM_PASSWORD = "vmpassword";
    public static final String FORWARDING_RULES = "forwardrules";
    public static final String FIREWALL_RULES = "firewallrules";
    public static final String STATICNAT_RULES = "staticnatrules";
    public static final String SITE2SITEVPN = "site2sitevpn";
    public static final String LOAD_BALANCER = "loadbalancer";
    public static final String VR = "virtualrouter";

    private String type = UNKNOWN;

    private ConfigBase() {
        // Empty constructor for (de)serialization
    }

    protected ConfigBase(final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }
}
