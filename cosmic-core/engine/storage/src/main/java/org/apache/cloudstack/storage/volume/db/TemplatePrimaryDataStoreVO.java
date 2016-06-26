package org.apache.cloudstack.storage.volume.db;

import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.fsm.StateObject;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine;

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

@Entity
@Table(name = "template_spool_ref")
public class TemplatePrimaryDataStoreVO implements StateObject<ObjectInDataStoreStateMachine.State> {
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

    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    ObjectInDataStoreStateMachine.State state;
    @Column(name = "pool_id")
    private long poolId;

    public TemplatePrimaryDataStoreVO(final long poolId, final long templateId) {
        super();
        this.poolId = poolId;
        this.templateId = templateId;
        this.downloadState = Status.NOT_DOWNLOADED;
        this.state = ObjectInDataStoreStateMachine.State.Allocated;
        this.markedForGC = false;
    }

    public TemplatePrimaryDataStoreVO(final long poolId, final long templateId, final Date lastUpdated, final int downloadPercent, final Status downloadState, final String
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

    protected TemplatePrimaryDataStoreVO() {

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

    public String getInstallPath() {
        return installPath;
    }

    public void setInstallPath(final String installPath) {
        this.installPath = installPath;
    }

    public long getTemplateSize() {
        return templateSize;
    }

    public void setTemplateSize(final long templateSize) {
        this.templateSize = templateSize;
    }

    public void setpoolId(final long poolId) {
        this.poolId = poolId;
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
        if (obj instanceof TemplatePrimaryDataStoreVO) {
            final TemplatePrimaryDataStoreVO other = (TemplatePrimaryDataStoreVO) obj;
            return (this.templateId == other.getTemplateId() && this.poolId == other.getPoolId());
        }
        return false;
    }

    public long getPoolId() {
        return poolId;
    }

    public long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(final long templateId) {
        this.templateId = templateId;
    }

    @Override
    public String toString() {
        return new StringBuilder("TmplPool[").append(id)
                                             .append("-")
                                             .append(templateId)
                                             .append("-")
                                             .append("poolId")
                                             .append("-")
                                             .append(installPath)
                                             .append("]")
                                             .toString();
    }

    @Override
    public ObjectInDataStoreStateMachine.State getState() {
        return this.state;
    }
}
