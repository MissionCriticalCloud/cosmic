//

//

package com.cloud.agent.api.routing;

import java.util.List;

public class CreateIpAliasCommand extends NetworkElementCommand {
    String routerip;
    List<IpAliasTO> ipAliasTOs;

    public CreateIpAliasCommand(final String routerip, final List<IpAliasTO> ipAliasTOs) {
        this.routerip = routerip;
        this.ipAliasTOs = ipAliasTOs;
    }

    public String getRouterip() {
        return routerip;
    }

    public List<IpAliasTO> getIpAliasList() {
        return ipAliasTOs;
    }
}
