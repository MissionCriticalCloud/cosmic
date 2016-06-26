package com.cloud.dc;

import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "vlan")
public class VlanVO implements Vlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "vlan_id")
    String vlanTag;

    @Column(name = "vlan_gateway")
    String vlanGateway;

    @Column(name = "vlan_netmask")
    String vlanNetmask;

    @Column(name = "ip6_gateway")
    String ip6Gateway;

    @Column(name = "ip6_cidr")
    String ip6Cidr;

    @Column(name = "data_center_id")
    long dataCenterId;

    @Column(name = "description")
    String ipRange;

    @Column(name = "ip6_range")
    String ip6Range;

    @Column(name = "network_id")
    Long networkId;

    @Column(name = "physical_network_id")
    Long physicalNetworkId;

    @Column(name = "vlan_type")
    @Enumerated(EnumType.STRING)
    VlanType vlanType;

    @Column(name = "uuid")
    String uuid;
    transient String toString;
    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;
    @Column(name = GenericDao.CREATED_COLUMN)
    private Date created;

    public VlanVO(final VlanType vlanType, final String vlanTag, final String vlanGateway, final String vlanNetmask, final long dataCenterId, final String ipRange, final Long
            networkId, final Long physicalNetworkId,
                  final String ip6Gateway, final String ip6Cidr, final String ip6Range) {
        this.vlanType = vlanType;
        this.vlanTag = vlanTag;
        this.vlanGateway = vlanGateway;
        this.vlanNetmask = vlanNetmask;
        this.ip6Gateway = ip6Gateway;
        this.ip6Cidr = ip6Cidr;
        this.dataCenterId = dataCenterId;
        this.ipRange = ipRange;
        this.ip6Range = ip6Range;
        this.networkId = networkId;
        this.uuid = UUID.randomUUID().toString();
        this.physicalNetworkId = physicalNetworkId;
    }

    public VlanVO() {
        this.uuid = UUID.randomUUID().toString();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getVlanTag() {
        return vlanTag;
    }

    @Override
    public String getVlanGateway() {
        return vlanGateway;
    }

    @Override
    public String getVlanNetmask() {
        return vlanNetmask;
    }

    @Override
    public long getDataCenterId() {
        return dataCenterId;
    }

    public void setDataCenterId(final long dcId) {
        this.dataCenterId = dcId;
    }

    @Override
    public String getIpRange() {
        return ipRange;
    }

    @Override
    public VlanType getVlanType() {
        return vlanType;
    }

    @Override
    public Long getNetworkId() {
        return networkId;
    }

    public void setNetworkId(final Long networkId) {
        this.networkId = networkId;
    }

    @Override
    public Date getRemoved() {
        return removed;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public Long getPhysicalNetworkId() {
        return physicalNetworkId;
    }

    public void setPhysicalNetworkId(final Long physicalNetworkId) {
        this.physicalNetworkId = physicalNetworkId;
    }

    @Override
    public String getIp6Gateway() {
        return ip6Gateway;
    }

    public void setIp6Gateway(final String ip6Gateway) {
        this.ip6Gateway = ip6Gateway;
    }

    @Override
    public String getIp6Cidr() {
        return ip6Cidr;
    }

    public void setIp6Cidr(final String ip6Cidr) {
        this.ip6Cidr = ip6Cidr;
    }

    @Override
    public String getIp6Range() {
        return ip6Range;
    }

    public void setIp6Range(final String ip6Range) {
        this.ip6Range = ip6Range;
    }

    public void setIpRange(final String ipRange) {
        this.ip6Range = ipRange;
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        if (toString == null) {
            toString =
                    new StringBuilder("Vlan[").append(vlanTag)
                                              .append("|")
                                              .append(vlanGateway)
                                              .append("|")
                                              .append(vlanNetmask)
                                              .append("|")
                                              .append(ip6Gateway)
                                              .append("|")
                                              .append(ip6Cidr)
                                              .append("|")
                                              .append(ipRange)
                                              .append("|")
                                              .append("|")
                                              .append(ip6Range)
                                              .append(networkId)
                                              .append("]")
                                              .toString();
        }
        return toString;
    }
}
