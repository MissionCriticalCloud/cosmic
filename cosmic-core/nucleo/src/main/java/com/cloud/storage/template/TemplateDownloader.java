//

//

package com.cloud.storage.template;

public interface TemplateDownloader extends Runnable {

    long DEFAULT_MAX_TEMPLATE_SIZE_IN_BYTES = 50L * 1024L * 1024L * 1024L;

    /**
     * Initiate download, resuming a previous one if required
     *
     * @param resume   resume if necessary
     * @param callback completion callback to be called after download is complete
     * @return bytes downloaded
     */
    long download(boolean resume, DownloadCompleteCallback callback);

    /**
     * @return
     */
    boolean stopDownload();

    /**
     * @return percent of file downloaded
     */
    int getDownloadPercent();

    /**
     * Get the status of the download
     *
     * @return status of download
     */
    TemplateDownloader.Status getStatus();

    void setStatus(TemplateDownloader.Status status);

    /**
     * Get time taken to download so far
     *
     * @return time in seconds taken to download
     */
    long getDownloadTime();

    /**
     * Get bytes downloaded
     *
     * @return bytes downloaded so far
     */
    long getDownloadedBytes();

    /**
     * Get the error if any
     *
     * @return error string if any
     */
    String getDownloadError();

    void setDownloadError(String string);

    /**
     * Get local path of the downloaded file
     *
     * @return local path of the file downloaded
     */
    String getDownloadLocalPath();

    void setResume(boolean resume);

    boolean isInited();

    long getMaxTemplateSizeInBytes();

    enum Status {
        UNKNOWN, NOT_STARTED, IN_PROGRESS, ABORTED, UNRECOVERABLE_ERROR, RECOVERABLE_ERROR, DOWNLOAD_FINISHED, POST_DOWNLOAD_FINISHED
    }

    /**
     * Callback used to notify completion of download
     */
    interface DownloadCompleteCallback {
        void downloadComplete(Status status);
    }
}
