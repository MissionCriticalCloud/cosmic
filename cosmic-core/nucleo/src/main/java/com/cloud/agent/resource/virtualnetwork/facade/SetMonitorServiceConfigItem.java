//

//

package com.cloud.agent.resource.virtualnetwork.facade;

import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.api.routing.SetMonitorServiceCommand;
import com.cloud.agent.resource.virtualnetwork.ConfigItem;
import com.cloud.agent.resource.virtualnetwork.VRScripts;
import com.cloud.agent.resource.virtualnetwork.model.ConfigBase;
import com.cloud.agent.resource.virtualnetwork.model.MonitorService;

import java.util.List;

public class SetMonitorServiceConfigItem extends AbstractConfigItemFacade {

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        final SetMonitorServiceCommand command = (SetMonitorServiceCommand) cmd;

        final MonitorService monitorService = new MonitorService(command.getConfiguration(), cmd.getAccessDetail(NetworkElementCommand.ROUTER_MONITORING_ENABLE));
        return generateConfigItems(monitorService);
    }

    @Override
    protected List<ConfigItem> generateConfigItems(final ConfigBase configuration) {
        destinationFile = VRScripts.MONITOR_SERVICE_CONFIG;

        return super.generateConfigItems(configuration);
    }
}
