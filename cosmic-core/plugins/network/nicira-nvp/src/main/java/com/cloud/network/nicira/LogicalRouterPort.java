//

//

package com.cloud.network.nicira;

import java.util.List;

public class LogicalRouterPort extends BaseNiciraNamedEntity {
    private final String type = "LogicalRouterPortConfig";
    private Integer portno;
    private boolean adminStatusEnabled;
    private List<String> ipAddresses;
    private String macAddress;

    public int getPortno() {
        return portno;
    }

    public void setPortno(final int portno) {
        this.portno = portno;
    }

    public boolean isAdminStatusEnabled() {
        return adminStatusEnabled;
    }

    public void setAdminStatusEnabled(final boolean adminStatusEnabled) {
        this.adminStatusEnabled = adminStatusEnabled;
    }

    public List<String> getIpAddresses() {
        return ipAddresses;
    }

    public void setIpAddresses(final List<String> ipAddresses) {
        this.ipAddresses = ipAddresses;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(final String macAddress) {
        this.macAddress = macAddress;
    }
}
