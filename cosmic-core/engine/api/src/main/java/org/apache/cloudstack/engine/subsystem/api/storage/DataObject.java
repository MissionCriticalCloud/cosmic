package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.to.DataObjectType;
import com.cloud.agent.api.to.DataTO;

public interface DataObject {
    long getId();

    String getUri();

    DataTO getTO();

    DataStore getDataStore();

    Long getSize();

    DataObjectType getType();

    String getUuid();

    boolean delete();

    void processEvent(ObjectInDataStoreStateMachine.Event event);

    void processEvent(ObjectInDataStoreStateMachine.Event event, Answer answer);

    void incRefCount();

    void decRefCount();

    Long getRefCount();
}
