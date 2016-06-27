package com.cloud.hypervisor;

import com.cloud.agent.api.Command;
import com.cloud.agent.api.to.DataObjectType;
import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.DataStoreRole;
import com.cloud.storage.GuestOSHypervisorVO;
import com.cloud.storage.GuestOSVO;
import com.cloud.storage.dao.GuestOSDao;
import com.cloud.storage.dao.GuestOSHypervisorDao;
import com.cloud.utils.Pair;
import com.cloud.vm.VirtualMachineProfile;
import org.apache.cloudstack.storage.command.CopyCommand;
import org.apache.cloudstack.storage.command.StorageSubSystemCommand;

import javax.inject.Inject;
import java.util.Map;

public class KVMGuru extends HypervisorGuruBase implements HypervisorGuru {
    @Inject
    GuestOSDao _guestOsDao;
    @Inject
    GuestOSHypervisorDao _guestOsHypervisorDao;
    @Inject
    HostDao _hostDao;

    protected KVMGuru() {
        super();
    }

    @Override
    public Pair<Boolean, Long> getCommandHostDelegation(final long hostId, final Command cmd) {
        if (cmd instanceof CopyCommand) {
            final CopyCommand c = (CopyCommand) cmd;
            boolean inSeq = true;
            if (c.getSrcTO().getObjectType() == DataObjectType.SNAPSHOT ||
                    c.getDestTO().getObjectType() == DataObjectType.SNAPSHOT) {
                inSeq = false;
            } else if (c.getDestTO().getDataStore().getRole() == DataStoreRole.Image ||
                    c.getDestTO().getDataStore().getRole() == DataStoreRole.ImageCache) {
                inSeq = false;
            }
            c.setExecuteInSequence(inSeq);
        }
        if (cmd instanceof StorageSubSystemCommand) {
            final StorageSubSystemCommand c = (StorageSubSystemCommand) cmd;
            c.setExecuteInSequence(false);
        }
        return new Pair<>(false, new Long(hostId));
    }

    @Override
    public HypervisorType getHypervisorType() {
        return HypervisorType.KVM;
    }

    @Override
    public Map<String, String> getClusterSettings(final long vmId) {
        return null;
    }

    @Override

    public VirtualMachineTO implement(final VirtualMachineProfile vm) {
        final VirtualMachineTO to = toVirtualMachineTO(vm);

        // Determine the VM's OS description
        final GuestOSVO guestOS = _guestOsDao.findByIdIncludingRemoved(vm.getVirtualMachine().getGuestOSId());
        to.setOs(guestOS.getDisplayName());
        final HostVO host = _hostDao.findById(vm.getVirtualMachine().getHostId());
        GuestOSHypervisorVO guestOsMapping = null;
        if (host != null) {
            guestOsMapping = _guestOsHypervisorDao.findByOsIdAndHypervisor(guestOS.getId(), getHypervisorType().toString(), host.getHypervisorVersion());
        }
        if (guestOsMapping == null || host == null) {
            to.setPlatformEmulator("Other");
        } else {
            to.setPlatformEmulator(guestOsMapping.getGuestOsName());
        }

        return to;
    }

    @Override
    public boolean trackVmHostChange() {
        return false;
    }
}
