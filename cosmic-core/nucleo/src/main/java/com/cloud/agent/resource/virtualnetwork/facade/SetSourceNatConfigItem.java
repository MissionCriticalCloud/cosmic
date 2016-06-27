//

//

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

        /* FIXME This seems useless as we already pass this info with the ipassoc
         * SetSourceNatCommand command = (SetSourceNatCommand) cmd;
         * IpAddressTO pubIP = command.getIpAddress();
         * String dev = "eth" + pubIP.getNicDevId();
         * String args = "-A";
         * args += " -l ";
         * args += pubIP.getPublicIp();
         * args += " -c ";
         * args += dev;
         * cfg.add(new ScriptConfigItem(VRScripts.VPC_SOURCE_NAT, args));
         */

        return cfg;
    }
}
