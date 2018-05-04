package com.cloud.network.element;

import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.network.FirewallRule;
import com.cloud.legacymodel.network.Network;

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
