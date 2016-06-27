//

//

package org.apache.cloudstack.storage.command;

public final class AttachPrimaryDataStoreCmd extends StorageSubSystemCommand {
    private final String dataStore;

    public AttachPrimaryDataStoreCmd(final String uri) {
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
