package com.cloud.dc;

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
@Table(name = "op_dc_ip_address_alloc")
public class DataCenterIpAddressVO implements InternalIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;

    @Column(name = "ip_address", updatable = false, nullable = false)
    String ipAddress;
    @Column(name = "reservation_id")
    String reservationId;
    @Column(name = "mac_address")
    long macAddress;
    @Column(name = "taken")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date takenAt;
    @Column(name = "data_center_id", updatable = false, nullable = false)
    private long dataCenterId;
    @Column(name = "pod_id", updatable = false, nullable = false)
    private long podId;
    @Column(name = "nic_id")
    private Long instanceId;

    protected DataCenterIpAddressVO() {
    }

    public DataCenterIpAddressVO(final String ipAddress, final long dataCenterId, final long podId) {
        this.ipAddress = ipAddress;
        this.dataCenterId = dataCenterId;
        this.podId = podId;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(final String reservationId) {
        this.reservationId = reservationId;
    }

    @Override
    public long getId() {
        return id;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(final Long instanceId) {
        this.instanceId = instanceId;
    }

    public long getPodId() {
        return podId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public long getDataCenterId() {
        return dataCenterId;
    }

    public Date getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(final Date takenDate) {
        this.takenAt = takenDate;
    }

    public long getMacAddress() {
        return macAddress;
    }
}
