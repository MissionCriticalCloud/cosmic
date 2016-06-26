package com.cloud.network.rules;

import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network;
import com.cloud.network.NetworkModel;
import com.cloud.network.dao.LoadBalancerDao;
import com.cloud.network.dao.LoadBalancerVO;
import com.cloud.network.lb.LoadBalancingRule;
import com.cloud.network.lb.LoadBalancingRule.LbDestination;
import com.cloud.network.lb.LoadBalancingRule.LbHealthCheckPolicy;
import com.cloud.network.lb.LoadBalancingRule.LbSslCert;
import com.cloud.network.lb.LoadBalancingRule.LbStickinessPolicy;
import com.cloud.network.lb.LoadBalancingRulesManager;
import com.cloud.network.router.VirtualRouter;
import com.cloud.network.rules.LoadBalancerContainer.Scheme;
import com.cloud.utils.net.Ip;
import org.apache.cloudstack.network.topology.NetworkTopologyVisitor;

import java.util.List;

public class LoadBalancingRules extends RuleApplier {

    private final List<LoadBalancingRule> _rules;

    public LoadBalancingRules(final Network network, final List<LoadBalancingRule> rules) {
        super(network);
        _rules = rules;
    }

    @Override
    public boolean accept(final NetworkTopologyVisitor visitor, final VirtualRouter router) throws ResourceUnavailableException {
        _router = router;

        final LoadBalancerDao loadBalancerDao = visitor.getVirtualNetworkApplianceFactory().getLoadBalancerDao();
        // For load balancer we have to resend all lb rules for the network
        final List<LoadBalancerVO> lbs = loadBalancerDao.listByNetworkIdAndScheme(_network.getId(), Scheme.Public);

        // We are cleaning it before because all the rules have to be sent to the router.
        _rules.clear();

        final LoadBalancingRulesManager lbMgr = visitor.getVirtualNetworkApplianceFactory().getLbMgr();
        final NetworkModel networkModel = visitor.getVirtualNetworkApplianceFactory().getNetworkModel();
        for (final LoadBalancerVO lb : lbs) {

            final List<LbDestination> dstList = lbMgr.getExistingDestinations(lb.getId());
            final List<LbStickinessPolicy> policyList = lbMgr.getStickinessPolicies(lb.getId());
            final List<LbHealthCheckPolicy> hcPolicyList = lbMgr.getHealthCheckPolicies(lb.getId());
            final LbSslCert sslCert = lbMgr.getLbSslCert(lb.getId());
            final Ip sourceIp = networkModel.getPublicIpAddress(lb.getSourceIpAddressId()).getAddress();
            final LoadBalancingRule loadBalancing = new LoadBalancingRule(lb, dstList, policyList, hcPolicyList, sourceIp, sslCert, lb.getLbProtocol());

            _rules.add(loadBalancing);
        }

        return visitor.visit(this);
    }

    public List<LoadBalancingRule> getRules() {
        return _rules;
    }
}
