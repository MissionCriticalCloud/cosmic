//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;
import com.cloud.storage.resource.StorageSubsystemCommandHandler;
import org.apache.cloudstack.storage.command.StorageSubSystemCommand;

@ResourceWrapper(handles = StorageSubSystemCommand.class)
public final class CitrixStorageSubSystemCommandWrapper extends CommandWrapper<StorageSubSystemCommand, Answer, CitrixResourceBase> {

    @Override
    public Answer execute(final StorageSubSystemCommand command, final CitrixResourceBase citrixResourceBase) {
        final StorageSubsystemCommandHandler handler = citrixResourceBase.getStorageHandler();
        return handler.handleStorageCommands(command);
    }
}
