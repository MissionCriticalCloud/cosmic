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
@Table(name = "op_dc_vnet_alloc")
public class DataCenterVnetVO implements InternalIdentity {

    @Column(name = "vnet", updatable = false, nullable = false)
    protected String vnet;
    @Column(name = "physical_network_id", updatable = false, nullable = false)
    protected long physicalNetworkId;
    @Column(name = "data_center_id", updatable = false, nullable = false)
    protected long dataCenterId;
    @Column(name = "account_id")
    protected Long accountId;
    @Column(name = "reservation_id")
    protected String reservationId;
    @Column(name = "account_vnet_map_id")
    protected Long accountGuestVlanMapId;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;
    @Column(name = "taken", nullable = true)
    @Temporal(value = TemporalType.TIMESTAMP)
    Date takenAt;

    public DataCenterVnetVO(final String vnet, final long dcId, final long physicalNetworkId) {
        this.vnet = vnet;
        this.dataCenterId = dcId;
        this.physicalNetworkId = physicalNetworkId;
        this.takenAt = null;
    }

    protected DataCenterVnetVO() {
    }

    public Date getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(final Date taken) {
        this.takenAt = taken;
    }

    @Override
    public long getId() {
        return id;
    }

    public String getVnet() {
        return vnet;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(final String reservationId) {
        this.reservationId = reservationId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(final Long accountId) {
        this.accountId = accountId;
    }

    public long getDataCenterId() {
        return dataCenterId;
    }

    public long getPhysicalNetworkId() {
        return physicalNetworkId;
    }

    public Long getAccountGuestVlanMapId() {
        return accountGuestVlanMapId;
    }

    public void setAccountGuestVlanMapId(final Long accountGuestVlanMapId) {
        this.accountGuestVlanMapId = accountGuestVlanMapId;
    }
}
