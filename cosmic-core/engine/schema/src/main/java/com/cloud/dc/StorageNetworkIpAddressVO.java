package com.cloud.dc;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "op_dc_storage_network_ip_address")
@SecondaryTables({@SecondaryTable(name = "dc_storage_network_ip_range", pkJoinColumns = {@PrimaryKeyJoinColumn(name = "range_id", referencedColumnName = "id")})})
public class StorageNetworkIpAddressVO implements InternalIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;

    @Column(name = "range_id")
    long rangeId;

    @Column(name = "ip_address", updatable = false, nullable = false)
    String ipAddress;
    @Column(name = "mac_address")
    long mac;
    @Column(name = "vlan", table = "dc_storage_network_ip_range", insertable = false, updatable = false)
    Integer vlan;
    @Column(name = "gateway", table = "dc_storage_network_ip_range", insertable = false, updatable = false)
    String gateway;
    @Column(name = "taken")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date takenAt;
    @Column(name = "netmask", table = "dc_storage_network_ip_range", insertable = false, updatable = false)
    private String netmask;

    protected StorageNetworkIpAddressVO() {
    }

    @Override
    public long getId() {
        return id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(final String ip) {
        this.ipAddress = ip;
    }

    public Date getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(final Date takenDate) {
        this.takenAt = takenDate;
    }

    public long getRangeId() {
        return rangeId;
    }

    public void setRangeId(final long id) {
        this.rangeId = id;
    }

    public long getMac() {
        return mac;
    }

    public void setMac(final long mac) {
        this.mac = mac;
    }

    public String getNetmask() {
        return netmask;
    }

    public void setNetmask(final String netmask) {
        this.netmask = netmask;
    }

    public Integer getVlan() {
        return vlan;
    }

    public String getGateway() {
        return gateway;
    }
}
