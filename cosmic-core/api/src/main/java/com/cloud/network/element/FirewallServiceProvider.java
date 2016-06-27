package com.cloud.network.element;

import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network;
import com.cloud.network.rules.FirewallRule;

import java.util.List;

public interface FirewallServiceProvider extends NetworkElement {
    /**
     * Apply rules
     *
     * @param network
     * @param rules
     * @return
     * @throws ResourceUnavailableException
     */
    boolean applyFWRules(Network network, List<? extends FirewallRule> rules) throws ResourceUnavailableException;
}
