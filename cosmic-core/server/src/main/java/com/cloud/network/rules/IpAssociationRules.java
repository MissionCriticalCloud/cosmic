package com.cloud.network.rules;

import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network;
import com.cloud.network.PublicIpAddress;
import com.cloud.network.router.VirtualRouter;
import org.apache.cloudstack.network.topology.NetworkTopologyVisitor;

import java.util.List;

public class IpAssociationRules extends RuleApplier {

    private final List<? extends PublicIpAddress> _ipAddresses;

    public IpAssociationRules(final Network network, final List<? extends PublicIpAddress> ipAddresses) {
        super(network);
        _ipAddresses = ipAddresses;
    }

    @Override
    public boolean accept(final NetworkTopologyVisitor visitor, final VirtualRouter router) throws ResourceUnavailableException {
        _router = router;

        return visitor.visit(this);
    }

    public List<? extends PublicIpAddress> getIpAddresses() {
        return _ipAddresses;
    }
}
