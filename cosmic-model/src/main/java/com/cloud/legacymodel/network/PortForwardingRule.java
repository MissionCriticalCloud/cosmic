package com.cloud.legacymodel.network;

public interface PortForwardingRule extends FirewallRule {
    Ip getDestinationIpAddress();

    void setDestinationIpAddress(Ip destinationIpAddress);

    int getDestinationPortStart();

    int getDestinationPortEnd();

    long getVirtualMachineId();
}
