//

//

package org.apache.cloudstack.storage.to;

import com.cloud.agent.api.to.DataObjectType;
import com.cloud.agent.api.to.DataStoreTO;
import com.cloud.agent.api.to.DataTO;
import com.cloud.hypervisor.Hypervisor;
import com.cloud.offering.DiskOffering.DiskCacheMode;
import com.cloud.storage.Storage;
import com.cloud.storage.Volume;
import org.apache.cloudstack.engine.subsystem.api.storage.VolumeInfo;

public class VolumeObjectTO implements DataTO {
    private String uuid;
    private Volume.Type volumeType;
    private DataStoreTO dataStore;
    private String name;
    private Long size;
    private String path;
    private Long volumeId;
    private String vmName;
    private long accountId;
    private String chainInfo;
    private Storage.ImageFormat format;
    private Storage.ProvisioningType provisioningType;
    private long id;

    private Long deviceId;
    private Long bytesReadRate;
    private Long bytesWriteRate;
    private Long iopsReadRate;
    private Long iopsWriteRate;
    private DiskCacheMode cacheMode;
    private Hypervisor.HypervisorType hypervisorType;

    public VolumeObjectTO() {

    }

    public VolumeObjectTO(final VolumeInfo volume) {
        uuid = volume.getUuid();
        path = volume.getPath();
        accountId = volume.getAccountId();
        if (volume.getDataStore() != null) {
            dataStore = volume.getDataStore().getTO();
        } else {
            dataStore = null;
        }
        vmName = volume.getAttachedVmName();
        size = volume.getSize();
        setVolumeId(volume.getId());
        chainInfo = volume.getChainInfo();
        volumeType = volume.getVolumeType();
        name = volume.getName();
        setId(volume.getId());
        format = volume.getFormat();
        provisioningType = volume.getProvisioningType();
        bytesReadRate = volume.getBytesReadRate();
        bytesWriteRate = volume.getBytesWriteRate();
        iopsReadRate = volume.getIopsReadRate();
        iopsWriteRate = volume.getIopsWriteRate();
        cacheMode = volume.getCacheMode();
        hypervisorType = volume.getHypervisorType();
        setDeviceId(volume.getDeviceId());
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public Volume.Type getVolumeType() {
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
    public Hypervisor.HypervisorType getHypervisorType() {
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

    public Storage.ImageFormat getFormat() {
        return format;
    }

    public void setFormat(final Storage.ImageFormat format) {
        this.format = format;
    }

    public Storage.ProvisioningType getProvisioningType() {
        return provisioningType;
    }

    public void setProvisioningType(final Storage.ProvisioningType provisioningType) {
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
