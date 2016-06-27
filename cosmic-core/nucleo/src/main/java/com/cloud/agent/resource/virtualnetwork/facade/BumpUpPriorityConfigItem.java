//

//

package com.cloud.agent.resource.virtualnetwork.facade;

import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.resource.virtualnetwork.ConfigItem;
import com.cloud.agent.resource.virtualnetwork.ScriptConfigItem;
import com.cloud.agent.resource.virtualnetwork.VRScripts;

import java.util.LinkedList;
import java.util.List;

public class BumpUpPriorityConfigItem extends AbstractConfigItemFacade {

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        final LinkedList<ConfigItem> cfg = new LinkedList<>();
        cfg.add(new ScriptConfigItem(VRScripts.RVR_BUMPUP_PRI, null));

        return cfg;
    }
}
