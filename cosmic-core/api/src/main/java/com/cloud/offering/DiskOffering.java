package com.cloud.offering;

import com.cloud.acl.InfrastructureEntity;
import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;
import com.cloud.storage.Storage.ProvisioningType;

import java.util.Date;

/**
 * Represents a disk offering that specifies what the end user needs in
 * the disk offering.
 */
public interface DiskOffering extends InfrastructureEntity, Identity, InternalIdentity {
    State getState();

    String getUniqueName();

    boolean getUseLocalStorage();

    Long getDomainId();

    String getName();

    boolean getSystemUse();

    String getDisplayText();

    public ProvisioningType getProvisioningType();

    public String getTags();

    public String[] getTagsArray();

    Date getCreated();

    boolean isCustomized();

    long getDiskSize();

    void setDiskSize(long diskSize);

    boolean isRecreatable();

    Long getBytesReadRate();

    void setBytesReadRate(Long bytesReadRate);

    Long getBytesWriteRate();

    void setBytesWriteRate(Long bytesWriteRate);

    Long getIopsReadRate();

    void setIopsReadRate(Long iopsReadRate);

    Long getIopsWriteRate();

    void setIopsWriteRate(Long iopsWriteRate);

    Long getIopsTotalRate();

    void setIopsTotalRate(Long iopsTotalRate);

    Integer getHypervisorSnapshotReserve();

    void setHypervisorSnapshotReserve(Integer hypervisorSnapshotReserve);

    DiskCacheMode getCacheMode();

    void setCacheMode(DiskCacheMode cacheMode);

    Type getType();

    enum State {
        Inactive, Active,
    }

    public enum Type {
        Disk, Service
    }

    public enum DiskCacheMode {
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
