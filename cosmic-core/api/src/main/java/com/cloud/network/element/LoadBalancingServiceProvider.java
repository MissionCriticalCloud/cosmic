package com.cloud.network.element;

import com.cloud.agent.api.to.LoadBalancerTO;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network;
import com.cloud.network.lb.LoadBalancingRule;

import java.util.List;

public interface LoadBalancingServiceProvider extends NetworkElement, IpDeployingRequester {
    /**
     * Apply rules
     *
     * @param network
     * @param rules
     * @return
     * @throws ResourceUnavailableException
     */
    boolean applyLBRules(Network network, List<LoadBalancingRule> rules) throws ResourceUnavailableException;

    /**
     * Validate rules
     *
     * @param network
     * @param rule
     * @return true/false. true should be return if there are no validations.
     * false should be return if any oneof the validation fails.
     * @throws
     */
    boolean validateLBRule(Network network, LoadBalancingRule rule);

    List<LoadBalancerTO> updateHealthChecks(Network network, List<LoadBalancingRule> lbrules);
}
