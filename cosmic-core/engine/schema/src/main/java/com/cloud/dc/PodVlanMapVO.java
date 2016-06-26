package com.cloud.dc;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "pod_vlan_map")
public class PodVlanMapVO implements InternalIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "pod_id")
    private long podId;

    @Column(name = "vlan_db_id")
    private long vlanDbId;

    public PodVlanMapVO(final long podId, final long vlanDbId) {
        this.podId = podId;
        this.vlanDbId = vlanDbId;
    }

    public PodVlanMapVO() {
    }

    @Override
    public long getId() {
        return id;
    }

    public long getPodId() {
        return podId;
    }

    public long getVlanDbId() {
        return vlanDbId;
    }
}
