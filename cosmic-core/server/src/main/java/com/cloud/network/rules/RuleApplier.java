package com.cloud.network.rules;

import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network;
import com.cloud.network.router.VirtualRouter;
import org.apache.cloudstack.network.topology.NetworkTopologyVisitor;

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
