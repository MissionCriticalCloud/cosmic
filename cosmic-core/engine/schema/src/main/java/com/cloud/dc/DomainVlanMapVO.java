package com.cloud.dc;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "domain_vlan_map")
public class DomainVlanMapVO implements InternalIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "domain_id")
    private long domainId;

    @Column(name = "vlan_db_id")
    private long vlanDbId;

    public DomainVlanMapVO(final long domainId, final long vlanDbId) {
        this.domainId = domainId;
        this.vlanDbId = vlanDbId;
    }

    public DomainVlanMapVO() {

    }

    public long getId() {
        return id;
    }

    public long getDomainId() {
        return domainId;
    }

    public long getVlanDbId() {
        return vlanDbId;
    }
}
