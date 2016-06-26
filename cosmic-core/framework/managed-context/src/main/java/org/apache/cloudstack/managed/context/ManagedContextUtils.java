package org.apache.cloudstack.managed.context;

public class ManagedContextUtils {

    private static final ThreadLocal<Object> OWNER = new ThreadLocal<>();

    public static boolean setAndCheckOwner(final Object owner) {
        if (OWNER.get() == null) {
            OWNER.set(owner);
            return true;
        }

        return false;
    }

    public static boolean clearOwner(final Object owner) {
        if (OWNER.get() == owner) {
            OWNER.remove();
            return true;
        }

        return false;
    }

    public static boolean isInContext() {
        return OWNER.get() != null;
    }

    public static void rethrowException(final Throwable t) {
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        } else if (t instanceof Error) {
            throw (Error) t;
        }
    }
}
