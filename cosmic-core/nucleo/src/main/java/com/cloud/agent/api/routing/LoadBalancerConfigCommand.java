//

//

package com.cloud.agent.api.routing;

import com.cloud.agent.api.to.LoadBalancerTO;
import com.cloud.agent.api.to.NicTO;

/**
 * LoadBalancerConfigCommand sends the load balancer configuration
 */
public class LoadBalancerConfigCommand extends NetworkElementCommand {
    public String lbStatsVisibility = "guest-network";
    public String lbStatsPublicIP; /* load balancer listen on this ips for stats */
    public String lbStatsPrivateIP; /* load balancer listen on this ips for stats */
    public String lbStatsGuestIP; /* load balancer listen on this ips for stats */
    public String lbStatsPort = "8081"; /*load balancer listen on this port for stats */
    public String lbStatsSrcCidrs = "0/0"; /* TODO : currently there is no filtering based on the source ip */
    public String lbStatsAuth = "admin1:AdMiN123";
    public String lbStatsUri = "/admin?stats";
    public String maxconn = "";
    public String lbProtocol;
    public boolean keepAliveEnabled = false;
    LoadBalancerTO[] loadBalancers;
    NicTO nic;
    Long vpcId;

    protected LoadBalancerConfigCommand() {
    }

    public LoadBalancerConfigCommand(final LoadBalancerTO[] loadBalancers, final Long vpcId) {
        this.loadBalancers = loadBalancers;
        this.vpcId = vpcId;
    }

    public LoadBalancerConfigCommand(final LoadBalancerTO[] loadBalancers, final String publicIp, final String guestIp, final String privateIp, final NicTO nic, final Long
            vpcId, final String maxconn,
                                     final boolean keepAliveEnabled) {
        this.loadBalancers = loadBalancers;
        this.lbStatsPublicIP = publicIp;
        this.lbStatsPrivateIP = privateIp;
        this.lbStatsGuestIP = guestIp;
        this.nic = nic;
        this.vpcId = vpcId;
        this.maxconn = maxconn;
        this.keepAliveEnabled = keepAliveEnabled;
    }

    public NicTO getNic() {
        return nic;
    }

    public LoadBalancerTO[] getLoadBalancers() {
        return loadBalancers;
    }

    public Long getVpcId() {
        return vpcId;
    }
}
