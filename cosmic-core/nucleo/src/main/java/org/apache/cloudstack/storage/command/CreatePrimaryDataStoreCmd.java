//

//

package org.apache.cloudstack.storage.command;

public final class CreatePrimaryDataStoreCmd extends StorageSubSystemCommand {
    private final String dataStore;

    public CreatePrimaryDataStoreCmd(final String uri) {
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
