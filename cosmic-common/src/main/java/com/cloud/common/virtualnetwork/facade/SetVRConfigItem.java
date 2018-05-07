package com.cloud.common.virtualnetwork.facade;

import com.cloud.common.virtualnetwork.ConfigItem;
import com.cloud.legacymodel.communication.command.NetworkElementCommand;
import com.cloud.legacymodel.communication.command.SetupVRCommand;
import com.cloud.legacymodel.network.VRScripts;
import com.cloud.legacymodel.network.rules.VRConfig;

import java.util.List;

public class SetVRConfigItem extends AbstractConfigItemFacade {

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        final SetupVRCommand command = (SetupVRCommand) cmd;

        return generateConfigItems(new VRConfig(command.getVpcName(), command.getSourceNatList()));
    }

    @Override
    protected List<ConfigItem> generateConfigItems(final Object configuration) {
        this.destinationFile = VRScripts.VR_CONFIG;

        return super.generateConfigItems(configuration);
    }
}
