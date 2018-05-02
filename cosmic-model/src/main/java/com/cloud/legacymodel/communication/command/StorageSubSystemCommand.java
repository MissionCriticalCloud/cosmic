package com.cloud.legacymodel.communication.command;

public abstract class StorageSubSystemCommand extends Command {
    public abstract void setExecuteInSequence(boolean inSeq);
}
