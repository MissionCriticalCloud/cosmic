//

//

package com.cloud.utils;

import java.nio.charset.Charset;

public class ConstantTimeComparator {

    public static boolean compareStrings(final String s1, final String s2) {
        final Charset encoding = Charset.forName("UTF-8");
        return compareBytes(s1.getBytes(encoding), s2.getBytes(encoding));
    }

    public static boolean compareBytes(final byte[] b1, final byte[] b2) {
        if (b1.length != b2.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < b1.length; i++) {
            result |= b1[i] ^ b2[i];
        }
        return result == 0;
    }
}
