//

//

package com.cloud.agent.api.routing;

import com.cloud.agent.api.Command;

import java.util.List;

/**
 * GlobalLoadBalancerConfigCommand used for sending the GSLB configuration to GSLB service provider
 */
public class GlobalLoadBalancerConfigCommand extends Command {

    // FQDN that represents the globally load balanced service
    String domainName;

    // type of the globally load balanced service
    String serviceType;

    // load balancing method to distribute traffic across the sites participating in global service load balancing
    String lbMethod;

    // session persistence type
    String persistenceType;

    // unique Id given per gslb rule, that is uniquely represents glsb rule on all participant sites
    long gslbId;

    // true if global load balancer rule is being deleted
    boolean revoked;

    // list of the site details that are participating in the GSLB service
    List<SiteLoadBalancerConfig> siteLoadBalancers;

    public GlobalLoadBalancerConfigCommand(final String domainName, final String lbMethod, final String persistenceType, final String serviceType, final long gslbId, final
    boolean revoked) {
        this.domainName = domainName;
        this.serviceType = serviceType;
        this.lbMethod = lbMethod;
        this.persistenceType = persistenceType;
        this.gslbId = gslbId;
        this.revoked = revoked;
    }

    public List<SiteLoadBalancerConfig> getSiteDetails() {
        return siteLoadBalancers;
    }

    public void setSiteLoadBalancers(final List<SiteLoadBalancerConfig> siteLoadBalancers) {
        this.siteLoadBalancers = siteLoadBalancers;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getLoadBalancerMethod() {
        return lbMethod;
    }

    public String getPersistenceType() {
        return persistenceType;
    }

    public long getGslbId() {
        return this.gslbId;
    }

    public String getDomainName() {
        return domainName;
    }

    public boolean isForRevoke() {
        return revoked;
    }

    public void setForRevoke(final boolean revoke) {
        this.revoked = revoke;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
