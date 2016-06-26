package com.cloud.network.rules;

import com.cloud.agent.api.PvlanSetupCommand;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network;
import com.cloud.network.NetworkModel;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.router.VirtualRouter;
import com.cloud.vm.NicProfile;
import org.apache.cloudstack.network.topology.NetworkTopologyVisitor;

public class DhcpPvlanRules extends RuleApplier {

    private final boolean _isAddPvlan;
    private final NicProfile _nic;

    private PvlanSetupCommand _setupCommand;

    public DhcpPvlanRules(final boolean isAddPvlan, final NicProfile nic) {
        super(null);

        _isAddPvlan = isAddPvlan;
        _nic = nic;
    }

    @Override
    public boolean accept(final NetworkTopologyVisitor visitor, final VirtualRouter router) throws ResourceUnavailableException {
        _router = router;

        String op = "add";
        if (!_isAddPvlan) {
            op = "delete";
        }

        final NetworkDao networkDao = visitor.getVirtualNetworkApplianceFactory().getNetworkDao();
        final Network network = networkDao.findById(_nic.getNetworkId());

        final NetworkModel networkModel = visitor.getVirtualNetworkApplianceFactory().getNetworkModel();
        final String networkTag = networkModel.getNetworkTag(_router.getHypervisorType(), network);

        _setupCommand = PvlanSetupCommand.createDhcpSetup(op, _nic.getBroadCastUri(), networkTag, _router.getInstanceName(), _nic.getMacAddress(), _nic.getIPv4Address());

        return visitor.visit(this);
    }

    public PvlanSetupCommand getSetupCommand() {
        return _setupCommand;
    }
}
