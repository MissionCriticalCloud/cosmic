package com.cloud.network.vpc;

import org.apache.cloudstack.api.InternalIdentity;

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
@Table(name = "private_ip_address")
public class PrivateIpVO implements InternalIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;

    @Column(name = "ip_address", updatable = false, nullable = false)
    String ipAddress;

    @Column(name = "mac_address")
    private long macAddress;

    @Column(name = "taken")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date takenAt;

    @Column(name = "network_id", updatable = false, nullable = false)
    private long networkId;

    @Column(name = "vpc_id")
    private Long vpcId;

    @Column(name = "source_nat")
    private boolean sourceNat;

    public PrivateIpVO() {
    }

    public PrivateIpVO(final String ipAddress, final long networkId, final long macAddress, final long vpcId, final boolean sourceNat) {
        this.ipAddress = ipAddress;
        this.networkId = networkId;
        this.macAddress = macAddress;
        this.vpcId = vpcId;
        this.sourceNat = sourceNat;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public long getNetworkId() {
        return networkId;
    }

    public Date getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(final Date takenDate) {
        this.takenAt = takenDate;
    }

    @Override
    public long getId() {
        return id;
    }

    public long getMacAddress() {
        return macAddress;
    }

    public Long getVpcId() {
        return vpcId;
    }

    public boolean getSourceNat() {
        return sourceNat;
    }
}
