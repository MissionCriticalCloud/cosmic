//

//

package com.cloud.agent.api.routing;

import java.util.List;

public class DeleteIpAliasCommand extends NetworkElementCommand {
    String routerip;
    List<IpAliasTO> deleteIpAliasTOs;
    List<IpAliasTO> createIpAliasTos;

    public DeleteIpAliasCommand(final String routerip, final List<IpAliasTO> deleteIpAliasTOs, final List<IpAliasTO> createIpAliasTos) {
        this.routerip = routerip;
        this.deleteIpAliasTOs = deleteIpAliasTOs;
        this.createIpAliasTos = createIpAliasTos;
    }

    public String getRouterip() {
        return routerip;
    }

    public List<IpAliasTO> getDeleteIpAliasTos() {
        return deleteIpAliasTOs;
    }

    public List<IpAliasTO> getCreateIpAliasTos() {
        return createIpAliasTos;
    }
}
