package com.cloud.secondarystorage;

import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.storage.DownloadCompleteCallback;
import com.cloud.legacymodel.storage.TemplateDownloadStatus;
import com.cloud.utils.storage.StorageLayer;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import com.trilead.ssh2.SCPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScpTemplateDownloader extends TemplateDownloaderBase implements TemplateDownloader {
    private static final Logger s_logger = LoggerFactory.getLogger(ScpTemplateDownloader.class);

    public ScpTemplateDownloader(final StorageLayer storageLayer, final String downloadUrl, final String toDir, final long maxTemplateSizeInBytes, final DownloadCompleteCallback
            callback) {
        super(storageLayer, downloadUrl, toDir, maxTemplateSizeInBytes, callback);

        final URI uri;
        try {
            uri = new URI(this._downloadUrl);
        } catch (final URISyntaxException e) {
            s_logger.warn("URI syntax error: " + this._downloadUrl);
            this._status = TemplateDownloadStatus.UNRECOVERABLE_ERROR;
            return;
        }

        final String path = uri.getPath();
        final String filename = path.substring(path.lastIndexOf("/") + 1);
        this._toFile = toDir + File.separator + filename;
    }

    @Override
    public long download(final boolean resume, final DownloadCompleteCallback callback) {
        if (this._status == TemplateDownloadStatus.ABORTED || this._status == TemplateDownloadStatus.UNRECOVERABLE_ERROR || this._status == TemplateDownloadStatus.DOWNLOAD_FINISHED) {
            return 0;
        }

        this._resume = resume;

        this._start = System.currentTimeMillis();

        final URI uri;
        try {
            uri = new URI(this._downloadUrl);
        } catch (final URISyntaxException e1) {
            this._status = TemplateDownloadStatus.UNRECOVERABLE_ERROR;
            return 0;
        }

        final String username = uri.getUserInfo();
        final String queries = uri.getQuery();
        String password = null;
        if (queries != null) {
            final String[] qs = queries.split("&");
            for (final String q : qs) {
                final String[] tokens = q.split("=");
                if (tokens[0].equalsIgnoreCase("password")) {
                    password = tokens[1];
                    break;
                }
            }
        }
        int port = uri.getPort();
        if (port == -1) {
            port = 22;
        }
        final File file = new File(this._toFile);

        final com.trilead.ssh2.Connection sshConnection = new com.trilead.ssh2.Connection(uri.getHost(), port);
        try {
            if (this._storage != null) {
                file.createNewFile();
                this._storage.setWorldReadableAndWriteable(file);
            }

            sshConnection.connect(null, 60000, 60000);
            if (!sshConnection.authenticateWithPassword(username, password)) {
                throw new CloudRuntimeException("Unable to authenticate");
            }

            final SCPClient scp = new SCPClient(sshConnection);

            final String src = uri.getPath();

            this._status = TemplateDownloadStatus.IN_PROGRESS;
            scp.get(src, this._toDir);

            if (!file.exists()) {
                this._status = TemplateDownloadStatus.UNRECOVERABLE_ERROR;
                s_logger.debug("unable to scp the file " + this._downloadUrl);
                return 0;
            }

            this._status = TemplateDownloadStatus.DOWNLOAD_FINISHED;

            this._totalBytes = file.length();

            final String downloaded = "(download complete)";

            this._errorString = "Downloaded " + this._remoteSize + " bytes " + downloaded;
            this._downloadTime += System.currentTimeMillis() - this._start;
            return this._totalBytes;
        } catch (final Exception e) {
            s_logger.warn("Unable to download " + this._downloadUrl, e);
            this._status = TemplateDownloadStatus.UNRECOVERABLE_ERROR;
            this._errorString = e.getMessage();
            return 0;
        } finally {
            sshConnection.close();
            if (this._status == TemplateDownloadStatus.UNRECOVERABLE_ERROR && file.exists()) {
                file.delete();
            }
            if (callback != null) {
                callback.downloadComplete(this._status);
            }
        }
    }

    @Override
    public int getDownloadPercent() {
        if (this._status == TemplateDownloadStatus.DOWNLOAD_FINISHED) {
            return 100;
        } else if (this._status == TemplateDownloadStatus.IN_PROGRESS) {
            return 50;
        } else {
            return 0;
        }
    }
}
