//

//

package com.cloud.agent.resource.virtualnetwork.facade;

import com.cloud.agent.api.routing.DhcpEntryCommand;
import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.resource.virtualnetwork.ConfigItem;
import com.cloud.agent.resource.virtualnetwork.VRScripts;
import com.cloud.agent.resource.virtualnetwork.model.ConfigBase;
import com.cloud.agent.resource.virtualnetwork.model.VmDhcpConfig;

import java.util.List;

public class DhcpEntryConfigItem extends AbstractConfigItemFacade {

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        final DhcpEntryCommand command = (DhcpEntryCommand) cmd;

        final VmDhcpConfig vmDhcpConfig = new VmDhcpConfig(command.getVmName(), command.getVmMac(), command.getVmIpAddress(), command.getVmIp6Address(), command.getDuid(),
                command.getDefaultDns(),
                command.getDefaultRouter(), command.getStaticRoutes(), command.isDefault());

        return generateConfigItems(vmDhcpConfig);
    }

    @Override
    protected List<ConfigItem> generateConfigItems(final ConfigBase configuration) {
        destinationFile = VRScripts.VM_DHCP_CONFIG;

        return super.generateConfigItems(configuration);
    }
}
