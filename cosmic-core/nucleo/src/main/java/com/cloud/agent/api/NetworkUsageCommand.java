//

//

package com.cloud.agent.api;

import com.cloud.agent.api.LogLevel.Log4jLevel;

@LogLevel(Log4jLevel.Trace)
public class NetworkUsageCommand extends Command {
    boolean forVpc = false;
    private String privateIP;
    private String domRName;
    private String option;
    private String gatewayIP;
    private String vpcCIDR;

    protected NetworkUsageCommand() {

    }

    public NetworkUsageCommand(final String privateIP, final String domRName, final boolean forVpc, final String gatewayIP) {
        this.privateIP = privateIP;
        this.domRName = domRName;
        this.forVpc = forVpc;
        this.gatewayIP = gatewayIP;
        this.option = "get";
    }

    public NetworkUsageCommand(final String privateIP, final String domRName, final String option, final boolean forVpc) {
        this.privateIP = privateIP;
        this.domRName = domRName;
        this.option = option;
        this.forVpc = forVpc;
    }

    public NetworkUsageCommand(final String privateIP, final String domRName, final boolean forVpc, final String gatewayIP, final String vpcCIDR) {
        this.privateIP = privateIP;
        this.domRName = domRName;
        this.forVpc = forVpc;
        this.gatewayIP = gatewayIP;
        this.option = "create";
        this.vpcCIDR = vpcCIDR;
    }

    public NetworkUsageCommand(final String privateIP, final String domRName, final String option, final boolean forVpc, final String gatewayIP) {
        this.privateIP = privateIP;
        this.domRName = domRName;
        this.forVpc = forVpc;
        this.gatewayIP = gatewayIP;
        this.option = option;
    }

    public String getPrivateIP() {
        return privateIP;
    }

    public String getDomRName() {
        return domRName;
    }

    public String getOption() {
        return option;
    }

    public boolean isForVpc() {
        return forVpc;
    }

    public String getVpcCIDR() {
        return vpcCIDR;
    }

    public String getGatewayIP() {
        return gatewayIP;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
