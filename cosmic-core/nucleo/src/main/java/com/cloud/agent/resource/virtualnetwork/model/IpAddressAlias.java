//

//

package com.cloud.agent.resource.virtualnetwork.model;

public class IpAddressAlias {
    private boolean revoke;
    private String IpAddress;
    private String netmask;
    private long count;

    public IpAddressAlias() {
        // Empty constructor for (de)serialization
    }

    public IpAddressAlias(final boolean revoke, final String ipAddress, final String netmask, final long count) {
        super();
        this.revoke = revoke;
        IpAddress = ipAddress;
        this.netmask = netmask;
        this.count = count;
    }

    public boolean isRevoke() {
        return revoke;
    }

    public void setRevoke(final boolean revoke) {
        this.revoke = revoke;
    }

    public String getIpAddress() {
        return IpAddress;
    }

    public void setIpAddress(final String ipAddress) {
        IpAddress = ipAddress;
    }

    public String getNetmask() {
        return netmask;
    }

    public void setNetmask(final String netmask) {
        this.netmask = netmask;
    }

    public long getCount() {
        return count;
    }

    public void setCount(final long count) {
        this.count = count;
    }
}
