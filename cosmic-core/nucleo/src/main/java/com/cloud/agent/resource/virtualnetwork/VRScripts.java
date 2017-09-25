package com.cloud.agent.resource.virtualnetwork;

public class VRScripts {
    public final static String CONFIG_PERSIST_LOCATION = "/var/cache/cloud/";
    public final static String IP_ASSOCIATION_CONFIG = "ip_associations.json";
    public final static String GUEST_NETWORK_CONFIG = "guest_network.json";
    public final static String PRIVATE_GATEWAY_CONFIG = "private_gateway.json";
    public final static String NETWORK_ACL_CONFIG = "network_acl.json";
    public final static String PUBLIC_IP_ACL_CONFIG = "public_ip_acl.json";
    public final static String VM_METADATA_CONFIG = "vm_metadata.json";
    public final static String VM_DHCP_CONFIG = "vm_dhcp_entry.json";
    public final static String VM_PASSWORD_CONFIG = "vm_password.json";
    public static final String FORWARDING_RULES_CONFIG = "forwarding_rules.json";
    public static final String FIREWALL_RULES_CONFIG = "firewall_rules.json";
    public static final String VPN_USER_LIST_CONFIG = "vpn_user_list.json";
    public static final String STATICNAT_RULES_CONFIG = "staticnat_rules.json";
    public static final String SITE_2_SITE_VPN_CONFIG = "site_2_site_vpn.json";
    public static final String STATIC_ROUTES_CONFIG = "static_routes.json";
    public static final String REMOTE_ACCESS_VPN_CONFIG = "remote_access_vpn.json";
    public static final String MONITOR_SERVICE_CONFIG = "monitor_service.json";
    public static final String DHCP_CONFIG = "dhcp.json";
    public static final String IP_ALIAS_CONFIG = "ip_aliases.json";
    public static final String LOAD_BALANCER_CONFIG = "load_balancer.json";
    public static final String VR_CONFIG = "vr.json";

    public final static String CONFIG_CACHE_LOCATION = "/var/cache/cloud/";
    public final static int DEFAULT_EXECUTEINVR_TIMEOUT = 120; //Seconds

    // New scripts for use with chef
    public static final String UPDATE_CONFIG = "bin/update_config.py";

    // TODO Remove scripts
    public static final String S2SVPN_CHECK = "scripts/checkbatchs2svpn.sh";
    public static final String S2SVPN_IPSEC = "scripts/ipsectunnel.sh";
    public static final String DHCP = "scripts/edithosts.sh";
    public static final String DNSMASQ_CONFIG = "scripts/dnsmasq.sh";
    public static final String IPASSOC = "scripts/ipassoc.sh";
    public static final String LB = "scripts/loadbalancer.sh";
    public static final String MONITOR_SERVICE = "scripts/monitor_service.sh";
    public static final String PASSWORD = "scripts/savepassword.sh";
    public static final String ROUTER_ALERTS = "scripts/getRouterAlerts.sh";
    public static final String RVR_CHECK = "scripts/checkrouter.sh";
    public static final String VMDATA = "bin/vmdata.py";
    public static final String RVR_BUMPUP_PRI = "scripts/bumpup_priority.sh";
    public static final String VERSION = "scripts/get_template_version.sh";
    public static final String VPC_SOURCE_NAT = "scripts/vpc_snat.sh";
    public static final String VPC_STATIC_ROUTE = "scripts/vpc_staticroute.sh";
    public static final String VPN_L2TP = "scripts/vpn_l2tp.sh";
    public static final String UPDATE_HOST_PASSWD = "update_host_passwd.sh";

    public static final String VR_CFG = "scripts/vr_cfg.sh";
}
