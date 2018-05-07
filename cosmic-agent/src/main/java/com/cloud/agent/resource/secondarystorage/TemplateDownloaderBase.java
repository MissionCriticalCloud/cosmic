package com.cloud.agent.resource.secondarystorage;

import com.cloud.common.managed.context.ManagedContextRunnable;
import com.cloud.legacymodel.storage.DownloadCompleteCallback;
import com.cloud.legacymodel.storage.TemplateDownloadStatus;
import com.cloud.utils.storage.StorageLayer;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TemplateDownloaderBase extends ManagedContextRunnable implements TemplateDownloader {
    private static final Logger s_logger = LoggerFactory.getLogger(TemplateDownloaderBase.class);

    protected String _downloadUrl;
    protected String _toFile;
    protected TemplateDownloadStatus _status = TemplateDownloadStatus.NOT_STARTED;
    protected String _errorString = " ";
    protected long _remoteSize = 0;
    protected long _downloadTime = 0;
    protected long _totalBytes;
    protected DownloadCompleteCallback _callback;
    protected boolean _resume = false;
    protected String _toDir;
    protected long _start;
    protected StorageLayer _storage;
    protected boolean _inited = false;
    private final long maxTemplateSizeInBytes;

    public TemplateDownloaderBase(final StorageLayer storage, final String downloadUrl, final String toDir, final long maxTemplateSizeInBytes, final DownloadCompleteCallback
            callback) {
        this._storage = storage;
        this._downloadUrl = downloadUrl;
        this._toDir = toDir;
        this._callback = callback;
        this._inited = true;

        this.maxTemplateSizeInBytes = maxTemplateSizeInBytes;
    }

    @Override
    public boolean stopDownload() {
        switch (getStatus()) {
            case IN_PROGRESS:
            case UNKNOWN:
            case NOT_STARTED:
            case RECOVERABLE_ERROR:
            case UNRECOVERABLE_ERROR:
            case ABORTED:
                this._status = TemplateDownloadStatus.ABORTED;
                break;
            case DOWNLOAD_FINISHED:
                break;
            default:
                break;
        }
        final File f = new File(this._toFile);
        if (f.exists()) {
            f.delete();
        }
        return true;
    }

    @Override
    public int getDownloadPercent() {
        if (this._remoteSize == 0) {
            return 0;
        }

        return (int) (100.0 * this._totalBytes / this._remoteSize);
    }

    @Override
    public TemplateDownloadStatus getStatus() {
        return this._status;
    }

    @Override
    public void setStatus(final TemplateDownloadStatus status) {
        this._status = status;
    }

    @Override
    public long getDownloadTime() {
        return this._downloadTime;
    }

    @Override
    public long getDownloadedBytes() {
        return this._totalBytes;
    }

    @Override
    public String getDownloadError() {
        return this._errorString;
    }

    @Override
    public void setDownloadError(final String string) {
        this._errorString = string;
    }

    @Override
    public String getDownloadLocalPath() {
        final File file = new File(this._toFile);
        return file.getAbsolutePath();
    }

    @Override
    public void setResume(final boolean resume) {
        this._resume = resume;
    }

    @Override
    public boolean isInited() {
        return this._inited;
    }

    @Override
    public long getMaxTemplateSizeInBytes() {
        return this.maxTemplateSizeInBytes;
    }

    @Override
    protected void runInContext() {
        try {
            download(this._resume, this._callback);
        } catch (final Exception e) {
            s_logger.warn("Unable to complete download due to ", e);
            this._errorString = "Failed to install: " + e.getMessage();
            this._status = TemplateDownloadStatus.UNRECOVERABLE_ERROR;
        }
    }
}
