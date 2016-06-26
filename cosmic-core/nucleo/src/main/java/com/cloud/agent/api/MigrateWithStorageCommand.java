//

//

package com.cloud.agent.api;

import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.agent.api.to.VolumeTO;
import com.cloud.utils.Pair;

import java.util.List;
import java.util.Map;

public class MigrateWithStorageCommand extends Command {
    VirtualMachineTO vm;
    Map<VolumeTO, StorageFilerTO> volumeToFiler;
    List<Pair<VolumeTO, StorageFilerTO>> volumeToFilerAsList;
    String tgtHost;

    public MigrateWithStorageCommand(final VirtualMachineTO vm, final Map<VolumeTO, StorageFilerTO> volumeToFiler) {
        this.vm = vm;
        this.volumeToFiler = volumeToFiler;
        this.volumeToFilerAsList = null;
        this.tgtHost = null;
    }

    public MigrateWithStorageCommand(final VirtualMachineTO vm, final List<Pair<VolumeTO, StorageFilerTO>> volumeToFilerAsList) {
        this.vm = vm;
        this.volumeToFiler = null;
        this.volumeToFilerAsList = volumeToFilerAsList;
        this.tgtHost = null;
    }

    public MigrateWithStorageCommand(final VirtualMachineTO vm, final Map<VolumeTO, StorageFilerTO> volumeToFiler, final String tgtHost) {
        this.vm = vm;
        this.volumeToFiler = volumeToFiler;
        this.volumeToFilerAsList = null;
        this.tgtHost = tgtHost;
    }

    public MigrateWithStorageCommand(final VirtualMachineTO vm, final List<Pair<VolumeTO, StorageFilerTO>> volumeToFilerAsList, final String tgtHost) {
        this.vm = vm;
        this.volumeToFiler = null;
        this.volumeToFilerAsList = volumeToFilerAsList;
        this.tgtHost = tgtHost;
    }

    public VirtualMachineTO getVirtualMachine() {
        return vm;
    }

    public Map<VolumeTO, StorageFilerTO> getVolumeToFiler() {
        return volumeToFiler;
    }

    public List<Pair<VolumeTO, StorageFilerTO>> getVolumeToFilerAsList() {
        return volumeToFilerAsList;
    }

    public String getTargetHost() {
        return tgtHost;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }
}
