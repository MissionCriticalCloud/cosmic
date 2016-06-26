package org.apache.cloudstack.storage;

public abstract class BaseType {
    @Override
    public int hashCode() {
        return toString().toLowerCase().hashCode();
    }

    @Override
    public boolean equals(final Object that) {
        if (this == that) {
            return true;
        } else if (that instanceof BaseType) {
            final BaseType th = (BaseType) that;
            if (toString().equalsIgnoreCase(th.toString())) {
                return true;
            }
        }
        return false;
    }

    public boolean isSameTypeAs(final Object that) {
        if (equals(that)) {
            return true;
        }
        if (that instanceof String) {
            if (toString().equalsIgnoreCase((String) that)) {
                return true;
            }
        }
        return false;
    }
}
