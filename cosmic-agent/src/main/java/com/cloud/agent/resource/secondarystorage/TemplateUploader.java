package com.cloud.agent.resource.secondarystorage;

import com.cloud.legacymodel.storage.TemplateUploadStatus;
import com.cloud.legacymodel.storage.UploadCompleteCallback;

public interface TemplateUploader extends Runnable {

    /**
     * Initiate upload
     *
     * @param callback completion callback to be called after upload is complete
     * @return bytes uploaded
     */
    long upload(UploadCompleteCallback callback);

    /**
     * @return
     */
    boolean stopUpload();

    /**
     * @return percent of file uploaded
     */
    int getUploadPercent();

    /**
     * Get the status of the upload
     *
     * @return status of upload
     */
    TemplateUploadStatus getStatus();

    void setStatus(TemplateUploadStatus status);

    /**
     * Get time taken to upload so far
     *
     * @return time in seconds taken to upload
     */
    long getUploadTime();

    /**
     * Get bytes uploaded
     *
     * @return bytes uploaded so far
     */
    long getUploadedBytes();

    /**
     * Get the error if any
     *
     * @return error string if any
     */
    String getUploadError();

    void setUploadError(String string);

    /**
     * Get local path of the uploaded file
     *
     * @return local path of the file uploaded
     */
    String getUploadLocalPath();

    void setResume(boolean resume);
}
