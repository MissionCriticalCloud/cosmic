//

//

package com.cloud.agent.api.routing;

// details of site participating in the GLSB service, represents configuration load balancer rule and the zone
// in which the rule is configured
public class SiteLoadBalancerConfig {

    // true if the site details are local to the zone receiving 'GlobalLoadBalancerConfigCommand'
    boolean local;

    // true if the site needs to be removed from GSLB service
    boolean revoked;

    // service type of the 'site load balanced' service
    String serviceType;

    // public IP corresponding to the site load balanced service
    String servicePublicIp;

    // port corresponding to the site load balanced service
    String servicePort;

    // Private IP corresponding to the GSLB service provider in the site.
    String gslbProviderPrivateIp;

    // Public IP corresponding to the GSLB service provider in the site.
    String gslbProviderPublicIp;

    // zone id in which site is located
    long dataCenterId;

    // wight corresponding to this site
    long weight = 1;

    public SiteLoadBalancerConfig(final String gslbProviderPublicIP, final String gslbProviderPrivateIp, final boolean local, final boolean revoked, final String serviceType,
                                  final String servicePublicIp,
                                  final String port, final long dataCenterId) {
        this(revoked, serviceType, servicePublicIp, port, dataCenterId);
        this.gslbProviderPrivateIp = gslbProviderPrivateIp;
        this.gslbProviderPublicIp = gslbProviderPublicIP;
        this.local = local;
    }

    public SiteLoadBalancerConfig(final boolean revoked, final String serviceType, final String servicePublicIp, final String servicePort, final long dataCenterId) {
        this.revoked = revoked;
        this.serviceType = serviceType;
        this.servicePublicIp = servicePublicIp;
        this.servicePort = servicePort;
        this.dataCenterId = dataCenterId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(final String serviceType) {
        this.serviceType = serviceType;
    }

    public String getServicePublicIp() {
        return servicePublicIp;
    }

    public void SetServicePublicIp(final String servicePublicIp) {
        this.servicePublicIp = servicePublicIp;
    }

    public String getServicePort() {
        return servicePort;
    }

    public void setServicePort(final String port) {
        this.servicePort = port;
    }

    public String getGslbProviderPrivateIp() {
        return gslbProviderPrivateIp;
    }

    public void setGslbProviderPrivateIp(final String privateIp) {
        this.gslbProviderPrivateIp = privateIp;
    }

    public String getGslbProviderPublicIp() {
        return gslbProviderPublicIp;
    }

    public void setGslbProviderPublicIp(final String publicIp) {
        this.gslbProviderPublicIp = publicIp;
    }

    public long getDataCenterId() {
        return dataCenterId;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(final boolean local) {
        this.local = local;
    }

    public boolean forRevoke() {
        return revoked;
    }

    public long getWeight() {
        return weight;
    }

    public void setWeight(final long weight) {
        assert (weight >= 1 && weight <= 100);
        this.weight = weight;
    }
}
