package com.cloud.agent.resource.secondarystorage;

import com.cloud.legacymodel.storage.TemplateUploadStatus;
import com.cloud.legacymodel.storage.UploadCompleteCallback;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FtpTemplateUploader implements TemplateUploader {

    public static final Logger s_logger = LoggerFactory.getLogger(FtpTemplateUploader.class.getName());
    private static final int CHUNK_SIZE = 1024 * 1024; //1M
    public TemplateUploadStatus status = TemplateUploadStatus.NOT_STARTED;
    public String errorString = "";
    public long totalBytes = 0;
    public long entitySizeinBytes;
    private final String sourcePath;
    private final String ftpUrl;
    private final UploadCompleteCallback completionCallback;
    private BufferedInputStream inputStream = null;
    private BufferedOutputStream outputStream = null;

    public FtpTemplateUploader(final String sourcePath, final String url, final UploadCompleteCallback callback, final long entitySizeinBytes) {

        this.sourcePath = sourcePath;
        this.ftpUrl = url;
        this.completionCallback = callback;
        this.entitySizeinBytes = entitySizeinBytes;
    }

    @Override
    public void run() {
        upload(this.completionCallback);
    }

    @Override
    public long upload(final UploadCompleteCallback callback) {

        switch (this.status) {
            case ABORTED:
            case UNRECOVERABLE_ERROR:
            case UPLOAD_FINISHED:
                return 0;
            default:
        }

        new Date();

        final StringBuffer sb = new StringBuffer(this.ftpUrl);
        // check for authentication else assume its anonymous access.
        /* if (user != null && password != null)
                 {
                    sb.append( user );
                    sb.append( ':' );
                    sb.append( password );
                    sb.append( '@' );
                 }*/
        /*
         * type ==> a=ASCII mode, i=image (binary) mode, d= file directory
         * listing
         */
        sb.append(";type=i");

        try {
            final URL url = new URL(sb.toString());
            final URLConnection urlc = url.openConnection();
            final File sourceFile = new File(this.sourcePath);
            this.entitySizeinBytes = sourceFile.length();

            this.outputStream = new BufferedOutputStream(urlc.getOutputStream());
            this.inputStream = new BufferedInputStream(new FileInputStream(sourceFile));

            this.status = TemplateUploadStatus.IN_PROGRESS;

            int bytes = 0;
            final byte[] block = new byte[CHUNK_SIZE];
            boolean done = false;
            while (!done && this.status != TemplateUploadStatus.ABORTED) {
                if ((bytes = this.inputStream.read(block, 0, CHUNK_SIZE)) > -1) {
                    this.outputStream.write(block, 0, bytes);
                    this.totalBytes += bytes;
                } else {
                    done = true;
                }
            }
            this.status = TemplateUploadStatus.UPLOAD_FINISHED;
            return this.totalBytes;
        } catch (final MalformedURLException e) {
            this.status = TemplateUploadStatus.UNRECOVERABLE_ERROR;
            this.errorString = e.getMessage();
            s_logger.error(this.errorString);
        } catch (final IOException e) {
            this.status = TemplateUploadStatus.UNRECOVERABLE_ERROR;
            this.errorString = e.getMessage();
            s_logger.error(this.errorString);
        } finally {
            try {
                if (this.inputStream != null) {
                    this.inputStream.close();
                }
                if (this.outputStream != null) {
                    this.outputStream.close();
                }
            } catch (final IOException ioe) {
                s_logger.error(" Caught exception while closing the resources");
            }
            if (callback != null) {
                callback.uploadComplete(this.status);
            }
        }

        return 0;
    }

    @Override
    public boolean stopUpload() {
        switch (getStatus()) {
            case IN_PROGRESS:
                try {
                    if (this.outputStream != null) {
                        this.outputStream.close();
                    }
                    if (this.inputStream != null) {
                        this.inputStream.close();
                    }
                } catch (final IOException e) {
                    s_logger.error(" Caught exception while closing the resources");
                }
                this.status = TemplateUploadStatus.ABORTED;
                return true;
            case UNKNOWN:
            case NOT_STARTED:
            case RECOVERABLE_ERROR:
            case UNRECOVERABLE_ERROR:
            case ABORTED:
                this.status = TemplateUploadStatus.ABORTED;
            case UPLOAD_FINISHED:
                return true;

            default:
                return true;
        }
    }

    @Override
    public int getUploadPercent() {
        if (this.entitySizeinBytes == 0) {
            return 0;
        }
        return (int) (100.0 * this.totalBytes / this.entitySizeinBytes);
    }

    @Override
    public TemplateUploadStatus getStatus() {
        return this.status;
    }

    @Override
    public void setStatus(final TemplateUploadStatus status) {
        this.status = status;
    }

    @Override
    public long getUploadTime() {
        // TODO
        return 0;
    }

    @Override
    public long getUploadedBytes() {
        return this.totalBytes;
    }

    @Override
    public String getUploadError() {
        return this.errorString;
    }

    @Override
    public void setUploadError(final String string) {
        this.errorString = string;
    }

    @Override
    public String getUploadLocalPath() {
        return this.sourcePath;
    }

    @Override
    public void setResume(final boolean resume) {

    }
}
