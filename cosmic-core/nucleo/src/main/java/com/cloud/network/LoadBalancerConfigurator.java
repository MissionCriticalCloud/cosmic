package com.cloud.network;

import com.cloud.agent.api.routing.LoadBalancerConfigCommand;
import com.cloud.agent.api.to.PortForwardingRuleTO;

import java.util.List;

public interface LoadBalancerConfigurator {
    int ADD = 0;
    int REMOVE = 1;
    int STATS = 2;

    String[] generateConfiguration(List<PortForwardingRuleTO> fwRules);

    String[] generateConfiguration(LoadBalancerConfigCommand lbCmd);

    String[][] generateFwRules(LoadBalancerConfigCommand lbCmd);
}
