package com.cloud.network.rules;

import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.network.Network;
import com.cloud.legacymodel.network.VirtualRouter;
import com.cloud.network.topology.NetworkTopologyVisitor;

public abstract class RuleApplier {

    protected Network _network;
    protected VirtualRouter _router;

    public RuleApplier(final Network network) {
        _network = network;
    }

    public Network getNetwork() {
        return _network;
    }

    public VirtualRouter getRouter() {
        return _router;
    }

    public abstract boolean accept(NetworkTopologyVisitor visitor, VirtualRouter router) throws ResourceUnavailableException;
}
