package com.cloud.vm;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.offering.DiskOffering;
import com.cloud.storage.Storage.ProvisioningType;
import com.cloud.storage.Volume;

/**
 * DiskProfile describes a disk and what functionality is required from it.
 * and resources to allocate and create disks. There object is immutable once
 */
public class DiskProfile {
    private long size;
    private String[] tags;
    private Volume.Type type;
    private String name;
    private boolean useLocalStorage;
    private boolean recreatable;
    private long diskOfferingId;
    private Long templateId;
    private long volumeId;
    private String path;
    private ProvisioningType provisioningType;
    private Long bytesReadRate;
    private Long bytesWriteRate;
    private Long iopsReadRate;
    private Long iopsWriteRate;
    private String cacheMode;

    private HypervisorType hyperType;

    protected DiskProfile() {
    }

    public DiskProfile(final Volume vol, final DiskOffering offering, final HypervisorType hyperType) {
        this(vol.getId(),
                vol.getVolumeType(),
                vol.getName(),
                offering.getId(),
                vol.getSize(),
                offering.getTagsArray(),
                offering.getUseLocalStorage(),
                offering.isCustomized(),
                null);
        this.hyperType = hyperType;
    }

    public DiskProfile(final long volumeId, final Volume.Type type, final String name, final long diskOfferingId, final long size, final String[] tags, final boolean
            useLocalStorage, final boolean recreatable,
                       final Long templateId) {
        this.type = type;
        this.name = name;
        this.size = size;
        this.tags = tags;
        this.useLocalStorage = useLocalStorage;
        this.recreatable = recreatable;
        this.diskOfferingId = diskOfferingId;
        this.templateId = templateId;
        this.volumeId = volumeId;
    }

    public DiskProfile(final DiskProfile dp) {

    }

    /**
     * @return size of the disk requested in bytes.
     */
    public long getSize() {
        return size;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    /**
     * @return id of the volume backing up this disk characteristics
     */
    public long getVolumeId() {
        return volumeId;
    }

    /**
     * @return Unique name for the disk.
     */
    public String getName() {
        return name;
    }

    /**
     * @return tags for the disk. This can be used to match it to different storage pools.
     */
    public String[] getTags() {
        return tags;
    }

    /**
     * @return type of volume.
     */
    public Volume.Type getType() {
        return type;
    }

    /**
     * @return Does this volume require local storage?
     */
    public boolean useLocalStorage() {
        return useLocalStorage;
    }

    public void setUseLocalStorage(final boolean useLocalStorage) {
        this.useLocalStorage = useLocalStorage;
    }

    /**
     * @return Is this volume recreatable? A volume is recreatable if the disk's content can be
     * reconstructed from the template.
     */
    public boolean isRecreatable() {
        return recreatable;
    }

    /**
     * @return template id the disk is based on. Can be null if it is not based on any templates.
     */
    public Long getTemplateId() {
        return templateId;
    }

    /**
     * @return disk offering id that the disk is based on.
     */
    public long getDiskOfferingId() {
        return diskOfferingId;
    }

    @Override
    public String toString() {
        return new StringBuilder("DskChr[").append(type).append("|").append(size).append("|").append("]").toString();
    }

    public void setHyperType(final HypervisorType hyperType) {
        this.hyperType = hyperType;
    }

    public HypervisorType getHypervisorType() {
        return this.hyperType;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public ProvisioningType getProvisioningType() {
        return this.provisioningType;
    }

    public void setProvisioningType(final ProvisioningType provisioningType) {
        this.provisioningType = provisioningType;
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
}
