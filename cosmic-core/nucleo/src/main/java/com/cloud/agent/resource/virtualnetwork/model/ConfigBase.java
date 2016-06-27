//

//

package com.cloud.agent.resource.virtualnetwork.model;

public abstract class ConfigBase {
    public final static String UNKNOWN = "unknown";
    public final static String VM_DHCP = "dhcpentry";
    public final static String IP_ASSOCIATION = "ips";
    public final static String GUEST_NETWORK = "guestnetwork";
    public static final String NETWORK_ACL = "networkacl";
    public static final String VM_METADATA = "vmdata";
    public static final String VM_PASSWORD = "vmpassword";
    public static final String FORWARDING_RULES = "forwardrules";
    public static final String FIREWALL_RULES = "firewallrules";
    public static final String VPN_USER_LIST = "vpnuserlist";
    public static final String STATICNAT_RULES = "staticnatrules";
    public static final String IP_ALIAS_CONFIG = "ipaliases";
    public static final String SITE2SITEVPN = "site2sitevpn";
    public static final String STATIC_ROUTES = "staticroutes";
    public static final String REMOTEACCESSVPN = "remoteaccessvpn";
    public static final String MONITORSERVICE = "monitorservice";
    public static final String DHCP_CONFIG = "dhcpconfig";
    public static final String LOAD_BALANCER = "loadbalancer";

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
