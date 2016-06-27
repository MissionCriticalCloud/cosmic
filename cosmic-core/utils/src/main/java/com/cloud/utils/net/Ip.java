//

//

package com.cloud.utils.net;

import com.cloud.utils.NumbersUtil;
import com.cloud.utils.SerialVersionUID;

import java.io.Serializable;

/**
 * Simple Ip implementation class that works with both ip4 and ip6.
 */
public class Ip implements Serializable, Comparable<Ip> {

    private static final long serialVersionUID = SerialVersionUID.Ip;

    long ip;

    public Ip(final long ip) {
        this.ip = ip;
    }

    public Ip(final String ip) {
        this.ip = NetUtils.ip2Long(ip);
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
        return NumbersUtil.hash(ip);
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
        return NetUtils.long2Ip(ip);
    }

    public boolean isSameAddressAs(final Object obj) {
        if (this.equals(obj)) {
            return true;
        } else if (obj instanceof String) {
            return ip == NetUtils.ip2Long((String) obj);
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
