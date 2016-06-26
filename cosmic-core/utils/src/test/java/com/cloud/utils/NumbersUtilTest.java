//

//

package com.cloud.utils;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;

public class NumbersUtilTest {

    @Test
    public void toReadableSize() {
        Locale.setDefault(Locale.US); // Fixed locale for the test
        assertEquals("1.0000 TB", NumbersUtil.toReadableSize((1024l * 1024l * 1024l * 1024l)));
        assertEquals("1.00 GB", NumbersUtil.toReadableSize(1024 * 1024 * 1024));
        assertEquals("1.00 MB", NumbersUtil.toReadableSize(1024 * 1024));
        assertEquals("1.00 KB", NumbersUtil.toReadableSize((1024)));
        assertEquals("1023 bytes", NumbersUtil.toReadableSize((1023)));
    }

    @Test
    public void bytesToLong() {
        assertEquals(0, NumbersUtil.bytesToLong(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}));
        assertEquals(1, NumbersUtil.bytesToLong(new byte[]{0, 0, 0, 0, 0, 0, 0, 1}));
        assertEquals(257, NumbersUtil.bytesToLong(new byte[]{0, 0, 0, 0, 0, 0, 1, 1}));
    }
}
