package com.cloud.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Serves static resources with support for gzip compression and content
 * caching.
 */
public class StaticResourceServlet extends HttpServlet {

    static final Map<String, String> contentTypes = Collections
            .unmodifiableMap(new HashMap<String, String>() {
                {
                    put("css", "text/css");
                    put("svg", "image/svg+xml");
                    put("js", "application/javascript");
                    put("htm", "text/html");
                    put("html", "text/html");
                    put("txt", "text/plain");
                    put("xml", "text/xml");
                }
            });
    private static final long serialVersionUID = -8833228931973461812L;

    @Override
    protected void doGet(final HttpServletRequest req,
                         final HttpServletResponse resp) throws ServletException,
            IOException {
        final File requestedFile = getRequestedFile(req);
        if (!requestedFile.exists() || !requestedFile.isFile()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        final String etag = getEtag(requestedFile);
        if (etag.equals(req.getHeader("If-None-Match"))) {
            resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        // have to send data, either compressed or the original
        final File compressedStatic = getCompressedVersion(requestedFile);
        InputStream fileContent = null;
        try {
            resp.setContentType(getContentType(requestedFile.getName()));
            resp.setHeader("ETag", etag);
            resp.setStatus(HttpServletResponse.SC_OK);
            if (isClientCompressionSupported(req) && compressedStatic.exists()) {
                // gzip compressed
                resp.setHeader("Content-Encoding", "gzip");
                resp.setContentLength((int) compressedStatic.length());
                fileContent = new FileInputStream(compressedStatic);
            } else {
                // uncompressed
                resp.setContentLength((int) requestedFile.length());
                fileContent = new FileInputStream(requestedFile);
            }
            IOUtils.copy(fileContent, resp.getOutputStream());
        } finally {
            IOUtils.closeQuietly(fileContent);
        }
    }

    private File getRequestedFile(final HttpServletRequest req) {
        return new File(getServletContext().getRealPath(req.getServletPath()));
    }

    static String getEtag(final File resource) {
        return "W/\"" + resource.length() + "-" + resource.lastModified();
    }

    static File getCompressedVersion(final File requestedFile) {
        return new File(requestedFile.getAbsolutePath() + ".gz");
    }

    static String getContentType(final String fileName) {
        return contentTypes.get(StringUtils.lowerCase(StringUtils
                .substringAfterLast(fileName, ".")));
    }

    static boolean isClientCompressionSupported(final HttpServletRequest req) {
        return StringUtils.contains(req.getHeader("Accept-Encoding"), "gzip");
    }
}
