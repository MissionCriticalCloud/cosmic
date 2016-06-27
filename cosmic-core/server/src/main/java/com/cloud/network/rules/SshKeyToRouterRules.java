package com.cloud.network.rules;

import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network;
import com.cloud.network.router.VirtualRouter;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.vm.NicProfile;
import com.cloud.vm.NicVO;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.dao.NicDao;
import com.cloud.vm.dao.UserVmDao;
import org.apache.cloudstack.network.topology.NetworkTopologyVisitor;

public class SshKeyToRouterRules extends RuleApplier {

    private final NicProfile _nic;
    private final VirtualMachineProfile _profile;
    private final String _sshPublicKey;

    private NicVO _nicVo;
    private VMTemplateVO _template;
    private UserVmVO _userVM;

    public SshKeyToRouterRules(final Network network, final NicProfile nic, final VirtualMachineProfile profile, final String sshPublicKey) {
        super(network);

        _nic = nic;
        _profile = profile;
        _sshPublicKey = sshPublicKey;
    }

    @Override
    public boolean accept(final NetworkTopologyVisitor visitor, final VirtualRouter router) throws ResourceUnavailableException {
        _router = router;

        final UserVmDao userVmDao = visitor.getVirtualNetworkApplianceFactory().getUserVmDao();
        _userVM = userVmDao.findById(_profile.getVirtualMachine().getId());

        userVmDao.loadDetails(_userVM);

        final NicDao nicDao = visitor.getVirtualNetworkApplianceFactory().getNicDao();
        _nicVo = nicDao.findById(_nic.getId());
        // for basic zone, send vm data/password information only to the router in the same pod
        final VMTemplateDao templateDao = visitor.getVirtualNetworkApplianceFactory().getTemplateDao();
        _template = templateDao.findByIdIncludingRemoved(_profile.getTemplateId());

        return visitor.visit(this);
    }

    public VirtualMachineProfile getProfile() {
        return _profile;
    }

    public String getSshPublicKey() {
        return _sshPublicKey;
    }

    public UserVmVO getUserVM() {
        return _userVM;
    }

    public NicVO getNicVo() {
        return _nicVo;
    }

    public VMTemplateVO getTemplate() {
        return _template;
    }
}
