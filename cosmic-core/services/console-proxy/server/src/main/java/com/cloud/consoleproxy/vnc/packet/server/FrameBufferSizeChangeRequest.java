package com.cloud.consoleproxy.vnc.packet.server;

import com.cloud.consoleproxy.vnc.BufferedImageCanvas;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class FrameBufferSizeChangeRequest extends AbstractRect {

    private final BufferedImageCanvas canvas;

    public FrameBufferSizeChangeRequest(final BufferedImageCanvas canvas, final int width, final int height) {
        super(0, 0, width, height);
        this.canvas = canvas;
        canvas.setCanvasSize(width, height);
    }

    @Override
    public void paint(final BufferedImage offlineImage, final Graphics2D graphics) {
        canvas.setCanvasSize(width, height);
    }
}
