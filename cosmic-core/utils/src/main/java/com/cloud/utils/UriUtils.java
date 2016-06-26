//

//

package com.cloud.utils;

import com.cloud.utils.crypt.DBEncryptionUtil;
import com.cloud.utils.exception.CloudRuntimeException;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UriUtils {

    public static final Logger s_logger = LoggerFactory.getLogger(UriUtils.class.getName());

    public static String formNfsUri(final String host, final String path) {
        try {
            final URI uri = new URI("nfs", host, path, null);
            return uri.toString();
        } catch (final URISyntaxException e) {
            throw new CloudRuntimeException("Unable to form nfs URI: " + host + " - " + path);
        }
    }

    public static String formIscsiUri(final String host, final String iqn, final Integer lun) {
        try {
            String path = iqn;
            if (lun != null) {
                path += "/" + lun.toString();
            }
            final URI uri = new URI("iscsi", host, path, null);
            return uri.toString();
        } catch (final URISyntaxException e) {
            throw new CloudRuntimeException("Unable to form iscsi URI: " + host + " - " + iqn + " - " + lun);
        }
    }

    public static String formFileUri(final String path) {
        final File file = new File(path);

        return file.toURI().toString();
    }

    // a simple URI component helper (Note: it does not deal with URI paramemeter area)
    public static String encodeURIComponent(final String url) {
        final int schemeTail = url.indexOf("://");

        int pathStart = 0;
        if (schemeTail > 0) {
            pathStart = url.indexOf('/', schemeTail + 3);
        } else {
            pathStart = url.indexOf('/');
        }

        if (pathStart > 0) {
            final String[] tokens = url.substring(pathStart + 1).split("/");
            final StringBuilder sb = new StringBuilder(url.substring(0, pathStart));
            for (final String token : tokens) {
                sb.append("/").append(URLEncoder.encode(token));
            }

            return sb.toString();
        }

        // no need to do URL component encoding
        return url;
    }

    public static String getCifsUriParametersProblems(final URI uri) {
        if (!UriUtils.hostAndPathPresent(uri)) {
            final String errMsg = "cifs URI missing host and/or path. Make sure it's of the format cifs://hostname/path";
            s_logger.warn(errMsg);
            return errMsg;
        }
        return null;
    }

    public static boolean hostAndPathPresent(final URI uri) {
        return !(uri.getHost() == null || uri.getHost().trim().isEmpty() || uri.getPath() == null || uri.getPath().trim().isEmpty());
    }

    public static boolean cifsCredentialsPresent(final URI uri) {
        final List<NameValuePair> args = URLEncodedUtils.parse(uri, "UTF-8");
        boolean foundUser = false;
        boolean foundPswd = false;
        for (final NameValuePair nvp : args) {
            final String name = nvp.getName();
            if (name.equals("user")) {
                foundUser = true;
                s_logger.debug("foundUser is" + foundUser);
            } else if (name.equals("password")) {
                foundPswd = true;
                s_logger.debug("foundPswd is" + foundPswd);
            }
        }
        return (foundUser && foundPswd);
    }

    public static String getUpdateUri(final String url, final boolean encrypt) {
        String updatedPath = null;
        try {
            final String query = URIUtil.getQuery(url);
            final URIBuilder builder = new URIBuilder(url);
            builder.removeQuery();

            final StringBuilder updatedQuery = new StringBuilder();
            final List<NameValuePair> queryParams = getUserDetails(query);
            final ListIterator<NameValuePair> iterator = queryParams.listIterator();
            while (iterator.hasNext()) {
                final NameValuePair param = iterator.next();
                String value = null;
                if ("password".equalsIgnoreCase(param.getName()) &&
                        param.getValue() != null) {
                    value = encrypt ? DBEncryptionUtil.encrypt(param.getValue()) : DBEncryptionUtil.decrypt(param.getValue());
                } else {
                    value = param.getValue();
                }

                if (updatedQuery.length() == 0) {
                    updatedQuery.append(param.getName()).append('=')
                                .append(value);
                } else {
                    updatedQuery.append('&').append(param.getName())
                                .append('=').append(value);
                }
            }

            String schemeAndHost = "";
            final URI newUri = builder.build();
            if (newUri.getScheme() != null) {
                schemeAndHost = newUri.getScheme() + "://" + newUri.getHost();
            }

            updatedPath = schemeAndHost + newUri.getPath() + "?" + updatedQuery;
        } catch (final URISyntaxException e) {
            throw new CloudRuntimeException("Couldn't generate an updated uri. " + e.getMessage());
        }

        return updatedPath;
    }

    private static List<NameValuePair> getUserDetails(final String query) {
        final List<NameValuePair> details = new ArrayList<>();
        if (query != null && !query.isEmpty()) {
            final StringTokenizer allParams = new StringTokenizer(query, "&");
            while (allParams.hasMoreTokens()) {
                final String param = allParams.nextToken();
                details.add(new BasicNameValuePair(param.substring(0, param.indexOf("=")),
                        param.substring(param.indexOf("=") + 1)));
            }
        }

        return details;
    }

    // Get the size of a file from URL response header.
    public static Long getRemoteSize(final String url) {
        Long remoteSize = (long) 0;
        HttpURLConnection httpConn = null;
        HttpsURLConnection httpsConn = null;
        try {
            final URI uri = new URI(url);
            if (uri.getScheme().equalsIgnoreCase("http")) {
                httpConn = (HttpURLConnection) uri.toURL().openConnection();
                if (httpConn != null) {
                    httpConn.setConnectTimeout(2000);
                    httpConn.setReadTimeout(5000);
                    final String contentLength = httpConn.getHeaderField("content-length");
                    if (contentLength != null) {
                        remoteSize = Long.parseLong(contentLength);
                    }
                    httpConn.disconnect();
                }
            } else if (uri.getScheme().equalsIgnoreCase("https")) {
                httpsConn = (HttpsURLConnection) uri.toURL().openConnection();
                if (httpsConn != null) {
                    final String contentLength = httpsConn.getHeaderField("content-length");
                    if (contentLength != null) {
                        remoteSize = Long.parseLong(contentLength);
                    }
                    httpsConn.disconnect();
                }
            }
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL " + url);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Unable to establish connection with URL " + url);
        }
        return remoteSize;
    }

    // use http HEAD method to validate url
    public static void checkUrlExistence(final String url) {
        if (url.toLowerCase().startsWith("http") || url.toLowerCase().startsWith("https")) {
            final HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
            final HeadMethod httphead = new HeadMethod(url);
            try {
                if (httpClient.executeMethod(httphead) != HttpStatus.SC_OK) {
                    throw new IllegalArgumentException("Invalid URL: " + url);
                }
            } catch (final HttpException hte) {
                throw new IllegalArgumentException("Cannot reach URL: " + url);
            } catch (final IOException ioe) {
                throw new IllegalArgumentException("Cannot reach URL: " + url);
            }
        }
    }

    public static InputStream getInputStreamFromUrl(final String url, final String user, final String password) {

        try {
            final Pair<String, Integer> hostAndPort = validateUrl(url);
            final HttpClient httpclient = new HttpClient(new MultiThreadedHttpConnectionManager());
            if ((user != null) && (password != null)) {
                httpclient.getParams().setAuthenticationPreemptive(true);
                final Credentials defaultcreds = new UsernamePasswordCredentials(user, password);
                httpclient.getState().setCredentials(new AuthScope(hostAndPort.first(), hostAndPort.second(), AuthScope.ANY_REALM), defaultcreds);
                s_logger.info("Added username=" + user + ", password=" + password + "for host " + hostAndPort.first() + ":" + hostAndPort.second());
            }
            // Execute the method.
            final GetMethod method = new GetMethod(url);
            final int statusCode = httpclient.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                s_logger.error("Failed to read from URL: " + url);
                return null;
            }

            return method.getResponseBodyAsStream();
        } catch (final Exception ex) {
            s_logger.error("Failed to read from URL: " + url);
            return null;
        }
    }

    public static Pair<String, Integer> validateUrl(final String url) throws IllegalArgumentException {
        return validateUrl(null, url);
    }

    public static Pair<String, Integer> validateUrl(final String format, final String url) throws IllegalArgumentException {
        try {
            final URI uri = new URI(url);
            if ((uri.getScheme() == null) ||
                    (!uri.getScheme().equalsIgnoreCase("http") && !uri.getScheme().equalsIgnoreCase("https") && !uri.getScheme().equalsIgnoreCase("file"))) {
                throw new IllegalArgumentException("Unsupported scheme for url: " + url);
            }
            int port = uri.getPort();
            if (!(port == 80 || port == 8080 || port == 443 || port == -1)) {
                throw new IllegalArgumentException("Only ports 80, 8080 and 443 are allowed");
            }

            if (port == -1 && uri.getScheme().equalsIgnoreCase("https")) {
                port = 443;
            } else if (port == -1 && uri.getScheme().equalsIgnoreCase("http")) {
                port = 80;
            }

            final String host = uri.getHost();
            try {
                final InetAddress hostAddr = InetAddress.getByName(host);
                if (hostAddr.isAnyLocalAddress() || hostAddr.isLinkLocalAddress() || hostAddr.isLoopbackAddress() || hostAddr.isMulticastAddress()) {
                    throw new IllegalArgumentException("Illegal host specified in url");
                }
                if (hostAddr instanceof Inet6Address) {
                    throw new IllegalArgumentException("IPV6 addresses not supported (" + hostAddr.getHostAddress() + ")");
                }
            } catch (final UnknownHostException uhe) {
                throw new IllegalArgumentException("Unable to resolve " + host);
            }

            // verify format
            if (format != null) {
                final String uripath = uri.getPath();
                checkFormat(format, uripath);
            }
            return new Pair<>(host, port);
        } catch (final URISyntaxException use) {
            throw new IllegalArgumentException("Invalid URL: " + url);
        }
    }

    // verify if a URI path is compliance with the file format given
    private static void checkFormat(final String format, final String uripath) {
        if ((!uripath.toLowerCase().endsWith("vhd")) && (!uripath.toLowerCase().endsWith("vhd.zip")) && (!uripath.toLowerCase().endsWith("vhd.bz2")) &&
                (!uripath.toLowerCase().endsWith("vhdx")) && (!uripath.toLowerCase().endsWith("vhdx.gz")) &&
                (!uripath.toLowerCase().endsWith("vhdx.bz2")) && (!uripath.toLowerCase().endsWith("vhdx.zip")) &&
                (!uripath.toLowerCase().endsWith("vhd.gz")) && (!uripath.toLowerCase().endsWith("qcow2")) && (!uripath.toLowerCase().endsWith("qcow2.zip")) &&
                (!uripath.toLowerCase().endsWith("qcow2.bz2")) && (!uripath.toLowerCase().endsWith("qcow2.gz")) && (!uripath.toLowerCase().endsWith("ova")) &&
                (!uripath.toLowerCase().endsWith("ova.zip")) && (!uripath.toLowerCase().endsWith("ova.bz2")) && (!uripath.toLowerCase().endsWith("ova.gz")) &&
                (!uripath.toLowerCase().endsWith("tar")) && (!uripath.toLowerCase().endsWith("tar.zip")) && (!uripath.toLowerCase().endsWith("tar.bz2")) &&
                (!uripath.toLowerCase().endsWith("tar.gz")) && (!uripath.toLowerCase().endsWith("vmdk")) && (!uripath.toLowerCase().endsWith("vmdk.gz")) &&
                (!uripath.toLowerCase().endsWith("vmdk.zip")) && (!uripath.toLowerCase().endsWith("vmdk.bz2")) && (!uripath.toLowerCase().endsWith("img")) &&
                (!uripath.toLowerCase().endsWith("img.gz")) && (!uripath.toLowerCase().endsWith("img.zip")) && (!uripath.toLowerCase().endsWith("img.bz2")) &&
                (!uripath.toLowerCase().endsWith("raw")) && (!uripath.toLowerCase().endsWith("raw.gz")) && (!uripath.toLowerCase().endsWith("raw.bz2")) &&
                (!uripath.toLowerCase().endsWith("raw.zip")) && (!uripath.toLowerCase().endsWith("iso")) && (!uripath.toLowerCase().endsWith("iso.zip"))
                && (!uripath.toLowerCase().endsWith("iso.bz2")) && (!uripath.toLowerCase().endsWith("iso.gz"))) {
            throw new IllegalArgumentException("Please specify a valid " + format.toLowerCase());
        }

        if ((format.equalsIgnoreCase("vhd")
                && (!uripath.toLowerCase().endsWith("vhd")
                && !uripath.toLowerCase().endsWith("vhd.zip")
                && !uripath.toLowerCase().endsWith("vhd.bz2")
                && !uripath.toLowerCase().endsWith("vhd.gz")))
                || (format.equalsIgnoreCase("vhdx")
                && (!uripath.toLowerCase().endsWith("vhdx")
                && !uripath.toLowerCase().endsWith("vhdx.zip")
                && !uripath.toLowerCase().endsWith("vhdx.bz2")
                && !uripath.toLowerCase().endsWith("vhdx.gz")))
                || (format.equalsIgnoreCase("qcow2")
                && (!uripath.toLowerCase().endsWith("qcow2")
                && !uripath.toLowerCase().endsWith("qcow2.zip")
                && !uripath.toLowerCase().endsWith("qcow2.bz2")
                && !uripath.toLowerCase().endsWith("qcow2.gz")))
                || (format.equalsIgnoreCase("ova")
                && (!uripath.toLowerCase().endsWith("ova")
                && !uripath.toLowerCase().endsWith("ova.zip")
                && !uripath.toLowerCase().endsWith("ova.bz2")
                && !uripath.toLowerCase().endsWith("ova.gz")))
                || (format.equalsIgnoreCase("tar")
                && (!uripath.toLowerCase().endsWith("tar")
                && !uripath.toLowerCase().endsWith("tar.zip")
                && !uripath.toLowerCase().endsWith("tar.bz2")
                && !uripath.toLowerCase().endsWith("tar.gz")))
                || (format.equalsIgnoreCase("raw")
                && (!uripath.toLowerCase().endsWith("img")
                && !uripath.toLowerCase().endsWith("img.zip")
                && !uripath.toLowerCase().endsWith("img.bz2")
                && !uripath.toLowerCase().endsWith("img.gz")
                && !uripath.toLowerCase().endsWith("raw")
                && !uripath.toLowerCase().endsWith("raw.bz2")
                && !uripath.toLowerCase().endsWith("raw.zip")
                && !uripath.toLowerCase().endsWith("raw.gz")))
                || (format.equalsIgnoreCase("vmdk")
                && (!uripath.toLowerCase().endsWith("vmdk")
                && !uripath.toLowerCase().endsWith("vmdk.zip")
                && !uripath.toLowerCase().endsWith("vmdk.bz2")
                && !uripath.toLowerCase().endsWith("vmdk.gz")))
                || (format.equalsIgnoreCase("iso")
                && (!uripath.toLowerCase().endsWith("iso")
                && !uripath.toLowerCase().endsWith("iso.zip")
                && !uripath.toLowerCase().endsWith("iso.bz2")
                && !uripath.toLowerCase().endsWith("iso.gz")))) {
            throw new IllegalArgumentException("Please specify a valid URL. URL:" + uripath + " is an invalid for the format " + format.toLowerCase());
        }
    }
}
