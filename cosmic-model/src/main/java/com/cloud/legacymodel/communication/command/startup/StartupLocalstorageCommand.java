package com.cloud.legacymodel.communication.command.startup;

import com.cloud.legacymodel.storage.StoragePoolInfo;
import com.cloud.model.enumeration.HostType;

public class StartupLocalstorageCommand extends StartupCommand {
    StoragePoolInfo poolInfo;

    public StartupLocalstorageCommand() {
        super(HostType.Storage);
    }

    public StoragePoolInfo getPoolInfo() {
        return poolInfo;
    }

    public void setPoolInfo(final StoragePoolInfo poolInfo) {
        this.poolInfo = poolInfo;
    }
}
