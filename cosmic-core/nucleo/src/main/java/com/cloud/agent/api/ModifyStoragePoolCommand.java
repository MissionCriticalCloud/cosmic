//

//

package com.cloud.agent.api;

import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.storage.StoragePool;

import java.io.File;
import java.util.UUID;

public class ModifyStoragePoolCommand extends Command {

    public static final String LOCAL_PATH_PREFIX = "/mnt/";
    boolean add;
    StorageFilerTO pool;
    String localPath;
    String[] options;

    public ModifyStoragePoolCommand() {

    }

    public ModifyStoragePoolCommand(final boolean add, final StoragePool pool) {
        this(add, pool, LOCAL_PATH_PREFIX + File.separator + UUID.nameUUIDFromBytes((pool.getHostAddress() + pool.getPath()).getBytes()));
    }

    public ModifyStoragePoolCommand(final boolean add, final StoragePool pool, final String localPath) {
        this.add = add;
        this.pool = new StorageFilerTO(pool);
        this.localPath = localPath;
    }

    public StorageFilerTO getPool() {
        return pool;
    }

    public void setPool(final StoragePool pool) {
        this.pool = new StorageFilerTO(pool);
    }

    public boolean getAdd() {
        return add;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setOptions(final String[] options) {
        this.options = options;
    }
}
