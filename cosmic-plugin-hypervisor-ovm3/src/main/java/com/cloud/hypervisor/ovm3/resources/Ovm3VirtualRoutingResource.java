package com.cloud.hypervisor.ovm3.resources;

import com.cloud.agent.api.SetupGuestNetworkCommand;
import com.cloud.agent.api.routing.IpAssocCommand;
import com.cloud.agent.api.routing.IpAssocVpcCommand;
import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.api.routing.SetSourceNatCommand;
import com.cloud.agent.api.to.IpAddressTO;
import com.cloud.agent.resource.virtualnetwork.VirtualRouterDeployer;
import com.cloud.hypervisor.ovm3.objects.CloudstackPlugin;
import com.cloud.hypervisor.ovm3.objects.Connection;
import com.cloud.hypervisor.ovm3.objects.Xen;
import com.cloud.utils.ExecutionResult;

import javax.ejb.Local;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Local(value = VirtualRouterDeployer.class)
public class Ovm3VirtualRoutingResource implements VirtualRouterDeployer {
    private final Logger logger = LoggerFactory.getLogger(Ovm3VirtualRoutingResource.class);
    private final String domRCloudPath = "/opt/cloud/bin/";
    private final int vrTimeout = 600;
    private Connection connection;
    private String agentName;

    public Ovm3VirtualRoutingResource() {
    }

    public Ovm3VirtualRoutingResource(final Connection conn) {
        connection = conn;
        agentName = connection.getIp();
    }

    public void setConnection(final Connection conn) {
        connection = conn;
    }

    @Override
    public ExecutionResult executeInVR(final String routerIp, final String script,
                                       final String args) {
        return executeInVR(routerIp, script, args, vrTimeout);
    }

    @Override
    public ExecutionResult executeInVR(final String routerIp, String script,
                                       final String args, final int timeout) {
        if (!script.contains(domRCloudPath)) {
            script = domRCloudPath + "/" + script;
        }
        final String cmd = script + " " + args;
        logger.debug("executeInVR via " + agentName + " on " + routerIp + ": "
                + cmd);
        try {
            final CloudstackPlugin cSp = new CloudstackPlugin(connection);
            final CloudstackPlugin.ReturnCode result;
            result = cSp.domrExec(routerIp, cmd);
            return new ExecutionResult(result.getRc(), result.getStdOut());
        } catch (final Exception e) {
            logger.error("executeInVR FAILED via " + agentName + " on "
                    + routerIp + ":" + cmd + ", " + e.getMessage(), e);
        }
        return new ExecutionResult(false, "");
    }

    @Override
    public ExecutionResult createFileInVR(final String routerIp, final String path,
                                          final String filename, final String content) {
        String error = null;
        logger.debug("createFileInVR via " + agentName + " on " + routerIp
                + ": " + path + "/" + filename + ", content: " + content);
        try {
            final CloudstackPlugin cSp = new CloudstackPlugin(connection);
            final boolean result = cSp.ovsDomrUploadFile(routerIp, path, filename,
                    content);
            return new ExecutionResult(result, "");
        } catch (final Exception e) {
            error = e.getMessage();
            logger.warn(
                    "createFileInVR failed for " + path + "/" + filename
                            + " in VR " + routerIp + " via " + agentName + ": "
                            + error,
                    e);
        }
        return new ExecutionResult(error == null, error);
    }

    @Override
    public ExecutionResult prepareCommand(final NetworkElementCommand cmd) {
        // Update IP used to access router
        cmd.setRouterAccessIp(cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP));
        assert cmd.getRouterAccessIp() != null;

        if (cmd instanceof IpAssocVpcCommand) {
            return prepareNetworkElementCommand((IpAssocVpcCommand) cmd);
        } else if (cmd instanceof IpAssocCommand) {
            return prepareNetworkElementCommand((IpAssocCommand) cmd);
        } else if (cmd instanceof SetupGuestNetworkCommand) {
            return prepareNetworkElementCommand((SetupGuestNetworkCommand) cmd);
        } else if (cmd instanceof SetSourceNatCommand) {
            return prepareNetworkElementCommand((SetSourceNatCommand) cmd);
        }
        return new ExecutionResult(true, null);
    }

    @Override
    public ExecutionResult cleanupCommand(final NetworkElementCommand cmd) {
        if (cmd instanceof IpAssocCommand
                && !(cmd instanceof IpAssocVpcCommand)) {
            return cleanupNetworkElementCommand((IpAssocCommand) cmd);
        }
        return new ExecutionResult(true, null);
    }

    private ExecutionResult cleanupNetworkElementCommand(final IpAssocCommand cmd) {
        return new ExecutionResult(true, null);
    }

    private ExecutionResult prepareNetworkElementCommand(final IpAssocVpcCommand cmd) {
        return prepNetBoth(cmd.getAccessDetail(NetworkElementCommand.ROUTER_NAME),
                cmd.getIpAddresses(), "IpAssocVpcCommand");
    }

    private ExecutionResult prepareNetworkElementCommand(final IpAssocCommand cmd) {
        return prepNetBoth(cmd.getAccessDetail(NetworkElementCommand.ROUTER_NAME),
                cmd.getIpAddresses(), "IpAssocCommand");
    }

    private ExecutionResult prepareNetworkElementCommand(
            final SetupGuestNetworkCommand cmd) {
        return new ExecutionResult(true, null);
    }

    private ExecutionResult prepareNetworkElementCommand(final SetSourceNatCommand cmd) {
        return new ExecutionResult(true, null);
    }

    private ExecutionResult prepNetBoth(final String routerName, final IpAddressTO[] ips, final String type) {
        final Xen xen = new Xen(connection);
        try {
            final Xen.Vm vm = xen.getVmConfig(routerName);
            for (final IpAddressTO ip : ips) {
                Integer devId = vm.getVifIdByMac(ip.getVifMacAddress());
                if (devId < 0 && "IpAssocVpcCommand".equals(type)) {
                    final String msg = "No valid Nic devId found for " + vm.getVmName() + " with " + ip.getVifMacAddress();
                    logger.error(msg);
                    return new ExecutionResult(false, msg);
                } else if (devId < 0 && "IpAssocCommand".equals(type)) {
                    // vm.get
                    final String msg = "No valid Nic devId found for " + vm.getVmName()
                            + " with " + ip.getVifMacAddress() + " Ignoring for now (routervm)";
                    logger.debug(msg);
                    devId = 2;
                }
                ip.setNicDevId(devId);
            }
        } catch (final Exception e) {
            final String msg = type + " failure on applying one ip due to exception:  " + e;
            logger.error(msg);
            return new ExecutionResult(false, msg);
        }
        return new ExecutionResult(true, null);
    }
}
