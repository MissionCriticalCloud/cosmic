package com.cloud.consoleproxy;

import com.cloud.consoleproxy.util.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ConsoleProxyResourceHandler implements HttpHandler {
    private static final Logger s_logger = Logger.getLogger(ConsoleProxyResourceHandler.class);

    static Map<String, String> s_mimeTypes;
    static Map<String, String> s_validResourceFolders;

    static {
        s_mimeTypes = new HashMap<>();
        s_mimeTypes.put("jar", "application/java-archive");
        s_mimeTypes.put("js", "text/javascript");
        s_mimeTypes.put("css", "text/css");
        s_mimeTypes.put("jpg", "image/jpeg");
        s_mimeTypes.put("html", "text/html");
        s_mimeTypes.put("htm", "text/html");
        s_mimeTypes.put("log", "text/plain");
    }

    static {
        s_validResourceFolders = new HashMap<>();
        s_validResourceFolders.put("applet", "");
        s_validResourceFolders.put("logs", "");
        s_validResourceFolders.put("images", "");
        s_validResourceFolders.put("js", "");
        s_validResourceFolders.put("css", "");
        s_validResourceFolders.put("html", "");
    }

    public ConsoleProxyResourceHandler() {
    }

    @Override
    public void handle(final HttpExchange t) throws IOException {
        try {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Resource Handler " + t.getRequestURI());
            }

            final long startTick = System.currentTimeMillis();

            doHandle(t);

            if (s_logger.isDebugEnabled()) {
                s_logger.debug(t.getRequestURI() + " Process time " + (System.currentTimeMillis() - startTick) + " ms");
            }
        } catch (final IOException e) {
            throw e;
        } catch (final Throwable e) {
            s_logger.error("Unexpected exception, ", e);
            t.sendResponseHeaders(500, -1);     // server error
        } finally {
            t.close();
        }
    }

    private void doHandle(final HttpExchange t) throws Exception {
        final String path = t.getRequestURI().getPath();

        if (s_logger.isInfoEnabled()) {
            s_logger.info("Get resource request for " + path);
        }

        int i = path.indexOf("/", 1);
        final String filepath = path.substring(i + 1);
        i = path.lastIndexOf(".");
        final String extension = (i == -1) ? "" : path.substring(i + 1);
        final String contentType = getContentType(extension);

        if (!validatePath(filepath)) {
            if (s_logger.isInfoEnabled()) {
                s_logger.info("Resource access is forbidden, uri: " + path);
            }

            t.sendResponseHeaders(403, -1);     // forbidden
            return;
        }

        final File f = new File("./" + filepath);
        if (f.exists()) {
            final long lastModified = f.lastModified();
            final String ifModifiedSince = t.getRequestHeaders().getFirst("If-Modified-Since");
            if (ifModifiedSince != null) {
                final long d = Date.parse(ifModifiedSince);
                if (d + 1000 >= lastModified) {
                    final Headers hds = t.getResponseHeaders();
                    hds.set("Content-Type", contentType);
                    t.sendResponseHeaders(304, -1);

                    if (s_logger.isInfoEnabled()) {
                        s_logger.info("Sent 304 file has not been " + "modified since " + ifModifiedSince);
                    }
                    return;
                }
            }

            final long length = f.length();
            final Headers hds = t.getResponseHeaders();
            hds.set("Content-Type", contentType);
            hds.set("Last-Modified", new Date(lastModified).toGMTString());
            t.sendResponseHeaders(200, length);
            responseFileContent(t, f);

            if (s_logger.isInfoEnabled()) {
                s_logger.info("Sent file " + path + " with content type " + contentType);
            }
        } else {
            if (s_logger.isInfoEnabled()) {
                s_logger.info("file does not exist" + path);
            }
            t.sendResponseHeaders(404, -1);
        }
    }

    private static String getContentType(final String extension) {
        final String key = extension.toLowerCase();
        if (s_mimeTypes.containsKey(key)) {
            return s_mimeTypes.get(key);
        }
        return "application/octet-stream";
    }

    private static boolean validatePath(final String path) {
        final int i = path.indexOf("/");
        if (i == -1) {
            if (s_logger.isInfoEnabled()) {
                s_logger.info("Invalid resource path: can not start at resource root");
            }
            return false;
        }

        if (path.contains("..")) {
            if (s_logger.isInfoEnabled()) {
                s_logger.info("Invalid resource path: contains relative up-level navigation");
            }

            return false;
        }

        return isValidResourceFolder(path.substring(0, i));
    }

    private static void responseFileContent(final HttpExchange t, final File f) throws Exception {
        try (OutputStream os = t.getResponseBody();
             FileInputStream fis = new FileInputStream(f)) {
            while (true) {
                final byte[] b = new byte[8192];
                final int n = fis.read(b);
                if (n < 0) {
                    break;
                }
                os.write(b, 0, n);
            }
        }
    }

    private static boolean isValidResourceFolder(final String name) {
        return s_validResourceFolders.containsKey(name);
    }
}
