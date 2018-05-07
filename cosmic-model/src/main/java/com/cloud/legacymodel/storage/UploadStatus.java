package com.cloud.legacymodel.storage;

public enum UploadStatus {
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
