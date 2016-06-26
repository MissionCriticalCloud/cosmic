//

//

package com.cloud.agent.resource.virtualnetwork.facade;

import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.resource.virtualnetwork.ConfigItem;

import java.util.List;

public class SetPortForwardingRulesVpcConfigItem extends SetPortForwardingRulesConfigItem {

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        return super.generateConfig(cmd);
    }
}
