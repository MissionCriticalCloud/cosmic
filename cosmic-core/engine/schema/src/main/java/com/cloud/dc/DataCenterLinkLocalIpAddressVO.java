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
@Table(name = "op_dc_link_local_ip_address_alloc")
public class DataCenterLinkLocalIpAddressVO implements InternalIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;

    @Column(name = "ip_address", updatable = false, nullable = false)
    String ipAddress;

    @Column(name = "taken")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date takenAt;

    @Column(name = "data_center_id", updatable = false, nullable = false)
    private long dataCenterId;

    @Column(name = "pod_id", updatable = false, nullable = false)
    private long podId;

    @Column(name = "nic_id")
    private Long instanceId;

    @Column(name = "reservation_id")
    private String reservationId;

    protected DataCenterLinkLocalIpAddressVO() {
    }

    public DataCenterLinkLocalIpAddressVO(final String ipAddress, final long dataCenterId, final long podId) {
        this.ipAddress = ipAddress;
        this.dataCenterId = dataCenterId;
        this.podId = podId;
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

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(final String reservationId) {
        this.reservationId = reservationId;
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
}
