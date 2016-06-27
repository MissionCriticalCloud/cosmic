//

//

package com.cloud.network;

import com.cloud.network.lb.LoadBalancingRule;

public interface LoadBalancerValidator {
    /**
     * Validate rules
     *
     * @param rule
     * @return true/false. If there are no validation then true should be return.
     */
    public boolean validateLBRule(LoadBalancingRule rule);
}
