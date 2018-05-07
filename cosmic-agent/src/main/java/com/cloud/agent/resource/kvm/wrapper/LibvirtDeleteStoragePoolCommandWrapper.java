package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.agent.resource.kvm.storage.KvmStoragePoolManager;
import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.DeleteStoragePoolCommand;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.to.StorageFilerTO;

@ResourceWrapper(handles = DeleteStoragePoolCommand.class)
public final class LibvirtDeleteStoragePoolCommandWrapper
        extends CommandWrapper<DeleteStoragePoolCommand, Answer, LibvirtComputingResource> {

    @Override
    public Answer execute(final DeleteStoragePoolCommand command,
                          final LibvirtComputingResource libvirtComputingResource) {
        try {
            final StorageFilerTO pool = command.getPool();
            final KvmStoragePoolManager storagePoolMgr = libvirtComputingResource.getStoragePoolMgr();
            storagePoolMgr.deleteStoragePool(pool.getType(), pool.getUuid());
            return new Answer(command);
        } catch (final CloudRuntimeException e) {
            return new Answer(command, false, e.toString());
        }
    }
}
