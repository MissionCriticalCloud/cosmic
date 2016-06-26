//

//

package com.cloud.agent.resource.virtualnetwork.facade;

import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.api.routing.VpnUsersCfgCommand;
import com.cloud.agent.resource.virtualnetwork.ConfigItem;
import com.cloud.agent.resource.virtualnetwork.VRScripts;
import com.cloud.agent.resource.virtualnetwork.model.ConfigBase;
import com.cloud.agent.resource.virtualnetwork.model.VpnUser;
import com.cloud.agent.resource.virtualnetwork.model.VpnUserList;

import java.util.LinkedList;
import java.util.List;

public class VpnUsersConfigItem extends AbstractConfigItemFacade {

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        final VpnUsersCfgCommand command = (VpnUsersCfgCommand) cmd;

        final List<VpnUser> vpnUsers = new LinkedList<>();
        for (final VpnUsersCfgCommand.UsernamePassword userpwd : command.getUserpwds()) {
            vpnUsers.add(new VpnUser(userpwd.getUsername(), userpwd.getPassword(), userpwd.isAdd()));
        }

        final VpnUserList vpnUserList = new VpnUserList(vpnUsers);
        return generateConfigItems(vpnUserList);
    }

    @Override
    protected List<ConfigItem> generateConfigItems(final ConfigBase configuration) {
        destinationFile = VRScripts.VPN_USER_LIST_CONFIG;

        return super.generateConfigItems(configuration);
    }
}
