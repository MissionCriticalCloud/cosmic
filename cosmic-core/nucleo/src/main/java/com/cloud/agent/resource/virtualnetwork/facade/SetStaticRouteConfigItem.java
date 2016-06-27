//

//

package com.cloud.agent.resource.virtualnetwork.facade;

import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.api.routing.SetStaticRouteCommand;
import com.cloud.agent.resource.virtualnetwork.ConfigItem;
import com.cloud.agent.resource.virtualnetwork.VRScripts;
import com.cloud.agent.resource.virtualnetwork.model.ConfigBase;
import com.cloud.agent.resource.virtualnetwork.model.StaticRoute;
import com.cloud.agent.resource.virtualnetwork.model.StaticRoutes;
import com.cloud.network.vpc.StaticRouteProfile;

import java.util.LinkedList;
import java.util.List;

public class SetStaticRouteConfigItem extends AbstractConfigItemFacade {

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        final SetStaticRouteCommand command = (SetStaticRouteCommand) cmd;

        final LinkedList<StaticRoute> routes = new LinkedList<>();

        for (final StaticRouteProfile profile : command.getStaticRoutes()) {
            final boolean keep = profile.getState() == com.cloud.network.vpc.StaticRoute.State.Active || profile.getState() == com.cloud.network.vpc.StaticRoute.State.Add;

            routes.add(new StaticRoute(!keep, profile.getIp4Address(), profile.getCidr()));
        }

        return generateConfigItems(new StaticRoutes(routes));
    }

    @Override
    protected List<ConfigItem> generateConfigItems(final ConfigBase configuration) {
        destinationFile = VRScripts.STATIC_ROUTES_CONFIG;

        return super.generateConfigItems(configuration);
    }
}
