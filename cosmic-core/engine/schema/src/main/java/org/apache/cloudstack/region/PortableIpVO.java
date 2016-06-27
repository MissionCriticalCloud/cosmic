package org.apache.cloudstack.region;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "portable_ip_address")
public class PortableIpVO implements PortableIp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "region_id")
    int regionId;
    @Column(name = "vlan")
    String vlan;
    @Column(name = "gateway")
    String gateway;
    @Column(name = "netmask")
    String netmask;
    @Column(name = "portable_ip_address")
    String address;
    @Column(name = "allocated")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date allocatedTime;
    @Column(name = "account_id")
    private Long allocatedToAccountId = null;
    @Column(name = "domain_id")
    private Long allocatedInDomainId = null;
    @Column(name = "state")
    private State state;
    @Column(name = "portable_ip_range_id")
    private long rangeId;

    @Column(name = "physical_network_id")
    private Long physicalNetworkId;

    @Column(name = "data_center_id")
    private Long dataCenterId;

    @Column(name = "network_id")
    private Long networkId;

    @Column(name = "vpc_id")
    private Long vpcId;

    public PortableIpVO() {

    }

    public PortableIpVO(final int regionId, final Long rangeId, final String vlan, final String gateway, final String netmask, final String address) {
        this.regionId = regionId;
        this.vlan = vlan;
        this.gateway = gateway;
        this.netmask = netmask;
        this.address = address;
        state = State.Free;
        this.rangeId = rangeId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Long getAllocatedToAccountId() {
        return allocatedToAccountId;
    }

    public void setAllocatedToAccountId(final Long accountId) {
        this.allocatedToAccountId = accountId;
    }

    @Override
    public Long getAllocatedInDomainId() {
        return allocatedInDomainId;
    }

    public void setAllocatedInDomainId(final Long domainId) {
        this.allocatedInDomainId = domainId;
    }

    @Override
    public Date getAllocatedTime() {
        return allocatedTime;
    }

    public void setAllocatedTime(final Date date) {
        this.allocatedTime = date;
    }

    @Override
    public State getState() {
        return state;
    }

    public void setState(final State state) {
        this.state = state;
    }

    @Override
    public int getRegionId() {
        return regionId;
    }

    public void setRegionId(final int regionId) {
        this.regionId = regionId;
    }

    @Override
    public Long getAssociatedDataCenterId() {
        return dataCenterId;
    }

    public void setAssociatedDataCenterId(final Long datacenterId) {
        this.dataCenterId = datacenterId;
    }

    @Override
    public Long getAssociatedWithNetworkId() {
        return networkId;
    }

    public void setAssociatedWithNetworkId(final Long networkId) {
        this.networkId = networkId;
    }

    @Override
    public Long getAssociatedWithVpcId() {
        return vpcId;
    }

    public void setAssociatedWithVpcId(final Long vpcId) {
        this.vpcId = vpcId;
    }

    @Override
    public Long getPhysicalNetworkId() {
        return physicalNetworkId;
    }

    public void setPhysicalNetworkId(final Long physicalNetworkId) {
        this.physicalNetworkId = physicalNetworkId;
    }

    @Override
    public String getAddress() {
        return address;
    }

    public void setAddress(final String address) {
        this.address = address;
    }

    @Override
    public String getVlan() {
        return vlan;
    }

    public void setVlan(final String vlan) {
        this.vlan = vlan;
    }

    @Override
    public String getNetmask() {
        return netmask;
    }

    public void setNetmask(final String netmask) {
        this.netmask = netmask;
    }

    @Override
    public String getGateway() {
        return gateway;
    }

    public void setGateay(final String gateway) {
        this.gateway = gateway;
    }

    Long getRangeId() {
        return rangeId;
    }

    public void setRangeId(final Long rangeId) {
        this.rangeId = rangeId;
    }
}
