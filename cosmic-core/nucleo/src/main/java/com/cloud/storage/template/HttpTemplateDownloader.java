package com.cloud.storage.template;

import com.cloud.common.managed.context.ManagedContextRunnable;
import com.cloud.legacymodel.communication.command.DownloadCommand.ResourceType;
import com.cloud.legacymodel.network.Proxy;
import com.cloud.legacymodel.storage.DownloadCompleteCallback;
import com.cloud.legacymodel.storage.TemplateDownloadStatus;
import com.cloud.legacymodel.utils.Pair;
import com.cloud.utils.UriUtils;
import com.cloud.utils.imagestore.ImageStoreUtil;
import com.cloud.utils.storage.StorageLayer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NoHttpResponseException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Download a template file using HTTP
 */
public class HttpTemplateDownloader extends ManagedContextRunnable implements TemplateDownloader {
    public static final Logger s_logger = LoggerFactory.getLogger(HttpTemplateDownloader.class.getName());
    private static final MultiThreadedHttpConnectionManager s_httpClientManager = new MultiThreadedHttpConnectionManager();

    private static final int CHUNK_SIZE = 1024 * 1024; //1M
    private final HttpClient client;
    private final HttpMethodRetryHandler myretryhandler;
    public TemplateDownloadStatus status = TemplateDownloadStatus.NOT_STARTED;
    public String errorString = " ";
    public long downloadTime = 0;
    public long totalBytes;
    StorageLayer _storage;
    boolean inited = true;
    private final String downloadUrl;
    private String toFile = "";
    private long remoteSize = 0;
    private GetMethod request = null;
    private boolean resume = false;
    private DownloadCompleteCallback completionCallback = null;
    private String toDir;
    private final long maxTemplateSizeInBytes;
    private ResourceType resourceType = ResourceType.TEMPLATE;

    public HttpTemplateDownloader(final StorageLayer storageLayer, final String downloadUrl, final String toDir, final DownloadCompleteCallback callback,
                                  final long maxTemplateSizeInBytes, final String user, final String password, final Proxy proxy, final ResourceType resourceType) {
        this._storage = storageLayer;
        this.downloadUrl = downloadUrl;
        setToDir(toDir);
        this.status = TemplateDownloadStatus.NOT_STARTED;
        this.resourceType = resourceType;
        this.maxTemplateSizeInBytes = maxTemplateSizeInBytes;

        this.totalBytes = 0;
        this.client = new HttpClient(s_httpClientManager);

        this.myretryhandler = (method, exception, executionCount) -> {
            if (executionCount >= 2) {
                // Do not retry if over max retry count
                return false;
            }
            if (exception instanceof NoHttpResponseException) {
                // Retry if the server dropped connection on us
                return true;
            }
            if (!method.isRequestSent()) {
                // Retry if the request has not been sent fully or
                // if it's OK to retry methods that have been sent
                return true;
            }
            // otherwise do not retry
            return false;
        };

        try {
            this.request = new GetMethod(downloadUrl);
            this.request.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, this.myretryhandler);
            this.completionCallback = callback;
            this.request.setFollowRedirects(true);

            final File f = File.createTempFile("dnld", "tmp_", new File(toDir));

            if (this._storage != null) {
                this._storage.setWorldReadableAndWriteable(f);
            }

            this.toFile = f.getAbsolutePath();
            final Pair<String, Integer> hostAndPort = UriUtils.validateUrl(downloadUrl);

            if (proxy != null) {
                this.client.getHostConfiguration().setProxy(proxy.getHost(), proxy.getPort());
                if (proxy.getUserName() != null) {
                    final Credentials proxyCreds = new UsernamePasswordCredentials(proxy.getUserName(), proxy.getPassword());
                    this.client.getState().setProxyCredentials(AuthScope.ANY, proxyCreds);
                }
            }
            if (user != null && password != null) {
                this.client.getParams().setAuthenticationPreemptive(true);
                final Credentials defaultcreds = new UsernamePasswordCredentials(user, password);
                this.client.getState().setCredentials(new AuthScope(hostAndPort.first(), hostAndPort.second(), AuthScope.ANY_REALM), defaultcreds);
                s_logger.info("Added username=" + user + ", password=" + password + "for host " + hostAndPort.first() + ":" + hostAndPort.second());
            } else {
                s_logger.info("No credentials configured for host=" + hostAndPort.first() + ":" + hostAndPort.second());
            }
        } catch (final IllegalArgumentException iae) {
            this.errorString = iae.getMessage();
            this.status = TemplateDownloadStatus.UNRECOVERABLE_ERROR;
            this.inited = false;
        } catch (final Exception ex) {
            this.errorString = "Unable to start download -- check url? ";
            this.status = TemplateDownloadStatus.UNRECOVERABLE_ERROR;
            s_logger.warn("Exception in constructor -- " + ex.toString());
        }
    }

    @Override
    protected void runInContext() {
        try {
            download(this.resume, this.completionCallback);
        } catch (final Exception e) {
            s_logger.warn("Caught exception during download " + e.getMessage(), e);
            this.errorString = "Failed to install: " + e.getMessage();
            this.status = TemplateDownloadStatus.UNRECOVERABLE_ERROR;
        }
    }

    @Override
    public long download(final boolean resume, final DownloadCompleteCallback callback) {
        switch (this.status) {
            case ABORTED:
            case UNRECOVERABLE_ERROR:
            case DOWNLOAD_FINISHED:
                return 0;
            default:
        }
        int bytes = 0;
        final File file = new File(this.toFile);
        try {

            long localFileSize = 0;
            if (file.exists() && resume) {
                localFileSize = file.length();
                s_logger.info("Resuming download to file (current size)=" + localFileSize);
            }

            final Date start = new Date();

            int responseCode = 0;

            if (localFileSize > 0) {
                // require partial content support for resume
                this.request.addRequestHeader("Range", "bytes=" + localFileSize + "-");
                if (this.client.executeMethod(this.request) != HttpStatus.SC_PARTIAL_CONTENT) {
                    this.errorString = "HTTP Server does not support partial get";
                    this.status = TemplateDownloadStatus.UNRECOVERABLE_ERROR;
                    return 0;
                }
            } else if ((responseCode = this.client.executeMethod(this.request)) != HttpStatus.SC_OK) {
                this.status = TemplateDownloadStatus.UNRECOVERABLE_ERROR;
                this.errorString = " HTTP Server returned " + responseCode + " (expected 200 OK) ";
                return 0; //FIXME: retry?
            }

            final Header contentLengthHeader = this.request.getResponseHeader("Content-Length");
            boolean chunked = false;
            long remoteSize2 = 0;
            if (contentLengthHeader == null) {
                final Header chunkedHeader = this.request.getResponseHeader("Transfer-Encoding");
                if (chunkedHeader == null || !"chunked".equalsIgnoreCase(chunkedHeader.getValue())) {
                    this.status = TemplateDownloadStatus.UNRECOVERABLE_ERROR;
                    this.errorString = " Failed to receive length of download ";
                    return 0; //FIXME: what status do we put here? Do we retry?
                } else if ("chunked".equalsIgnoreCase(chunkedHeader.getValue())) {
                    chunked = true;
                }
            } else {
                remoteSize2 = Long.parseLong(contentLengthHeader.getValue());
                if (remoteSize2 == 0) {
                    this.status = TemplateDownloadStatus.DOWNLOAD_FINISHED;
                    final String downloaded = "(download complete remote=" + this.remoteSize + "bytes)";
                    this.errorString = "Downloaded " + this.totalBytes + " bytes " + downloaded;
                    this.downloadTime = 0;
                    return 0;
                }
            }

            if (this.remoteSize == 0) {
                this.remoteSize = remoteSize2;
            }

            if (this.remoteSize > this.maxTemplateSizeInBytes) {
                s_logger.info("Remote size is too large: " + this.remoteSize + " , max=" + this.maxTemplateSizeInBytes);
                this.status = TemplateDownloadStatus.UNRECOVERABLE_ERROR;
                this.errorString = "Download file size is too large";
                return 0;
            }

            if (this.remoteSize == 0) {
                this.remoteSize = this.maxTemplateSizeInBytes;
            }

            final InputStream in = this.request.getResponseBodyAsStream();

            final RandomAccessFile out = new RandomAccessFile(file, "rw");
            out.seek(localFileSize);

            s_logger.info("Starting download from " + getDownloadUrl() + " to " + this.toFile + " remoteSize=" + this.remoteSize + " , max size=" + this.maxTemplateSizeInBytes);

            final byte[] block = new byte[CHUNK_SIZE];
            long offset = 0;
            boolean done = false;
            boolean verifiedFormat = false;
            this.status = TemplateDownloadStatus.IN_PROGRESS;
            while (!done && this.status != TemplateDownloadStatus.ABORTED && offset <= this.remoteSize) {
                if ((bytes = in.read(block, 0, CHUNK_SIZE)) > -1) {
                    out.write(block, 0, bytes);
                    offset += bytes;
                    out.seek(offset);
                    this.totalBytes += bytes;
                    if (!verifiedFormat && (offset >= 1048576 || offset >= this.remoteSize)) { //let's check format after we get 1MB or full file
                        String uripath = null;
                        try {
                            final URI str = new URI(getDownloadUrl());
                            uripath = str.getPath();
                        } catch (final URISyntaxException e) {
                            s_logger.warn("Invalid download url: " + getDownloadUrl() + ", This should not happen since we have validated the url before!!");
                        }
                        final String unsupportedFormat = ImageStoreUtil.checkTemplateFormat(file.getAbsolutePath(), uripath);
                        if (unsupportedFormat == null || !unsupportedFormat.isEmpty()) {
                            try {
                                this.request.abort();
                                out.close();
                                in.close();
                            } catch (final Exception ex) {
                                s_logger.debug("Error on http connection : " + ex.getMessage());
                            }
                            this.status = TemplateDownloadStatus.UNRECOVERABLE_ERROR;
                            this.errorString = "Template content is unsupported, or mismatch between selected format and template content. Found  : " + unsupportedFormat;
                            return 0;
                        }
                        s_logger.debug("Verified format of downloading file " + file.getAbsolutePath() + " is supported");
                        verifiedFormat = true;
                    }
                } else {
                    done = true;
                }
            }
            out.getFD().sync();

            final Date finish = new Date();
            String downloaded = "(incomplete download)";
            if (this.totalBytes >= this.remoteSize) {
                this.status = TemplateDownloadStatus.DOWNLOAD_FINISHED;
                downloaded = "(download complete remote=" + this.remoteSize + "bytes)";
            }
            this.errorString = "Downloaded " + this.totalBytes + " bytes " + downloaded;
            this.downloadTime += finish.getTime() - start.getTime();
            in.close();
            out.close();

            return this.totalBytes;
        } catch (final HttpException hte) {
            this.status = TemplateDownloadStatus.UNRECOVERABLE_ERROR;
            this.errorString = hte.getMessage();
        } catch (final IOException ioe) {
            this.status = TemplateDownloadStatus.UNRECOVERABLE_ERROR; //probably a file write error?
            this.errorString = ioe.getMessage();
        } finally {
            if (this.status == TemplateDownloadStatus.UNRECOVERABLE_ERROR && file.exists() && !file.isDirectory()) {
                file.delete();
            }
            this.request.releaseConnection();
            if (callback != null) {
                callback.downloadComplete(this.status);
            }
        }
        return 0;
    }

    public String getDownloadUrl() {
        return this.downloadUrl;
    }

    @Override
    public boolean stopDownload() {
        switch (getStatus()) {
            case IN_PROGRESS:
                if (this.request != null) {
                    this.request.abort();
                }
                this.status = TemplateDownloadStatus.ABORTED;
                return true;
            case UNKNOWN:
            case NOT_STARTED:
            case RECOVERABLE_ERROR:
            case UNRECOVERABLE_ERROR:
            case ABORTED:
                this.status = TemplateDownloadStatus.ABORTED;
            case DOWNLOAD_FINISHED:
                final File f = new File(this.toFile);
                if (f.exists()) {
                    f.delete();
                }
                return true;

            default:
                return true;
        }
    }

    @Override
    public int getDownloadPercent() {
        if (this.remoteSize == 0) {
            return 0;
        }

        return (int) (100.0 * this.totalBytes / this.remoteSize);
    }

    @Override
    public TemplateDownloadStatus getStatus() {
        return this.status;
    }

    @Override
    public void setStatus(final TemplateDownloadStatus status) {
        this.status = status;
    }

    @Override
    public long getDownloadTime() {
        return this.downloadTime;
    }

    @Override
    public long getDownloadedBytes() {
        return this.totalBytes;
    }

    @Override
    public String getDownloadError() {
        return this.errorString;
    }

    @Override
    public void setDownloadError(final String error) {
        this.errorString = error;
    }

    @Override
    public String getDownloadLocalPath() {
        return getToFile();
    }

    public String getToFile() {
        final File file = new File(this.toFile);

        return file.getAbsolutePath();
    }

    public boolean isResume() {
        return this.resume;
    }

    @Override
    public void setResume(final boolean resume) {
        this.resume = resume;
    }

    @Override
    public boolean isInited() {
        return this.inited;
    }

    @Override
    public long getMaxTemplateSizeInBytes() {
        return this.maxTemplateSizeInBytes;
    }

    public String getToDir() {
        return this.toDir;
    }

    public void setToDir(final String toDir) {
        this.toDir = toDir;
    }

    public ResourceType getResourceType() {
        return this.resourceType;
    }
}
