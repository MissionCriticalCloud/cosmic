package com.cloud.common.virtualnetwork.facade;

import com.cloud.common.virtualnetwork.ConfigItem;
import com.cloud.legacymodel.communication.command.NetworkElementCommand;
import com.cloud.legacymodel.communication.command.UpdateVmOverviewCommand;
import com.cloud.legacymodel.network.VRScripts;

import java.util.List;

public class VmOverviewConfigItem extends AbstractConfigItemFacade {

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        return generateConfigItems(((UpdateVmOverviewCommand) cmd).getVmOverview());
    }

    @Override
    protected List<ConfigItem> generateConfigItems(final Object configuration) {
        this.destinationFile = VRScripts.VM_OVERVIEW_CONFIG;

        return super.generateConfigItems(configuration);
    }
}
