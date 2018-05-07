package com.cloud.common.virtualnetwork.facade;

import com.cloud.common.virtualnetwork.ConfigItem;
import com.cloud.legacymodel.communication.command.NetworkElementCommand;

import java.util.List;

public class SetPortForwardingRulesVpcConfigItem extends SetPortForwardingRulesConfigItem {

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        return super.generateConfig(cmd);
    }
}
