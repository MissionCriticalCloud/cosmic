package com.cloud.dc;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

// NOTE: This particular table is totally internal to the CS MS.
// Do not ever include a uuid/guid field in this table. We just
// need it map cluster Ids with Cisco Nexus VSM Ids.

@Entity
@Table(name = "cluster_vsm_map")
public class ClusterVSMMapVO {

    @Column(name = "cluster_id")
    long clusterId;

    @Column(name = "vsm_id")
    long vsmId;

    public ClusterVSMMapVO(final long clusterId, final long vsmId) {
        this.clusterId = clusterId;
        this.vsmId = vsmId;
    }

    public ClusterVSMMapVO() {
        // Do nothing.
    }

    public long getClusterId() {
        return clusterId;
    }

    public void setClusterId(final long clusterId) {
        this.clusterId = clusterId;
    }

    public long getVsmId() {
        return vsmId;
    }

    public void setVsmId(final long vsmId) {
        this.vsmId = vsmId;
    }
}
