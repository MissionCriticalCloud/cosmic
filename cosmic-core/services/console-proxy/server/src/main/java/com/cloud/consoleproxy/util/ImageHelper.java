package com.cloud.consoleproxy.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageHelper {
    public static byte[] jpegFromImage(final BufferedImage image) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(128000);
        javax.imageio.ImageIO.write(image, "jpg", bos);

        final byte[] jpegBits = bos.toByteArray();
        bos.close();
        return jpegBits;
    }
}
