//

//

package com.cloud.agent.resource.virtualnetwork.model;

public abstract class AclRule {
    private String cidr;
    private boolean allowed;

    protected AclRule() {
        // Empty constructor for (de)serialization
    }

    protected AclRule(final String cidr, final boolean allowed) {
        this.cidr = cidr;
        this.allowed = allowed;
    }

    public String getCidr() {
        return cidr;
    }

    public void setCidr(final String cidr) {
        this.cidr = cidr;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(final boolean allowed) {
        this.allowed = allowed;
    }
}

/*
{"device":"eth2","mac_address":"02:00:56:36:00:02","private_gateway_acl":false,"nic_ip":"172.16.1.1","nic_netmask":"24",
    "rule":"Ingress:41:0:0:192.168.5.0/24:DROP:,"
            + "Ingress:all:0:0:192.168.4.0/24:ACCEPT:,"
            + "Ingress:icmp:8:-1:192.168.3.0/24:ACCEPT:,"
            + "Ingress:udp:8080:8081:192.168.2.0/24:ACCEPT:,"
            + "Ingress:tcp:22:22:192.168.1.0/24:ACCEPT:,","type":"networkacl"}
 */
