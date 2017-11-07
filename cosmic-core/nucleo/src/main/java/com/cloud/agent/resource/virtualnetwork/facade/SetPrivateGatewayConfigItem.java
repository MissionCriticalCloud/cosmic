package com.cloud.agent.resource.virtualnetwork.facade;

import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.api.routing.SetupPrivateGatewayCommand;
import com.cloud.agent.api.to.IpAddressTO;
import com.cloud.agent.resource.virtualnetwork.ConfigItem;
import com.cloud.agent.resource.virtualnetwork.VRScripts;
import com.cloud.agent.resource.virtualnetwork.model.ConfigBase;
import com.cloud.agent.resource.virtualnetwork.model.PrivateGateway;

import java.util.List;

public class SetPrivateGatewayConfigItem extends AbstractConfigItemFacade {

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        final SetupPrivateGatewayCommand command = (SetupPrivateGatewayCommand) cmd;

        final IpAddressTO ip = command.getIpAddress();
        return generateConfigItems(new PrivateGateway(ip.getPublicIp(), ip.isSourceNat(), ip.isAdd(), ip.getVlanNetmask(), ip.getMacAddress()));
    }

    @Override
    protected List<ConfigItem> generateConfigItems(final ConfigBase configuration) {
        destinationFile = VRScripts.PRIVATE_GATEWAY_CONFIG;

        return super.generateConfigItems(configuration);
    }
}
