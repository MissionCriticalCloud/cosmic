package com.cloud.legacymodel.storage;

public enum TemplateDownloadStatus {
    UNKNOWN,
    NOT_STARTED,
    IN_PROGRESS,
    ABORTED,
    UNRECOVERABLE_ERROR,
    RECOVERABLE_ERROR,
    DOWNLOAD_FINISHED,
    POST_DOWNLOAD_FINISHED
}
