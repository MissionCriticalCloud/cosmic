package com.cloud.network.element;

import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network;
import com.cloud.network.vpc.NetworkACLItem;

import java.util.List;

public interface NetworkACLServiceProvider extends NetworkElement {

    /**
     * @param config
     * @param rules
     * @return
     * @throws ResourceUnavailableException
     */
    boolean applyNetworkACLs(Network config, List<? extends NetworkACLItem> rules) throws ResourceUnavailableException;
}
