//

//

package com.cloud.utils;

import java.io.Serializable;

public class Pair<T, U> implements Serializable {
    private static final long serialVersionUID = 2L;
    T t;
    U u;

    protected Pair() {

    }

    public Pair(final T t, final U u) {
        this.t = t;
        this.u = u;
    }

    public T first() {
        return t;
    }

    public U second() {
        return u;
    }

    public U second(final U value) {
        u = value;
        return u;
    }

    public T first(final T value) {
        t = value;
        return t;
    }

    public void set(final T t, final U u) {
        this.t = t;
        this.u = u;
    }

    @Override
    // Note: This means any two pairs with null for both values will match each
    // other but what can I do?  This is due to stupid type erasure.
    public int hashCode() {
        return (t != null ? t.hashCode() : 0) | (u != null ? u.hashCode() : 0);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Pair)) {
            return false;
        }
        final Pair<?, ?> that = (Pair<?, ?>) obj;
        return (t != null ? t.equals(that.t) : that.t == null) && (u != null ? u.equals(that.u) : that.u == null);
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder("P[");
        b.append((t != null) ? t.toString() : "null");
        b.append(":");
        b.append((u != null) ? u.toString() : "null");
        b.append("]");
        return b.toString();
    }
}
