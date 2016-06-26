package com.cloud.network.rules;

import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network;
import com.cloud.network.router.VirtualRouter;
import com.cloud.network.vpc.NetworkACLItem;
import org.apache.cloudstack.network.topology.NetworkTopologyVisitor;

import java.util.List;

public class NetworkAclsRules extends RuleApplier {

    private final List<? extends NetworkACLItem> _rules;
    private final boolean _isPrivateGateway;

    public NetworkAclsRules(final Network network, final List<? extends NetworkACLItem> rules, final boolean isPrivateGateway) {
        super(network);
        _rules = rules;
        _isPrivateGateway = isPrivateGateway;
    }

    @Override
    public boolean accept(final NetworkTopologyVisitor visitor, final VirtualRouter router) throws ResourceUnavailableException {
        _router = router;

        return visitor.visit(this);
    }

    public List<? extends NetworkACLItem> getRules() {
        return _rules;
    }

    public boolean isPrivateGateway() {
        return _isPrivateGateway;
    }
}
