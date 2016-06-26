//

//

package com.cloud.agent.api;

import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.storage.StoragePool;

import java.io.File;
import java.util.Map;
import java.util.UUID;

public class DeleteStoragePoolCommand extends Command {
    public static final String DATASTORE_NAME = "datastoreName";
    public static final String IQN = "iqn";
    public static final String STORAGE_HOST = "storageHost";
    public static final String STORAGE_PORT = "storagePort";

    public static final String LOCAL_PATH_PREFIX = "/mnt/";

    private StorageFilerTO _pool;
    private String _localPath;
    private boolean _removeDatastore;
    private Map<String, String> _details;

    public DeleteStoragePoolCommand() {

    }

    public DeleteStoragePoolCommand(final StoragePool pool) {
        this(pool, LOCAL_PATH_PREFIX + File.separator + UUID.nameUUIDFromBytes((pool.getHostAddress() + pool.getPath()).getBytes()));
    }

    public DeleteStoragePoolCommand(final StoragePool pool, final String localPath) {
        _pool = new StorageFilerTO(pool);
        _localPath = localPath;
    }

    public StorageFilerTO getPool() {
        return _pool;
    }

    public void setPool(final StoragePool pool) {
        _pool = new StorageFilerTO(pool);
    }

    public String getLocalPath() {
        return _localPath;
    }

    public boolean getRemoveDatastore() {
        return _removeDatastore;
    }

    public void setRemoveDatastore(final boolean removeDatastore) {
        _removeDatastore = removeDatastore;
    }

    public Map<String, String> getDetails() {
        return _details;
    }

    public void setDetails(final Map<String, String> details) {
        _details = details;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
