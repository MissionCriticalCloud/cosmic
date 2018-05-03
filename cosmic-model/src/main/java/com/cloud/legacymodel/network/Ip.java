package com.cloud.legacymodel.network;

import java.io.Serializable;

public class Ip implements Comparable<Ip>, Serializable {
    long ip;

    public Ip(final long ip) {
        this.ip = ip;
    }

    protected Ip() {
    }

    public String addr() {
        return toString();
    }

    public long longValue() {
        return ip;
    }

    public boolean isIp4() {
        return ip <= 2L * Integer.MAX_VALUE + 1;
    }

    public boolean isIp6() {
        return ip > Integer.MAX_VALUE;
    }

    @Override
    public int hashCode() {
        return (int) (ip ^ (ip >>> 32));
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Ip) {
            return ip == ((Ip) obj).ip;
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder(15);
        result.append(ip >> 24 & 0xff).append(".");
        result.append(ip >> 16 & 0xff).append(".");
        result.append(ip >> 8 & 0xff).append(".");
        result.append(ip & 0xff);

        return result.toString();
    }

    public boolean isSameAddressAs(final Object obj) {
        if (this.equals(obj)) {
            return true;
        } else if (obj instanceof Long) {
            return ip == (Long) obj;
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(final Ip that) {
        return (int) (this.ip - that.ip);
    }
}
