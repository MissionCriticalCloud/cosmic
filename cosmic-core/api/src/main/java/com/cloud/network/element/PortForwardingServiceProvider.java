package com.cloud.network.element;

import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.network.Network;
import com.cloud.legacymodel.network.PortForwardingRule;

import java.util.List;

public interface PortForwardingServiceProvider extends NetworkElement, IpDeployingRequester {
    /**
     * Apply rules
     *
     * @param network
     * @param rules
     * @return
     * @throws ResourceUnavailableException
     */
    boolean applyPFRules(Network network, List<PortForwardingRule> rules) throws ResourceUnavailableException;
}
