//

//

package com.cloud.agent.api.storage;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.storage.Upload;

import java.io.File;

public class UploadAnswer extends Answer {

    public Long templateSize = 0L;
    private String jobId;
    private int uploadPct;
    private String errorString;
    private Upload.Status uploadStatus;
    private String uploadPath;
    private String installPath;

    protected UploadAnswer() {

    }

    public UploadAnswer(final String jobId, final int uploadPct, final String errorString, final Upload.Status uploadStatus, final String fileSystemPath, final String
            installPath, final long templateSize) {
        super();
        this.jobId = jobId;
        this.uploadPct = uploadPct;
        this.errorString = errorString;
        this.details = errorString;
        this.uploadStatus = uploadStatus;
        this.uploadPath = fileSystemPath;
        this.installPath = fixPath(installPath);
        this.templateSize = templateSize;
    }

    private static String fixPath(String path) {
        if (path == null) {
            return path;
        }
        if (path.startsWith(File.separator)) {
            path = path.substring(File.separator.length());
        }
        if (path.endsWith(File.separator)) {
            path = path.substring(0, path.length() - File.separator.length());
        }
        return path;
    }

    public UploadAnswer(final String jobId, final int uploadPct, final Command command, final Upload.Status uploadStatus, final String fileSystemPath, final String installPath) {
        super(command);
        this.jobId = jobId;
        this.uploadPct = uploadPct;
        this.uploadStatus = uploadStatus;
        this.uploadPath = fileSystemPath;
        this.installPath = installPath;
    }

    public int getUploadPct() {
        return uploadPct;
    }

    public String getErrorString() {
        return errorString;
    }

    public void setErrorString(final String errorString) {
        this.errorString = errorString;
    }

    public String getUploadStatusString() {
        return uploadStatus.toString();
    }

    public Upload.Status getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(final Upload.Status uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    public String getUploadPath() {
        return uploadPath;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(final String jobId) {
        this.jobId = jobId;
    }

    public String getInstallPath() {
        return installPath;
    }

    public void setInstallPath(final String installPath) {
        this.installPath = fixPath(installPath);
    }

    public Long getTemplateSize() {
        return templateSize;
    }

    public void setTemplateSize(final long templateSize) {
        this.templateSize = templateSize;
    }
}
