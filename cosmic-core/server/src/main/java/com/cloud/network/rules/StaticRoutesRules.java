package com.cloud.network.rules;

import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.network.VirtualRouter;
import com.cloud.legacymodel.network.vpc.StaticRouteProfile;
import com.cloud.network.topology.NetworkTopologyVisitor;

import java.util.List;

public class StaticRoutesRules extends RuleApplier {

    private final List<StaticRouteProfile> staticRoutes;

    public StaticRoutesRules(final List<StaticRouteProfile> staticRoutes) {
        super(null);
        this.staticRoutes = staticRoutes;
    }

    public List<StaticRouteProfile> getStaticRoutes() {
        return staticRoutes;
    }

    @Override
    public boolean accept(final NetworkTopologyVisitor visitor, final VirtualRouter router) throws ResourceUnavailableException {
        _router = router;

        return visitor.visit(this);
    }
}
