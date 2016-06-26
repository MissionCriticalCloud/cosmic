//

//

package com.cloud.agent.api.routing;

import com.cloud.agent.api.Command;

import java.util.HashMap;

public abstract class NetworkElementCommand extends Command {
    public static final String ACCOUNT_ID = "account.id";
    public static final String GUEST_NETWORK_CIDR = "guest.network.cidr";
    public static final String GUEST_NETWORK_GATEWAY = "guest.network.gateway";
    public static final String GUEST_VLAN_TAG = "guest.vlan.tag";
    public static final String ROUTER_NAME = "router.name";
    public static final String ROUTER_IP = "router.ip";
    public static final String ROUTER_GUEST_IP = "router.guest.ip";
    public static final String ZONE_NETWORK_TYPE = "zone.network.type";
    public static final String GUEST_BRIDGE = "guest.bridge";
    public static final String VPC_PRIVATE_GATEWAY = "vpc.gateway.private";
    public static final String FIREWALL_EGRESS_DEFAULT = "firewall.egress.default";
    public static final String ROUTER_MONITORING_ENABLE = "router.monitor.enable";
    HashMap<String, String> accessDetails = new HashMap<>(0);
    private String routerAccessIp;

    protected NetworkElementCommand() {
        super();
    }

    public void setAccessDetail(final String name, final String value) {
        accessDetails.put(name, value);
    }

    public String getAccessDetail(final String name) {
        return accessDetails.get(name);
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    public String getRouterAccessIp() {
        return routerAccessIp;
    }

    public void setRouterAccessIp(final String routerAccessIp) {
        this.routerAccessIp = routerAccessIp;
    }

    public int getAnswersCount() {
        return 1;
    }

    public boolean isQuery() {
        return false;
    }
}
