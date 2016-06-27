package com.cloud.consoleproxy.util;

import org.apache.cloudstack.utils.security.SSLUtils;
import org.apache.cloudstack.utils.security.SecureSSLSocketFactory;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//
// This file is originally from XenConsole with modifications
//

/**
 * Send an HTTP CONNECT or PUT request to a XenAPI host with a Session ID,
 * return the connected socket and the Task ID. Used for tunnelling VNC
 * connections and import/export operations.
 */
public final class RawHTTP {
    private static final Logger s_logger = Logger.getLogger(RawHTTP.class);

    private static final Pattern END_PATTERN = Pattern.compile("^\r\n$");
    private static final Pattern HEADER_PATTERN = Pattern.compile("^([A-Z_a-z0-9-]+):\\s*(.*)\r\n$");
    private static final Pattern HTTP_PATTERN = Pattern.compile("^HTTP/\\d+\\.\\d+ (\\d*) (.*)\r\n$");
    private static final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        @Override
        public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }};
    /**
     * @uml.property name="command"
     */
    private final String command;
    /**
     * @uml.property name="host"
     */
    private final String host;
    /**
     * @uml.property name="port"
     */
    private final int port;
    /**
     * @uml.property name="path"
     */
    private final String path;
    /**
     * @uml.property name="session"
     */
    private final String session;
    /**
     * @uml.property name="useSSL"
     */
    private final boolean useSSL;
    /**
     * @uml.property name="responseHeaders"
     * @uml.associationEnd qualifier="group:java.lang.String java.lang.String"
     */
    private final Map<String, String> responseHeaders = new HashMap<>();
    /**
     * @uml.property name="ic"
     */
    private InputStream ic;
    /**
     * @uml.property name="oc"
     */
    private OutputStream oc;
    /**
     * @uml.property name="s"
     */
    private Socket s;

    public RawHTTP(final String command, final String host, final int port, final String path, final String session, final boolean useSSL) {
        this.command = command;
        this.host = host;
        this.port = port;
        this.path = path;
        this.session = session;
        this.useSSL = useSSL;
    }

    public InputStream getInputStream() {
        return ic;
    }

    public OutputStream getOutputStream() {
        return oc;
    }

    public Socket getSocket() {
        return s;
    }

    public Socket connect() throws IOException {
        final String[] headers = makeHeaders();
        s = _getSocket();
        try {
            oc = s.getOutputStream();
            for (final String header : headers) {
                oc.write(header.getBytes());
                oc.write("\r\n".getBytes());
            }
            oc.flush();
            ic = s.getInputStream();
            while (true) {
                final String line = readline(ic);

                Matcher m = END_PATTERN.matcher(line);
                if (m.matches()) {
                    return s;
                }

                m = HEADER_PATTERN.matcher(line);
                if (m.matches()) {
                    responseHeaders.put(m.group(1), m.group(2));
                    continue;
                }

                m = HTTP_PATTERN.matcher(line);
                if (m.matches()) {
                    final String status_code = m.group(1);
                    final String reason_phrase = m.group(2);
                    if (!"200".equals(status_code)) {
                        throw new IOException("HTTP status " + status_code + " " + reason_phrase);
                    }
                } else {
                    throw new IOException("Unknown HTTP line " + line);
                }
            }
        } catch (final IOException exn) {
            s.close();
            throw exn;
        } catch (final RuntimeException exn) {
            s.close();
            throw exn;
        }
    }

    private String[] makeHeaders() {
        final String[] headers = {String.format("%s %s HTTP/1.0", command, path), String.format("Host: %s", host), String.format("Cookie: session_id=%s", session), ""};
        return headers;
    }

    private Socket _getSocket() throws IOException {
        if (useSSL) {
            SSLContext context = null;
            try {
                context = SSLUtils.getSSLContext("SunJSSE");
            } catch (final NoSuchAlgorithmException e) {
                s_logger.error("Unexpected exception ", e);
            } catch (final NoSuchProviderException e) {
                s_logger.error("Unexpected exception ", e);
            }

            if (context == null) {
                throw new IOException("Unable to setup SSL context");
            }

            SSLSocket ssl = null;
            try {
                context.init(null, trustAllCerts, new SecureRandom());
                final SocketFactory factory = new SecureSSLSocketFactory(context);
                ssl = (SSLSocket) factory.createSocket(host, port);
                ssl.setEnabledProtocols(SSLUtils.getSupportedProtocols(ssl.getEnabledProtocols()));
                /* ssl.setSSLParameters(context.getDefaultSSLParameters()); */
            } catch (final IOException e) {
                s_logger.error("IOException: " + e.getMessage(), e);
                throw e;
            } catch (final KeyManagementException e) {
                s_logger.error("KeyManagementException: " + e.getMessage(), e);
            } catch (final NoSuchAlgorithmException e) {
                s_logger.error("NoSuchAlgorithmException: " + e.getMessage(), e);
            }
            return ssl;
        } else {
            return new Socket(host, port);
        }
    }

    private static String readline(final InputStream ic) throws IOException {
        String result = "";
        while (true) {
            try {
                final int c = ic.read();

                if (c == -1) {
                    return result;
                }
                result = result + (char) c;
                if (c == 0x0a /* LF */) {
                    return result;
                }
            } catch (final IOException e) {
                ic.close();
                throw e;
            }
        }
    }

    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }
}
