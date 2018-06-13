package com.cloud.legacymodel.storage;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.model.enumeration.StorageProvisioningType;

import java.util.Date;

/**
 * Represents a disk offering that specifies what the end user needs in
 * the disk offering.
 */
public interface DiskOffering extends Identity, InternalIdentity {
    State getState();

    String getUniqueName();

    boolean getUseLocalStorage();

    Long getDomainId();

    String getName();

    boolean getSystemUse();

    String getDisplayText();

    StorageProvisioningType getProvisioningType();

    String getTags();

    String[] getTagsArray();

    Date getCreated();

    boolean isCustomized();

    long getDiskSize();

    void setDiskSize(long diskSize);

    void setCustomizedIops(Boolean customizedIops);

    Boolean isCustomizedIops();

    Long getMinIops();

    void setMinIops(Long minIops);

    Long getMaxIops();

    void setMaxIops(Long maxIops);

    boolean isRecreatable();

    Long getBytesReadRate();

    void setBytesReadRate(Long bytesReadRate);

    Long getBytesWriteRate();

    void setBytesWriteRate(Long bytesWriteRate);

    Long getIopsReadRate();

    void setIopsReadRate(Long iopsReadRate);

    Long getIopsWriteRate();

    void setIopsWriteRate(Long iopsWriteRate);

    Integer getHypervisorSnapshotReserve();

    void setHypervisorSnapshotReserve(Integer hypervisorSnapshotReserve);

    DiskCacheMode getCacheMode();

    void setCacheMode(DiskCacheMode cacheMode);

    Type getType();

    enum State {
        Inactive, Active,
    }

    enum Type {
        Disk, Service
    }

    enum DiskCacheMode {
        NONE("none"), WRITEBACK("writeback"), WRITETHROUGH("writethrough");

        private final String _diskCacheMode;

        DiskCacheMode(final String cacheMode) {
            _diskCacheMode = cacheMode;
        }

        @Override
        public String toString() {
            return _diskCacheMode;
        }
    }
}
