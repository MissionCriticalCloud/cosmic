package com.cloud.network.rules;

import com.cloud.dc.DataCenter;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.RemoteAccessVpn;
import com.cloud.network.VpnUser;
import com.cloud.network.router.VirtualRouter;
import com.cloud.network.vpc.Vpc;
import com.cloud.network.vpc.dao.VpcDao;
import com.cloud.vm.VirtualMachine.State;
import org.apache.cloudstack.network.topology.NetworkTopologyVisitor;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvancedVpnRules extends BasicVpnRules {

    private static final Logger s_logger = LoggerFactory.getLogger(AdvancedVpnRules.class);

    private final RemoteAccessVpn _remoteAccessVpn;

    public AdvancedVpnRules(final RemoteAccessVpn remoteAccessVpn, final List<? extends VpnUser> users) {
        super(null, users);
        _remoteAccessVpn = remoteAccessVpn;
    }

    @Override
    public boolean accept(final NetworkTopologyVisitor visitor, final VirtualRouter router) throws ResourceUnavailableException {
        _router = router;

        final VpcDao vpcDao = visitor.getVirtualNetworkApplianceFactory().getVpcDao();
        final Vpc vpc = vpcDao.findById(_remoteAccessVpn.getVpcId());

        if (_router.getState() != State.Running) {
            s_logger.warn("Failed to add/remove Remote Access VPN users: router not in running state");
            throw new ResourceUnavailableException("Failed to add/remove Remote Access VPN users: router not in running state: " + router.getState(), DataCenter.class,
                    vpc.getZoneId());
        }

        return visitor.visit(this);
    }
}
