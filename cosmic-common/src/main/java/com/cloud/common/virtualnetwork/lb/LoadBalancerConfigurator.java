package com.cloud.common.virtualnetwork.lb;

import com.cloud.legacymodel.communication.command.LoadBalancerConfigCommand;
import com.cloud.legacymodel.to.PortForwardingRuleTO;

import java.util.List;

public interface LoadBalancerConfigurator {
    int ADD = 0;
    int REMOVE = 1;
    int STATS = 2;

    String[] generateConfiguration(List<PortForwardingRuleTO> fwRules);

    String[] generateConfiguration(LoadBalancerConfigCommand lbCmd);

    String[][] generateFwRules(LoadBalancerConfigCommand lbCmd);
}
