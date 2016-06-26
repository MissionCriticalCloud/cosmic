//

//

package com.cloud.utils.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtil {
    public static String toString(final Throwable th) {
        return toString(th, true);
    }

    public static String toString(final Throwable th, final boolean printStack) {
        final StringWriter writer = new StringWriter();
        writer.append("Exception: " + th.getClass().getName() + "\n");
        writer.append("Message: ");
        writer.append(th.getMessage()).append("\n");

        if (printStack) {
            writer.append("Stack: ");
            th.printStackTrace(new PrintWriter(writer));
        }
        return writer.toString();
    }

    public static <T extends Throwable> void rethrowRuntime(final Throwable t) {
        rethrow(t, RuntimeException.class);
        rethrow(t, Error.class);
    }

    public static <T extends Throwable> void rethrow(final Throwable t, final Class<T> clz) throws T {
        if (clz.isAssignableFrom(t.getClass())) {
            throw (T) t;
        }
    }
}
