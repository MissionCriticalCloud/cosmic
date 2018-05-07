package com.cloud.secondarystorage;

import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.storage.DownloadCompleteCallback;
import com.cloud.legacymodel.storage.TemplateDownloadStatus;
import com.cloud.utils.storage.StorageLayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalTemplateDownloader extends TemplateDownloaderBase implements TemplateDownloader {
    public static final Logger s_logger = LoggerFactory.getLogger(LocalTemplateDownloader.class);

    public LocalTemplateDownloader(final StorageLayer storageLayer, final String downloadUrl, final String toDir,
                                   final long maxTemplateSizeInBytes, final DownloadCompleteCallback callback) {
        super(storageLayer, downloadUrl, toDir, maxTemplateSizeInBytes, callback);
        final String filename = new File(downloadUrl).getName();
        this._toFile = toDir.endsWith(File.separator) ? toDir + filename : toDir + File.separator + filename;
    }

    public LocalTemplateDownloader(final StorageLayer storageLayer, final String downloadUrl, final String toDir,
                                   final long maxTemplateSizeInBytes) {
        super(storageLayer, downloadUrl, toDir, maxTemplateSizeInBytes, null);
        final String filename = new File(downloadUrl).getName();
        this._toFile = toDir.endsWith(File.separator) ? toDir + filename : toDir + File.separator + filename;
    }

    public LocalTemplateDownloader(final String downloadUrl, final String toDir, final long maxTemplateSizeInBytes) {
        super(null, downloadUrl, toDir, maxTemplateSizeInBytes, null);
        final String filename = new File(downloadUrl).getName();
        this._toFile = toDir.endsWith(File.separator) ? toDir + filename : toDir + File.separator + filename;
    }

    @Override
    public long download(final boolean resume, final DownloadCompleteCallback callback) {
        if (this._status == TemplateDownloadStatus.ABORTED || this._status == TemplateDownloadStatus.UNRECOVERABLE_ERROR || this._status == TemplateDownloadStatus.DOWNLOAD_FINISHED) {
            throw new CloudRuntimeException("Invalid status for downloading: " + this._status);
        }

        this._start = System.currentTimeMillis();
        this._resume = resume;

        final File src;
        try {
            src = new File(new URI(this._downloadUrl));
        } catch (final URISyntaxException e) {
            final String message = "Invalid URI " + this._downloadUrl;
            s_logger.warn(message);
            this._status = TemplateDownloadStatus.UNRECOVERABLE_ERROR;
            throw new CloudRuntimeException(message, e);
        }

        final File dst = new File(this._toFile);

        FileChannel fic = null;
        FileChannel foc = null;
        FileInputStream fis = null;
        FileOutputStream fos = null;

        try {
            if (this._storage != null) {
                dst.createNewFile();
                this._storage.setWorldReadableAndWriteable(dst);
            }

            final ByteBuffer buffer = ByteBuffer.allocate(1024 * 512);

            try {
                fis = new FileInputStream(src);
            } catch (final FileNotFoundException e) {
                this._errorString = "Unable to find " + this._downloadUrl;
                s_logger.warn(this._errorString);
                throw new CloudRuntimeException(this._errorString, e);
            }
            fic = fis.getChannel();
            try {
                if (!dst.exists()) {
                    dst.delete();
                }
                fos = new FileOutputStream(dst);
            } catch (final FileNotFoundException e) {
                final String message = "Unable to find " + this._toFile;
                s_logger.warn(message);
                throw new CloudRuntimeException(message, e);
            }
            foc = fos.getChannel();

            this._remoteSize = src.length();
            this._totalBytes = 0;
            this._status = TemplateDownloadStatus.IN_PROGRESS;

            try {
                while (this._status != TemplateDownloadStatus.ABORTED && fic.read(buffer) != -1) {
                    buffer.flip();
                    final int count = foc.write(buffer);
                    this._totalBytes += count;
                    buffer.clear();
                }
            } catch (final IOException e) {
                s_logger.warn("Unable to download");
            }

            String downloaded = "(incomplete download)";
            if (this._totalBytes == this._remoteSize) {
                this._status = TemplateDownloadStatus.DOWNLOAD_FINISHED;
                downloaded = "(download complete)";
            }

            this._errorString = "Downloaded " + this._remoteSize + " bytes " + downloaded;
            this._downloadTime += System.currentTimeMillis() - this._start;
            return this._totalBytes;
        } catch (final Exception e) {
            this._status = TemplateDownloadStatus.UNRECOVERABLE_ERROR;
            this._errorString = e.getMessage();
            throw new CloudRuntimeException(this._errorString, e);
        } finally {
            if (fic != null) {
                try {
                    fic.close();
                } catch (final IOException e) {
                    s_logger.info("[ignore] error while closing file input channel.");
                }
            }

            if (foc != null) {
                try {
                    foc.close();
                } catch (final IOException e) {
                    s_logger.info("[ignore] error while closing file output channel.");
                }
            }

            if (fis != null) {
                try {
                    fis.close();
                } catch (final IOException e) {
                    s_logger.info("[ignore] error while closing file input stream.");
                }
            }

            if (fos != null) {
                try {
                    fos.close();
                } catch (final IOException e) {
                    s_logger.info("[ignore] error while closing file output stream.");
                }
            }

            if (this._status == TemplateDownloadStatus.UNRECOVERABLE_ERROR && dst.exists()) {
                dst.delete();
            }
            if (callback != null) {
                callback.downloadComplete(this._status);
            }
        }
    }
}
