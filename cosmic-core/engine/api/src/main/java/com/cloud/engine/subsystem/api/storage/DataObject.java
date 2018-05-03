package com.cloud.engine.subsystem.api.storage;

import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.storage.ObjectInDataStoreStateMachine;
import com.cloud.legacymodel.to.DataTO;
import com.cloud.model.enumeration.DataObjectType;

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
