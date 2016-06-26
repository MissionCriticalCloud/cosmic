package org.apache.cloudstack.network.topology;

import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.rules.AdvancedVpnRules;
import com.cloud.network.rules.BasicVpnRules;
import com.cloud.network.rules.DhcpEntryRules;
import com.cloud.network.rules.DhcpPvlanRules;
import com.cloud.network.rules.DhcpSubNetRules;
import com.cloud.network.rules.FirewallRules;
import com.cloud.network.rules.IpAssociationRules;
import com.cloud.network.rules.LoadBalancingRules;
import com.cloud.network.rules.NetworkAclsRules;
import com.cloud.network.rules.NicPlugInOutRules;
import com.cloud.network.rules.PasswordToRouterRules;
import com.cloud.network.rules.PrivateGatewayRules;
import com.cloud.network.rules.SshKeyToRouterRules;
import com.cloud.network.rules.StaticNatRules;
import com.cloud.network.rules.StaticRoutesRules;
import com.cloud.network.rules.UserdataPwdRules;
import com.cloud.network.rules.UserdataToRouterRules;
import com.cloud.network.rules.VirtualNetworkApplianceFactory;
import com.cloud.network.rules.VpcIpAssociationRules;

public abstract class NetworkTopologyVisitor {

    public abstract VirtualNetworkApplianceFactory getVirtualNetworkApplianceFactory();

    public abstract boolean visit(StaticNatRules nat) throws ResourceUnavailableException;

    public abstract boolean visit(LoadBalancingRules loadbalancing) throws ResourceUnavailableException;

    public abstract boolean visit(FirewallRules firewall) throws ResourceUnavailableException;

    public abstract boolean visit(IpAssociationRules ipAddresses) throws ResourceUnavailableException;

    public abstract boolean visit(UserdataPwdRules userdata) throws ResourceUnavailableException;

    public abstract boolean visit(DhcpEntryRules dhcp) throws ResourceUnavailableException;

    public abstract boolean visit(SshKeyToRouterRules ssh) throws ResourceUnavailableException;

    public abstract boolean visit(PasswordToRouterRules pwd) throws ResourceUnavailableException;

    public abstract boolean visit(NetworkAclsRules acl) throws ResourceUnavailableException;

    public abstract boolean visit(VpcIpAssociationRules vpcIp) throws ResourceUnavailableException;

    public abstract boolean visit(UserdataToRouterRules userdata) throws ResourceUnavailableException;

    public abstract boolean visit(BasicVpnRules vpnRules) throws ResourceUnavailableException;

    public abstract boolean visit(AdvancedVpnRules vpnRules) throws ResourceUnavailableException;

    public abstract boolean visit(PrivateGatewayRules pvtGatewayRules) throws ResourceUnavailableException;

    public abstract boolean visit(DhcpPvlanRules dhcpRules) throws ResourceUnavailableException;

    public abstract boolean visit(DhcpSubNetRules dhcpRules) throws ResourceUnavailableException;

    public abstract boolean visit(NicPlugInOutRules nicPlugInOutRules) throws ResourceUnavailableException;

    public abstract boolean visit(StaticRoutesRules staticRoutesRules) throws ResourceUnavailableException;
}
