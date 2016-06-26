//

//

package com.cloud.agent.resource.virtualnetwork.facade;

import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.api.routing.SavePasswordCommand;
import com.cloud.agent.resource.virtualnetwork.ConfigItem;
import com.cloud.agent.resource.virtualnetwork.VRScripts;
import com.cloud.agent.resource.virtualnetwork.model.ConfigBase;
import com.cloud.agent.resource.virtualnetwork.model.VmPassword;

import java.util.List;

public class SavePasswordConfigItem extends AbstractConfigItemFacade {

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        final SavePasswordCommand command = (SavePasswordCommand) cmd;
        final VmPassword vmPassword = new VmPassword(command.getVmIpAddress(), command.getPassword());

        return generateConfigItems(vmPassword);
    }

    @Override
    protected List<ConfigItem> generateConfigItems(final ConfigBase configuration) {
        destinationFile = VRScripts.VM_PASSWORD_CONFIG;

        return super.generateConfigItems(configuration);
    }
}
