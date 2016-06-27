package com.cloud.consoleproxy.vnc.packet.server;

import com.cloud.consoleproxy.util.Logger;
import com.cloud.consoleproxy.vnc.VncScreenDescription;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.io.DataInputStream;
import java.io.IOException;

public class RawRect extends AbstractRect {
    private static final Logger s_logger = Logger.getLogger(RawRect.class);
    private final int[] buf;

    public RawRect(final VncScreenDescription screen, final int x, final int y, final int width, final int height, final DataInputStream is) throws IOException {
        super(x, y, width, height);

        final byte[] bbuf = new byte[width * height * screen.getBytesPerPixel()];
        is.readFully(bbuf);

        // Convert array of bytes to array of int
        final int size = width * height;
        buf = new int[size];
        for (int i = 0, j = 0; i < size; i++, j += 4) {
            buf[i] = (bbuf[j + 0] & 0xFF) | ((bbuf[j + 1] & 0xFF) << 8) | ((bbuf[j + 2] & 0xFF) << 16) | ((bbuf[j + 3] & 0xFF) << 24);
        }
    }

    @Override
    public void paint(final BufferedImage image, final Graphics2D graphics) {

        final DataBuffer dataBuf = image.getRaster().getDataBuffer();

        switch (dataBuf.getDataType()) {

            case DataBuffer.TYPE_INT: {
                // We chose RGB888 model, so Raster will use DataBufferInt type
                final DataBufferInt dataBuffer = (DataBufferInt) dataBuf;

                final int imageWidth = image.getWidth();
                final int imageHeight = image.getHeight();

                // Paint rectangle directly on buffer, line by line
                final int[] imageBuffer = dataBuffer.getData();
                for (int srcLine = 0, dstLine = y; srcLine < height && dstLine < imageHeight; srcLine++, dstLine++) {
                    try {
                        System.arraycopy(buf, srcLine * width, imageBuffer, x + dstLine * imageWidth, width);
                    } catch (final IndexOutOfBoundsException e) {
                        s_logger.info("[ignored] buffer overflow!?!", e);
                    }
                }
                break;
            }

            default:
                throw new RuntimeException("Unsupported data buffer in buffered image: expected data buffer of type int (DataBufferInt). Actual data buffer type: " +
                        dataBuf.getClass().getSimpleName());
        }
    }
}
