package com.cloud.consoleproxy.vnc.packet.server;

import com.cloud.consoleproxy.ConsoleProxyClientListener;
import com.cloud.consoleproxy.vnc.BufferedImageCanvas;
import com.cloud.consoleproxy.vnc.RfbConstants;
import com.cloud.consoleproxy.vnc.VncScreenDescription;

import java.io.DataInputStream;
import java.io.IOException;

public class FramebufferUpdatePacket {

    private final VncScreenDescription screen;
    private final BufferedImageCanvas canvas;
    private final ConsoleProxyClientListener clientListener;

    public FramebufferUpdatePacket(final BufferedImageCanvas canvas, final VncScreenDescription screen, final DataInputStream is, final ConsoleProxyClientListener clientListener)
            throws IOException {

        this.screen = screen;
        this.canvas = canvas;
        this.clientListener = clientListener;
        readPacketData(is);
    }

    private void readPacketData(final DataInputStream is) throws IOException {
        is.skipBytes(1);// Skip padding

        // Read number of rectangles
        final int numberOfRectangles = is.readUnsignedShort();

        // For all rectangles
        for (int i = 0; i < numberOfRectangles; i++) {

            // Read coordinate of rectangle
            final int x = is.readUnsignedShort();
            final int y = is.readUnsignedShort();
            final int width = is.readUnsignedShort();
            final int height = is.readUnsignedShort();

            final int encodingType = is.readInt();

            // Process rectangle
            final Rect rect;
            switch (encodingType) {

                case RfbConstants.ENCODING_RAW: {
                    rect = new RawRect(screen, x, y, width, height, is);
                    break;
                }

                case RfbConstants.ENCODING_COPY_RECT: {
                    rect = new CopyRect(x, y, width, height, is);
                    break;
                }

                case RfbConstants.ENCODING_DESKTOP_SIZE: {
                    rect = new FrameBufferSizeChangeRequest(canvas, width, height);
                    if (this.clientListener != null) {
                        this.clientListener.onFramebufferSizeChange(width, height);
                    }
                    break;
                }

                default:
                    throw new RuntimeException("Unsupported ecnoding: " + encodingType);
            }

            paint(rect, canvas);

            if (this.clientListener != null) {
                this.clientListener.onFramebufferUpdate(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
            }
        }
    }

    public void paint(final Rect rect, final BufferedImageCanvas canvas) {
        // Draw rectangle on offline buffer
        rect.paint(canvas.getOfflineImage(), canvas.getOfflineGraphics());

        // Request update of repainted area
        canvas.repaint(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }
}
