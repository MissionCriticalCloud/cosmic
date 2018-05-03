package com.cloud.legacymodel.communication.command;

import com.cloud.legacymodel.communication.LogLevel;
import com.cloud.legacymodel.communication.LogLevel.Level;
import com.cloud.legacymodel.to.DataStoreTO;
import com.cloud.model.enumeration.StoragePoolType;

@LogLevel(Level.Trace)
public class GetStorageStatsCommand extends Command {
    private String id;
    private String localPath;
    private StoragePoolType pooltype;
    private String secUrl;
    private DataStoreTO store;

    public GetStorageStatsCommand() {
    }

    public GetStorageStatsCommand(final DataStoreTO store) {
        this.store = store;
    }

    public GetStorageStatsCommand(final String secUrl) {
        this.secUrl = secUrl;
    }

    public GetStorageStatsCommand(final String id, final StoragePoolType pooltype) {
        this.id = id;
        this.pooltype = pooltype;
    }

    public GetStorageStatsCommand(final String id, final StoragePoolType pooltype, final String localPath) {
        this.id = id;
        this.pooltype = pooltype;
        this.localPath = localPath;
    }

    public String getSecUrl() {
        return secUrl;
    }

    public void setSecUrl(final String secUrl) {
        this.secUrl = secUrl;
    }

    public StoragePoolType getPooltype() {
        return pooltype;
    }

    public void setPooltype(final StoragePoolType pooltype) {
        this.pooltype = pooltype;
    }

    public String getStorageId() {
        return this.id;
    }

    public String getLocalPath() {
        return this.localPath;
    }

    public DataStoreTO getStore() {
        return this.store;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
