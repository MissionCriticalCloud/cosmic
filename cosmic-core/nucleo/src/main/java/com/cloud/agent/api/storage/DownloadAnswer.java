//

//

package com.cloud.agent.api.storage;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.storage.VMTemplateStorageResourceAssoc;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;

import java.io.File;

public class DownloadAnswer extends Answer {
    private String jobId;
    private int downloadPct;
    private String errorString;
    private VMTemplateStorageResourceAssoc.Status downloadStatus;
    private String downloadPath;
    private String installPath;
    private long templateSize = 0L;
    private long templatePhySicalSize = 0L;
    private String checkSum;

    protected DownloadAnswer() {

    }

    public DownloadAnswer(final String errorString, final Status status) {
        super();
        this.downloadPct = 0;
        this.errorString = errorString;
        this.downloadStatus = status;
        this.details = errorString;
    }

    public DownloadAnswer(final String jobId, final int downloadPct, final String errorString, final Status downloadStatus, final String fileSystemPath, final String
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

    public DownloadAnswer(final String jobId, final int downloadPct, final Command command, final Status downloadStatus, final String fileSystemPath, final String installPath) {
        super(command);
        this.jobId = jobId;
        this.downloadPct = downloadPct;
        this.downloadStatus = downloadStatus;
        this.downloadPath = fileSystemPath;
        this.installPath = installPath;
    }

    public String getCheckSum() {
        return checkSum;
    }

    public int getDownloadPct() {
        return downloadPct;
    }

    public String getErrorString() {
        return errorString;
    }

    public String getDownloadStatusString() {
        return downloadStatus.toString();
    }

    public VMTemplateStorageResourceAssoc.Status getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(final VMTemplateStorageResourceAssoc.Status downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public String getDownloadPath() {
        return downloadPath;
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

    public long getTemplatePhySicalSize() {
        return templatePhySicalSize;
    }

    public void setTemplatePhySicalSize(final long templatePhySicalSize) {
        this.templatePhySicalSize = templatePhySicalSize;
    }
}
