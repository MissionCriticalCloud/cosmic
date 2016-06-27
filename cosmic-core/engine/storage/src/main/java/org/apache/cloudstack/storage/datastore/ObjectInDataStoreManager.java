package org.apache.cloudstack.storage.datastore;

import com.cloud.agent.api.to.DataObjectType;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.storage.DataStoreRole;
import com.cloud.utils.fsm.NoTransitionException;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObject;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObjectInStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.Event;

public interface ObjectInDataStoreManager {
    DataObject create(DataObject dataObj, DataStore dataStore);

    boolean delete(DataObject dataObj);

    boolean deleteIfNotReady(DataObject dataObj);

    DataObject get(DataObject dataObj, DataStore store);

    boolean update(DataObject vo, Event event) throws NoTransitionException, ConcurrentOperationException;

    DataObjectInStore findObject(long objId, DataObjectType type, long dataStoreId, DataStoreRole role);

    DataObjectInStore findObject(DataObject obj, DataStore store);

    DataStore findStore(long objId, DataObjectType type, DataStoreRole role);
}
