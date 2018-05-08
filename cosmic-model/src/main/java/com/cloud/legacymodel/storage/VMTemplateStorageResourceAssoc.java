package com.cloud.legacymodel.storage;

import com.cloud.legacymodel.InternalIdentity;

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

    VMTemplateStatus getDownloadState();

    void setDownloadState(VMTemplateStatus downloadState);

    String getLocalDownloadPath();

    void setLocalDownloadPath(String localPath);

    String getErrorString();

    void setErrorString(String errorString);

    String getJobId();

    void setJobId(String jobId);

    long getTemplateSize();
}
