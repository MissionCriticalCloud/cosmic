package com.cloud.legacymodel.communication.command;

public final class AttachPrimaryDataStoreCommand extends StorageSubSystemCommand {
    private final String dataStore;

    public AttachPrimaryDataStoreCommand(final String uri) {
        super();
        dataStore = uri;
    }

    @Override
    public void setExecuteInSequence(final boolean inSeq) {

    }

    public String getDataStore() {
        return dataStore;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
