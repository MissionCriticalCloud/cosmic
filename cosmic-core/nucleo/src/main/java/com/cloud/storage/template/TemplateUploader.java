//

//

package com.cloud.storage.template;

public interface TemplateUploader extends Runnable {

    /**
     * Initiate upload
     *
     * @param callback completion callback to be called after upload is complete
     * @return bytes uploaded
     */
    public long upload(UploadCompleteCallback callback);

    /**
     * @return
     */
    public boolean stopUpload();

    /**
     * @return percent of file uploaded
     */
    public int getUploadPercent();

    /**
     * Get the status of the upload
     *
     * @return status of upload
     */
    public TemplateUploader.Status getStatus();

    public void setStatus(TemplateUploader.Status status);

    /**
     * Get time taken to upload so far
     *
     * @return time in seconds taken to upload
     */
    public long getUploadTime();

    /**
     * Get bytes uploaded
     *
     * @return bytes uploaded so far
     */
    public long getUploadedBytes();

    /**
     * Get the error if any
     *
     * @return error string if any
     */
    public String getUploadError();

    public void setUploadError(String string);

    /**
     * Get local path of the uploaded file
     *
     * @return local path of the file uploaded
     */
    public String getUploadLocalPath();

    public void setResume(boolean resume);

    public static enum Status {
        UNKNOWN, NOT_STARTED, IN_PROGRESS, ABORTED, UNRECOVERABLE_ERROR, RECOVERABLE_ERROR, UPLOAD_FINISHED, POST_UPLOAD_FINISHED
    }

    /**
     * Callback used to notify completion of upload
     */
    public interface UploadCompleteCallback {
        void uploadComplete(Status status);
    }
}
