package com.cloud.agent.api.to;

import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.storage.StoragePool;
import com.cloud.storage.Volume;
import org.apache.cloudstack.api.InternalIdentity;

public class VolumeTO implements InternalIdentity {
    private long id;
    private String name;
    private String mountPoint;
    private String path;
    private long size;
    private Volume.Type type;
    private StoragePoolType storagePoolType;
    private String storagePoolUuid;
    private long deviceId;
    private String chainInfo;
    private String guestOsType;
    private Long bytesReadRate;
    private Long bytesWriteRate;
    private Long iopsReadRate;
    private Long iopsWriteRate;
    private String cacheMode;
    private Long chainSize;

    protected VolumeTO() {
    }

    public VolumeTO(final long id, final Volume.Type type, final StoragePoolType poolType, final String poolUuid, final String name, final String mountPoint, final String path,
                    final long size, final String chainInfo) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.size = size;
        this.type = type;
        this.storagePoolType = poolType;
        this.storagePoolUuid = poolUuid;
        this.mountPoint = mountPoint;
        this.chainInfo = chainInfo;
    }

    public VolumeTO(final long id, final Volume.Type type, final StoragePoolType poolType, final String poolUuid, final String name, final String mountPoint, final String path,
                    final long size, final String chainInfo,
                    final String guestOsType) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.size = size;
        this.type = type;
        this.storagePoolType = poolType;
        this.storagePoolUuid = poolUuid;
        this.mountPoint = mountPoint;
        this.chainInfo = chainInfo;
        this.guestOsType = guestOsType;
    }

    public VolumeTO(final Volume volume, final StoragePool pool) {
        this.id = volume.getId();
        this.name = volume.getName();
        this.path = volume.getPath();
        this.size = volume.getSize();
        this.type = volume.getVolumeType();
        this.storagePoolType = pool.getPoolType();
        this.storagePoolUuid = pool.getUuid();
        this.mountPoint = volume.getFolder();
        this.chainInfo = volume.getChainInfo();
        this.chainSize = volume.getVmSnapshotChainSize();
        if (volume.getDeviceId() != null) {
            this.deviceId = volume.getDeviceId();
        }
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(final long id) {
        this.deviceId = id;
    }

    @Override
    public long getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public Volume.Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getMountPoint() {
        return mountPoint;
    }

    public StoragePoolType getPoolType() {
        return storagePoolType;
    }

    public String getPoolUuid() {
        return storagePoolUuid;
    }

    public String getChainInfo() {
        return chainInfo;
    }

    public void setChainInfo(final String chainInfo) {
        this.chainInfo = chainInfo;
    }

    public String getOsType() {
        return guestOsType;
    }

    @Override
    public String toString() {
        return new StringBuilder("Vol[").append(id).append("|").append(type).append("|").append(path).append("|").append(size).append("]").toString();
    }

    public Long getBytesReadRate() {
        return bytesReadRate;
    }

    public void setBytesReadRate(final Long bytesReadRate) {
        this.bytesReadRate = bytesReadRate;
    }

    public Long getBytesWriteRate() {
        return bytesWriteRate;
    }

    public void setBytesWriteRate(final Long bytesWriteRate) {
        this.bytesWriteRate = bytesWriteRate;
    }

    public Long getIopsReadRate() {
        return iopsReadRate;
    }

    public void setIopsReadRate(final Long iopsReadRate) {
        this.iopsReadRate = iopsReadRate;
    }

    public Long getIopsWriteRate() {
        return iopsWriteRate;
    }

    public void setIopsWriteRate(final Long iopsWriteRate) {
        this.iopsWriteRate = iopsWriteRate;
    }

    public String getCacheMode() {
        return cacheMode;
    }

    public void setCacheMode(final String cacheMode) {
        this.cacheMode = cacheMode;
    }

    public Long getChainSize() {
        return chainSize;
    }

    public void setChainSize(final Long chainSize) {
        this.chainSize = chainSize;
    }
}
