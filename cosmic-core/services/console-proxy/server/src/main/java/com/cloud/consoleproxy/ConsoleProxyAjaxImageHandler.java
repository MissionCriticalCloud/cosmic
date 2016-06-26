package com.cloud.consoleproxy;

import com.cloud.consoleproxy.util.Logger;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ConsoleProxyAjaxImageHandler implements HttpHandler {
    private static final Logger s_logger = Logger.getLogger(ConsoleProxyAjaxImageHandler.class);

    @Override
    public void handle(final HttpExchange t) throws IOException {
        try {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("AjaxImageHandler " + t.getRequestURI());
            }

            final long startTick = System.currentTimeMillis();

            doHandle(t);

            if (s_logger.isDebugEnabled()) {
                s_logger.debug(t.getRequestURI() + "Process time " + (System.currentTimeMillis() - startTick) + " ms");
            }
        } catch (final IOException e) {
            throw e;
        } catch (final IllegalArgumentException e) {
            s_logger.warn("Exception, ", e);
            t.sendResponseHeaders(400, -1);     // bad request
        } catch (final OutOfMemoryError e) {
            s_logger.error("Unrecoverable OutOfMemory Error, exit and let it be re-launched");
            System.exit(1);
        } catch (final Throwable e) {
            s_logger.error("Unexpected exception, ", e);
            t.sendResponseHeaders(500, -1);     // server error
        } finally {
            t.close();
        }
    }

    private void doHandle(final HttpExchange t) throws Exception, IllegalArgumentException {
        final String queries = t.getRequestURI().getQuery();
        final Map<String, String> queryMap = ConsoleProxyHttpHandlerHelper.getQueryMap(queries);

        final String host = queryMap.get("host");
        final String portStr = queryMap.get("port");
        final String sid = queryMap.get("sid");
        String tag = queryMap.get("tag");
        final String ticket = queryMap.get("ticket");
        final String keyStr = queryMap.get("key");
        final String console_url = queryMap.get("consoleurl");
        final String console_host_session = queryMap.get("sessionref");
        final String w = queryMap.get("w");
        final String h = queryMap.get("h");

        int key = 0;
        int width = 144;
        int height = 110;

        if (tag == null) {
            tag = "";
        }

        final int port;
        if (host == null || portStr == null || sid == null) {
            throw new IllegalArgumentException();
        }

        try {
            port = Integer.parseInt(portStr);
        } catch (final NumberFormatException e) {
            s_logger.warn("Invalid numeric parameter in query string: " + portStr);
            throw new IllegalArgumentException(e);
        }

        try {
            if (keyStr != null) {
                key = Integer.parseInt(keyStr);
            }
            if (null != w) {
                width = Integer.parseInt(w);
            }

            if (null != h) {
                height = Integer.parseInt(h);
            }
        } catch (final NumberFormatException e) {
            s_logger.warn("Invalid numeric parameter in query string: " + keyStr);
            throw new IllegalArgumentException(e);
        }

        final ConsoleProxyClientParam param = new ConsoleProxyClientParam();
        param.setClientHostAddress(host);
        param.setClientHostPort(port);
        param.setClientHostPassword(sid);
        param.setClientTag(tag);
        param.setTicket(ticket);
        param.setClientTunnelUrl(console_url);
        param.setClientTunnelSession(console_host_session);

        final ConsoleProxyClient viewer = ConsoleProxy.getVncViewer(param);

        if (key == 0) {
            final Image scaledImage = viewer.getClientScaledImage(width, height);
            final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            final Graphics2D bufImageGraphics = bufferedImage.createGraphics();
            bufImageGraphics.drawImage(scaledImage, 0, 0, null);
            final ByteArrayOutputStream bos = new ByteArrayOutputStream(8196);
            javax.imageio.ImageIO.write(bufferedImage, "jpg", bos);
            final byte[] bs = bos.toByteArray();
            final Headers hds = t.getResponseHeaders();
            hds.set("Content-Type", "image/jpeg");
            hds.set("Cache-Control", "no-cache");
            hds.set("Cache-Control", "no-store");
            t.sendResponseHeaders(200, bs.length);
            final OutputStream os = t.getResponseBody();
            os.write(bs);
            os.close();
        } else {
            final AjaxFIFOImageCache imageCache = viewer.getAjaxImageCache();
            final byte[] img = imageCache.getImage(key);

            if (img != null) {
                final Headers hds = t.getResponseHeaders();
                hds.set("Content-Type", "image/jpeg");
                t.sendResponseHeaders(200, img.length);

                final OutputStream os = t.getResponseBody();
                try {
                    os.write(img, 0, img.length);
                } finally {
                    os.close();
                }
            } else {
                if (s_logger.isInfoEnabled()) {
                    s_logger.info("Image has already been swept out, key: " + key);
                }
                t.sendResponseHeaders(404, -1);
            }
        }
    }
}
