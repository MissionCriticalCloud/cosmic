package org.apache.cloudstack.affinity;

import com.cloud.domain.PartOf;
import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "affinity_group_domain_map")
public class AffinityGroupDomainMapVO implements PartOf, InternalIdentity {

    @Column(name = "subdomain_access")
    public Boolean subdomainAccess;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @Column(name = "domain_id")
    long domainId;
    @Column(name = "affinity_group_id")
    private long affinityGroupId;

    protected AffinityGroupDomainMapVO() {
    }

    public AffinityGroupDomainMapVO(final long affinityGroupId, final long domainId, final Boolean subdomainAccess) {
        this.affinityGroupId = affinityGroupId;
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

    public long getAffinityGroupId() {
        return affinityGroupId;
    }

    public Boolean isSubdomainAccess() {
        return subdomainAccess;
    }
}
