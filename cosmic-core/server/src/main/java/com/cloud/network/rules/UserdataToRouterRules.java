package com.cloud.network.rules;

import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network;
import com.cloud.network.router.VirtualRouter;
import com.cloud.vm.NicProfile;
import com.cloud.vm.NicVO;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.dao.NicDao;
import com.cloud.vm.dao.UserVmDao;
import org.apache.cloudstack.network.topology.NetworkTopologyVisitor;

public class UserdataToRouterRules extends RuleApplier {

    private final NicProfile _nic;
    private final VirtualMachineProfile _profile;

    private NicVO _nicVo;
    private UserVmVO _userVM;

    public UserdataToRouterRules(final Network network, final NicProfile nic, final VirtualMachineProfile profile) {
        super(network);

        _nic = nic;
        _profile = profile;
    }

    @Override
    public boolean accept(final NetworkTopologyVisitor visitor, final VirtualRouter router) throws ResourceUnavailableException {
        _router = router;

        final UserVmDao userVmDao = visitor.getVirtualNetworkApplianceFactory().getUserVmDao();
        _userVM = userVmDao.findById(_profile.getVirtualMachine().getId());
        userVmDao.loadDetails(_userVM);

        // for basic zone, send vm data/password information only to the router in the same pod
        final NicDao nicDao = visitor.getVirtualNetworkApplianceFactory().getNicDao();
        _nicVo = nicDao.findById(_nic.getId());

        return visitor.visit(this);
    }

    public NicVO getNicVo() {
        return _nicVo;
    }

    public UserVmVO getUserVM() {
        return _userVM;
    }
}
