package com.cloud.storage;

import com.cloud.utils.NumbersUtil;
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
import java.util.UUID;

@Entity
@Table(name = "upload")
public class UploadVO implements Upload {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "host_id")
    private long dataStoreId;

    @Column(name = "type_id")
    private long typeId;

    @Column(name = GenericDaoBase.CREATED_COLUMN)
    private Date created = null;

    @Column(name = "last_updated")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date lastUpdated = null;

    @Column(name = "upload_pct")
    private int uploadPercent;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(name = "mode")
    @Enumerated(EnumType.STRING)
    private Mode mode = Mode.FTP_UPLOAD;

    @Column(name = "upload_state")
    @Enumerated(EnumType.STRING)
    private Status uploadState;

    @Column(name = "error_str")
    private String errorString;

    @Column(name = "job_id")
    private String jobId;

    @Column(name = "url", length = 2048)
    private String uploadUrl;

    @Column(name = "install_path")
    private String installPath;

    public UploadVO(final long hostId, final long templateId) {
        super();
        this.dataStoreId = hostId;
        this.typeId = templateId;
        this.uuid = UUID.randomUUID().toString();
    }

    public UploadVO(final long hostId, final long typeId, final Date lastUpdated, final Status uploadState, final Type type, final String uploadUrl, final Mode mode) {
        super();
        this.dataStoreId = hostId;
        this.typeId = typeId;
        this.lastUpdated = lastUpdated;
        this.uploadState = uploadState;
        this.mode = mode;
        this.type = type;
        this.uploadUrl = uploadUrl;
        this.uuid = UUID.randomUUID().toString();
    }

    public UploadVO(final long hostId, final long typeId, final Date lastUpdated, final Status uploadState, final int uploadPercent, final Type type, final Mode mode) {
        super();
        this.dataStoreId = hostId;
        this.typeId = typeId;
        this.lastUpdated = lastUpdated;
        this.uploadState = uploadState;
        this.uploadPercent = uploadPercent;
        this.type = type;
        this.mode = mode;
        this.uuid = UUID.randomUUID().toString();
    }

    protected UploadVO() {
    }

    public UploadVO(final Long uploadId) {
        this.id = uploadId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
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
    public String getUuid() {
        return uuid;
    }

    @Override
    public int hashCode() {
        return NumbersUtil.hash(id);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof UploadVO) {
            final UploadVO other = (UploadVO) obj;
            return (this.typeId == other.getTypeId() && this.dataStoreId == other.getDataStoreId() && this.type == other.getType());
        }
        return false;
    }

    @Override
    public long getDataStoreId() {
        return dataStoreId;
    }

    public void setDataStoreId(final long hostId) {
        this.dataStoreId = hostId;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public void setCreated(final Date created) {
        this.created = created;
    }

    @Override
    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(final Date date) {
        lastUpdated = date;
    }

    @Override
    public String getErrorString() {
        return errorString;
    }

    public void setErrorString(final String errorString) {
        this.errorString = errorString;
    }

    @Override
    public String getJobId() {
        return jobId;
    }

    public void setJobId(final String jobId) {
        this.jobId = jobId;
    }

    @Override
    public int getUploadPercent() {
        return uploadPercent;
    }

    public void setUploadPercent(final int uploadPercent) {
        this.uploadPercent = uploadPercent;
    }

    @Override
    public Status getUploadState() {
        return uploadState;
    }

    public void setUploadState(final Status uploadState) {
        this.uploadState = uploadState;
    }

    @Override
    public long getTypeId() {
        return typeId;
    }

    public void setTypeId(final long typeId) {
        this.typeId = typeId;
    }

    @Override
    public Type getType() {
        return type;
    }

    public void setType(final Type type) {
        this.type = type;
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    public void setMode(final Mode mode) {
        this.mode = mode;
    }

    @Override
    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(final String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }
}
