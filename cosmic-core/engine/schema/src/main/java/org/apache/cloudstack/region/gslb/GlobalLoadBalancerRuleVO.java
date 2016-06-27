package org.apache.cloudstack.region.gslb;

import com.cloud.region.ha.GlobalLoadBalancerRule;

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
@Table(name = ("global_load_balancing_rules"))
public class GlobalLoadBalancerRuleVO implements GlobalLoadBalancerRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;
    @Column(name = "account_id")
    long accountId;
    @Column(name = "domain_id", updatable = false)
    long domainId;
    @Column(name = "uuid")
    String uuid;
    @Enumerated(value = EnumType.STRING)
    @Column(name = "state")
    GlobalLoadBalancerRule.State state;
    @Column(name = "name")
    private String name;
    @Column(name = "description", length = 4096)
    private String description;
    @Column(name = "algorithm")
    private String algorithm;
    @Column(name = "persistence")
    private String persistence;
    @Column(name = "gslb_domain_name")
    private String gslbDomain;
    @Column(name = "service_type")
    private String serviceType;
    @Column(name = "region_id")
    private int region;

    public GlobalLoadBalancerRuleVO() {
        uuid = UUID.randomUUID().toString();
    }

    public GlobalLoadBalancerRuleVO(final String name, final String description, final String gslbDomain, final String algorithm, final String persistence, final String
            serviceType, final int regionId,
                                    final long accountId, final long domainId, final State state) {
        this.name = name;
        this.description = description;
        region = regionId;
        this.algorithm = algorithm;
        this.gslbDomain = gslbDomain;
        this.persistence = persistence;
        this.accountId = accountId;
        this.domainId = domainId;
        this.serviceType = serviceType;
        uuid = UUID.randomUUID().toString();
        this.state = state;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public String getGslbDomain() {
        return gslbDomain;
    }

    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    @Override
    public String getPersistence() {
        return persistence;
    }

    @Override
    public int getRegion() {
        return region;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public GlobalLoadBalancerRule.State getState() {
        return state;
    }

    @Override
    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(final String serviceType) {
        this.serviceType = serviceType;
    }

    public void setState(final GlobalLoadBalancerRule.State state) {
        this.state = state;
    }

    public void setRegion(final int region) {
        this.region = region;
    }

    public void setPersistence(final String persistence) {
        this.persistence = persistence;
    }

    public void setAlgorithm(final String algorithm) {
        this.algorithm = algorithm;
    }

    public void setGslbDomain(final String gslbDomain) {
        this.gslbDomain = gslbDomain;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    @Override
    public Class<?> getEntityType() {
        return GlobalLoadBalancerRule.class;
    }
}
