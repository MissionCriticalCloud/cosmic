package org.apache.cloudstack.engine.cloud.entity.api.db;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "vm_reservation")
public class VMReservationVO implements Identity, InternalIdentity {

    // VolumeId -> poolId
    @Transient
    Map<Long, Long> volumeReservationMap;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "vm_id")
    private long vmId;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "data_center_id")
    private long dataCenterId;
    @Column(name = "pod_id")
    private long podId;
    @Column(name = "cluster_id")
    private long clusterId;
    @Column(name = "host_id")
    private long hostId;
    @Column(name = GenericDao.CREATED_COLUMN)
    private Date created;
    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;
    @Column(name = "deployment_planner")
    private String deploymentPlanner;

    /**
     * There should never be a public constructor for this class. Since it's
     * only here to define the table for the DAO class.
     */
    protected VMReservationVO() {
    }

    public VMReservationVO(final long vmId, final long dataCenterId, final long podId, final long clusterId, final long hostId) {
        this.vmId = vmId;
        this.dataCenterId = dataCenterId;
        this.podId = podId;
        this.clusterId = clusterId;
        this.hostId = hostId;
        this.uuid = UUID.randomUUID().toString();
    }

    @Override
    public long getId() {
        return id;
    }

    public long getVmId() {
        return vmId;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public long getDataCenterId() {
        return dataCenterId;
    }

    public Long getPodId() {
        return podId;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public Long getHostId() {
        return hostId;
    }

    public Map<Long, Long> getVolumeReservation() {
        return volumeReservationMap;
    }

    public void setVolumeReservation(final Map<Long, Long> volumeReservationMap) {
        this.volumeReservationMap = volumeReservationMap;
    }

    public String getDeploymentPlanner() {
        return this.deploymentPlanner;
    }

    public void setDeploymentPlanner(final String planner) {
        this.deploymentPlanner = planner;
    }
}
