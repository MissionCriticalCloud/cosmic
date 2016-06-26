//

//

package com.cloud.utils.net;

public class Ip4Address {
    static final String s_empty_mac = "00:00:00:00:00:00";
    String _addr;
    String _mac;

    public Ip4Address(final long addr, final long mac) {
        _addr = NetUtils.long2Ip(addr);
        _mac = NetUtils.long2Mac(mac);
    }

    public Ip4Address(final String addr) {
        this(addr, s_empty_mac);
    }

    public Ip4Address(final String addr, final String mac) {
        _addr = addr;
        _mac = mac;
    }

    public Ip4Address(final long addr) {
        this(NetUtils.long2Ip(addr), s_empty_mac);
    }

    public String ip4() {
        return _addr;
    }

    public String mac() {
        return _mac;
    }

    public long toLong() {
        return NetUtils.ip2Long(_addr);
    }

    public boolean isSameAddressAs(final Object other) {
        if (other instanceof String) { // Assume that is an ip4 address in String form
            return _addr.equals(other);
        } else {
            return equals(other);
        }
    }

    @Override
    public int hashCode() {
        return _mac.hashCode() * _addr.hashCode();
    }

    @Override
    public boolean equals(final Object that) {

        if (that instanceof Ip4Address) {
            final Ip4Address ip4 = (Ip4Address) that;
            return _addr.equals(ip4._addr) && (_mac == ip4._mac || _mac.equals(ip4._mac));
        } else {
            return false;
        }
    }
}
