package com.cloud.legacymodel.storage;

/**
 * Callback used to notify completion of download
 */
public interface DownloadCompleteCallback {
    void downloadComplete(TemplateDownloadStatus status);
}
