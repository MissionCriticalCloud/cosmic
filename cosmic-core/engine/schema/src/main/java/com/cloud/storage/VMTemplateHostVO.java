package com.cloud.storage;

import com.cloud.engine.subsystem.api.storage.DataObjectInStore;
import com.cloud.legacymodel.storage.ObjectInDataStoreStateMachine;
import com.cloud.legacymodel.storage.ObjectInDataStoreStateMachine.State;
import com.cloud.legacymodel.storage.VMTemplateStorageResourceAssoc;
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
 * Join table for storage hosts and templates
 */
@Entity
@Table(name = "template_host_ref")
public class VMTemplateHostVO implements VMTemplateStorageResourceAssoc, DataObjectInStore {
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
    @Column(name = "template_id")
    private long templateId;
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
    @Column(name = "is_copy")
    private boolean isCopy = false;

    public VMTemplateHostVO(final long hostId, final long templateId) {
        super();
        this.hostId = hostId;
        this.templateId = templateId;
        this.state = ObjectInDataStoreStateMachine.State.Allocated;
    }

    public VMTemplateHostVO(final long hostId, final long templateId, final Date lastUpdated, final int downloadPercent, final Status downloadState, final String
            localDownloadPath, final String errorString,
                            final String jobId, final String installPath, final String downloadUrl) {
        super();
        this.hostId = hostId;
        this.templateId = templateId;
        this.lastUpdated = lastUpdated;
        this.downloadPercent = downloadPercent;
        this.downloadState = downloadState;
        this.localDownloadPath = localDownloadPath;
        this.errorString = errorString;
        this.jobId = jobId;
        this.installPath = installPath;
        this.setDownloadUrl(downloadUrl);
    }

    protected VMTemplateHostVO() {

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
        return -1;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public int hashCode() {
        final Long tid = new Long(templateId);
        final Long hid = new Long(hostId);
        return tid.hashCode() + hid.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof VMTemplateHostVO) {
            final VMTemplateHostVO other = (VMTemplateHostVO) obj;
            return (this.templateId == other.getTemplateId() && this.hostId == other.getHostId());
        }
        return false;
    }

    public long getHostId() {
        return hostId;
    }

    public void setHostId(final long hostId) {
        this.hostId = hostId;
    }

    @Override
    public String toString() {
        return new StringBuilder("TmplHost[").append(id).append("-").append(templateId).append("-").append(hostId).append(installPath).append("]").toString();
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

    public boolean isCopy() {
        return isCopy;
    }

    public void setCopy(final boolean isCopy) {
        this.isCopy = isCopy;
    }

    @Override
    public ObjectInDataStoreStateMachine.State getState() {
        // TODO Auto-generated method stub
        return this.state;
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
        return this.getHostId();
    }

    @Override
    public State getObjectInStoreState() {
        return this.state;
    }
}
