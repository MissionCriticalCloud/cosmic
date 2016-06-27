package com.cloud.storage;

import org.apache.cloudstack.api.InternalIdentity;

import java.util.Date;

public interface VMTemplateStorageResourceAssoc extends InternalIdentity {
    String getInstallPath();

    void setInstallPath(String installPath);

    long getTemplateId();

    void setTemplateId(long templateId);

    int getDownloadPercent();

    void setDownloadPercent(int downloadPercent);

    Date getCreated();

    Date getLastUpdated();

    void setLastUpdated(Date date);

    Status getDownloadState();

    void setDownloadState(Status downloadState);

    String getLocalDownloadPath();

    void setLocalDownloadPath(String localPath);

    String getErrorString();

    void setErrorString(String errorString);

    String getJobId();

    void setJobId(String jobId);

    long getTemplateSize();

    public static enum Status {
        UNKNOWN, DOWNLOAD_ERROR, NOT_DOWNLOADED, DOWNLOAD_IN_PROGRESS, DOWNLOADED, ABANDONED, UPLOADED, NOT_UPLOADED, UPLOAD_ERROR, UPLOAD_IN_PROGRESS, CREATING, CREATED
    }
}
