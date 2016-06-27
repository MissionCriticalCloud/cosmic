package com.cloud.consoleproxy;

import com.cloud.consoleproxy.util.Logger;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ConsoleProxyThumbnailHandler implements HttpHandler {
    private static final Logger s_logger = Logger.getLogger(ConsoleProxyThumbnailHandler.class);

    public ConsoleProxyThumbnailHandler() {
    }

    public static BufferedImage generateTextImage(final int w, final int h, final String text) {
        final BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        final Graphics2D g = img.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, w, h);
        g.setColor(Color.WHITE);
        try {
            g.setFont(new Font(null, Font.PLAIN, 12));
            final FontMetrics fm = g.getFontMetrics();
            final int textWidth = fm.stringWidth(text);
            int startx = (w - textWidth) / 2;
            if (startx < 0) {
                startx = 0;
            }
            g.drawString(text, startx, h / 2);
        } catch (final Throwable e) {
            s_logger.warn("Problem in generating text to thumnail image, return blank image");
        }
        return img;
    }

    @Override
    public void handle(final HttpExchange t) throws IOException {
        try {
            Thread.currentThread().setName("JPG Thread " + Thread.currentThread().getId() + " " + t.getRemoteAddress());

            if (s_logger.isDebugEnabled()) {
                s_logger.debug("ScreenHandler " + t.getRequestURI());
            }

            final long startTick = System.currentTimeMillis();
            doHandle(t);

            if (s_logger.isDebugEnabled()) {
                s_logger.debug(t.getRequestURI() + "Process time " + (System.currentTimeMillis() - startTick) + " ms");
            }
        } catch (final IllegalArgumentException e) {
            final String response = "Bad query string";
            s_logger.error(response + ", request URI : " + t.getRequestURI());
            t.sendResponseHeaders(200, response.length());
            final OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (final OutOfMemoryError e) {
            s_logger.error("Unrecoverable OutOfMemory Error, exit and let it be re-launched");
            System.exit(1);
        } catch (final Throwable e) {
            s_logger.error("Unexpected exception while handing thumbnail request, ", e);

            final String queries = t.getRequestURI().getQuery();
            final Map<String, String> queryMap = getQueryMap(queries);
            int width = 0;
            int height = 0;
            final String ws = queryMap.get("w");
            final String hs = queryMap.get("h");
            try {
                width = Integer.parseInt(ws);
                height = Integer.parseInt(hs);
            } catch (final NumberFormatException ex) {
                s_logger.debug("Cannot parse width: " + ws + " or height: " + hs, ex);
            }
            width = Math.min(width, 800);
            height = Math.min(height, 600);

            final BufferedImage img = generateTextImage(width, height, "Cannot Connect");
            final ByteArrayOutputStream bos = new ByteArrayOutputStream(8196);
            javax.imageio.ImageIO.write(img, "jpg", bos);
            final byte[] bs = bos.toByteArray();
            final Headers hds = t.getResponseHeaders();
            hds.set("Content-Type", "image/jpeg");
            hds.set("Cache-Control", "no-cache");
            hds.set("Cache-Control", "no-store");
            t.sendResponseHeaders(200, bs.length);
            final OutputStream os = t.getResponseBody();
            os.write(bs);
            os.close();
            s_logger.error("Cannot get console, sent error JPG response for " + t.getRequestURI());
            return;
        } finally {
            t.close();
        }
    }

    private void doHandle(final HttpExchange t) throws Exception, IllegalArgumentException {
        final String queries = t.getRequestURI().getQuery();
        final Map<String, String> queryMap = getQueryMap(queries);
        int width = 0;
        int height = 0;
        int port = 0;
        final String ws = queryMap.get("w");
        final String hs = queryMap.get("h");
        final String host = queryMap.get("host");
        final String portStr = queryMap.get("port");
        final String sid = queryMap.get("sid");
        String tag = queryMap.get("tag");
        final String ticket = queryMap.get("ticket");
        final String console_url = queryMap.get("consoleurl");
        final String console_host_session = queryMap.get("sessionref");

        if (tag == null) {
            tag = "";
        }

        if (ws == null || hs == null || host == null || portStr == null || sid == null) {
            throw new IllegalArgumentException();
        }
        try {
            width = Integer.parseInt(ws);
            height = Integer.parseInt(hs);
            port = Integer.parseInt(portStr);
        } catch (final NumberFormatException e) {
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

        if (!viewer.isHostConnected()) {
            // use generated image instead of static
            final BufferedImage img = generateTextImage(width, height, "Connecting");
            final ByteArrayOutputStream bos = new ByteArrayOutputStream(8196);
            javax.imageio.ImageIO.write(img, "jpg", bos);
            final byte[] bs = bos.toByteArray();
            final Headers hds = t.getResponseHeaders();
            hds.set("Content-Type", "image/jpeg");
            hds.set("Cache-Control", "no-cache");
            hds.set("Cache-Control", "no-store");
            t.sendResponseHeaders(200, bs.length);
            final OutputStream os = t.getResponseBody();
            os.write(bs);
            os.close();

            if (s_logger.isInfoEnabled()) {
                s_logger.info("Console not ready, sent dummy JPG response");
            }
            return;
        }

        {
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
        }
    }

    public static Map<String, String> getQueryMap(final String query) {
        final String[] params = query.split("&");
        final Map<String, String> map = new HashMap<>();
        for (final String param : params) {
            final String name = param.split("=")[0];
            final String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }
}
