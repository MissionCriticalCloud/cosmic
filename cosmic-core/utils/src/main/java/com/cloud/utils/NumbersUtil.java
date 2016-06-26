//

//

package com.cloud.utils;

import com.cloud.utils.exception.CloudRuntimeException;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

import org.apache.commons.lang.math.NumberUtils;

public class NumbersUtil {
    protected static final long KB = 1024;
    protected static final long MB = 1024 * KB;
    protected static final long GB = 1024 * MB;
    protected static final long TB = 1024 * GB;

    public static long parseLong(final String s, final long defaultValue) {
        return NumberUtils.toLong(s, defaultValue);
    }

    public static int parseInt(final String s, final int defaultValue) {
        return NumberUtils.toInt(s, defaultValue);
    }

    public static float parseFloat(final String s, final float defaultValue) {
        return NumberUtils.toFloat(s, defaultValue);
    }

    /**
     * Converts bytes to long on input.
     */
    public static long bytesToLong(final byte[] b) {
        return bytesToLong(b, 0);
    }

    public static long bytesToLong(final byte[] b, final int pos) {
        return ByteBuffer.wrap(b, pos, 8).getLong();
    }

    /**
     * Converts a byte array to a hex readable string.
     **/
    public static String bytesToString(final byte[] data, final int start, int end) {
        final StringBuilder buf = new StringBuilder();
        if (end > data.length) {
            end = data.length;
        }
        for (int i = start; i < end; i++) {
            buf.append(" ");
            buf.append(Integer.toHexString(data[i] & 0xff));
        }
        return buf.toString();
    }

    public static String toReadableSize(final long bytes) {
        if (bytes < KB && bytes >= 0) {
            return Long.toString(bytes) + " bytes";
        }
        final StringBuilder builder = new StringBuilder();
        final Formatter format = new Formatter(builder, Locale.getDefault());
        if (bytes < MB) {
            format.format("%.2f KB", (float) bytes / (float) KB);
        } else if (bytes < GB) {
            format.format("%.2f MB", (float) bytes / (float) MB);
        } else if (bytes < TB) {
            format.format("%.2f GB", (float) bytes / (float) GB);
        } else {
            format.format("%.4f TB", (float) bytes / (float) TB);
        }
        format.close();
        return builder.toString();
    }

    /**
     * Converts a string of the format 'yy-MM-dd'T'HH:mm:ss.SSS" into ms.
     *
     * @param str          containing the interval.
     * @param defaultValue value to return if str doesn't parse.  If -1, throws VmopsRuntimeException
     * @return interval in ms
     */
    public static long parseInterval(final String str, final long defaultValue) {
        try {
            if (str == null) {
                throw new ParseException("String is wrong", 0);
            }

            SimpleDateFormat sdf = null;
            if (str.contains("D")) {
                sdf = new SimpleDateFormat("dd'D'HH'h'mm'M'ss'S'SSS'ms'");
            } else if (str.contains("h")) {
                sdf = new SimpleDateFormat("HH'h'mm'M'ss'S'SSS'ms'");
            } else if (str.contains("M")) {
                sdf = new SimpleDateFormat("mm'M'ss'S'SSS'ms'");
            } else if (str.contains("S")) {
                sdf = new SimpleDateFormat("ss'S'SSS'ms'");
            } else if (str.contains("ms")) {
                sdf = new SimpleDateFormat("SSS'ms'");
            }
            if (sdf == null) {
                throw new ParseException("String is wrong", 0);
            }

            final Date date = sdf.parse(str);
            return date.getTime();
        } catch (final ParseException e) {
            if (defaultValue != -1) {
                return defaultValue;
            } else {
                throw new CloudRuntimeException("Unable to parse: " + str, e);
            }
        }
    }

    public static int hash(final long value) {
        return (int) (value ^ (value >>> 32));
    }
}
