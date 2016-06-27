//

//

package org.apache.cloudstack.storage.command;

import com.cloud.agent.api.Answer;

public class UploadStatusAnswer extends Answer {
    private UploadStatus status;
    private long virtualSize = 0;
    private long physicalSize = 0;
    private String installPath = null;
    private int downloadPercent = 0;

    protected UploadStatusAnswer() {
    }

    public UploadStatusAnswer(final UploadStatusCommand cmd, final UploadStatus status, final String msg) {
        super(cmd, false, msg);
        this.status = status;
    }

    public UploadStatusAnswer(final UploadStatusCommand cmd, final Exception e) {
        super(cmd, false, e.getMessage());
        this.status = UploadStatus.ERROR;
    }

    public UploadStatusAnswer(final UploadStatusCommand cmd, final UploadStatus status) {
        super(cmd, true, null);
        this.status = status;
    }

    public UploadStatus getStatus() {
        return status;
    }

    public long getVirtualSize() {
        return virtualSize;
    }

    public void setVirtualSize(final long virtualSize) {
        this.virtualSize = virtualSize;
    }

    public long getPhysicalSize() {
        return physicalSize;
    }

    public void setPhysicalSize(final long physicalSize) {
        this.physicalSize = physicalSize;
    }

    public String getInstallPath() {
        return installPath;
    }

    public void setInstallPath(final String installPath) {
        this.installPath = installPath;
    }

    public int getDownloadPercent() {
        return downloadPercent;
    }

    public void setDownloadPercent(final int downloadPercent) {
        this.downloadPercent = downloadPercent;
    }

    public static enum UploadStatus {
        UNKNOWN, IN_PROGRESS, COMPLETED, ERROR
    }
}
