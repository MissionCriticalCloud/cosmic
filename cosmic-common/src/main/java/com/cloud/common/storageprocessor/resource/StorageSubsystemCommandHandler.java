package com.cloud.common.storageprocessor.resource;

import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.StorageSubSystemCommand;

public interface StorageSubsystemCommandHandler {
    public Answer handleStorageCommands(StorageSubSystemCommand command);
}
