//

//

package com.cloud.agent.resource.virtualnetwork.facade;

import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.api.routing.VmDataCommand;
import com.cloud.agent.resource.virtualnetwork.ConfigItem;
import com.cloud.agent.resource.virtualnetwork.VRScripts;
import com.cloud.agent.resource.virtualnetwork.model.ConfigBase;
import com.cloud.agent.resource.virtualnetwork.model.VmData;

import java.util.List;

public class VmDataConfigItem extends AbstractConfigItemFacade {

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        final VmDataCommand command = (VmDataCommand) cmd;

        final VmData vmData = new VmData(command.getVmIpAddress(), command.getVmData());

        return generateConfigItems(vmData);
    }

    @Override
    protected List<ConfigItem> generateConfigItems(final ConfigBase configuration) {
        destinationFile = VRScripts.VM_METADATA_CONFIG;

        return super.generateConfigItems(configuration);
    }
}
