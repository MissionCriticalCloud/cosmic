package com.cloud.hypervisor;

import com.cloud.agent.api.Command;
import com.cloud.agent.api.to.DataObjectType;
import com.cloud.agent.api.to.DataTO;
import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.DataStoreRole;
import com.cloud.utils.Pair;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.storage.command.CopyCommand;
import com.cloud.storage.command.StorageSubSystemCommand;

import java.util.Map;

public class KVMGuru extends HypervisorGuruBase implements HypervisorGuru {

    protected KVMGuru() {
        super();
    }

    @Override
    public Pair<Boolean, Long> getCommandHostDelegation(final long hostId, final Command cmd) {

        if (cmd instanceof StorageSubSystemCommand) {
            final StorageSubSystemCommand c = (StorageSubSystemCommand) cmd;
            c.setExecuteInSequence(false);
        }
        if (cmd instanceof CopyCommand) {
            final CopyCommand c = (CopyCommand) cmd;
            final DataTO srcData = c.getSrcTO();
            boolean inSeq = true;
            if (c.getSrcTO().getObjectType() == DataObjectType.SNAPSHOT ||
                    c.getDestTO().getObjectType() == DataObjectType.SNAPSHOT) {
                inSeq = false;
            } else if (c.getDestTO().getDataStore().getRole() == DataStoreRole.Image ||
                    c.getDestTO().getDataStore().getRole() == DataStoreRole.ImageCache) {
                inSeq = false;
            }
            c.setExecuteInSequence(inSeq);
            if (srcData.getHypervisorType() == HypervisorType.KVM) {
                return new Pair<>(true, new Long(hostId));
            }
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

        // @TODO: Fixme
        to.setPlatformEmulator("Default - VirtIO capable OS (64-bit)");

        return to;
    }

    @Override
    public boolean trackVmHostChange() {
        return false;
    }
}
