package com.cloud.network.rules;

import com.cloud.utils.net.Ip;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = ("port_forwarding_rules"))
@DiscriminatorValue(value = "PortForwarding")
@PrimaryKeyJoinColumn(name = "id")
public class PortForwardingRuleVO extends FirewallRuleVO implements PortForwardingRule {

    @Enumerated(value = EnumType.STRING)
    @Column(name = "dest_ip_address")
    private Ip destinationIpAddress = null;

    @Column(name = "dest_port_start")
    private int destinationPortStart;

    @Column(name = "dest_port_end")
    private int destinationPortEnd;

    @Column(name = "instance_id")
    private long virtualMachineId;

    public PortForwardingRuleVO() {
    }

    public PortForwardingRuleVO(final String xId, final long srcIpId, final int srcPort, final Ip dstIp, final int dstPort, final String protocol, final List<String>
            sourceCidrs, final long networkId, final long accountId,
                                final long domainId, final long instanceId) {
        this(xId, srcIpId, srcPort, srcPort, dstIp, dstPort, dstPort, protocol.toLowerCase(), networkId, accountId, domainId, instanceId);
    }

    public PortForwardingRuleVO(final String xId, final long srcIpId, final int srcPortStart, final int srcPortEnd, final Ip dstIp, final int dstPortStart, final int dstPortEnd,
                                final String protocol, final long networkId,
                                final long accountId, final long domainId, final long instanceId) {
        super(xId, srcIpId, srcPortStart, srcPortEnd, protocol, networkId, accountId, domainId, Purpose.PortForwarding, null, null, null, null, null);
        this.destinationIpAddress = dstIp;
        this.virtualMachineId = instanceId;
        this.destinationPortStart = dstPortStart;
        this.destinationPortEnd = dstPortEnd;
    }

    @Override
    public Ip getDestinationIpAddress() {
        return destinationIpAddress;
    }

    @Override
    public void setDestinationIpAddress(final Ip destinationIpAddress) {
        this.destinationIpAddress = destinationIpAddress;
    }

    @Override
    public int getDestinationPortStart() {
        return destinationPortStart;
    }

    public void setDestinationPortStart(final int destinationPortStart) {
        this.destinationPortStart = destinationPortStart;
    }

    @Override
    public int getDestinationPortEnd() {
        return destinationPortEnd;
    }

    public void setDestinationPortEnd(final int destinationPortEnd) {
        this.destinationPortEnd = destinationPortEnd;
    }

    @Override
    public long getVirtualMachineId() {
        return virtualMachineId;
    }

    public void setVirtualMachineId(final long virtualMachineId) {
        this.virtualMachineId = virtualMachineId;
    }

    @Override
    public Long getRelated() {
        return null;
    }
}
