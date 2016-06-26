package com.cloud.gpu;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "host_gpu_groups")
public class HostGpuGroupsVO implements InternalIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "host_id")
    private long hostId;

    protected HostGpuGroupsVO() {
    }

    public HostGpuGroupsVO(final long hostId, final String groupName) {
        this.hostId = hostId;
        this.groupName = groupName;
    }

    public long getHostId() {
        return hostId;
    }

    public void setHostId(final long hostId) {
        this.hostId = hostId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(final String groupName) {
        this.groupName = groupName;
    }

    @Override
    public long getId() {
        return id;
    }
}
