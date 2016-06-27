//

//

package com.cloud.storage.template;

import com.cloud.storage.StorageLayer;
import com.cloud.utils.Pair;
import com.cloud.utils.UriUtils;
import com.cloud.utils.net.Proxy;
import org.apache.cloudstack.managed.context.ManagedContextRunnable;
import org.apache.cloudstack.storage.command.DownloadCommand.ResourceType;
import org.apache.cloudstack.utils.imagestore.ImageStoreUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
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
    public TemplateDownloader.Status status = TemplateDownloader.Status.NOT_STARTED;
    public String errorString = " ";
    public long downloadTime = 0;
    public long totalBytes;
    StorageLayer _storage;
    boolean inited = true;
    private final String downloadUrl;
    private String toFile;
    private long remoteSize = 0;
    private GetMethod request;
    private boolean resume = false;
    private DownloadCompleteCallback completionCallback;
    private String toDir;
    private final long maxTemplateSizeInBytes;
    private ResourceType resourceType = ResourceType.TEMPLATE;

    public HttpTemplateDownloader(final StorageLayer storageLayer, final String downloadUrl, final String toDir, final DownloadCompleteCallback callback, final long
            maxTemplateSizeInBytes,
                                  final String user, final String password, final Proxy proxy, final ResourceType resourceType) {
        _storage = storageLayer;
        this.downloadUrl = downloadUrl;
        setToDir(toDir);
        status = TemplateDownloader.Status.NOT_STARTED;
        this.resourceType = resourceType;
        this.maxTemplateSizeInBytes = maxTemplateSizeInBytes;

        totalBytes = 0;
        client = new HttpClient(s_httpClientManager);

        myretryhandler = new HttpMethodRetryHandler() {
            @Override
            public boolean retryMethod(final HttpMethod method, final IOException exception, final int executionCount) {
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
            }
        };

        try {
            request = new GetMethod(downloadUrl);
            request.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, myretryhandler);
            completionCallback = callback;
            //this.request.setFollowRedirects(false);

            final File f = File.createTempFile("dnld", "tmp_", new File(toDir));

            if (_storage != null) {
                _storage.setWorldReadableAndWriteable(f);
            }

            toFile = f.getAbsolutePath();
            final Pair<String, Integer> hostAndPort = UriUtils.validateUrl(downloadUrl);

            if (proxy != null) {
                client.getHostConfiguration().setProxy(proxy.getHost(), proxy.getPort());
                if (proxy.getUserName() != null) {
                    final Credentials proxyCreds = new UsernamePasswordCredentials(proxy.getUserName(), proxy.getPassword());
                    client.getState().setProxyCredentials(AuthScope.ANY, proxyCreds);
                }
            }
            if (user != null && password != null) {
                client.getParams().setAuthenticationPreemptive(true);
                final Credentials defaultcreds = new UsernamePasswordCredentials(user, password);
                client.getState().setCredentials(new AuthScope(hostAndPort.first(), hostAndPort.second(), AuthScope.ANY_REALM), defaultcreds);
                s_logger.info("Added username=" + user + ", password=" + password + "for host " + hostAndPort.first() + ":" + hostAndPort.second());
            } else {
                s_logger.info("No credentials configured for host=" + hostAndPort.first() + ":" + hostAndPort.second());
            }
        } catch (final IllegalArgumentException iae) {
            errorString = iae.getMessage();
            status = TemplateDownloader.Status.UNRECOVERABLE_ERROR;
            inited = false;
        } catch (final Exception ex) {
            errorString = "Unable to start download -- check url? ";
            status = TemplateDownloader.Status.UNRECOVERABLE_ERROR;
            s_logger.warn("Exception in constructor -- " + ex.toString());
        } catch (final Throwable th) {
            s_logger.warn("throwable caught ", th);
        }
    }

    @Override
    protected void runInContext() {
        try {
            download(resume, completionCallback);
        } catch (final Throwable t) {
            s_logger.warn("Caught exception during download " + t.getMessage(), t);
            errorString = "Failed to install: " + t.getMessage();
            status = TemplateDownloader.Status.UNRECOVERABLE_ERROR;
        }
    }

    @Override
    public long download(final boolean resume, final DownloadCompleteCallback callback) {
        switch (status) {
            case ABORTED:
            case UNRECOVERABLE_ERROR:
            case DOWNLOAD_FINISHED:
                return 0;
            default:
        }
        int bytes = 0;
        final File file = new File(toFile);
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
                request.addRequestHeader("Range", "bytes=" + localFileSize + "-");
                if (client.executeMethod(request) != HttpStatus.SC_PARTIAL_CONTENT) {
                    errorString = "HTTP Server does not support partial get";
                    status = TemplateDownloader.Status.UNRECOVERABLE_ERROR;
                    return 0;
                }
            } else if ((responseCode = client.executeMethod(request)) != HttpStatus.SC_OK) {
                status = TemplateDownloader.Status.UNRECOVERABLE_ERROR;
                errorString = " HTTP Server returned " + responseCode + " (expected 200 OK) ";
                return 0; //FIXME: retry?
            }

            final Header contentLengthHeader = request.getResponseHeader("Content-Length");
            boolean chunked = false;
            long remoteSize2 = 0;
            if (contentLengthHeader == null) {
                final Header chunkedHeader = request.getResponseHeader("Transfer-Encoding");
                if (chunkedHeader == null || !"chunked".equalsIgnoreCase(chunkedHeader.getValue())) {
                    status = TemplateDownloader.Status.UNRECOVERABLE_ERROR;
                    errorString = " Failed to receive length of download ";
                    return 0; //FIXME: what status do we put here? Do we retry?
                } else if ("chunked".equalsIgnoreCase(chunkedHeader.getValue())) {
                    chunked = true;
                }
            } else {
                remoteSize2 = Long.parseLong(contentLengthHeader.getValue());
                if (remoteSize2 == 0) {
                    status = TemplateDownloader.Status.DOWNLOAD_FINISHED;
                    final String downloaded = "(download complete remote=" + remoteSize + "bytes)";
                    errorString = "Downloaded " + totalBytes + " bytes " + downloaded;
                    downloadTime = 0;
                    return 0;
                }
            }

            if (remoteSize == 0) {
                remoteSize = remoteSize2;
            }

            if (remoteSize > maxTemplateSizeInBytes) {
                s_logger.info("Remote size is too large: " + remoteSize + " , max=" + maxTemplateSizeInBytes);
                status = Status.UNRECOVERABLE_ERROR;
                errorString = "Download file size is too large";
                return 0;
            }

            if (remoteSize == 0) {
                remoteSize = maxTemplateSizeInBytes;
            }

            final URL url = new URL(getDownloadUrl());
            final InputStream in = url.openStream();

            final RandomAccessFile out = new RandomAccessFile(file, "rw");
            out.seek(localFileSize);

            s_logger.info("Starting download from " + getDownloadUrl() + " to " + toFile + " remoteSize=" + remoteSize + " , max size=" + maxTemplateSizeInBytes);

            final byte[] block = new byte[CHUNK_SIZE];
            long offset = 0;
            boolean done = false;
            boolean verifiedFormat = false;
            status = TemplateDownloader.Status.IN_PROGRESS;
            while (!done && status != Status.ABORTED && offset <= remoteSize) {
                if ((bytes = in.read(block, 0, CHUNK_SIZE)) > -1) {
                    out.write(block, 0, bytes);
                    offset += bytes;
                    out.seek(offset);
                    totalBytes += bytes;
                    if (!verifiedFormat && (offset >= 1048576 || offset >= remoteSize)) { //let's check format after we get 1MB or full file
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
                                request.abort();
                                out.close();
                                in.close();
                            } catch (final Exception ex) {
                                s_logger.debug("Error on http connection : " + ex.getMessage());
                            }
                            status = Status.UNRECOVERABLE_ERROR;
                            errorString = "Template content is unsupported, or mismatch between selected format and template content. Found  : " + unsupportedFormat;
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
            if (totalBytes >= remoteSize) {
                status = TemplateDownloader.Status.DOWNLOAD_FINISHED;
                downloaded = "(download complete remote=" + remoteSize + "bytes)";
            }
            errorString = "Downloaded " + totalBytes + " bytes " + downloaded;
            downloadTime += finish.getTime() - start.getTime();
            in.close();
            out.close();

            return totalBytes;
        } catch (final HttpException hte) {
            status = TemplateDownloader.Status.UNRECOVERABLE_ERROR;
            errorString = hte.getMessage();
        } catch (final IOException ioe) {
            status = TemplateDownloader.Status.UNRECOVERABLE_ERROR; //probably a file write error?
            errorString = ioe.getMessage();
        } finally {
            if (status == Status.UNRECOVERABLE_ERROR && file.exists() && !file.isDirectory()) {
                file.delete();
            }
            request.releaseConnection();
            if (callback != null) {
                callback.downloadComplete(status);
            }
        }
        return 0;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    @Override
    public boolean stopDownload() {
        switch (getStatus()) {
            case IN_PROGRESS:
                if (request != null) {
                    request.abort();
                }
                status = TemplateDownloader.Status.ABORTED;
                return true;
            case UNKNOWN:
            case NOT_STARTED:
            case RECOVERABLE_ERROR:
            case UNRECOVERABLE_ERROR:
            case ABORTED:
                status = TemplateDownloader.Status.ABORTED;
            case DOWNLOAD_FINISHED:
                final File f = new File(toFile);
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
        if (remoteSize == 0) {
            return 0;
        }

        return (int) (100.0 * totalBytes / remoteSize);
    }

    @Override
    public TemplateDownloader.Status getStatus() {
        return status;
    }

    @Override
    public void setStatus(final TemplateDownloader.Status status) {
        this.status = status;
    }

    @Override
    public long getDownloadTime() {
        return downloadTime;
    }

    @Override
    public long getDownloadedBytes() {
        return totalBytes;
    }

    @Override
    public String getDownloadError() {
        return errorString;
    }

    @Override
    public void setDownloadError(final String error) {
        errorString = error;
    }

    @Override
    public String getDownloadLocalPath() {
        return getToFile();
    }

    public String getToFile() {
        final File file = new File(toFile);

        return file.getAbsolutePath();
    }

    public boolean isResume() {
        return resume;
    }

    @Override
    public void setResume(final boolean resume) {
        this.resume = resume;
    }

    @Override
    public boolean isInited() {
        return inited;
    }

    @Override
    public long getMaxTemplateSizeInBytes() {
        return maxTemplateSizeInBytes;
    }

    public String getToDir() {
        return toDir;
    }

    public void setToDir(final String toDir) {
        this.toDir = toDir;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }
}
