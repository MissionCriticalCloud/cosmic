//

//

package com.cloud.agent.resource.virtualnetwork.facade;

import com.cloud.agent.api.routing.LoadBalancerConfigCommand;
import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.resource.virtualnetwork.ConfigItem;
import com.cloud.agent.resource.virtualnetwork.VRScripts;
import com.cloud.agent.resource.virtualnetwork.model.ConfigBase;
import com.cloud.agent.resource.virtualnetwork.model.LoadBalancerRule;
import com.cloud.agent.resource.virtualnetwork.model.LoadBalancerRules;
import com.cloud.network.HAProxyConfigurator;
import com.cloud.network.LoadBalancerConfigurator;

import java.util.LinkedList;
import java.util.List;

public class LoadBalancerConfigItem extends AbstractConfigItemFacade {

    @Override
    public List<ConfigItem> generateConfig(final NetworkElementCommand cmd) {
        final LoadBalancerConfigCommand command = (LoadBalancerConfigCommand) cmd;

        final LoadBalancerConfigurator cfgtr = new HAProxyConfigurator();
        final String[] configuration = cfgtr.generateConfiguration(command);

        String routerIp = command.getNic().getIp();
        if (command.getVpcId() == null) {
            routerIp = cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP);
        }

        final String tmpCfgFilePath = "/etc/haproxy/";
        final String tmpCfgFileName = "haproxy.cfg.new." + String.valueOf(System.currentTimeMillis());

        final String[][] allRules = cfgtr.generateFwRules(command);

        final String[] addRules = allRules[LoadBalancerConfigurator.ADD];
        final String[] removeRules = allRules[LoadBalancerConfigurator.REMOVE];
        final String[] statRules = allRules[LoadBalancerConfigurator.STATS];

        final LoadBalancerRule loadBalancerRule = new LoadBalancerRule(configuration, tmpCfgFilePath, tmpCfgFileName, addRules, removeRules, statRules, routerIp);

        final List<LoadBalancerRule> rules = new LinkedList<>();
        rules.add(loadBalancerRule);

        final LoadBalancerRules configRules = new LoadBalancerRules(rules);

        return generateConfigItems(configRules);
    }

    @Override
    protected List<ConfigItem> generateConfigItems(final ConfigBase configuration) {
        destinationFile = VRScripts.LOAD_BALANCER_CONFIG;

        return super.generateConfigItems(configuration);
    }
}
