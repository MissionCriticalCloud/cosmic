package com.cloud.network.element;

import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.network.vpc.NetworkACLItem;
import com.cloud.network.Network;

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
