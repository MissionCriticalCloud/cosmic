package com.cloud.consoleproxy.vnc;

import com.cloud.consoleproxy.util.ImageHelper;
import com.cloud.consoleproxy.util.Logger;
import com.cloud.consoleproxy.util.TileInfo;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

/**
 * A <code>BuffereImageCanvas</code> component represents frame buffer image on
 * the screen. It also notifies its subscribers when screen is repainted.
 */
public class BufferedImageCanvas extends Canvas implements FrameBufferCanvas {
    private static final long serialVersionUID = 1L;
    private static final Logger s_logger = Logger.getLogger(BufferedImageCanvas.class);
    private final PaintNotificationListener listener;
    // Offline screen buffer
    private BufferedImage offlineImage;
    // Cached Graphics2D object for offline screen buffer
    private Graphics2D graphics;

    public BufferedImageCanvas(final PaintNotificationListener listener, final int width, final int height) {
        super();
        this.listener = listener;

        setBackground(Color.black);

        setFocusable(true);

        // Don't intercept TAB key
        setFocusTraversalKeysEnabled(false);

        setCanvasSize(width, height);
    }

    public void setCanvasSize(final int width, final int height) {
        offlineImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        graphics = offlineImage.createGraphics();

        setSize(offlineImage.getWidth(), offlineImage.getHeight());
    }

    public BufferedImage getOfflineImage() {
        return offlineImage;
    }

    public Graphics2D getOfflineGraphics() {
        return graphics;
    }

    @Override
    public void update(final Graphics g) {
        // Call paint() directly, without clearing screen first
        paint(g);
    }

    public void copyTile(final Graphics2D g, final int x, final int y, final Rectangle rc) {
        synchronized (offlineImage) {
            g.drawImage(offlineImage, x, y, x + rc.width, y + rc.height, rc.x, rc.y, rc.x + rc.width, rc.y + rc.height, null);
        }
    }

    @Override
    public Image getFrameBufferScaledImage(final int width, final int height) {
        if (offlineImage != null) {
            return offlineImage.getScaledInstance(width, height, Image.SCALE_DEFAULT);
        }
        return null;
    }

    @Override
    public void paint(final Graphics g) {
        // Only part of image, requested with repaint(Rectangle), will be
        // painted on screen.
        synchronized (offlineImage) {
            g.drawImage(offlineImage, 0, 0, this);
        }
        // Notify server that update is painted on screen
        listener.imagePaintedOnScreen();
    }

    @Override
    public byte[] getFrameBufferJpeg() {
        int width = 800;
        int height = 600;

        width = offlineImage.getWidth();
        height = offlineImage.getHeight();

        final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        final Graphics2D g = bufferedImage.createGraphics();
        synchronized (offlineImage) {
            g.drawImage(offlineImage, 0, 0, width, height, 0, 0, width, height, null);
        }

        byte[] imgBits = null;
        try {
            imgBits = ImageHelper.jpegFromImage(bufferedImage);
        } catch (final IOException e) {
            s_logger.info("[ignored] read error on image", e);
        }
        return imgBits;
    }

    @Override
    public byte[] getTilesMergedJpeg(final List<TileInfo> tileList, final int tileWidth, final int tileHeight) {
        final int width = Math.max(tileWidth, tileWidth * tileList.size());
        final BufferedImage bufferedImage = new BufferedImage(width, tileHeight, BufferedImage.TYPE_3BYTE_BGR);
        final Graphics2D g = bufferedImage.createGraphics();

        synchronized (offlineImage) {
            int i = 0;
            for (final TileInfo tile : tileList) {
                final Rectangle rc = tile.getTileRect();
                g.drawImage(offlineImage, i * tileWidth, 0, i * tileWidth + rc.width, rc.height, rc.x, rc.y, rc.x + rc.width, rc.y + rc.height, null);
                i++;
            }
        }

        byte[] imgBits = null;
        try {
            imgBits = ImageHelper.jpegFromImage(bufferedImage);
        } catch (final IOException e) {
            s_logger.info("[ignored] read error on image tiles", e);
        }
        return imgBits;
    }
}
