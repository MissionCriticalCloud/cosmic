//

//

package com.cloud.agent.resource.virtualnetwork.facade;

import com.cloud.agent.api.routing.DeleteIpAliasCommand;
import com.cloud.agent.api.routing.IpAliasTO;
import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.resource.virtualnetwork.ConfigItem;
import com.cloud.agent.resource.virtualnetwork.VRScripts;
import com.cloud.agent.resource.virtualnetwork.model.ConfigBase;
import com.cloud.agent.resource.virtualnetwork.model.IpAddressAlias;
import com.cloud.agent.resource.virtualnetwork.model.IpAliases;

import java.util.LinkedList;
import java.util.List;

public class DeleteIpAliasConfigItem extends AbstractConfigItemFacade {

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        final DeleteIpAliasCommand command = (DeleteIpAliasCommand) cmd;

        final List<IpAddressAlias> ipAliases = new LinkedList<>();

        final List<IpAliasTO> revokedIpAliasTOs = command.getDeleteIpAliasTos();
        for (final IpAliasTO ipAliasTO : revokedIpAliasTOs) {
            final IpAddressAlias alias = new IpAddressAlias(true, ipAliasTO.getRouterip(), ipAliasTO.getNetmask(), Long.parseLong(ipAliasTO.getAlias_count()));
            ipAliases.add(alias);
        }

        final List<IpAliasTO> activeIpAliasTOs = command.getCreateIpAliasTos();
        for (final IpAliasTO ipAliasTO : activeIpAliasTOs) {
            final IpAddressAlias alias = new IpAddressAlias(false, ipAliasTO.getRouterip(), ipAliasTO.getNetmask(), Long.parseLong(ipAliasTO.getAlias_count()));
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
