package com.cloud.dc;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.org.Cluster;
import com.cloud.org.Managed.ManagedState;
import com.cloud.utils.NumbersUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "cluster")
public class ClusterVO implements Cluster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "guid")
    private String guid;

    @Column(name = "data_center_id")
    private long dataCenterId;

    @Column(name = "pod_id")
    private long podId;

    @Column(name = "hypervisor_type")
    private String hypervisorType;

    @Column(name = "cluster_type")
    @Enumerated(value = EnumType.STRING)
    private ClusterType clusterType;

    @Column(name = "allocation_state")
    @Enumerated(value = EnumType.STRING)
    private AllocationState allocationState;

    @Column(name = "managed_state")
    @Enumerated(value = EnumType.STRING)
    private ManagedState managedState;

    @Column(name = "uuid")
    private String uuid;

    public ClusterVO() {
        clusterType = ClusterType.CloudManaged;
        allocationState = AllocationState.Enabled;

        this.uuid = UUID.randomUUID().toString();
    }

    public ClusterVO(final long dataCenterId, final long podId, final String name) {
        this.dataCenterId = dataCenterId;
        this.podId = podId;
        this.name = name;
        this.clusterType = ClusterType.CloudManaged;
        this.allocationState = AllocationState.Enabled;
        this.managedState = ManagedState.Managed;
        this.uuid = UUID.randomUUID().toString();
    }

    public ClusterVO(final long clusterId) {
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
        return (hypervisorType != null)
                ? HypervisorType.getType(hypervisorType)
                : HypervisorType.KVM;
    }

    @Override
    public ClusterType getClusterType() {
        return clusterType;
    }

    public void setClusterType(final ClusterType clusterType) {
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

    public void setHypervisorType(final String hypervisorType) {
        this.hypervisorType = hypervisorType;
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
        if (!(obj instanceof ClusterVO)) {
            return false;
        }
        final ClusterVO that = (ClusterVO) obj;
        return this.id == that.id;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(final String guid) {
        this.guid = guid;
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }
}
