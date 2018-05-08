package com.cloud.legacymodel.storage;

public enum TemplateUploadStatus {
    UNKNOWN,
    NOT_STARTED,
    IN_PROGRESS,
    ABORTED,
    UNRECOVERABLE_ERROR,
    RECOVERABLE_ERROR,
    UPLOAD_FINISHED,
    POST_UPLOAD_FINISHED
}
