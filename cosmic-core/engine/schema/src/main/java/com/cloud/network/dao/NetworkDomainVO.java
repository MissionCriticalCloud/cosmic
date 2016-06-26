package com.cloud.network.dao;

import com.cloud.domain.PartOf;
import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "domain_network_ref")
public class NetworkDomainVO implements PartOf, InternalIdentity {
    @Column(name = "subdomain_access")
    public Boolean subdomainAccess;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @Column(name = "domain_id")
    long domainId;
    @Column(name = "network_id")
    long networkId;

    protected NetworkDomainVO() {
    }

    public NetworkDomainVO(final long networkId, final long domainId, final Boolean subdomainAccess) {
        this.networkId = networkId;
        this.domainId = domainId;
        this.subdomainAccess = subdomainAccess;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    public long getNetworkId() {
        return networkId;
    }

    public Boolean isSubdomainAccess() {
        return subdomainAccess;
    }
}
