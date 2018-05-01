package com.cloud.storage;

import com.cloud.engine.subsystem.api.storage.DataObjectInStore;
import com.cloud.engine.subsystem.api.storage.ObjectInDataStoreStateMachine;
import com.cloud.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.State;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import com.cloud.utils.db.GenericDaoBase;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Join table for storage hosts and volumes
 */
@Entity
@Table(name = "volume_host_ref")
public class VolumeHostVO implements InternalIdentity, DataObjectInStore {
    @Column(name = "update_count", updatable = true, nullable = false)
    protected long updatedCount;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "destroyed")
    boolean destroyed = false;
    @Column(name = "updated")
    @Temporal(value = TemporalType.TIMESTAMP)
    Date updated;
    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    ObjectInDataStoreStateMachine.State state;
    @Column(name = "host_id")
    private long hostId;
    @Column(name = "volume_id")
    private long volumeId;
    @Column(name = "zone_id")
    private long zoneId;
    @Column(name = GenericDaoBase.CREATED_COLUMN)
    private Date created = null;
    @Column(name = "last_updated")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date lastUpdated = null;
    @Column(name = "download_pct")
    private int downloadPercent;
    @Column(name = "size")
    private long size;
    @Column(name = "physical_size")
    private long physicalSize;
    @Column(name = "download_state")
    @Enumerated(EnumType.STRING)
    private Status downloadState;
    @Column(name = "checksum")
    private String checksum;
    @Column(name = "local_path")
    private String localDownloadPath;
    @Column(name = "error_str")
    private String errorString;
    @Column(name = "job_id")
    private String jobId;
    @Column(name = "install_path")
    private String installPath;
    @Column(name = "url", length = 2048)
    private String downloadUrl;
    @Column(name = "format")
    private Storage.ImageFormat format;

    public VolumeHostVO(final long hostId, final long volumeId) {
        super();
        this.hostId = hostId;
        this.volumeId = volumeId;
        this.state = ObjectInDataStoreStateMachine.State.Allocated;
    }

    public VolumeHostVO(final long hostId, final long volumeId, final long zoneId, final Date lastUpdated, final int downloadPercent, final Status downloadState, final String
            localDownloadPath,
                        final String errorString, final String jobId, final String installPath, final String downloadUrl, final String checksum, final ImageFormat format) {
        // super();
        this.hostId = hostId;
        this.volumeId = volumeId;
        this.zoneId = zoneId;
        this.lastUpdated = lastUpdated;
        this.downloadPercent = downloadPercent;
        this.downloadState = downloadState;
        this.localDownloadPath = localDownloadPath;
        this.errorString = errorString;
        this.jobId = jobId;
        this.installPath = installPath;
        this.setDownloadUrl(downloadUrl);
        this.checksum = checksum;
        this.format = format;
    }

    protected VolumeHostVO() {

    }

    public void setUpdatedCount(final long updatedCount) {
        this.updatedCount = updatedCount;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setUpdated(final Date updated) {
        this.updated = updated;
    }

    public void setState(final State state) {
        this.state = state;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    @Override
    public String getInstallPath() {
        return installPath;
    }

    @Override
    public void setInstallPath(final String installPath) {
        this.installPath = installPath;
    }

    @Override
    public long getObjectId() {
        return this.getVolumeId();
    }

    @Override
    public long getDataStoreId() {
        return this.getHostId();
    }

    @Override
    public State getObjectInStoreState() {
        return this.state;
    }

    public long getHostId() {
        return hostId;
    }

    public void setHostId(final long hostId) {
        this.hostId = hostId;
    }

    public long getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(final long volumeId) {
        this.volumeId = volumeId;
    }

    public long getZoneId() {
        return zoneId;
    }

    public void setZoneId(final long zoneId) {
        this.zoneId = zoneId;
    }

    public int getDownloadPercent() {
        return downloadPercent;
    }

    public void setDownloadPercent(final int downloadPercent) {
        this.downloadPercent = downloadPercent;
    }

    @Override
    public long getId() {
        return id;
    }

    public Date getCreated() {
        return created;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(final Date date) {
        lastUpdated = date;
    }

    public Status getDownloadState() {
        return downloadState;
    }

    public void setDownloadState(final Status downloadState) {
        this.downloadState = downloadState;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(final String checksum) {
        this.checksum = checksum;
    }

    public String getLocalDownloadPath() {
        return localDownloadPath;
    }

    public void setLocalDownloadPath(final String localPath) {
        this.localDownloadPath = localPath;
    }

    public String getErrorString() {
        return errorString;
    }

    public void setErrorString(final String errorString) {
        this.errorString = errorString;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(final String jobId) {
        this.jobId = jobId;
    }

    @Override
    public int hashCode() {
        final Long tid = new Long(volumeId);
        final Long hid = new Long(hostId);
        return tid.hashCode() + hid.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof VolumeHostVO) {
            final VolumeHostVO other = (VolumeHostVO) obj;
            return (this.volumeId == other.getVolumeId() && this.hostId == other.getHostId());
        }
        return false;
    }

    @Override
    public String toString() {
        return new StringBuilder("VolumeHost[").append(id).append("-").append(volumeId).append("-").append(hostId).append(installPath).append("]").toString();
    }

    public long getSize() {
        return size;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public long getPhysicalSize() {
        return physicalSize;
    }

    public void setPhysicalSize(final long physicalSize) {
        this.physicalSize = physicalSize;
    }

    public boolean getDestroyed() {
        return destroyed;
    }

    public void setDestroyed(final boolean destroyed) {
        this.destroyed = destroyed;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(final String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Storage.ImageFormat getFormat() {
        return format;
    }

    public void setFormat(final Storage.ImageFormat format) {
        this.format = format;
    }

    public long getVolumeSize() {
        return -1;
    }

    public long getUpdatedCount() {
        return this.updatedCount;
    }

    public void incrUpdatedCount() {
        this.updatedCount++;
    }

    public void decrUpdatedCount() {
        this.updatedCount--;
    }

    public Date getUpdated() {
        return updated;
    }

    @Override
    public ObjectInDataStoreStateMachine.State getState() {
        // TODO Auto-generated method stub
        return this.state;
    }
}
