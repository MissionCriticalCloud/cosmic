package com.cloud.legacymodel.storage;

/**
 * Callback used to notify completion of upload
 */
public interface UploadCompleteCallback {
    void uploadComplete(TemplateUploadStatus status);
}
