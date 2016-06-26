package com.cloud.host;

/**
 * the networking environment of the host.  The
 * the environment.
 */
public class HostEnvironment {

    public String managementIpAddress;
    public String managementNetmask;
    public String managementGateway;
    public String managementVlan;

    public String[] neighborHosts;

    public String storageIpAddress;
    public String storageNetwork;
    public String storageGateway;
    public String storageVlan;
    public String secondaryStroageIpAddress;

    public String storage2IpAddress;
    public String storage2Network;
    public String storage2Gateway;
    public String storage2Vlan;
    public String secondaryStorageIpAddress2;

    public String[] neighborStorages;
    public String[] neighborStorages2;

    public String publicIpAddress;
    public String publicNetmask;
    public String publicGateway;
    public String publicVlan;
}
