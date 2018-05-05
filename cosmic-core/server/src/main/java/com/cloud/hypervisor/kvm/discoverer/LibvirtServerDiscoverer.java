package com.cloud.hypervisor.kvm.discoverer;

import com.cloud.agent.AgentManager;
import com.cloud.agent.Listener;
import com.cloud.common.resource.ServerResource;
import com.cloud.configuration.Config;
import com.cloud.dc.ClusterVO;
import com.cloud.host.HostVO;
import com.cloud.legacymodel.communication.answer.AgentControlAnswer;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.AgentControlCommand;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.communication.command.ShutdownCommand;
import com.cloud.legacymodel.communication.command.StartupCommand;
import com.cloud.legacymodel.communication.command.StartupRoutingCommand;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.dc.HostStatus;
import com.cloud.legacymodel.exceptions.AgentUnavailableException;
import com.cloud.legacymodel.exceptions.DiscoveredWithErrorException;
import com.cloud.legacymodel.exceptions.DiscoveryException;
import com.cloud.legacymodel.exceptions.OperationTimedoutException;
import com.cloud.legacymodel.exceptions.UnableDeleteHostException;
import com.cloud.legacymodel.network.PhysicalNetworkSetupInfo;
import com.cloud.model.enumeration.HostType;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.resource.Discoverer;
import com.cloud.resource.DiscovererBase;
import com.cloud.resource.ResourceStateAdapter;
import com.cloud.utils.ssh.SSHCmdHelper;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LibvirtServerDiscoverer extends DiscovererBase implements Discoverer, Listener, ResourceStateAdapter {
    public static final int SSH_PORT = 22;
    private static final Logger s_logger = LoggerFactory.getLogger(LibvirtServerDiscoverer.class);
    private final int _waitTime = 5; /* wait for 5 minutes */
    @Inject
    AgentManager _agentMgr;
    private String _hostIp;
    private String _kvmPrivateNic;
    private String _kvmPublicNic;
    private String _kvmGuestNic;

    @Override
    public boolean processAnswers(final long agentId, final long seq, final Answer[] answers) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean processCommands(final long agentId, final long seq, final Command[] commands) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public AgentControlAnswer processControlCommand(final long agentId, final AgentControlCommand cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void processConnect(final Host host, final StartupCommand cmd, final boolean forRebalance) {
    }

    @Override
    public boolean processDisconnect(final long agentId, final HostStatus state) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRecurring() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getTimeout() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean processTimeout(final long agentId, final long seq) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Map<? extends ServerResource, Map<String, String>>
    find(final long dcId, final Long podId, final Long clusterId, final URI uri, final String username, final String password, final List<String> hostTags) throws
            DiscoveryException {

        final ClusterVO cluster = this._clusterDao.findById(clusterId);
        if (cluster == null || cluster.getHypervisorType() != getHypervisorType()) {
            if (s_logger.isInfoEnabled()) {
                s_logger.info("invalid cluster id or cluster is not for " + getHypervisorType() + " hypervisors");
            }
            return null;
        }

        final Map<KvmDummyResourceBase, Map<String, String>> resources = new HashMap<>();
        final Map<String, String> details = new HashMap<>();
        if (!uri.getScheme().equals("http")) {
            final String msg = "urlString is not http so we're not taking care of the discovery for this: " + uri;
            s_logger.debug(msg);
            return null;
        }
        com.trilead.ssh2.Connection sshConnection = null;
        String agentIp = null;
        try {

            final String hostname = uri.getHost();
            final InetAddress ia = InetAddress.getByName(hostname);
            agentIp = ia.getHostAddress();
            final String guid = UUID.nameUUIDFromBytes(agentIp.getBytes()).toString();

            final List<HostVO> existingHosts = this._resourceMgr.listAllHostsInOneZoneByType(HostType.Routing, dcId);
            if (existingHosts != null) {
                for (final HostVO existingHost : existingHosts) {
                    if (existingHost.getGuid().toLowerCase().startsWith(guid.toLowerCase())) {
                        s_logger.debug("Skipping " + agentIp + " because " + guid + " is already in the database for resource " + existingHost.getGuid());
                        return null;
                    }
                }
            }

            sshConnection = new com.trilead.ssh2.Connection(agentIp, SSH_PORT);
            try {
                sshConnection.connect(null, 60000, 60000);
            } catch (final IOException e) {
                s_logger.error("Cannot connect to KVM host at " + agentIp + ":" + SSH_PORT + "  due to: " + e.getMessage(), e);
                return null;
            }
            try {
                if (!sshConnection.authenticateWithPassword(username, password)) {
                    s_logger.warn("Failed to authenticate to KVM host at " + agentIp + ":" + SSH_PORT);
                    throw new DiscoveredWithErrorException("Authentication error");
                }
            } catch (final IOException e) {
                s_logger.error("Failed to authenticate to KVM host at  " + agentIp + ":" + SSH_PORT + "  due to: " + e.getMessage(), e);
                return null;
            }

            if (!SSHCmdHelper.sshExecuteCmd(sshConnection, "lsmod|grep kvm", 3)) {
                s_logger.debug("It's not a KVM enabled machine");
                return null;
            }

            final List<PhysicalNetworkSetupInfo> netInfos = this._networkMgr.getPhysicalNetworkInfo(dcId, getHypervisorType());
            String kvmPrivateNic = null;
            String kvmPublicNic = null;
            String kvmGuestNic = null;

            for (final PhysicalNetworkSetupInfo info : netInfos) {
                if (info.getPrivateNetworkName() != null) {
                    kvmPrivateNic = info.getPrivateNetworkName();
                }
                if (info.getPublicNetworkName() != null) {
                    kvmPublicNic = info.getPublicNetworkName();
                }
                if (info.getGuestNetworkName() != null) {
                    kvmGuestNic = info.getGuestNetworkName();
                }
            }

            if (kvmPrivateNic == null && kvmPublicNic == null && kvmGuestNic == null) {
                kvmPrivateNic = this._kvmPrivateNic;
                kvmPublicNic = this._kvmPublicNic;
                kvmGuestNic = this._kvmGuestNic;
            }

            if (kvmPublicNic == null) {
                kvmPublicNic = (kvmGuestNic != null) ? kvmGuestNic : kvmPrivateNic;
            }

            if (kvmPrivateNic == null) {
                kvmPrivateNic = (kvmPublicNic != null) ? kvmPublicNic : kvmGuestNic;
            }

            if (kvmGuestNic == null) {
                kvmGuestNic = (kvmPublicNic != null) ? kvmPublicNic : kvmPrivateNic;
            }

            String parameters = " -m " + this._hostIp + " -z " + dcId + " -p " + podId + " -c " + clusterId + " -g " + guid + " -a";

            parameters += " --pubNic=" + kvmPublicNic;
            parameters += " --prvNic=" + kvmPrivateNic;
            parameters += " --guestNic=" + kvmGuestNic;
            parameters += " --hypervisor=" + cluster.getHypervisorType().toString().toLowerCase();

            String setupAgentCommand = "cosmic-setup-agent ";
            if (!username.equals("root")) {
                setupAgentCommand = "sudo cosmic-setup-agent ";
            }
            if (!SSHCmdHelper.sshExecuteCmd(sshConnection, setupAgentCommand + parameters, 3)) {
                s_logger.info("cosmic agent setup command failed: " + setupAgentCommand + parameters);
                return null;
            }

            final KvmDummyResourceBase kvmResource = new KvmDummyResourceBase();
            final Map<String, Object> params = new HashMap<>();

            params.put("router.aggregation.command.each.timeout", this._configDao.getValue(Config.RouterAggregationCommandEachTimeout.toString()));

            params.put("zone", Long.toString(dcId));
            params.put("pod", Long.toString(podId));
            params.put("cluster", Long.toString(clusterId));
            params.put("guid", guid);
            params.put("agentIp", agentIp);
            kvmResource.configure("kvm agent", params);
            resources.put(kvmResource, details);

            final HostVO connectedHost = waitForHostConnect(dcId, podId, clusterId, guid);
            if (connectedHost == null) {
                return null;
            }

            details.put("guid", connectedHost.getGuid());

            // place a place holder guid derived from cluster ID
            if (cluster.getGuid() == null) {
                cluster.setGuid(UUID.nameUUIDFromBytes(String.valueOf(clusterId).getBytes()).toString());
                this._clusterDao.update(clusterId, cluster);
            }

            // save user name and password
            this._hostDao.loadDetails(connectedHost);
            final Map<String, String> hostDetails = connectedHost.getDetails();
            hostDetails.put("password", password);
            hostDetails.put("username", username);
            this._hostDao.saveDetails(connectedHost);
            return resources;
        } catch (final ConfigurationException e) {
            s_logger.error("Failed to obtain configuration parameters for KVM host: " + e.getMessage(), e);
        } catch (final UnknownHostException e) {
            s_logger.error("Failed to discover IP of KVM host at " + agentIp + " due to: " + e.getMessage(), e);
        } finally {
            if (sshConnection != null) {
                sshConnection.close();
            }
        }

        return null;
    }

    private HostVO waitForHostConnect(final long dcId, final long podId, final long clusterId, final String guid) {
        for (int i = 0; i < this._waitTime * 2; i++) {
            final List<HostVO> hosts = this._resourceMgr.listAllUpAndEnabledHosts(HostType.Routing, clusterId, podId, dcId);
            for (final HostVO host : hosts) {
                if (host.getGuid().toLowerCase().startsWith(guid.toLowerCase())) {
                    return host;
                }
            }
            try {
                Thread.sleep(30000);
            } catch (final InterruptedException e) {
                s_logger.debug("Failed to sleep: " + e.toString());
            }
        }
        s_logger.debug("Timeout, to wait for the host connecting to mgt svr, assuming it is failed");
        final List<HostVO> hosts = this._resourceMgr.findHostByGuid(dcId, guid);
        if (hosts.size() == 1) {
            return hosts.get(0);
        } else {
            return null;
        }
    }

    @Override
    public void postDiscovery(final List<HostVO> hosts, final long msId) throws DiscoveryException {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean matchHypervisor(final String hypervisor) {
        // for backwards compatibility, if not supplied, always let to try it
        if (hypervisor == null) {
            return true;
        }

        return getHypervisorType().toString().equalsIgnoreCase(hypervisor);
    }

    @Override
    public abstract HypervisorType getHypervisorType();

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        // _setupAgentPath = Script.findScript(getPatchPath(),
        // "setup_agent.sh");
        this._kvmPrivateNic = this._configDao.getValue(Config.KvmPrivateNetwork.key());
        if (this._kvmPrivateNic == null) {
            this._kvmPrivateNic = "cloudbr0";
        }

        this._kvmPublicNic = this._configDao.getValue(Config.KvmPublicNetwork.key());
        if (this._kvmPublicNic == null) {
            this._kvmPublicNic = this._kvmPrivateNic;
        }

        this._kvmGuestNic = this._configDao.getValue(Config.KvmGuestNetwork.key());
        if (this._kvmGuestNic == null) {
            this._kvmGuestNic = this._kvmPrivateNic;
        }

        this._hostIp = this._configDao.getValue("host");
        if (this._hostIp == null) {
            throw new ConfigurationException("Can't get host IP");
        }
        this._resourceMgr.registerResourceStateAdapter(this.getClass().getSimpleName(), this);
        return true;
    }

    @Override
    public boolean stop() {
        this._resourceMgr.unregisterResourceStateAdapter(this.getClass().getSimpleName());
        return super.stop();
    }

    protected String getPatchPath() {
        return "scripts/vm/hypervisor/kvm/";
    }

    @Override
    public HostVO createHostVOForConnectedAgent(final HostVO host, final StartupCommand[] cmd) {
        final StartupCommand firstCmd = cmd[0];
        if (!(firstCmd instanceof StartupRoutingCommand)) {
            return null;
        }

        final StartupRoutingCommand ssCmd = ((StartupRoutingCommand) firstCmd);
        if (ssCmd.getHypervisorType() != getHypervisorType()) {
            return null;
        }

        /* KVM requires host are the same in cluster */
        final ClusterVO clusterVO = this._clusterDao.findById(host.getClusterId());
        if (clusterVO == null) {
            s_logger.debug("cannot find cluster: " + host.getClusterId());
            throw new IllegalArgumentException("cannot add host, due to can't find cluster: " + host.getClusterId());
        }

        final List<HostVO> hostsInCluster = this._resourceMgr.listAllHostsInCluster(clusterVO.getId());
        if (!hostsInCluster.isEmpty()) {
            final HostVO oneHost = hostsInCluster.get(0);
            this._hostDao.loadDetails(oneHost);
            final String hostOsInCluster = oneHost.getDetail("Host.OS");
            final String hostOs = ssCmd.getHostDetails().get("Host.OS");
            if (!hostOsInCluster.equalsIgnoreCase(hostOs)) {
                throw new IllegalArgumentException("Can't add host: " + firstCmd.getPrivateIpAddress() + " with hostOS: " + hostOs + " into a cluster," +
                        "in which there are " + hostOsInCluster + " hosts added");
            }
        }

        this._hostDao.loadDetails(host);

        return this._resourceMgr.fillRoutingHostVO(host, ssCmd, getHypervisorType(), host.getDetails(), null);
    }

    @Override
    public HostVO createHostVOForDirectConnectAgent(final HostVO host, final StartupCommand[] startup, final ServerResource resource, final Map<String, String> details, final
    List<String> hostTags) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DeleteHostAnswer deleteHost(final HostVO host, final boolean isForced, final boolean isForceDeleteStorage) throws UnableDeleteHostException {
        if (host.getType() != HostType.Routing || host.getHypervisorType() != HypervisorType.KVM) {
            return null;
        }

        this._resourceMgr.deleteRoutingHost(host, isForced, isForceDeleteStorage);
        try {
            final ShutdownCommand cmd = new ShutdownCommand(ShutdownCommand.DeleteHost, null);
            this._agentMgr.send(host.getId(), cmd);
        } catch (final AgentUnavailableException e) {
            s_logger.warn("Sending ShutdownCommand failed: ", e);
        } catch (final OperationTimedoutException e) {
            s_logger.warn("Sending ShutdownCommand failed: ", e);
        }

        return new DeleteHostAnswer(true);
    }
}
