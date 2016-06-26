package org.apache.cloudstack.engine.subsystem.api.storage;

import java.util.List;

public interface EndPointSelector {
    EndPoint select(DataObject srcData, DataObject destData);

    EndPoint select(DataObject srcData, DataObject destData, StorageAction action);

    EndPoint select(DataObject object);

    EndPoint select(DataStore store);

    EndPoint select(DataObject object, StorageAction action);

    List<EndPoint> selectAll(DataStore store);

    EndPoint select(Scope scope, Long storeId);

    EndPoint selectHypervisorHost(Scope scope);

    EndPoint select(DataStore store, String downloadUrl);
}
