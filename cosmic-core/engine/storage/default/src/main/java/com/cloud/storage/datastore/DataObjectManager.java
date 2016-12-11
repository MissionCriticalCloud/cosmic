package com.cloud.storage.datastore;

import com.cloud.engine.subsystem.api.storage.CreateCmdResult;
import com.cloud.engine.subsystem.api.storage.DataObject;
import com.cloud.framework.async.AsyncCompletionCallback;

public interface DataObjectManager {
    void update(DataObject data, String path, Long size);

    void copyAsync(DataObject srcData, DataObject destData, AsyncCompletionCallback<CreateCmdResult> callback);
}
