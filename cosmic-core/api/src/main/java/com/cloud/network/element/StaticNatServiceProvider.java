package com.cloud.network.element;

import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network;
import com.cloud.network.rules.StaticNat;

import java.util.List;

public interface StaticNatServiceProvider extends NetworkElement, IpDeployingRequester {
    /**
     * Creates static nat rule (public IP to private IP mapping) on the network element
     *
     * @param config
     * @param rules
     * @return
     * @throws ResourceUnavailableException
     */
    boolean applyStaticNats(Network config, List<? extends StaticNat> rules) throws ResourceUnavailableException;
}
