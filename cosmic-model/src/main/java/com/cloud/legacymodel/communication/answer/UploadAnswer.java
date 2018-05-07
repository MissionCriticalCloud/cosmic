package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.storage.UploadStatus;

import java.io.File;

public class UploadAnswer extends Answer {

    public Long templateSize = 0L;
    private String jobId;
    private int uploadPct;
    private String errorString;
    private UploadStatus uploadStatus;
    private String uploadPath;
    private String installPath;

    protected UploadAnswer() {

    }

    public UploadAnswer(final String jobId, final int uploadPct, final String errorString, final UploadStatus uploadStatus, final String fileSystemPath, final String
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

    public UploadAnswer(final String jobId, final int uploadPct, final Command command, final UploadStatus uploadStatus, final String fileSystemPath, final String installPath) {
        super(command);
        this.jobId = jobId;
        this.uploadPct = uploadPct;
        this.uploadStatus = uploadStatus;
        this.uploadPath = fileSystemPath;
        this.installPath = installPath;
    }

    public int getUploadPct() {
        return this.uploadPct;
    }

    public String getErrorString() {
        return this.errorString;
    }

    public void setErrorString(final String errorString) {
        this.errorString = errorString;
    }

    public String getUploadStatusString() {
        return this.uploadStatus.toString();
    }

    public UploadStatus getUploadStatus() {
        return this.uploadStatus;
    }

    public void setUploadStatus(final UploadStatus uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    public String getUploadPath() {
        return this.uploadPath;
    }

    public String getJobId() {
        return this.jobId;
    }

    public void setJobId(final String jobId) {
        this.jobId = jobId;
    }

    public String getInstallPath() {
        return this.installPath;
    }

    public void setInstallPath(final String installPath) {
        this.installPath = fixPath(installPath);
    }

    public Long getTemplateSize() {
        return this.templateSize;
    }

    public void setTemplateSize(final long templateSize) {
        this.templateSize = templateSize;
    }
}
