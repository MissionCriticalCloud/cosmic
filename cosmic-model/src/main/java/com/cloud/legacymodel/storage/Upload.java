package com.cloud.legacymodel.storage;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;

import java.util.Date;

public interface Upload extends InternalIdentity, Identity {

    long getDataStoreId();

    Date getCreated();

    void setCreated(Date created);

    Date getLastUpdated();

    String getErrorString();

    String getJobId();

    int getUploadPercent();

    Status getUploadState();

    long getTypeId();

    Type getType();

    Mode getMode();

    String getUploadUrl();

    void setId(Long id);

    String getInstallPath();

    void setInstallPath(String installPath);

    enum Status {
        UNKNOWN,
        ABANDONED,
        UPLOADED,
        NOT_UPLOADED,
        UPLOAD_ERROR,
        UPLOAD_IN_PROGRESS,
        NOT_COPIED,
        COPY_IN_PROGRESS,
        COPY_ERROR,
        COPY_COMPLETE,
        DOWNLOAD_URL_CREATED,
        DOWNLOAD_URL_NOT_CREATED,
        ERROR
    }

    enum Type {
        VOLUME, TEMPLATE, ISO
    }

    enum Mode {
        FTP_UPLOAD, HTTP_DOWNLOAD
    }
}
