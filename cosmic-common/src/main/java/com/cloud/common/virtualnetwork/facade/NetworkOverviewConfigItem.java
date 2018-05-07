package com.cloud.common.virtualnetwork.facade;

import com.cloud.common.virtualnetwork.ConfigItem;
import com.cloud.legacymodel.communication.command.NetworkElementCommand;
import com.cloud.legacymodel.communication.command.UpdateNetworkOverviewCommand;
import com.cloud.legacymodel.network.VRScripts;

import java.util.List;

public class NetworkOverviewConfigItem extends AbstractConfigItemFacade {

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        return generateConfigItems(((UpdateNetworkOverviewCommand) cmd).getNetworkOverview());
    }

    @Override
    protected List<ConfigItem> generateConfigItems(final Object configuration) {
        this.destinationFile = VRScripts.NETWORK_OVERVIEW_CONFIG;

        return super.generateConfigItems(configuration);
    }
}
