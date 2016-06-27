//

//

package com.cloud.storage.template;

import com.cloud.storage.StorageLayer;
import org.apache.cloudstack.managed.context.ManagedContextRunnable;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TemplateDownloaderBase extends ManagedContextRunnable implements TemplateDownloader {
    private static final Logger s_logger = LoggerFactory.getLogger(TemplateDownloaderBase.class);

    protected String _downloadUrl;
    protected String _toFile;
    protected TemplateDownloader.Status _status = TemplateDownloader.Status.NOT_STARTED;
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
        _storage = storage;
        _downloadUrl = downloadUrl;
        _toDir = toDir;
        _callback = callback;
        _inited = true;

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
                _status = TemplateDownloader.Status.ABORTED;
                break;
            case DOWNLOAD_FINISHED:
                break;
            default:
                break;
        }
        final File f = new File(_toFile);
        if (f.exists()) {
            f.delete();
        }
        return true;
    }

    @Override
    public int getDownloadPercent() {
        if (_remoteSize == 0) {
            return 0;
        }

        return (int) (100.0 * _totalBytes / _remoteSize);
    }

    @Override
    public Status getStatus() {
        return _status;
    }

    @Override
    public void setStatus(final Status status) {
        _status = status;
    }

    @Override
    public long getDownloadTime() {
        return _downloadTime;
    }

    @Override
    public long getDownloadedBytes() {
        return _totalBytes;
    }

    @Override
    public String getDownloadError() {
        return _errorString;
    }

    @Override
    public void setDownloadError(final String string) {
        _errorString = string;
    }

    @Override
    public String getDownloadLocalPath() {
        final File file = new File(_toFile);
        return file.getAbsolutePath();
    }

    @Override
    public void setResume(final boolean resume) {
        _resume = resume;
    }

    @Override
    public boolean isInited() {
        return _inited;
    }

    @Override
    public long getMaxTemplateSizeInBytes() {
        return this.maxTemplateSizeInBytes;
    }

    @Override
    protected void runInContext() {
        try {
            download(_resume, _callback);
        } catch (final Exception e) {
            s_logger.warn("Unable to complete download due to ", e);
            _errorString = "Failed to install: " + e.getMessage();
            _status = TemplateDownloader.Status.UNRECOVERABLE_ERROR;
        }
    }
}
