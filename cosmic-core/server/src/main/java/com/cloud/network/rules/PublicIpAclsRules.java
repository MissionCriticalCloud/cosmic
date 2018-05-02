package com.cloud.network.rules;

import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.network.vpc.NetworkACLItem;
import com.cloud.network.IpAddress;
import com.cloud.network.router.VirtualRouter;
import com.cloud.network.topology.NetworkTopologyVisitor;

import java.util.List;

public class PublicIpAclsRules extends RuleApplier {

    private final List<? extends NetworkACLItem> _rules;
    private final IpAddress _publicIp;

    public PublicIpAclsRules(final IpAddress publicIp, final List<? extends NetworkACLItem> rules) {
        super(null);
        _rules = rules;
        _publicIp = publicIp;
    }

    @Override
    public boolean accept(final NetworkTopologyVisitor visitor, final VirtualRouter router) throws ResourceUnavailableException {
        _router = router;

        return visitor.visit(this);
    }

    public List<? extends NetworkACLItem> getRules() {
        return _rules;
    }

    public IpAddress getPublicIp() {
        return _publicIp;
    }
}
