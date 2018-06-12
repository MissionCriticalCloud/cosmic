package com.cloud.legacymodel.to;

import com.cloud.legacymodel.storage.DiskOffering.DiskCacheMode;
import com.cloud.model.enumeration.DataObjectType;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.model.enumeration.ImageFormat;
import com.cloud.model.enumeration.StorageProvisioningType;
import com.cloud.model.enumeration.VolumeType;

public class VolumeObjectTO implements DataTO {
    private String uuid;
    private VolumeType volumeType;
    private DataStoreTO dataStore;
    private String name;
    private Long size;
    private String path;
    private Long volumeId;
    private String vmName;
    private long accountId;
    private String chainInfo;
    private ImageFormat format;
    private StorageProvisioningType provisioningType;
    private long id;

    private Long deviceId;
    private Long bytesReadRate;
    private Long bytesWriteRate;
    private Long iopsReadRate;
    private Long iopsWriteRate;
    private DiskCacheMode cacheMode;
    private HypervisorType hypervisorType;

    public VolumeObjectTO() {

    }

    public VolumeObjectTO(final String uuid, final VolumeType volumeType, final DataStoreTO dataStore, final String name, final Long size, final String path, final Long volumeId, final String
            vmName, final long accountId, final String chainInfo, final ImageFormat format, final StorageProvisioningType provisioningType, final long id, final Long deviceId, final Long
                                  bytesReadRate, final Long bytesWriteRate, final Long iopsReadRate, final Long iopsWriteRate, final DiskCacheMode cacheMode, final HypervisorType hypervisorType) {
        this.uuid = uuid;
        this.volumeType = volumeType;
        this.dataStore = dataStore;
        this.name = name;
        this.size = size;
        this.path = path;
        this.volumeId = volumeId;
        this.vmName = vmName;
        this.accountId = accountId;
        this.chainInfo = chainInfo;
        this.format = format;
        this.provisioningType = provisioningType;
        this.id = id;
        this.deviceId = deviceId;
        this.bytesReadRate = bytesReadRate;
        this.bytesWriteRate = bytesWriteRate;
        this.iopsReadRate = iopsReadRate;
        this.iopsWriteRate = iopsWriteRate;
        this.cacheMode = cacheMode;
        this.hypervisorType = hypervisorType;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public VolumeType getVolumeType() {
        return volumeType;
    }

    public void setDataStore(final DataStoreTO store) {
        dataStore = store;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    @Override
    public DataObjectType getObjectType() {
        return DataObjectType.VOLUME;
    }

    @Override
    public DataStoreTO getDataStore() {
        return dataStore;
    }

    @Override
    public HypervisorType getHypervisorType() {
        return hypervisorType;
    }

    @Override
    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setDataStore(final PrimaryDataStoreTO dataStore) {
        this.dataStore = dataStore;
    }

    public Long getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(final Long volumeId) {
        this.volumeId = volumeId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(final long accountId) {
        this.accountId = accountId;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(final String vmName) {
        this.vmName = vmName;
    }

    public String getChainInfo() {
        return chainInfo;
    }

    public void setChainInfo(final String chainInfo) {
        this.chainInfo = chainInfo;
    }

    public ImageFormat getFormat() {
        return format;
    }

    public void setFormat(final ImageFormat format) {
        this.format = format;
    }

    public StorageProvisioningType getProvisioningType() {
        return provisioningType;
    }

    public void setProvisioningType(final StorageProvisioningType provisioningType) {
        this.provisioningType = provisioningType;
    }

    @Override
    public String toString() {
        return new StringBuilder("volumeTO[uuid=").append(uuid).append("|path=").append(path).append("|datastore=").append(dataStore).append("]").toString();
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

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(final Long deviceId) {
        this.deviceId = deviceId;
    }

    public DiskCacheMode getCacheMode() {
        return cacheMode;
    }

    public void setCacheMode(final DiskCacheMode cacheMode) {
        this.cacheMode = cacheMode;
    }
}
