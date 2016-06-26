package org.apache.cloudstack.storage.datastore;

import org.apache.cloudstack.engine.subsystem.api.storage.CreateCmdResult;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObject;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.framework.async.AsyncCompletionCallback;
import org.apache.cloudstack.storage.command.CommandResult;

public interface DataObjectManager {
    public void createAsync(DataObject data, DataStore store, AsyncCompletionCallback<CreateCmdResult> callback, boolean noCopy);

    /*
     * Only create internal state, without actually send down create command.
     * It's up to device driver decides whether to create object before copying
     */
    public DataObject createInternalStateOnly(DataObject data, DataStore store);

    public void update(DataObject data, String path, Long size);

    public void copyAsync(DataObject srcData, DataObject destData, AsyncCompletionCallback<CreateCmdResult> callback);

    public void deleteAsync(DataObject data, AsyncCompletionCallback<CommandResult> callback);
}
