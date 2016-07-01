package org.apache.cloudstack.storage.datastore.protocol;

public enum DataStoreProtocol {
    NFS("nfs"), CIFS("cifs"), ISCSI("iscsi");

    private final String name;

    DataStoreProtocol(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
