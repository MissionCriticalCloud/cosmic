package org.apache.cloudstack.engine.datacenter.entity.api.db;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.org.Cluster;
import com.cloud.org.Grouping;
import com.cloud.org.Managed.ManagedState;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.db.StateMachine;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceEntity.State;
import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceEntity.State.Event;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "cluster")
public class EngineClusterVO implements EngineCluster, Identity {

    @Column(name = GenericDao.CREATED_COLUMN)
    protected Date created;
    @Column(name = "lastUpdated", updatable = true)
    @Temporal(value = TemporalType.TIMESTAMP)
    protected Date lastUpdated;
    /**
     * Note that state is intentionally missing the setter.  Any updates to
     * the state machine needs to go through the DAO object because someone
     * else could be updating it as well.
     */
    @Enumerated(value = EnumType.STRING)
    @StateMachine(state = State.class, event = Event.class)
    @Column(name = "engine_state", updatable = true, nullable = false, length = 32)
    protected State state = null;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;
    @Column(name = "name")
    String name;
    @Column(name = "guid")
    String guid;
    @Column(name = "data_center_id")
    long dataCenterId;
    @Column(name = "pod_id")
    long podId;
    @Column(name = "hypervisor_type")
    String hypervisorType;
    @Column(name = "cluster_type")
    @Enumerated(value = EnumType.STRING)
    Cluster.ClusterType clusterType;
    @Column(name = "allocation_state")
    @Enumerated(value = EnumType.STRING)
    AllocationState allocationState;

    //orchestration
    @Column(name = "managed_state")
    @Enumerated(value = EnumType.STRING)
    ManagedState managedState;
    @Column(name = "uuid")
    String uuid;
    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;
    @Column(name = "owner")
    private String owner = null;

    public EngineClusterVO() {
        clusterType = Cluster.ClusterType.CloudManaged;
        allocationState = Grouping.AllocationState.Enabled;

        this.uuid = UUID.randomUUID().toString();
        this.state = State.Disabled;
    }

    public EngineClusterVO(final long dataCenterId, final long podId, final String name) {
        this.dataCenterId = dataCenterId;
        this.podId = podId;
        this.name = name;
        this.clusterType = Cluster.ClusterType.CloudManaged;
        this.allocationState = Grouping.AllocationState.Enabled;
        this.managedState = ManagedState.Managed;
        this.uuid = UUID.randomUUID().toString();
        this.state = State.Disabled;
    }

    public EngineClusterVO(final long clusterId) {
        this.id = clusterId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getDataCenterId() {
        return dataCenterId;
    }

    @Override
    public long getPodId() {
        return podId;
    }

    @Override
    public HypervisorType getHypervisorType() {
        return HypervisorType.getType(hypervisorType);
    }

    @Override
    public Cluster.ClusterType getClusterType() {
        return clusterType;
    }

    public void setClusterType(final Cluster.ClusterType clusterType) {
        this.clusterType = clusterType;
    }

    @Override
    public AllocationState getAllocationState() {
        return allocationState;
    }

    public void setAllocationState(final AllocationState allocationState) {
        this.allocationState = allocationState;
    }

    @Override
    public ManagedState getManagedState() {
        return managedState;
    }

    public void setManagedState(final ManagedState managedState) {
        this.managedState = managedState;
    }

    public void setHypervisorType(final String hy) {
        hypervisorType = hy;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return NumbersUtil.hash(id);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof EngineClusterVO)) {
            return false;
        }
        final EngineClusterVO that = (EngineClusterVO) obj;
        return this.id == that.id;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(final String guid) {
        this.guid = guid;
    }

    public Date getRemoved() {
        return removed;
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(final String owner) {
        this.owner = owner;
    }

    public Date getCreated() {
        return created;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public State getState() {
        return state;
    }
}
