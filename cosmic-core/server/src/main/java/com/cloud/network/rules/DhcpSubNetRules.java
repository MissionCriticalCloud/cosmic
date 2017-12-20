package com.cloud.network.rules;

import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network;
import com.cloud.network.router.VirtualRouter;
import com.cloud.network.topology.NetworkTopologyVisitor;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.NicProfile;
import com.cloud.vm.NicVO;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.dao.NicDao;
import com.cloud.vm.dao.UserVmDao;

public class DhcpSubNetRules extends RuleApplier {

    private final NicProfile _nic;
    private final VirtualMachineProfile _profile;

    public DhcpSubNetRules(final Network network, final NicProfile nic, final VirtualMachineProfile profile) {
        super(network);

        _nic = nic;
        _profile = profile;
    }

    @Override
    public boolean accept(final NetworkTopologyVisitor visitor, final VirtualRouter router) throws ResourceUnavailableException {
        _router = router;

        final UserVmDao userVmDao = visitor.getVirtualNetworkApplianceFactory().getUserVmDao();
        final UserVmVO vm = userVmDao.findById(_profile.getId());
        userVmDao.loadDetails(vm);

        final NicDao nicDao = visitor.getVirtualNetworkApplianceFactory().getNicDao();
        // check if this is not the primary subnet.
        final NicVO domrGuestNic = nicDao.findByInstanceIdAndIpAddressAndVmtype(_router.getId(), nicDao.getIpAddress(_nic.getNetworkId(), _router.getId()),
                VirtualMachine.Type.DomainRouter);
        // check if the router ip address and the vm ip address belong to same
        // subnet.
        // if they do not belong to same netwoek check for the alias ips. if not
        // create one.
        // This should happen only in case of Basic and Advanced SG enabled
        // networks.
        if (!NetUtils.sameSubnet(domrGuestNic.getIPv4Address(), _nic.getIPv4Address(), _nic.getIPv4Netmask())) {
            return true;
        }
        return true;
    }
}
