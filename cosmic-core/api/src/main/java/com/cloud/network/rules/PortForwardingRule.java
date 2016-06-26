package com.cloud.network.rules;

import com.cloud.utils.net.Ip;

/**
 * Specifies the port forwarding for firewall rule.
 */
public interface PortForwardingRule extends FirewallRule {
    /**
     * @return destination ip address.
     */
    Ip getDestinationIpAddress();

    /**
     * updates the destination ip address.
     */
    void setDestinationIpAddress(Ip destinationIpAddress);

    /**
     * @return start of destination port.
     */
    int getDestinationPortStart();

    /**
     * @return end of destination port range
     */
    int getDestinationPortEnd();

    /**
     * @return destination ip address.
     */
    long getVirtualMachineId();
}
