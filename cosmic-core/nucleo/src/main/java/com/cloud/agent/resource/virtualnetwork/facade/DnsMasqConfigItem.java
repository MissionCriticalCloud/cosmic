//

//

package com.cloud.agent.resource.virtualnetwork.facade;

import com.cloud.agent.api.routing.DnsMasqConfigCommand;
import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.api.to.DhcpTO;
import com.cloud.agent.resource.virtualnetwork.ConfigItem;
import com.cloud.agent.resource.virtualnetwork.VRScripts;
import com.cloud.agent.resource.virtualnetwork.model.ConfigBase;
import com.cloud.agent.resource.virtualnetwork.model.DhcpConfig;
import com.cloud.agent.resource.virtualnetwork.model.DhcpConfigEntry;

import java.util.LinkedList;
import java.util.List;

public class DnsMasqConfigItem extends AbstractConfigItemFacade {

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        final DnsMasqConfigCommand command = (DnsMasqConfigCommand) cmd;

        final LinkedList<DhcpConfigEntry> entries = new LinkedList<>();

        for (final DhcpTO dhcpTo : command.getIps()) {
            final DhcpConfigEntry entry = new DhcpConfigEntry(dhcpTo.getRouterIp(), dhcpTo.getGateway(), dhcpTo.getNetmask(), dhcpTo.getStartIpOfSubnet());
            entries.add(entry);
        }

        return generateConfigItems(new DhcpConfig(entries));
    }

    @Override
    protected List<ConfigItem> generateConfigItems(final ConfigBase configuration) {
        destinationFile = VRScripts.DHCP_CONFIG;

        return super.generateConfigItems(configuration);
    }
}
