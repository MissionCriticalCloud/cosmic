//

//

package com.cloud.agent.api;

import com.cloud.storage.StoragePool;

import java.util.Map;

public class CreateStoragePoolCommand extends ModifyStoragePoolCommand {
    public static final String DATASTORE_NAME = "datastoreName";
    public static final String IQN = "iqn";
    public static final String STORAGE_HOST = "storageHost";
    public static final String STORAGE_PORT = "storagePort";

    private boolean _createDatastore;
    private Map<String, String> _details;

    public CreateStoragePoolCommand() {
    }

    public CreateStoragePoolCommand(final boolean add, final StoragePool pool) {
        super(add, pool);
    }

    public boolean getCreateDatastore() {
        return _createDatastore;
    }

    public void setCreateDatastore(final boolean createDatastore) {
        _createDatastore = createDatastore;
    }

    public Map<String, String> getDetails() {
        return _details;
    }

    public void setDetails(final Map<String, String> details) {
        _details = details;
    }
}
