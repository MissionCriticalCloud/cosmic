//

//

package com.cloud.agent.api;

import com.cloud.agent.api.to.NicTO;
import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.agent.api.to.VolumeTO;
import com.cloud.utils.Pair;

import java.util.List;
import java.util.Map;

public class MigrateWithStorageSendCommand extends Command {
    VirtualMachineTO vm;
    List<Pair<VolumeTO, Object>> volumeToSr;
    List<Pair<NicTO, Object>> nicToNetwork;
    Map<String, String> token;

    public MigrateWithStorageSendCommand(final VirtualMachineTO vm, final List<Pair<VolumeTO, Object>> volumeToSr, final List<Pair<NicTO, Object>> nicToNetwork, final
    Map<String, String> token) {
        this.vm = vm;
        this.volumeToSr = volumeToSr;
        this.nicToNetwork = nicToNetwork;
        this.token = token;
    }

    public VirtualMachineTO getVirtualMachine() {
        return vm;
    }

    public List<Pair<VolumeTO, Object>> getVolumeToSr() {
        return volumeToSr;
    }

    public List<Pair<NicTO, Object>> getNicToNetwork() {
        return nicToNetwork;
    }

    public Map<String, String> getToken() {
        return token;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }
}
