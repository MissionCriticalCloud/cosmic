package com.cloud.network.rules;

import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network;
import com.cloud.network.VpnUser;
import com.cloud.network.router.VirtualRouter;
import org.apache.cloudstack.network.topology.NetworkTopologyVisitor;

import java.util.List;

public class BasicVpnRules extends RuleApplier {

    private final List<? extends VpnUser> _users;

    public BasicVpnRules(final Network network, final List<? extends VpnUser> users) {
        super(network);
        _users = users;
    }

    @Override
    public boolean accept(final NetworkTopologyVisitor visitor, final VirtualRouter router) throws ResourceUnavailableException {
        _router = router;

        return visitor.visit(this);
    }

    public List<? extends VpnUser> getUsers() {
        return _users;
    }
}
