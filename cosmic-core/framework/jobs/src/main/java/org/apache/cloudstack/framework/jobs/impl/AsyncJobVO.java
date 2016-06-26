package org.apache.cloudstack.framework.jobs.impl;

import com.cloud.utils.UuidUtils;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.framework.jobs.AsyncJob;
import org.apache.cloudstack.jobs.JobInfo;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "async_job")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "job_type", discriminatorType = DiscriminatorType.STRING, length = 32)
public class AsyncJobVO implements AsyncJob, JobInfo {

    public static final String JOB_DISPATCHER_PSEUDO = "pseudoJobDispatcher";
    public static final String PSEUDO_JOB_INSTANCE_TYPE = "Thread";
    @Column(name = "job_type", length = 32)
    protected String type;
    @Column(name = "job_dispatcher", length = 64)
    protected String dispatcher;
    @Column(name = "job_pending_signals")
    protected int pendingSignals;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "user_id")
    private long userId;

    @Column(name = "account_id")
    private long accountId;

    @Column(name = "job_cmd")
    private String cmd;

    @Column(name = "job_cmd_ver")
    private int cmdVersion;

    @Column(name = "related")
    private String related;

    @Column(name = "job_cmd_info", length = 65535)
    private String cmdInfo;

    @Column(name = "job_status")
    @Enumerated(value = EnumType.ORDINAL)
    private Status status;

    @Column(name = "job_process_status")
    private int processStatus;

    @Column(name = "job_result_code")
    private int resultCode;

    @Column(name = "job_result", length = 65535)
    private String result;

    @Column(name = "instance_type", length = 64)
    private String instanceType;

    @Column(name = "instance_id", length = 64)
    private Long instanceId;

    @Column(name = "job_init_msid")
    private Long initMsid;

    @Column(name = "job_complete_msid")
    private Long completeMsid;

    @Column(name = "job_executing_msid")
    private Long executingMsid;

    @Column(name = GenericDao.CREATED_COLUMN)
    private Date created;

    @Column(name = "last_updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;

    @Column(name = "last_polled")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastPolled;

    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;

    @Column(name = "uuid")
    private String uuid;

    @Transient
    private SyncQueueItem syncSource = null;

    public AsyncJobVO() {
        uuid = UUID.randomUUID().toString();
        related = "";
        status = Status.IN_PROGRESS;
    }

    public AsyncJobVO(final String related, final long userId, final long accountId, final String cmd, final String cmdInfo, final Long instanceId, final String instanceType,
                      final String injectedUuid) {
        this.userId = userId;
        this.accountId = accountId;
        this.cmd = cmd;
        this.cmdInfo = cmdInfo;
        uuid = (injectedUuid == null ? UUID.randomUUID().toString() : injectedUuid);
        this.related = related;
        this.instanceId = instanceId;
        this.instanceType = instanceType;
        status = Status.IN_PROGRESS;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    @Override
    public String getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(final String dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public int getPendingSignals() {
        return pendingSignals;
    }

    public void setPendingSignals(final int signals) {
        pendingSignals = signals;
    }

    @Override
    public long getUserId() {
        return userId;
    }

    public void setUserId(final long userId) {
        this.userId = userId;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(final long accountId) {
        this.accountId = accountId;
    }

    @Override
    public String getCmd() {
        return cmd;
    }

    public void setCmd(final String cmd) {
        this.cmd = cmd;
    }

    @Override
    public int getCmdVersion() {
        return cmdVersion;
    }

    public void setCmdVersion(final int version) {
        cmdVersion = version;
    }

    @Override
    public String getCmdInfo() {
        return cmdInfo;
    }

    public void setCmdInfo(final String cmdInfo) {
        this.cmdInfo = cmdInfo;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    @Override
    public int getProcessStatus() {
        return processStatus;
    }

    public void setProcessStatus(final int status) {
        processStatus = status;
    }

    @Override
    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(final int resultCode) {
        this.resultCode = resultCode;
    }

    @Override
    public String getResult() {
        return result;
    }

    public void setResult(final String result) {
        this.result = result;
    }

    @Override
    public Long getInitMsid() {
        return initMsid;
    }

    @Override
    public void setInitMsid(final Long initMsid) {
        this.initMsid = initMsid;
    }

    @Override
    public Long getExecutingMsid() {
        return executingMsid;
    }

    public void setExecutingMsid(final Long executingMsid) {
        this.executingMsid = executingMsid;
    }

    @Override
    public Long getCompleteMsid() {
        return completeMsid;
    }

    @Override
    public void setCompleteMsid(final Long completeMsid) {
        this.completeMsid = completeMsid;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    @Override
    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(final Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public Date getLastPolled() {
        return lastPolled;
    }

    public void setLastPolled(final Date lastPolled) {
        this.lastPolled = lastPolled;
    }

    @Override
    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(final String instanceType) {
        this.instanceType = instanceType;
    }

    @Override
    public Long getInstanceId() {
        return instanceId;
    }

    @Override
    public String getShortUuid() {
        return UuidUtils.first(uuid);
    }

    @Override
    public SyncQueueItem getSyncSource() {
        return syncSource;
    }

    @Override
    public void setSyncSource(final SyncQueueItem syncSource) {
        this.syncSource = syncSource;
    }

    @Override
    public String getRelated() {
        return related;
    }

    public void setRelated(final String related) {
        this.related = related;
    }

    public void setInstanceId(final Long instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("AsyncJobVO {id:").append(getId());
        sb.append(", userId: ").append(getUserId());
        sb.append(", accountId: ").append(getAccountId());
        sb.append(", instanceType: ").append(getInstanceType());
        sb.append(", instanceId: ").append(getInstanceId());
        sb.append(", cmd: ").append(getCmd());
        sb.append(", cmdInfo: ").append(getCmdInfo());
        sb.append(", cmdVersion: ").append(getCmdVersion());
        sb.append(", status: ").append(getStatus());
        sb.append(", processStatus: ").append(getProcessStatus());
        sb.append(", resultCode: ").append(getResultCode());
        sb.append(", result: ").append(getResult());
        sb.append(", initMsid: ").append(getInitMsid());
        sb.append(", completeMsid: ").append(getCompleteMsid());
        sb.append(", lastUpdated: ").append(getLastUpdated());
        sb.append(", lastPolled: ").append(getLastPolled());
        sb.append(", created: ").append(getCreated());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }
}
