//

//

package com.cloud.agent.api;

import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.agent.api.to.VolumeTO;
import com.cloud.utils.Pair;

import java.util.List;

public class MigrateWithStorageReceiveCommand extends Command {
    VirtualMachineTO vm;
    List<Pair<VolumeTO, StorageFilerTO>> volumeToFiler;

    public MigrateWithStorageReceiveCommand(final VirtualMachineTO vm, final List<Pair<VolumeTO, StorageFilerTO>> volumeToFiler) {
        this.vm = vm;
        this.volumeToFiler = volumeToFiler;
    }

    public VirtualMachineTO getVirtualMachine() {
        return vm;
    }

    public List<Pair<VolumeTO, StorageFilerTO>> getVolumeToFiler() {
        return volumeToFiler;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }
}
