package com.cloud.storage.db;

import com.cloud.engine.subsystem.api.storage.DataObjectInStore;
import com.cloud.engine.subsystem.api.storage.ObjectInDataStoreStateMachine;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.fsm.StateDao;

public interface ObjectInDataStoreDao extends GenericDao<ObjectInDataStoreVO, Long>,
        StateDao<ObjectInDataStoreStateMachine.State, ObjectInDataStoreStateMachine.Event, DataObjectInStore> {

}
