//

//

package com.cloud.agent.resource.virtualnetwork.facade;

import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.api.routing.RemoteAccessVpnCfgCommand;
import com.cloud.agent.resource.virtualnetwork.ConfigItem;
import com.cloud.agent.resource.virtualnetwork.VRScripts;
import com.cloud.agent.resource.virtualnetwork.model.ConfigBase;
import com.cloud.agent.resource.virtualnetwork.model.RemoteAccessVpn;

import java.util.List;

public class RemoteAccessVpnConfigItem extends AbstractConfigItemFacade {

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        final RemoteAccessVpnCfgCommand command = (RemoteAccessVpnCfgCommand) cmd;

        final RemoteAccessVpn remoteAccessVpn = new RemoteAccessVpn(command.isCreate(), command.getIpRange(), command.getPresharedKey(), command.getVpnServerIp(), command
                .getLocalIp(), command.getLocalCidr(),
                command.getPublicInterface());
        return generateConfigItems(remoteAccessVpn);
    }

    @Override
    protected List<ConfigItem> generateConfigItems(final ConfigBase configuration) {
        destinationFile = VRScripts.REMOTE_ACCESS_VPN_CONFIG;

        return super.generateConfigItems(configuration);
    }
}
