package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.agent.api.to.DataStoreTO;
import com.cloud.storage.DataStoreRole;

public interface DataStore {
    DataStoreDriver getDriver();

    DataStoreRole getRole();

    long getId();

    String getUuid();

    String getUri();

    Scope getScope();

    String getName();

    DataObject create(DataObject obj);

    boolean delete(DataObject obj);

    DataStoreTO getTO();
}
