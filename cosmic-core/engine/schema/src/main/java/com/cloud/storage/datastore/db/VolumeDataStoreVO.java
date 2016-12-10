package com.cloud.storage.datastore.db;

import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.fsm.StateObject;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObjectInStore;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.State;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Join table for image_data_store and volumes
 */
@Entity
@Table(name = "volume_store_ref")
public class VolumeDataStoreVO implements StateObject<ObjectInDataStoreStateMachine.State>, DataObjectInStore {
    private static final Logger s_logger = LoggerFactory.getLogger(VolumeDataStoreVO.class);
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
    @Column(name = "ref_cnt")
    Long refCnt = 0L;
    @Column(name = "store_id")
    private long dataStoreId;
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
    @Column(name = "download_url", length = 2048)
    private String extractUrl;
    @Column(name = "download_url_created")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date extractUrlCreated = null;

    public VolumeDataStoreVO(final long hostId, final long volumeId) {
        super();
        dataStoreId = hostId;
        this.volumeId = volumeId;
        state = ObjectInDataStoreStateMachine.State.Allocated;
        refCnt = 0L;
    }

    public VolumeDataStoreVO(final long hostId, final long volumeId, final Date lastUpdated, final int downloadPercent, final Status downloadState, final String
            localDownloadPath, final String errorString,
                             final String jobId, final String installPath, final String downloadUrl, final String checksum) {
        // super();
        dataStoreId = hostId;
        this.volumeId = volumeId;
        // this.zoneId = zoneId;
        this.lastUpdated = lastUpdated;
        this.downloadPercent = downloadPercent;
        this.downloadState = downloadState;
        this.localDownloadPath = localDownloadPath;
        this.errorString = errorString;
        this.jobId = jobId;
        this.installPath = installPath;
        setDownloadUrl(downloadUrl);
        this.checksum = checksum;
        refCnt = 0L;
    }

    public VolumeDataStoreVO() {
        refCnt = 0L;
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
        return getVolumeId();
    }

    @Override
    public long getDataStoreId() {
        return dataStoreId;
    }

    public void setDataStoreId(final long storeId) {
        dataStoreId = storeId;
    }

    @Override
    public State getObjectInStoreState() {
        return state;
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
        localDownloadPath = localPath;
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
        final Long hid = new Long(dataStoreId);
        return tid.hashCode() + hid.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof VolumeDataStoreVO) {
            final VolumeDataStoreVO other = (VolumeDataStoreVO) obj;
            return (volumeId == other.getVolumeId() && dataStoreId == other.getDataStoreId());
        }
        return false;
    }

    @Override
    public String toString() {
        return new StringBuilder("VolumeDataStore[").append(id).append("-").append(volumeId).append("-").append(dataStoreId).append(installPath).append("]").toString();
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

    public long getVolumeSize() {
        return -1;
    }

    public long getUpdatedCount() {
        return updatedCount;
    }

    public void incrUpdatedCount() {
        updatedCount++;
    }

    public void decrUpdatedCount() {
        updatedCount--;
    }

    public Date getUpdated() {
        return updated;
    }

    @Override
    public ObjectInDataStoreStateMachine.State getState() {
        // TODO Auto-generated method stub
        return state;
    }

    public void setState(final ObjectInDataStoreStateMachine.State state) {
        this.state = state;
    }

    public Long getRefCnt() {
        return refCnt;
    }

    public void setRefCnt(final Long refCnt) {
        this.refCnt = refCnt;
    }

    public void incrRefCnt() {
        refCnt++;
    }

    public void decrRefCnt() {
        if (refCnt > 0) {
            refCnt--;
        } else {
            s_logger.warn("We should not try to decrement a zero reference count even though our code has guarded");
        }
    }

    public String getExtractUrl() {
        return extractUrl;
    }

    public void setExtractUrl(final String extractUrl) {
        this.extractUrl = extractUrl;
    }

    public Date getExtractUrlCreated() {
        return extractUrlCreated;
    }

    public void setExtractUrlCreated(final Date extractUrlCreated) {
        this.extractUrlCreated = extractUrlCreated;
    }
}
