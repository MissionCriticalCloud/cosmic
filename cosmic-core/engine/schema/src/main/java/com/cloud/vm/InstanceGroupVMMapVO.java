package com.cloud.vm;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;
import javax.persistence.Table;

@Entity
@Table(name = ("instance_group_vm_map"))
@SecondaryTables({@SecondaryTable(name = "user_vm", pkJoinColumns = {@PrimaryKeyJoinColumn(name = "instance_id", referencedColumnName = "id")}),
        @SecondaryTable(name = "instance_group", pkJoinColumns = {@PrimaryKeyJoinColumn(name = "group_id", referencedColumnName = "id")})})
public class InstanceGroupVMMapVO implements InternalIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "group_id")
    private long groupId;

    @Column(name = "instance_id")
    private long instanceId;

    public InstanceGroupVMMapVO() {
    }

    public InstanceGroupVMMapVO(final long groupId, final long instanceId) {
        this.groupId = groupId;
        this.instanceId = instanceId;
    }

    @Override
    public long getId() {
        return id;
    }

    public long getGroupId() {
        return groupId;
    }

    public long getInstanceId() {
        return instanceId;
    }
}
