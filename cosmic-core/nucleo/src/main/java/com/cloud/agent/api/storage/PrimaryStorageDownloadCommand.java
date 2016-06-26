//

//

package com.cloud.agent.api.storage;

import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.StoragePool;

/**
 *
 */
public class PrimaryStorageDownloadCommand extends AbstractDownloadCommand {
    String localPath;
    String poolUuid;
    long poolId;

    StorageFilerTO primaryPool;

    String secondaryStorageUrl;
    String primaryStorageUrl;

    protected PrimaryStorageDownloadCommand() {
    }

    public PrimaryStorageDownloadCommand(final String name, final String url, final ImageFormat format, final long accountId, final StoragePool pool, final int wait) {
        super(name, url, format, accountId);
        poolId = pool.getId();
        poolUuid = pool.getUuid();
        primaryPool = new StorageFilerTO(pool);
        setWait(wait);
    }

    public String getPoolUuid() {
        return poolUuid;
    }

    public long getPoolId() {
        return poolId;
    }

    public StorageFilerTO getPool() {
        return primaryPool;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(final String path) {
        localPath = path;
    }

    public String getSecondaryStorageUrl() {
        return secondaryStorageUrl;
    }

    public void setSecondaryStorageUrl(final String url) {
        secondaryStorageUrl = url;
    }

    public String getPrimaryStorageUrl() {
        return primaryStorageUrl;
    }

    public void setPrimaryStorageUrl(final String url) {
        primaryStorageUrl = url;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }
}
