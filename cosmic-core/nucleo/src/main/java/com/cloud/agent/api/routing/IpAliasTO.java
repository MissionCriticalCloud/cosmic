//

//

package com.cloud.agent.api.routing;

public class IpAliasTO {
    String routerip;
    String netmask;
    String aliasCount;

    public IpAliasTO(final String routerip, final String netmask, final String aliasCount) {
        this.routerip = routerip;
        this.netmask = netmask;
        this.aliasCount = aliasCount;
    }

    public String getRouterip() {
        return routerip;
    }

    public String getNetmask() {
        return netmask;
    }

    public String getAlias_count() {
        return aliasCount;
    }
}
