//

//

package com.cloud.agent.resource.virtualnetwork.facade;

import com.cloud.agent.api.routing.IpAssocCommand;
import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.api.to.IpAddressTO;
import com.cloud.agent.resource.virtualnetwork.ConfigItem;
import com.cloud.agent.resource.virtualnetwork.VRScripts;
import com.cloud.agent.resource.virtualnetwork.model.ConfigBase;
import com.cloud.agent.resource.virtualnetwork.model.IpAddress;
import com.cloud.agent.resource.virtualnetwork.model.IpAssociation;

import java.util.LinkedList;
import java.util.List;

public class IpAssociationConfigItem extends AbstractConfigItemFacade {

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        final IpAssocCommand command = (IpAssocCommand) cmd;

        final List<IpAddress> ips = new LinkedList<>();

        for (final IpAddressTO ip : command.getIpAddresses()) {
            final IpAddress ipAddress = new IpAddress(ip.getPublicIp(), ip.isSourceNat(), ip.isAdd(), ip.isOneToOneNat(), ip.isFirstIP(), ip.getVlanGateway(), ip.getVlanNetmask(),
                    ip.getVifMacAddress(), ip.getNicDevId(), ip.isNewNic());
            ips.add(ipAddress);
        }

        final IpAssociation ipAssociation = new IpAssociation(ips.toArray(new IpAddress[ips.size()]));

        return generateConfigItems(ipAssociation);
    }

    @Override
    protected List<ConfigItem> generateConfigItems(final ConfigBase configuration) {
        destinationFile = VRScripts.IP_ASSOCIATION_CONFIG;

        return super.generateConfigItems(configuration);
    }
}
