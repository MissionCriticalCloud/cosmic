package com.cloud.network.rules;

import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network;
import com.cloud.network.router.VirtualRouter;
import org.apache.cloudstack.network.topology.NetworkTopologyVisitor;

import java.util.List;

public class StaticNatRules extends RuleApplier {

    private final List<? extends StaticNat> _rules;

    public StaticNatRules(final Network network, final List<? extends StaticNat> rules) {
        super(network);
        _rules = rules;
    }

    public List<? extends StaticNat> getRules() {
        return _rules;
    }

    @Override
    public boolean accept(final NetworkTopologyVisitor visitor, final VirtualRouter router) throws ResourceUnavailableException {
        _router = router;
        return visitor.visit(this);
    }
}
