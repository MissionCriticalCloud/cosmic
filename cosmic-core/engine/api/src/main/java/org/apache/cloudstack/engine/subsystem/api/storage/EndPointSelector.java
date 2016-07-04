package org.apache.cloudstack.engine.subsystem.api.storage;

public interface EndPointSelector {
    EndPoint select(DataObject srcData, DataObject destData);

    EndPoint select(DataObject srcData, DataObject destData, StorageAction action);

    EndPoint select(DataObject object);

    EndPoint select(DataStore store);

    EndPoint select(DataObject object, StorageAction action);

    EndPoint select(Scope scope, Long storeId);

    EndPoint selectHypervisorHost(Scope scope);

    EndPoint select(DataStore store, String downloadUrl);
}
