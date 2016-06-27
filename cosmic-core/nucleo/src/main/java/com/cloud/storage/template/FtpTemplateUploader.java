//

//

package com.cloud.storage.template;

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
    public TemplateUploader.Status status = TemplateUploader.Status.NOT_STARTED;
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
        ftpUrl = url;
        completionCallback = callback;
        this.entitySizeinBytes = entitySizeinBytes;
    }

    @Override
    public void run() {
        try {
            upload(completionCallback);
        } catch (final Throwable t) {
            s_logger.warn("Caught exception during upload " + t.getMessage(), t);
            errorString = "Failed to install: " + t.getMessage();
            status = TemplateUploader.Status.UNRECOVERABLE_ERROR;
        }
    }

    @Override
    public long upload(final UploadCompleteCallback callback) {

        switch (status) {
            case ABORTED:
            case UNRECOVERABLE_ERROR:
            case UPLOAD_FINISHED:
                return 0;
            default:
        }

        new Date();

        final StringBuffer sb = new StringBuffer(ftpUrl);
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
            final File sourceFile = new File(sourcePath);
            entitySizeinBytes = sourceFile.length();

            outputStream = new BufferedOutputStream(urlc.getOutputStream());
            inputStream = new BufferedInputStream(new FileInputStream(sourceFile));

            status = TemplateUploader.Status.IN_PROGRESS;

            int bytes = 0;
            final byte[] block = new byte[CHUNK_SIZE];
            boolean done = false;
            while (!done && status != Status.ABORTED) {
                if ((bytes = inputStream.read(block, 0, CHUNK_SIZE)) > -1) {
                    outputStream.write(block, 0, bytes);
                    totalBytes += bytes;
                } else {
                    done = true;
                }
            }
            status = TemplateUploader.Status.UPLOAD_FINISHED;
            return totalBytes;
        } catch (final MalformedURLException e) {
            status = TemplateUploader.Status.UNRECOVERABLE_ERROR;
            errorString = e.getMessage();
            s_logger.error(errorString);
        } catch (final IOException e) {
            status = TemplateUploader.Status.UNRECOVERABLE_ERROR;
            errorString = e.getMessage();
            s_logger.error(errorString);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (final IOException ioe) {
                s_logger.error(" Caught exception while closing the resources");
            }
            if (callback != null) {
                callback.uploadComplete(status);
            }
        }

        return 0;
    }

    @Override
    public boolean stopUpload() {
        switch (getStatus()) {
            case IN_PROGRESS:
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (final IOException e) {
                    s_logger.error(" Caught exception while closing the resources");
                }
                status = TemplateUploader.Status.ABORTED;
                return true;
            case UNKNOWN:
            case NOT_STARTED:
            case RECOVERABLE_ERROR:
            case UNRECOVERABLE_ERROR:
            case ABORTED:
                status = TemplateUploader.Status.ABORTED;
            case UPLOAD_FINISHED:
                return true;

            default:
                return true;
        }
    }

    @Override
    public int getUploadPercent() {
        if (entitySizeinBytes == 0) {
            return 0;
        }
        return (int) (100.0 * totalBytes / entitySizeinBytes);
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void setStatus(final Status status) {
        this.status = status;
    }

    @Override
    public long getUploadTime() {
        // TODO
        return 0;
    }

    @Override
    public long getUploadedBytes() {
        return totalBytes;
    }

    @Override
    public String getUploadError() {
        return errorString;
    }

    @Override
    public void setUploadError(final String string) {
        errorString = string;
    }

    @Override
    public String getUploadLocalPath() {
        return sourcePath;
    }

    @Override
    public void setResume(final boolean resume) {

    }
}
