//

//

package com.cloud.utils;

public class EnumUtils {
    public static String listValues(final Enum<?>[] enums) {
        final StringBuilder b = new StringBuilder("[");

        for (final Enum<?> e : enums) {
            b.append(e).append(", ");
        }
        b.append("]");
        return b.toString();
    }

    public static <T extends Enum<T>> T fromString(final Class<T> clz, final String value, final T defaultVal) {
        assert (clz != null);

        if (value != null) {
            try {
                return Enum.valueOf(clz, value.trim());
            } catch (final IllegalArgumentException ex) {
                assert (false);
            }
        }
        return defaultVal;
    }

    public static <T extends Enum<T>> T fromString(final Class<T> clz, final String value) {
        assert (clz != null);

        if (value != null) {
            try {
                return Enum.valueOf(clz, value.trim());
            } catch (final IllegalArgumentException ex) {
                assert (false);
            }
        }
        return null;
    }
}
