//

//

package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.GetStorageStatsAnswer;
import com.cloud.agent.api.GetStorageStatsCommand;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.hypervisor.kvm.storage.KvmStoragePool;
import com.cloud.hypervisor.kvm.storage.KvmStoragePoolManager;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;
import com.cloud.utils.exception.CloudRuntimeException;

@ResourceWrapper(handles = GetStorageStatsCommand.class)
public final class LibvirtGetStorageStatsCommandWrapper
        extends CommandWrapper<GetStorageStatsCommand, Answer, LibvirtComputingResource> {

    @Override
    public Answer execute(final GetStorageStatsCommand command, final LibvirtComputingResource libvirtComputingResource) {
        try {
            final KvmStoragePoolManager storagePoolMgr = libvirtComputingResource.getStoragePoolMgr();
            final KvmStoragePool sp = storagePoolMgr.getStoragePool(command.getPooltype(), command.getStorageId(), true);
            return new GetStorageStatsAnswer(command, sp.getCapacity(), sp.getUsed());
        } catch (final CloudRuntimeException e) {
            return new GetStorageStatsAnswer(command, e.toString());
        }
    }
}
