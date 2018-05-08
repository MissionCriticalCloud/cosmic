package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.agent.resource.kvm.storage.KvmStoragePool;
import com.cloud.agent.resource.kvm.storage.KvmStoragePoolManager;
import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.GetStorageStatsAnswer;
import com.cloud.legacymodel.communication.command.GetStorageStatsCommand;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;

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
