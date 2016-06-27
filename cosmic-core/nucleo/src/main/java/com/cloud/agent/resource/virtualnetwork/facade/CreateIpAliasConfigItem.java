//

//

package com.cloud.agent.resource.virtualnetwork.facade;

import com.cloud.agent.api.routing.CreateIpAliasCommand;
import com.cloud.agent.api.routing.IpAliasTO;
import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.resource.virtualnetwork.ConfigItem;
import com.cloud.agent.resource.virtualnetwork.VRScripts;
import com.cloud.agent.resource.virtualnetwork.model.ConfigBase;
import com.cloud.agent.resource.virtualnetwork.model.IpAddressAlias;
import com.cloud.agent.resource.virtualnetwork.model.IpAliases;

import java.util.LinkedList;
import java.util.List;

public class CreateIpAliasConfigItem extends AbstractConfigItemFacade {

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        final CreateIpAliasCommand command = (CreateIpAliasCommand) cmd;

        final List<IpAddressAlias> ipAliases = new LinkedList<>();
        final List<IpAliasTO> ipAliasTOs = command.getIpAliasList();
        for (final IpAliasTO ipaliasto : ipAliasTOs) {
            final IpAddressAlias alias = new IpAddressAlias(false, ipaliasto.getRouterip(), ipaliasto.getNetmask(), Long.parseLong(ipaliasto.getAlias_count()));
            ipAliases.add(alias);
        }

        final IpAliases ipAliasList = new IpAliases(ipAliases);
        return generateConfigItems(ipAliasList);
    }

    @Override
    protected List<ConfigItem> generateConfigItems(final ConfigBase configuration) {
        destinationFile = VRScripts.IP_ALIAS_CONFIG;

        return super.generateConfigItems(configuration);
    }
}
