package com.cloud.storage;

import com.cloud.utils.db.GenericDaoBase;
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

/**
 * Join table for storage pools and templates
 */
@Entity
@Table(name = "template_spool_ref")
public class VMTemplateStoragePoolVO implements VMTemplateStorageResourceAssoc, DataObjectInStore {
    @Column(name = "update_count", updatable = true, nullable = false)
    protected long updatedCount;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @Column(name = "template_id")
    long templateId;

    @Column(name = GenericDaoBase.CREATED_COLUMN)
    Date created = null;

    @Column(name = "last_updated")
    @Temporal(value = TemporalType.TIMESTAMP)
    Date lastUpdated = null;

    @Column(name = "download_pct")
    int downloadPercent;

    @Column(name = "download_state")
    @Enumerated(EnumType.STRING)
    Status downloadState;

    @Column(name = "local_path")
    String localDownloadPath;

    @Column(name = "error_str")
    String errorString;

    @Column(name = "job_id")
    String jobId;

    @Column(name = "install_path")
    String installPath;

    @Column(name = "template_size")
    long templateSize;

    @Column(name = "marked_for_gc")
    boolean markedForGC;
    @Column(name = "updated")
    @Temporal(value = TemporalType.TIMESTAMP)
    Date updated;
    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    ObjectInDataStoreStateMachine.State state;
    @Column(name = "pool_id")
    private long poolId;

    public VMTemplateStoragePoolVO(final long poolId, final long templateId) {
        super();
        this.poolId = poolId;
        this.templateId = templateId;
        this.downloadState = Status.NOT_DOWNLOADED;
        this.state = ObjectInDataStoreStateMachine.State.Allocated;
        this.markedForGC = false;
    }

    public VMTemplateStoragePoolVO(final long poolId, final long templateId, final Date lastUpdated, final int downloadPercent, final Status downloadState, final String
            localDownloadPath,
                                   final String errorString, final String jobId, final String installPath, final long templateSize) {
        super();
        this.poolId = poolId;
        this.templateId = templateId;
        this.lastUpdated = lastUpdated;
        this.downloadPercent = downloadPercent;
        this.downloadState = downloadState;
        this.localDownloadPath = localDownloadPath;
        this.errorString = errorString;
        this.jobId = jobId;
        this.installPath = installPath;
        this.templateSize = templateSize;
    }

    protected VMTemplateStoragePoolVO() {

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
    public long getTemplateId() {
        return templateId;
    }

    @Override
    public void setTemplateId(final long templateId) {
        this.templateId = templateId;
    }

    @Override
    public int getDownloadPercent() {
        return downloadPercent;
    }

    @Override
    public void setDownloadPercent(final int downloadPercent) {
        this.downloadPercent = downloadPercent;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public Date getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public void setLastUpdated(final Date date) {
        lastUpdated = date;
    }

    @Override
    public Status getDownloadState() {
        return downloadState;
    }

    @Override
    public void setDownloadState(final Status downloadState) {
        this.downloadState = downloadState;
    }

    @Override
    public String getLocalDownloadPath() {
        return localDownloadPath;
    }

    @Override
    public void setLocalDownloadPath(final String localPath) {
        this.localDownloadPath = localPath;
    }

    @Override
    public String getErrorString() {
        return errorString;
    }

    @Override
    public void setErrorString(final String errorString) {
        this.errorString = errorString;
    }

    @Override
    public String getJobId() {
        return jobId;
    }

    @Override
    public void setJobId(final String jobId) {
        this.jobId = jobId;
    }

    @Override
    public long getTemplateSize() {
        return templateSize;
    }

    public void setTemplateSize(final long templateSize) {
        this.templateSize = templateSize;
    }

    public void setpoolId(final long poolId) {
        this.poolId = poolId;
    }

    @Override
    public long getId() {
        return id;
    }

    public boolean getMarkedForGC() {
        return markedForGC;
    }

    public void setMarkedForGC(final boolean markedForGC) {
        this.markedForGC = markedForGC;
    }

    @Override
    public int hashCode() {
        final Long tid = new Long(templateId);
        final Long hid = new Long(poolId);
        return tid.hashCode() + hid.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof VMTemplateStoragePoolVO) {
            final VMTemplateStoragePoolVO other = (VMTemplateStoragePoolVO) obj;
            return (this.templateId == other.getTemplateId() && this.poolId == other.getPoolId());
        }
        return false;
    }

    public long getPoolId() {
        return poolId;
    }

    @Override
    public String toString() {
        return new StringBuilder("TmplPool[").append(id).append("-").append(templateId).append("-").append(poolId).append("-").append(installPath).append("]").toString();
    }

    @Override
    public State getState() {
        return this.state;
    }

    //TODO: this should be revisited post-4.2 to completely use state transition machine
    public void setState(final ObjectInDataStoreStateMachine.State state) {
        this.state = state;
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
    public long getObjectId() {
        return this.getTemplateId();
    }

    @Override
    public long getDataStoreId() {
        return this.getPoolId();
    }

    @Override
    public State getObjectInStoreState() {
        return this.state;
    }
}
