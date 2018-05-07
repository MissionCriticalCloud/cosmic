package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.common.storageprocessor.resource.StorageSubsystemCommandHandler;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.StorageSubSystemCommand;

@ResourceWrapper(handles = StorageSubSystemCommand.class)
public final class CitrixStorageSubSystemCommandWrapper extends CommandWrapper<StorageSubSystemCommand, Answer, CitrixResourceBase> {

    @Override
    public Answer execute(final StorageSubSystemCommand command, final CitrixResourceBase citrixResourceBase) {
        final StorageSubsystemCommandHandler handler = citrixResourceBase.getStorageHandler();
        return handler.handleStorageCommands(command);
    }
}
