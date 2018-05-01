package com.cloud.storage.datastore;

import com.cloud.engine.subsystem.api.storage.DataObject;
import com.cloud.engine.subsystem.api.storage.DataObjectInStore;
import com.cloud.engine.subsystem.api.storage.DataStore;
import com.cloud.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.Event;
import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.model.enumeration.DataObjectType;
import com.cloud.model.enumeration.DataStoreRole;
import com.cloud.utils.fsm.NoTransitionException;

public interface ObjectInDataStoreManager {
    DataObject create(DataObject dataObj, DataStore dataStore);

    boolean delete(DataObject dataObj);

    boolean deleteIfNotReady(DataObject dataObj);

    DataObject get(DataObject dataObj, DataStore store);

    boolean update(DataObject vo, Event event) throws NoTransitionException, ConcurrentOperationException;

    DataObjectInStore findObject(long objId, DataObjectType type, long dataStoreId, DataStoreRole role);

    DataObjectInStore findObject(DataObject obj, DataStore store);
}
