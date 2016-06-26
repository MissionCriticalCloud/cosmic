//

//

package com.cloud.utils.encoding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.BitSet;

/**
 * This class is very similar to the java.net.URLEncoder class.
 * <p>
 * Unfortunately, with java.net.URLEncoder there is no way to specify to the
 * java.net.URLEncoder which characters should NOT be encoded.
 * <p>
 * This code was moved from DefaultServlet.java
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 */

public class URLEncoder {
    protected static final char[] hexadecimal = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    static CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder(); // or "ISO-8859-1" for ISO Latin 1

    //Array containing the safe characters set.
    protected BitSet safeCharacters = new BitSet(256);

    public URLEncoder() {
        for (char i = 'a'; i <= 'z'; i++) {
            addSafeCharacter(i);
        }
        for (char i = 'A'; i <= 'Z'; i++) {
            addSafeCharacter(i);
        }
        for (char i = '0'; i <= '9'; i++) {
            addSafeCharacter(i);
        }
    }

    private void addSafeCharacter(final char c) {
        safeCharacters.set(c);
    }

    public String encode(final String path) {
        final int maxBytesPerChar = 10;
        final StringBuffer rewrittenPath = new StringBuffer(path.length());
        final ByteArrayOutputStream buf = new ByteArrayOutputStream(maxBytesPerChar);
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(buf, "UTF8");
        } catch (final Exception e) {
            e.printStackTrace();
            writer = new OutputStreamWriter(buf);
        }

        for (int i = 0; i < path.length(); i++) {
            final int c = path.charAt(i);
            // NOTICE - !isPureAscii(path.charAt(i)) check was added by
            // CloudStack
            if (safeCharacters.get(c) || !isPureAscii(path.charAt(i))) {
                rewrittenPath.append((char) c);
            } else {
                // convert to external encoding before hex conversion
                try {
                    writer.write((char) c);
                    writer.flush();
                } catch (final IOException e) {
                    buf.reset();
                    continue;
                }
                final byte[] ba = buf.toByteArray();
                for (int j = 0; j < ba.length; j++) {
                    // Converting each byte in the buffer
                    final byte toEncode = ba[j];
                    rewrittenPath.append('%');
                    final int low = toEncode & 0x0f;
                    final int high = (toEncode & 0xf0) >> 4;
                    rewrittenPath.append(hexadecimal[high]);
                    rewrittenPath.append(hexadecimal[low]);
                }
                buf.reset();
            }
        }
        return rewrittenPath.toString();
    }

    // NOTICE - this part was added by CloudStack
    public static boolean isPureAscii(final Character v) {
        return asciiEncoder.canEncode(v);
    }
}
