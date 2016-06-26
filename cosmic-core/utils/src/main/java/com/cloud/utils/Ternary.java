//

//

package com.cloud.utils;

public class Ternary<T, U, V> {
    private T t;
    private U u;
    private V v;

    public Ternary(final T t, final U u, final V v) {
        this.t = t;
        this.u = u;
        this.v = v;
    }

    public T first() {
        return t;
    }

    public void first(final T t) {
        this.t = t;
    }

    public U second() {
        return u;
    }

    public void second(final U u) {
        this.u = u;
    }

    public V third() {
        return v;
    }

    public void third(final V v) {
        this.v = v;
    }

    @Override
    // Note: This means any two pairs with null for both values will match each
    // other but what can I do?  This is due to stupid type erasure.
    public int hashCode() {
        return (t != null ? t.hashCode() : 0) | (u != null ? u.hashCode() : 0) | (v != null ? v.hashCode() : 0);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Ternary)) {
            return false;
        }
        final Ternary<?, ?, ?> that = (Ternary<?, ?, ?>) obj;
        return (t != null ? t.equals(that.t) : that.t == null) && (u != null ? u.equals(that.u) : that.u == null) && (v != null ? v.equals(that.v) : that.v == null);
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder("T[");
        b.append(t != null ? t.toString() : "null");
        b.append(":");
        b.append(u != null ? u.toString() : "null");
        b.append(":");
        b.append(v != null ? v.toString() : "null");
        b.append("]");
        return b.toString();
    }
}
