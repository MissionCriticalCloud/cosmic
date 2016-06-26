//

//

package com.cloud.agent.resource.virtualnetwork.facade;

import com.cloud.agent.api.SetupGuestNetworkCommand;
import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.api.to.NicTO;
import com.cloud.agent.resource.virtualnetwork.ConfigItem;
import com.cloud.agent.resource.virtualnetwork.VRScripts;
import com.cloud.agent.resource.virtualnetwork.model.ConfigBase;
import com.cloud.agent.resource.virtualnetwork.model.GuestNetwork;
import com.cloud.utils.net.NetUtils;

import java.util.List;

public class SetGuestNetworkConfigItem extends AbstractConfigItemFacade {

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        final SetupGuestNetworkCommand command = (SetupGuestNetworkCommand) cmd;

        final NicTO nic = command.getNic();
        final String routerGIP = command.getAccessDetail(NetworkElementCommand.ROUTER_GUEST_IP);
        final String gateway = command.getAccessDetail(NetworkElementCommand.GUEST_NETWORK_GATEWAY);
        final String cidr = Long.toString(NetUtils.getCidrSize(nic.getNetmask()));
        final String netmask = nic.getNetmask();
        final String domainName = command.getNetworkDomain();
        String dns = command.getDefaultDns1();

        if (dns == null || dns.isEmpty()) {
            dns = command.getDefaultDns2();
        } else {
            final String dns2 = command.getDefaultDns2();
            if (dns2 != null && !dns2.isEmpty()) {
                dns += "," + dns2;
            }
        }

        final GuestNetwork guestNetwork = new GuestNetwork(command.isAdd(), nic.getMac(), "eth" + nic.getDeviceId(), routerGIP, netmask, gateway,
                cidr, dns, domainName);

        return generateConfigItems(guestNetwork);
    }

    @Override
    protected List<ConfigItem> generateConfigItems(final ConfigBase configuration) {
        destinationFile = VRScripts.GUEST_NETWORK_CONFIG;

        return super.generateConfigItems(configuration);
    }
}
