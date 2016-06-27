//

//

package com.cloud.agent.resource.virtualnetwork.model;

public class StaticRoute {
    private boolean revoke;
    private String ipAddress;
    private String cidr;

    public StaticRoute() {
        // Empty constructor for (de)serialization
    }

    public StaticRoute(final boolean revoke, final String ipAddress, final String cidr) {
        super();
        this.revoke = revoke;
        this.ipAddress = ipAddress;
        this.cidr = cidr;
    }

    public boolean isRevoke() {
        return revoke;
    }

    public void setRevoke(final boolean revoke) {
        this.revoke = revoke;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getCidr() {
        return cidr;
    }

    public void setCidr(final String cidr) {
        this.cidr = cidr;
    }
}
