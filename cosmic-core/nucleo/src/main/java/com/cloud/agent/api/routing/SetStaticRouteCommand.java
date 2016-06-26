//

//

package com.cloud.agent.api.routing;

import com.cloud.network.vpc.StaticRoute;
import com.cloud.network.vpc.StaticRouteProfile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SetStaticRouteCommand extends NetworkElementCommand {
    StaticRouteProfile[] staticRoutes;

    protected SetStaticRouteCommand() {
    }

    public SetStaticRouteCommand(final List<StaticRouteProfile> staticRoutes) {
        this.staticRoutes = staticRoutes.toArray(new StaticRouteProfile[staticRoutes.size()]);
    }

    public StaticRouteProfile[] getStaticRoutes() {
        return staticRoutes;
    }

    public String[] generateSRouteRules() {
        final Set<String> toAdd = new HashSet<>();
        for (final StaticRouteProfile route : staticRoutes) {
            final String entry;
            if (route.getState() == StaticRoute.State.Active || route.getState() == StaticRoute.State.Add) {
                entry = route.getIp4Address() + ":" + route.getCidr();
            } else {
                entry = "Revoke:" + route.getCidr();
            }
            toAdd.add(entry);
        }
        return toAdd.toArray(new String[toAdd.size()]);
    }

    @Override
    public int getAnswersCount() {
        return staticRoutes.length;
    }
}
