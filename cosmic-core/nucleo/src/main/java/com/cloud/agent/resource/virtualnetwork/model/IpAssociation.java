//

//

package com.cloud.agent.resource.virtualnetwork.model;

public class IpAssociation extends ConfigBase {
    private IpAddress[] ipAddress;

    public IpAssociation() {
        super(IP_ASSOCIATION);
    }

    public IpAssociation(final IpAddress[] ipAddress) {
        super(IP_ASSOCIATION);
        this.ipAddress = ipAddress;
    }

    public IpAddress[] getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(final IpAddress[] ipAddress) {
        this.ipAddress = ipAddress;
    }
}
