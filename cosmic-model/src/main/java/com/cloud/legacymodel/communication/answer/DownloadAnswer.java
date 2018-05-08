package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.storage.VMTemplateStatus;

import java.io.File;

public class DownloadAnswer extends Answer {
    private String jobId;
    private int downloadPct;
    private String errorString;
    private VMTemplateStatus downloadStatus;
    private String downloadPath;
    private String installPath;
    private long templateSize = 0L;
    private long templatePhySicalSize = 0L;
    private String checkSum;

    protected DownloadAnswer() {

    }

    public DownloadAnswer(final String errorString, final VMTemplateStatus status) {
        super();
        this.downloadPct = 0;
        this.errorString = errorString;
        this.downloadStatus = status;
        this.details = errorString;
    }

    public DownloadAnswer(final String jobId, final int downloadPct, final String errorString, final VMTemplateStatus downloadStatus, final String fileSystemPath, final String
            installPath, final long templateSize,
                          final long templatePhySicalSize, final String checkSum) {
        super();
        this.jobId = jobId;
        this.downloadPct = downloadPct;
        this.errorString = errorString;
        this.details = errorString;
        this.downloadStatus = downloadStatus;
        this.downloadPath = fileSystemPath;
        this.installPath = fixPath(installPath);
        this.templateSize = templateSize;
        this.templatePhySicalSize = templatePhySicalSize;
        this.checkSum = checkSum;
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

    public DownloadAnswer(final String jobId, final int downloadPct, final Command command, final VMTemplateStatus downloadStatus, final String fileSystemPath, final String installPath) {
        super(command);
        this.jobId = jobId;
        this.downloadPct = downloadPct;
        this.downloadStatus = downloadStatus;
        this.downloadPath = fileSystemPath;
        this.installPath = installPath;
    }

    public String getCheckSum() {
        return this.checkSum;
    }

    public int getDownloadPct() {
        return this.downloadPct;
    }

    public String getErrorString() {
        return this.errorString;
    }

    public String getDownloadStatusString() {
        return this.downloadStatus.toString();
    }

    public VMTemplateStatus getDownloadStatus() {
        return this.downloadStatus;
    }

    public void setDownloadStatus(final VMTemplateStatus downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public String getDownloadPath() {
        return this.downloadPath;
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

    public long getTemplatePhySicalSize() {
        return this.templatePhySicalSize;
    }

    public void setTemplatePhySicalSize(final long templatePhySicalSize) {
        this.templatePhySicalSize = templatePhySicalSize;
    }
}
