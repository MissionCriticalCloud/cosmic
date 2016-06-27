package org.apache.cloudstack.engine.datacenter.entity.api.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "cluster_details")
public class ClusterDetailsVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "cluster_id")
    private long clusterId;

    @Column(name = "name")
    private String name;

    @Column(name = "value")
    private String value;

    protected ClusterDetailsVO() {
    }

    public ClusterDetailsVO(final long clusterId, final String name, final String value) {
        this.clusterId = clusterId;
        this.name = name;
        this.value = value;
    }

    public long getClusterId() {
        return clusterId;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public long getId() {
        return id;
    }
}
