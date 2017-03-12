package com.cloud.agent.resource.virtualnetwork.facade;

import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.resource.virtualnetwork.ConfigItem;
import com.cloud.agent.resource.virtualnetwork.model.ConfigBase;

import java.util.LinkedList;
import java.util.List;

public class SetSourceNatConfigItem extends AbstractConfigItemFacade {

    @Override
    protected List<ConfigItem> generateConfigItems(final ConfigBase configuration) {
        return null;
    }

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        final LinkedList<ConfigItem> cfg = new LinkedList<>();

        return cfg;
    }
}
