//

//

package com.cloud.network.nicira;

/**
 *
 */
public class SingleDefaultRouteImplicitRoutingConfig extends RoutingConfig {
    public final String type = "SingleDefaultRouteImplicitRoutingConfig";
    public RouterNextHop defaultRouteNextHop;

    public SingleDefaultRouteImplicitRoutingConfig(final RouterNextHop routerNextHop) {
        defaultRouteNextHop = routerNextHop;
    }

    public RouterNextHop getDefaultRouteNextHop() {
        return defaultRouteNextHop;
    }

    public void setDefaultRouteNextHop(final RouterNextHop defaultRouteNextHop) {
        this.defaultRouteNextHop = defaultRouteNextHop;
    }
}
