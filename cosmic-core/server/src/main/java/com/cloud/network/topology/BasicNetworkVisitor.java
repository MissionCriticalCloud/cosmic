package com.cloud.network.topology;

import com.cloud.agent.api.Command;
import com.cloud.agent.api.UpdateVmOverviewCommand;
import com.cloud.agent.api.to.overviews.VMOverviewTO;
import com.cloud.agent.manager.Commands;
import com.cloud.deploy.DeployDestination;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.network.Network;
import com.cloud.network.lb.LoadBalancingRule;
import com.cloud.network.router.CommandSetupHelper;
import com.cloud.network.router.NetworkHelper;
import com.cloud.network.router.VirtualRouter;
import com.cloud.network.rules.AdvancedVpnRules;
import com.cloud.network.rules.BasicVpnRules;
import com.cloud.network.rules.DhcpEntryRules;
import com.cloud.network.rules.DhcpPvlanRules;
import com.cloud.network.rules.DhcpSubNetRules;
import com.cloud.network.rules.FirewallRule;
import com.cloud.network.rules.FirewallRule.Purpose;
import com.cloud.network.rules.FirewallRules;
import com.cloud.network.rules.IpAssociationRules;
import com.cloud.network.rules.LoadBalancingRules;
import com.cloud.network.rules.NetworkAclsRules;
import com.cloud.network.rules.NicPlugInOutRules;
import com.cloud.network.rules.PasswordToRouterRules;
import com.cloud.network.rules.PortForwardingRule;
import com.cloud.network.rules.PrivateGatewayRules;
import com.cloud.network.rules.PublicIpAclsRules;
import com.cloud.network.rules.SshKeyToRouterRules;
import com.cloud.network.rules.StaticNat;
import com.cloud.network.rules.StaticNatRule;
import com.cloud.network.rules.StaticNatRules;
import com.cloud.network.rules.StaticRoutesRules;
import com.cloud.network.rules.UserdataPwdRules;
import com.cloud.network.rules.UserdataToRouterRules;
import com.cloud.network.rules.VirtualNetworkApplianceFactory;
import com.cloud.network.rules.VpcIpAssociationRules;
import com.cloud.storage.VMTemplateVO;
import com.cloud.vm.DomainRouterVO;
import com.cloud.vm.NicVO;
import com.cloud.vm.VirtualMachineProfile;

import javax.inject.Inject;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class BasicNetworkVisitor extends NetworkTopologyVisitor {

    private static final Logger s_logger = LoggerFactory.getLogger(BasicNetworkVisitor.class);

    @Inject
    @Qualifier("networkHelper")
    protected NetworkHelper _networkGeneralHelper;

    @Inject
    protected VirtualNetworkApplianceFactory _virtualNetworkApplianceFactory;

    @Inject
    protected CommandSetupHelper _commandSetupHelper;

    @Override
    public VirtualNetworkApplianceFactory getVirtualNetworkApplianceFactory() {
        return _virtualNetworkApplianceFactory;
    }

    @Override
    public boolean visit(final StaticNatRules nat) throws ResourceUnavailableException {
        final DomainRouterVO router = (DomainRouterVO) nat.getRouter();
        final List<? extends StaticNat> rules = nat.getRules();

        final Commands cmds = new Commands(Command.OnError.Continue);
        _commandSetupHelper.createApplyStaticNatCommands(rules, router, cmds);
        _commandSetupHelper.createPublicIpACLsCommands(router, cmds);

        return _networkGeneralHelper.sendCommandsToRouter(router, cmds);
    }

    @Override
    public boolean visit(final LoadBalancingRules loadbalancing) throws ResourceUnavailableException {
        final Network network = loadbalancing.getNetwork();
        final DomainRouterVO router = (DomainRouterVO) loadbalancing.getRouter();
        final List<LoadBalancingRule> rules = loadbalancing.getRules();

        final Commands cmds = new Commands(Command.OnError.Continue);
        _commandSetupHelper.createApplyLoadBalancingRulesCommands(rules, router, cmds, network.getId());
        _commandSetupHelper.createPublicIpACLsCommands(router, cmds);

        return _networkGeneralHelper.sendCommandsToRouter(router, cmds);
    }

    @Override
    public boolean visit(final FirewallRules firewall) throws ResourceUnavailableException {
        final Network network = firewall.getNetwork();
        final DomainRouterVO router = (DomainRouterVO) firewall.getRouter();
        final List<? extends FirewallRule> rules = firewall.getRules();
        final List<LoadBalancingRule> loadbalancingRules = firewall.getLoadbalancingRules();

        final Purpose purpose = firewall.getPurpose();

        final Commands cmds = new Commands(Command.OnError.Continue);
        _commandSetupHelper.createPublicIpACLsCommands(router, cmds);
        if (purpose == Purpose.LoadBalancing) {

            _commandSetupHelper.createApplyLoadBalancingRulesCommands(loadbalancingRules, router, cmds, network.getId());

            return _networkGeneralHelper.sendCommandsToRouter(router, cmds);
        } else if (purpose == Purpose.PortForwarding) {

            _commandSetupHelper.createApplyPortForwardingRulesCommands((List<? extends PortForwardingRule>) rules, router, cmds, network.getId());

            return _networkGeneralHelper.sendCommandsToRouter(router, cmds);
        } else if (purpose == Purpose.StaticNat) {

            _commandSetupHelper.createApplyStaticNatRulesCommands((List<StaticNatRule>) rules, router, cmds, network.getId());

            return _networkGeneralHelper.sendCommandsToRouter(router, cmds);
        } else if (purpose == Purpose.Firewall) {

            _commandSetupHelper.createApplyFirewallRulesCommands(rules, router, cmds, network.getId());

            return _networkGeneralHelper.sendCommandsToRouter(router, cmds);
        }
        s_logger.warn("Unable to apply rules of purpose: " + rules.get(0).getPurpose());

        return false;
    }

    @Override
    public boolean visit(final IpAssociationRules ipRules) throws ResourceUnavailableException {
        throw new CloudRuntimeException("NetworkAclsRules not implemented in Basic Network Topology.");
    }

    @Override
    public boolean visit(final UserdataPwdRules userdata) throws ResourceUnavailableException {
        final VirtualRouter router = userdata.getRouter();

        final Commands commands = new Commands(Command.OnError.Stop);
        final VirtualMachineProfile profile = userdata.getProfile();
        final NicVO nicVO = userdata.getNicVo();
        final DeployDestination destination = userdata.getDestination();

        if (router.getPodIdToDeployIn() == destination.getPod().getId()) {
            _commandSetupHelper.createPasswordCommand(router, profile, nicVO, commands);

            final VMOverviewTO vmOverview = _commandSetupHelper.createVmOverviewFromRouter(router);
            final UpdateVmOverviewCommand updateVmOverviewCommand = _commandSetupHelper.createUpdateVmOverviewCommand(router, vmOverview);
            commands.addCommand(updateVmOverviewCommand);

            return _networkGeneralHelper.sendCommandsToRouter(router, commands);
        }

        return true;
    }

    @Override
    public boolean visit(final DhcpEntryRules dhcp) throws ResourceUnavailableException {
        final VirtualRouter router = dhcp.getRouter();

        final Commands commands = new Commands(Command.OnError.Stop);
        final DeployDestination destination = dhcp.getDestination();

        return router.getPodIdToDeployIn() != destination.getPod().getId() || _networkGeneralHelper.sendCommandsToRouter(router, commands);
    }

    @Override
    public boolean visit(final SshKeyToRouterRules sshkey) throws ResourceUnavailableException {
        final VirtualRouter router = sshkey.getRouter();
        final VirtualMachineProfile profile = sshkey.getProfile();

        final Commands commands = new Commands(Command.OnError.Stop);
        final NicVO nicVO = sshkey.getNicVo();
        final VMTemplateVO template = sshkey.getTemplate();

        if (template != null && template.getEnablePassword()) {
            _commandSetupHelper.createPasswordCommand(router, profile, nicVO, commands);
        }

        final VMOverviewTO vmOverview = _commandSetupHelper.createVmOverviewFromRouter(router);
        final UpdateVmOverviewCommand updateVmOverviewCommand = _commandSetupHelper.createUpdateVmOverviewCommand(router, vmOverview);
        commands.addCommand(updateVmOverviewCommand);

        return _networkGeneralHelper.sendCommandsToRouter(router, commands);
    }

    @Override
    public boolean visit(final PasswordToRouterRules passwd) throws ResourceUnavailableException {
        final VirtualRouter router = passwd.getRouter();
        final NicVO nicVo = passwd.getNicVo();
        final VirtualMachineProfile profile = passwd.getProfile();

        final Commands cmds = new Commands(Command.OnError.Stop);
        _commandSetupHelper.createPasswordCommand(router, profile, nicVo, cmds);

        return _networkGeneralHelper.sendCommandsToRouter(router, cmds);
    }

    @Override
    public boolean visit(final NetworkAclsRules aclsRules) throws ResourceUnavailableException {
        throw new CloudRuntimeException("NetworkAclsRules not implemented in Basic Network Topology.");
    }

    @Override
    public boolean visit(final PublicIpAclsRules aclsRules) throws ResourceUnavailableException {
        throw new CloudRuntimeException("PublicIpAclsRules not implemented in Basic Network Topology.");
    }

    @Override
    public boolean visit(final VpcIpAssociationRules ipRules) throws ResourceUnavailableException {
        throw new CloudRuntimeException("VpcIpAssociationRules not implemented in Basic Network Topology.");
    }

    @Override
    public boolean visit(final UserdataToRouterRules userdata) throws ResourceUnavailableException {
        final VirtualRouter router = userdata.getRouter();

        final Commands commands = new Commands(Command.OnError.Stop);

        final VMOverviewTO vmOverview = _commandSetupHelper.createVmOverviewFromRouter(router);
        final UpdateVmOverviewCommand updateVmOverviewCommand = _commandSetupHelper.createUpdateVmOverviewCommand(router, vmOverview);
        commands.addCommand(updateVmOverviewCommand);

        return _networkGeneralHelper.sendCommandsToRouter(router, commands);
    }

    @Override
    public boolean visit(final BasicVpnRules vpnRules) throws ResourceUnavailableException {
        throw new CloudRuntimeException("BasicVpnRules not implemented in Basic Network Topology.");
    }

    @Override
    public boolean visit(final AdvancedVpnRules vpnRules) throws ResourceUnavailableException {
        throw new CloudRuntimeException("AdvancedVpnRules not implemented in Basic Network Topology.");
    }

    @Override
    public boolean visit(final PrivateGatewayRules pvtGatewayRules) throws ResourceUnavailableException {
        throw new CloudRuntimeException("PrivateGatewayRules not implemented in Basic Network Topology.");
    }

    @Override
    public boolean visit(final DhcpPvlanRules dhcpRules) throws ResourceUnavailableException {
        throw new CloudRuntimeException("DhcpPvlanRules not implemented in Basic Network Topology.");
    }

    @Override
    public boolean visit(final DhcpSubNetRules subnet) throws ResourceUnavailableException {
        throw new CloudRuntimeException("DhcpSubNetRules not implemented in Basic Network Topology.");
    }

    @Override
    public boolean visit(final NicPlugInOutRules nicPlugInOutRules) throws ResourceUnavailableException {
        throw new CloudRuntimeException("NicPlugInOutRules not implemented in Basic Network Topology.");
    }

    @Override
    public boolean visit(final StaticRoutesRules staticRoutesRules) throws ResourceUnavailableException {
        throw new CloudRuntimeException("StaticRoutesRules not implemented in Basic Network Topology.");
    }
}
