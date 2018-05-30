package com.cloud.legacymodel.storage;

import com.cloud.model.enumeration.StoragePoolType;

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

    public StoragePoolInfo() {
    }

    public StoragePoolInfo(final String uuid, final String host, final String hostPath, final String localPath, final StoragePoolType poolType, final long capacityBytes, final long availableBytes) {
        this.uuid = uuid;
        this.host = host;
        this.localPath = localPath;
        this.hostPath = hostPath;
        this.poolType = poolType;
        this.capacityBytes = capacityBytes;
        this.availableBytes = availableBytes;
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

    public void setHost(final String host) {
        this.host = host;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(final String localPath) {
        this.localPath = localPath;
    }

    public String getHostPath() {
        return hostPath;
    }

    public void setHostPath(final String hostPath) {
        this.hostPath = hostPath;
    }

    public StoragePoolType getPoolType() {
        return poolType;
    }

    public void setPoolType(final StoragePoolType poolType) {
        this.poolType = poolType;
    }

    public long getCapacityBytes() {
        return capacityBytes;
    }

    public void setCapacityBytes(final long capacityBytes) {
        this.capacityBytes = capacityBytes;
    }

    public long getAvailableBytes() {
        return availableBytes;
    }

    public void setAvailableBytes(final long availableBytes) {
        this.availableBytes = availableBytes;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(final Map<String, String> details) {
        this.details = details;
    }
}
