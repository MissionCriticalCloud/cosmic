//

//

package com.cloud.storage.resource;

import com.cloud.agent.api.Answer;
import org.apache.cloudstack.storage.command.StorageSubSystemCommand;

public interface StorageSubsystemCommandHandler {
    public Answer handleStorageCommands(StorageSubSystemCommand command);
}
