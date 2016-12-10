package com.cloud.storage.datastore;

import com.cloud.framework.async.AsyncCompletionCallback;
import org.apache.cloudstack.engine.subsystem.api.storage.CreateCmdResult;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObject;

public interface DataObjectManager {
    void update(DataObject data, String path, Long size);

    void copyAsync(DataObject srcData, DataObject destData, AsyncCompletionCallback<CreateCmdResult> callback);
}
