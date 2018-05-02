package com.cloud.agent.resource.virtualnetwork.facade;

import com.cloud.agent.api.SetupVRCommand;
import com.cloud.agent.resource.virtualnetwork.ConfigItem;
import com.cloud.agent.resource.virtualnetwork.VRScripts;
import com.cloud.agent.resource.virtualnetwork.model.VRConfig;
import com.cloud.legacymodel.communication.command.NetworkElementCommand;

import java.util.List;

public class SetVRConfigItem extends AbstractConfigItemFacade {

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        final SetupVRCommand command = (SetupVRCommand) cmd;

        return generateConfigItems(new VRConfig(command.getVpcName(), command.getSourceNatList()));
    }

    @Override
    protected List<ConfigItem> generateConfigItems(final Object configuration) {
        destinationFile = VRScripts.VR_CONFIG;

        return super.generateConfigItems(configuration);
    }
}
