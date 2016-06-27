package com.cloud.agent.api;

import com.cloud.storage.Storage.StoragePoolType;

import java.util.Map;

public class StoragePoolInfo {
    String uuid;
    String host;
    String localPath;
    String hostPath;
    StoragePoolType poolType;
    long capacityBytes;
    long availableBytes;
    Map<String, String> details;

    protected StoragePoolInfo() {
        super();
    }

    public StoragePoolInfo(final String uuid, final String host, final String hostPath, final String localPath, final StoragePoolType poolType, final long capacityBytes, final
    long availableBytes,
                           final Map<String, String> details) {
        this(uuid, host, hostPath, localPath, poolType, capacityBytes, availableBytes);
        this.details = details;
    }

    public StoragePoolInfo(final String uuid, final String host, final String hostPath, final String localPath, final StoragePoolType poolType, final long capacityBytes, final
    long availableBytes) {
        super();
        this.uuid = uuid;
        this.host = host;
        this.localPath = localPath;
        this.hostPath = hostPath;
        this.poolType = poolType;
        this.capacityBytes = capacityBytes;
        this.availableBytes = availableBytes;
    }

    public long getCapacityBytes() {
        return capacityBytes;
    }

    public long getAvailableBytes() {
        return availableBytes;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public String getHost() {
        return host;
    }

    public String getLocalPath() {
        return localPath;
    }

    public String getHostPath() {
        return hostPath;
    }

    public StoragePoolType getPoolType() {
        return poolType;
    }

    public Map<String, String> getDetails() {
        return details;
    }
}
