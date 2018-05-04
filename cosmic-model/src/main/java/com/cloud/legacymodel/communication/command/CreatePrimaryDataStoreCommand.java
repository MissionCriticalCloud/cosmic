package com.cloud.legacymodel.communication.command;

public final class CreatePrimaryDataStoreCommand extends StorageSubSystemCommand {
    private final String dataStore;

    public CreatePrimaryDataStoreCommand(final String uri) {
        super();
        dataStore = uri;
    }

    public String getDataStore() {
        return dataStore;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    @Override
    public void setExecuteInSequence(final boolean inSeq) {

    }
}
