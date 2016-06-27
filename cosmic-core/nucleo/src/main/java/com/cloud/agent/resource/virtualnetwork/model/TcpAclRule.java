//

//

package com.cloud.agent.resource.virtualnetwork.model;

public class TcpAclRule extends AclRule {
    private final String type = "tcp";
    private int firstPort;
    private int lastPort;

    public TcpAclRule() {
        // Empty contructor for (de)serialization
    }

    public TcpAclRule(final String cidr, final boolean allowed, final int firstPort, final int lastPort) {
        super(cidr, allowed);
        this.firstPort = firstPort;
        this.lastPort = lastPort;
    }

    public int getFirstPort() {
        return firstPort;
    }

    public void setFirstPort(final int firstPort) {
        this.firstPort = firstPort;
    }

    public int getLastPort() {
        return lastPort;
    }

    public void setLastPort(final int lastPort) {
        this.lastPort = lastPort;
    }
}
