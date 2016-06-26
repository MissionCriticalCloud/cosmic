package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.agent.api.to.DataStoreTO;
import com.cloud.agent.api.to.DataTO;
import org.apache.cloudstack.framework.async.AsyncCompletionCallback;
import org.apache.cloudstack.storage.command.CommandResult;

import java.util.Map;

public interface DataStoreDriver {
    Map<String, String> getCapabilities();

    DataTO getTO(DataObject data);

    DataStoreTO getStoreTO(DataStore store);

    void createAsync(DataStore store, DataObject data, AsyncCompletionCallback<CreateCmdResult> callback);

    void deleteAsync(DataStore store, DataObject data, AsyncCompletionCallback<CommandResult> callback);

    void copyAsync(DataObject srcdata, DataObject destData, AsyncCompletionCallback<CopyCommandResult> callback);

    boolean canCopy(DataObject srcData, DataObject destData);

    void resize(DataObject data, AsyncCompletionCallback<CreateCmdResult> callback);
}
