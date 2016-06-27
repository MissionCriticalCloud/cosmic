package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.utils.fsm.StateObject;

public interface DataObjectInStore extends StateObject<ObjectInDataStoreStateMachine.State> {
    String getInstallPath();

    void setInstallPath(String path);

    long getObjectId();

    long getDataStoreId();

    ObjectInDataStoreStateMachine.State getObjectInStoreState();
}
