//

//

package com.cloud.utils.storage;

import com.cloud.utils.NumbersUtil;

import java.io.IOException;
import java.io.InputStream;

public final class QCOW2Utils {
    private static final int VIRTUALSIZE_HEADER_LOCATION = 24;
    private static final int VIRTUALSIZE_HEADER_LENGTH = 8;

    /**
     * Private constructor ->  This utility class cannot be instantiated.
     */
    private QCOW2Utils() {
    }

    /**
     * @return the header location of the virtual size field.
     */
    public static int getVirtualSizeHeaderLocation() {
        return VIRTUALSIZE_HEADER_LOCATION;
    }

    /**
     * @param inputStream The QCOW2 object in stream format.
     * @return The virtual size of the QCOW2 object.
     */
    public static long getVirtualSize(final InputStream inputStream) throws IOException {
        final byte[] bytes = new byte[VIRTUALSIZE_HEADER_LENGTH];

        if (inputStream.skip(VIRTUALSIZE_HEADER_LOCATION) != VIRTUALSIZE_HEADER_LOCATION) {
            throw new IOException("Unable to skip to the virtual size header");
        }

        if (inputStream.read(bytes) != VIRTUALSIZE_HEADER_LENGTH) {
            throw new IOException("Unable to properly read the size");
        }

        return NumbersUtil.bytesToLong(bytes);
    }
}
