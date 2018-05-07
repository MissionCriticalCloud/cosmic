package com.cloud.common.virtualnetwork.facade;

import com.cloud.common.virtualnetwork.ConfigItem;
import com.cloud.legacymodel.communication.command.NetworkElementCommand;
import com.cloud.legacymodel.communication.command.SavePasswordCommand;
import com.cloud.legacymodel.network.VRScripts;
import com.cloud.legacymodel.network.rules.VmPassword;

import java.util.List;

public class SavePasswordConfigItem extends AbstractConfigItemFacade {

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        final SavePasswordCommand command = (SavePasswordCommand) cmd;
        final VmPassword vmPassword = new VmPassword(command.getVmIpAddress(), command.getPassword());

        return generateConfigItems(vmPassword);
    }

    @Override
    protected List<ConfigItem> generateConfigItems(final Object configuration) {
        this.destinationFile = VRScripts.VM_PASSWORD_CONFIG;

        return super.generateConfigItems(configuration);
    }
}
