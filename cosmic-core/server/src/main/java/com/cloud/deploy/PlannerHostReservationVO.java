package com.cloud.deploy;

import com.cloud.deploy.DeploymentPlanner.PlannerResourceUsage;
import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "op_host_planner_reservation")
public class PlannerHostReservationVO implements InternalIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "host_id")
    private Long hostId;

    @Column(name = "data_center_id")
    private Long dataCenterId;

    @Column(name = "pod_id")
    private Long podId;

    @Column(name = "cluster_id")
    private Long clusterId;

    @Column(name = "resource_usage")
    @Enumerated(EnumType.STRING)
    private PlannerResourceUsage resourceUsage;

    public PlannerHostReservationVO() {
    }

    public PlannerHostReservationVO(final Long hostId, final Long dataCenterId, final Long podId, final Long clusterId) {
        this.hostId = hostId;
        this.dataCenterId = dataCenterId;
        this.podId = podId;
        this.clusterId = clusterId;
    }

    public PlannerHostReservationVO(final Long hostId, final Long dataCenterId, final Long podId, final Long clusterId, final PlannerResourceUsage resourceUsage) {
        this.hostId = hostId;
        this.dataCenterId = dataCenterId;
        this.podId = podId;
        this.clusterId = clusterId;
        this.resourceUsage = resourceUsage;
    }

    @Override
    public long getId() {
        return id;
    }

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(final Long hostId) {
        this.hostId = hostId;
    }

    public Long getDataCenterId() {
        return dataCenterId;
    }

    public void setDataCenterId(final Long dataCenterId) {
        this.dataCenterId = dataCenterId;
    }

    public Long getPodId() {
        return podId;
    }

    public void setPodId(final long podId) {
        this.podId = new Long(podId);
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(final long clusterId) {
        this.clusterId = new Long(clusterId);
    }

    public PlannerResourceUsage getResourceUsage() {
        return resourceUsage;
    }

    public void setResourceUsage(final PlannerResourceUsage resourceType) {
        this.resourceUsage = resourceType;
    }
}
