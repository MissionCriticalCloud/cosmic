//

//

package com.cloud.network;

import com.cloud.agent.api.routing.LoadBalancerConfigCommand;
import com.cloud.agent.api.to.PortForwardingRuleTO;

import java.util.List;

public interface LoadBalancerConfigurator {
    public final static int ADD = 0;
    public final static int REMOVE = 1;
    public final static int STATS = 2;

    public String[] generateConfiguration(List<PortForwardingRuleTO> fwRules);

    public String[] generateConfiguration(LoadBalancerConfigCommand lbCmd);

    public String[][] generateFwRules(LoadBalancerConfigCommand lbCmd);
}
