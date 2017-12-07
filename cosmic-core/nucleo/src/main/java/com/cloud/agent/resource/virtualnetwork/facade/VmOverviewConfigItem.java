package com.cloud.agent.resource.virtualnetwork.facade;

import com.cloud.agent.api.UpdateVmOverviewCommand;
import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.resource.virtualnetwork.ConfigItem;
import com.cloud.agent.resource.virtualnetwork.VRScripts;

import java.util.List;

public class VmOverviewConfigItem extends AbstractConfigItemFacade {

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        return generateConfigItems(((UpdateVmOverviewCommand) cmd).getVmOverview());
    }

    @Override
    protected List<ConfigItem> generateConfigItems(final Object configuration) {
        destinationFile = VRScripts.VM_OVERVIEW_CONFIG;

        return super.generateConfigItems(configuration);
    }
}
