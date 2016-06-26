package com.cloud.capacity;

import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.util.Date;

@Entity
@Table(name = "op_host_capacity")
public class CapacityVO implements Capacity {
    @Column(name = GenericDao.CREATED_COLUMN)
    protected Date created;
    @Column(name = "update_time", updatable = true, nullable = true)
    @Temporal(value = TemporalType.TIMESTAMP)
    protected Date updateTime;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "host_id")
    private Long hostOrPoolId;
    @Column(name = "data_center_id")
    private Long dataCenterId;
    @Column(name = "pod_id")
    private Long podId;
    @Column(name = "cluster_id")
    private Long clusterId;
    @Column(name = "used_capacity")
    private long usedCapacity;
    @Column(name = "reserved_capacity")
    private long reservedCapacity;
    @Column(name = "total_capacity")
    private long totalCapacity;
    @Column(name = "capacity_type")
    private short capacityType;
    @Column(name = "capacity_state")
    private CapacityState capacityState;
    @Transient
    private Float usedPercentage;

    public CapacityVO() {
    }

    public CapacityVO(final Long hostId, final Long dataCenterId, final Long podId, final Long clusterId, final long usedCapacity, final long totalCapacity, final short
            capacityType) {
        this.hostOrPoolId = hostId;
        this.dataCenterId = dataCenterId;
        this.podId = podId;
        this.clusterId = clusterId;
        this.usedCapacity = usedCapacity;
        this.totalCapacity = totalCapacity;
        this.capacityType = capacityType;
        this.updateTime = new Date();
        this.capacityState = CapacityState.Enabled;
    }

    public CapacityVO(final Long dataCenterId, final Long podId, final Long clusterId, final short capacityType, final float usedPercentage) {
        this.dataCenterId = dataCenterId;
        this.podId = podId;
        this.clusterId = clusterId;
        this.capacityType = capacityType;
        this.usedPercentage = usedPercentage;
        this.capacityState = CapacityState.Enabled;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Long getHostOrPoolId() {
        return hostOrPoolId;
    }

    @Override
    public Long getDataCenterId() {
        return dataCenterId;
    }

    public void setDataCenterId(final Long dataCenterId) {
        this.dataCenterId = dataCenterId;
    }

    @Override
    public Long getPodId() {
        return podId;
    }

    public void setPodId(final long podId) {
        this.podId = new Long(podId);
    }

    @Override
    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(final long clusterId) {
        this.clusterId = new Long(clusterId);
    }

    @Override
    public long getUsedCapacity() {
        return usedCapacity;
    }

    public void setUsedCapacity(final long usedCapacity) {
        this.usedCapacity = usedCapacity;
        this.setUpdateTime(new Date());
    }

    @Override
    public long getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(final long totalCapacity) {
        this.totalCapacity = totalCapacity;
        this.setUpdateTime(new Date());
    }

    @Override
    public short getCapacityType() {
        return capacityType;
    }

    @Override
    public long getReservedCapacity() {
        return reservedCapacity;
    }

    public void setReservedCapacity(final long reservedCapacity) {
        this.reservedCapacity = reservedCapacity;
        this.setUpdateTime(new Date());
    }

    @Override
    public Float getUsedPercentage() {
        return usedPercentage;
    }

    public void setUsedPercentage(final float usedPercentage) {
        this.usedPercentage = usedPercentage;
    }

    public void setCapacityType(final short capacityType) {
        this.capacityType = capacityType;
    }

    public void setHostId(final Long hostId) {
        this.hostOrPoolId = hostId;
    }

    public CapacityState getCapacityState() {
        return capacityState;
    }

    public void setCapacityState(final CapacityState capacityState) {
        this.capacityState = capacityState;
    }

    public Date getCreated() {
        return created;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(final Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String getUuid() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
