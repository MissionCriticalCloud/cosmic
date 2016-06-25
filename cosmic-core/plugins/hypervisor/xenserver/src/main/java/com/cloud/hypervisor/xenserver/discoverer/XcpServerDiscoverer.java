package com.cloud.hypervisor.xenserver.discoverer;

import com.cloud.agent.AgentManager;
import com.cloud.agent.Listener;
import com.cloud.agent.api.AgentControlAnswer;
import com.cloud.agent.api.AgentControlCommand;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.SetupAnswer;
import com.cloud.agent.api.SetupCommand;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupRoutingCommand;
import com.cloud.alert.AlertManager;
import com.cloud.configuration.Config;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.ConnectionException;
import com.cloud.exception.DiscoveredWithErrorException;
import com.cloud.exception.DiscoveryException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.host.HostEnvironment;
import com.cloud.host.HostInfo;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.hypervisor.Hypervisor;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.hypervisor.xenserver.resource.CitrixHelper;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.hypervisor.xenserver.resource.XcpOssResource;
import com.cloud.hypervisor.xenserver.resource.XcpServerResource;
import com.cloud.hypervisor.xenserver.resource.XenServer56FP1Resource;
import com.cloud.hypervisor.xenserver.resource.XenServer56Resource;
import com.cloud.hypervisor.xenserver.resource.XenServer56SP2Resource;
import com.cloud.hypervisor.xenserver.resource.XenServer600Resource;
import com.cloud.hypervisor.xenserver.resource.XenServer610Resource;
import com.cloud.hypervisor.xenserver.resource.XenServer620Resource;
import com.cloud.hypervisor.xenserver.resource.XenServer620SP1Resource;
import com.cloud.hypervisor.xenserver.resource.XenServer650Resource;
import com.cloud.hypervisor.xenserver.resource.XenServerConnectionPool;
import com.cloud.hypervisor.xenserver.resource.Xenserver625Resource;
import com.cloud.resource.Discoverer;
import com.cloud.resource.DiscovererBase;
import com.cloud.resource.ResourceStateAdapter;
import com.cloud.resource.ServerResource;
import com.cloud.resource.UnableDeleteHostException;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Storage.TemplateType;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.user.Account;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.db.QueryBuilder;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.exception.HypervisorVersionChangedException;
import org.apache.cloudstack.hypervisor.xenserver.XenserverConfigs;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import javax.persistence.EntityExistsException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Host;
import com.xensource.xenapi.HostPatch;
import com.xensource.xenapi.Pool;
import com.xensource.xenapi.PoolPatch;
import com.xensource.xenapi.Session;
import com.xensource.xenapi.Types.SessionAuthenticationFailed;
import com.xensource.xenapi.Types.UuidInvalid;
import com.xensource.xenapi.Types.XenAPIException;
import org.apache.xmlrpc.XmlRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XcpServerDiscoverer extends DiscovererBase implements Discoverer, Listener, ResourceStateAdapter {
    private static final Logger s_logger = LoggerFactory.getLogger(XcpServerDiscoverer.class);
    protected String _publicNic;
    protected String _privateNic;
    protected String _storageNic1;
    protected String _storageNic2;
    protected int _wait;
    protected XenServerConnectionPool _connPool;
    protected boolean _checkHvm;
    protected String _guestNic;
    protected boolean _setupMultipath;
    protected String _instance;
    @Inject
    protected AlertManager _alertMgr;
    @Inject
    protected AgentManager _agentMgr;
    @Inject
    VMTemplateDao _tmpltDao;
    @Inject
    HostPodDao _podDao;
    private final String xs620snapshothotfix = "Xenserver-Vdi-Copy-HotFix";

    protected XcpServerDiscoverer() {
    }

    @Override
    public Map<? extends ServerResource, Map<String, String>>
    find(final long dcId, final Long podId, final Long clusterId, final URI url, final String username, final String password, final List<String> hostTags) throws
            DiscoveryException {
        final Map<CitrixResourceBase, Map<String, String>> resources = new HashMap<>();
        Connection conn = null;
        if (!url.getScheme().equals("http")) {
            final String msg = "urlString is not http so we're not taking care of the discovery for this: " + url;
            s_logger.debug(msg);
            return null;
        }
        if (clusterId == null) {
            final String msg = "must specify cluster Id when add host";
            s_logger.debug(msg);
            throw new RuntimeException(msg);
        }

        if (podId == null) {
            final String msg = "must specify pod Id when add host";
            s_logger.debug(msg);
            throw new RuntimeException(msg);
        }

        final ClusterVO cluster = _clusterDao.findById(clusterId);
        if (cluster == null || cluster.getHypervisorType() != HypervisorType.XenServer) {
            if (s_logger.isInfoEnabled()) {
                s_logger.info("invalid cluster id or cluster is not for XenServer hypervisors");
            }
            return null;
        }

        try {
            final String hostname = url.getHost();
            final InetAddress ia = InetAddress.getByName(hostname);
            final String hostIp = ia.getHostAddress();
            final Queue<String> pass = new LinkedList<>();
            pass.add(password);
            conn = _connPool.getConnect(hostIp, username, pass);
            if (conn == null) {
                final String msg = "Unable to get a connection to " + url;
                s_logger.debug(msg);
                throw new DiscoveryException(msg);
            }

            final Set<Pool> pools = Pool.getAll(conn);
            final Pool pool = pools.iterator().next();
            final Pool.Record pr = pool.getRecord(conn);
            String poolUuid = pr.uuid;
            final Map<Host, Host.Record> hosts = Host.getAllRecords(conn);
            String latestHotFix = "";
            if (poolHasHotFix(conn, hostIp, XenserverConfigs.XSHotFix62ESP1004)) {
                latestHotFix = XenserverConfigs.XSHotFix62ESP1004;
            } else if (poolHasHotFix(conn, hostIp, XenserverConfigs.XSHotFix62ESP1)) {
                latestHotFix = XenserverConfigs.XSHotFix62ESP1;
            }

            /*set cluster hypervisor type to xenserver*/
            final ClusterVO clu = _clusterDao.findById(clusterId);
            if (clu.getGuid() == null) {
                setClusterGuid(clu, poolUuid);
            } else {
                final List<HostVO> clusterHosts = _resourceMgr.listAllHostsInCluster(clusterId);
                if (clusterHosts != null && clusterHosts.size() > 0) {
                    if (!clu.getGuid().equals(poolUuid)) {
                        final String msg = "Please join the host " + hostIp + " to XS pool  "
                                + clu.getGuid() + " through XC/XS before adding it through CS UI";
                        s_logger.warn(msg);
                        throw new DiscoveryException(msg);
                    }
                } else {
                    setClusterGuid(clu, poolUuid);
                }
            }
            // can not use this conn after this point, because this host may join a pool, this conn is retired
            if (conn != null) {
                try {
                    Session.logout(conn);
                } catch (final Exception e) {
                    s_logger.debug("Caught exception during logout", e);
                }
                conn.dispose();
                conn = null;
            }

            poolUuid = clu.getGuid();
            _clusterDao.update(clusterId, clu);

            if (_checkHvm) {
                for (final Map.Entry<Host, Host.Record> entry : hosts.entrySet()) {
                    final Host.Record record = entry.getValue();

                    boolean support_hvm = false;
                    for (final String capability : record.capabilities) {
                        if (capability.contains("hvm")) {
                            support_hvm = true;
                            break;
                        }
                    }
                    if (!support_hvm) {
                        final String msg = "Unable to add host " + record.address + " because it doesn't support hvm";
                        _alertMgr.sendAlert(AlertManager.AlertType.ALERT_TYPE_HOST, dcId, podId, msg, msg);
                        s_logger.debug(msg);
                        throw new RuntimeException(msg);
                    }
                }
            }

            for (final Map.Entry<Host, Host.Record> entry : hosts.entrySet()) {
                final Host.Record record = entry.getValue();
                final String hostAddr = record.address;

                final String prodVersion = CitrixHelper.getProductVersion(record);
                final String xenVersion = record.softwareVersion.get("xen");
                String hostOS = record.softwareVersion.get("product_brand");
                if (hostOS == null) {
                    hostOS = record.softwareVersion.get("platform_name");
                }

                final String hostOSVer = prodVersion;
                final String hostKernelVer = record.softwareVersion.get("linux");

                if (_resourceMgr.findHostByGuid(record.uuid) != null) {
                    s_logger.debug("Skipping " + record.address + " because " + record.uuid + " is already in the database.");
                    continue;
                }

                final CitrixResourceBase resource = createServerResource(dcId, podId, record, latestHotFix);
                s_logger.info("Found host " + record.hostname + " ip=" + record.address + " product version=" + prodVersion);

                final Map<String, String> details = new HashMap<>();
                final Map<String, Object> params = new HashMap<>();
                details.put("url", hostAddr);
                details.put("username", username);
                params.put("username", username);
                details.put("password", password);
                params.put("password", password);
                params.put("zone", Long.toString(dcId));
                params.put("guid", record.uuid);
                params.put("pod", podId.toString());
                params.put("cluster", clusterId.toString());
                params.put("pool", poolUuid);
                params.put("ipaddress", record.address);

                details.put(HostInfo.HOST_OS, hostOS);
                details.put(HostInfo.HOST_OS_VERSION, hostOSVer);
                details.put(HostInfo.HOST_OS_KERNEL_VERSION, hostKernelVer);
                details.put(HostInfo.HYPERVISOR_VERSION, xenVersion);

                final String privateNetworkLabel = _networkMgr.getDefaultManagementTrafficLabel(dcId, HypervisorType.XenServer);
                final String storageNetworkLabel = _networkMgr.getDefaultStorageTrafficLabel(dcId, HypervisorType.XenServer);

                if (!params.containsKey("private.network.device") && privateNetworkLabel != null) {
                    params.put("private.network.device", privateNetworkLabel);
                    details.put("private.network.device", privateNetworkLabel);
                }

                if (!params.containsKey("storage.network.device1") && storageNetworkLabel != null) {
                    params.put("storage.network.device1", storageNetworkLabel);
                    details.put("storage.network.device1", storageNetworkLabel);
                }

                final DataCenterVO zone = _dcDao.findById(dcId);
                final boolean securityGroupEnabled = zone.isSecurityGroupEnabled();
                params.put("securitygroupenabled", Boolean.toString(securityGroupEnabled));

                params.put("router.aggregation.command.each.timeout", _configDao.getValue(Config.RouterAggregationCommandEachTimeout.toString()));
                params.put("wait", Integer.toString(_wait));
                details.put("wait", Integer.toString(_wait));
                params.put("migratewait", _configDao.getValue(Config.MigrateWait.toString()));
                params.put(Config.XenServerMaxNics.toString().toLowerCase(), _configDao.getValue(Config.XenServerMaxNics.toString()));
                params.put(Config.XenServerHeartBeatTimeout.toString().toLowerCase(), _configDao.getValue(Config.XenServerHeartBeatTimeout.toString()));
                params.put(Config.XenServerHeartBeatInterval.toString().toLowerCase(), _configDao.getValue(Config.XenServerHeartBeatInterval.toString()));
                params.put(Config.InstanceName.toString().toLowerCase(), _instance);
                details.put(Config.InstanceName.toString().toLowerCase(), _instance);
                try {
                    resource.configure("XenServer", params);
                } catch (final ConfigurationException e) {
                    _alertMgr.sendAlert(AlertManager.AlertType.ALERT_TYPE_HOST, dcId, podId, "Unable to add " + record.address, "Error is " + e.getMessage());
                    s_logger.warn("Unable to instantiate " + record.address, e);
                    continue;
                }
                resource.start();
                resources.put(resource, details);
            }
        } catch (final SessionAuthenticationFailed e) {
            throw new DiscoveredWithErrorException("Authentication error");
        } catch (final XenAPIException e) {
            s_logger.warn("XenAPI exception", e);
            return null;
        } catch (final XmlRpcException e) {
            s_logger.warn("Xml Rpc Exception", e);
            return null;
        } catch (final UnknownHostException e) {
            s_logger.warn("Unable to resolve the host name", e);
            return null;
        } catch (final Exception e) {
            s_logger.debug("other exceptions: " + e.toString(), e);
            return null;
        }
        return resources;
    }

    protected boolean poolHasHotFix(final Connection conn, final String hostIp, final String hotFixUuid) {
        try {
            final Map<Host, Host.Record> hosts = Host.getAllRecords(conn);
            for (final Map.Entry<Host, Host.Record> entry : hosts.entrySet()) {

                final Host.Record re = entry.getValue();
                if (!re.address.equalsIgnoreCase(hostIp)) {
                    continue;
                }
                final Set<HostPatch> patches = re.patches;
                final PoolPatch poolPatch = PoolPatch.getByUuid(conn, hotFixUuid);
                for (final HostPatch patch : patches) {
                    final PoolPatch pp = patch.getPoolPatch(conn);
                    if (pp != null && pp.equals(poolPatch) && patch.getApplied(conn)) {
                        s_logger.debug("host " + hostIp + " does have " + hotFixUuid + " Hotfix.");
                        return true;
                    }
                }
            }
            return false;
        } catch (final UuidInvalid e) {
            s_logger.debug("host " + hostIp + " doesn't have " + hotFixUuid + " Hotfix");
        } catch (final Exception e) {
            s_logger.debug("can't get patches information, consider it doesn't have " + hotFixUuid + " Hotfix");
        }
        return false;
    }

    void setClusterGuid(final ClusterVO cluster, final String guid) {
        cluster.setGuid(guid);
        try {
            _clusterDao.update(cluster.getId(), cluster);
        } catch (final EntityExistsException e) {
            final QueryBuilder<ClusterVO> sc = QueryBuilder.create(ClusterVO.class);
            sc.and(sc.entity().getGuid(), Op.EQ, guid);
            final List<ClusterVO> clusters = sc.list();
            final ClusterVO clu = clusters.get(0);
            final List<HostVO> clusterHosts = _resourceMgr.listAllHostsInCluster(clu.getId());
            if (clusterHosts == null || clusterHosts.size() == 0) {
                clu.setGuid(null);
                _clusterDao.update(clu.getId(), clu);
                _clusterDao.update(cluster.getId(), cluster);
                return;
            }
            throw e;
        }
    }

    protected CitrixResourceBase createServerResource(final long dcId, final Long podId, final Host.Record record, final String hotfix) {
        String prodBrand = record.softwareVersion.get("product_brand");
        if (prodBrand == null) {
            prodBrand = record.softwareVersion.get("platform_name").trim();
        } else {
            prodBrand = prodBrand.trim();
        }
        final String prodVersion = CitrixHelper.getProductVersion(record);

        final String prodVersionTextShort = record.softwareVersion.get("product_version_text_short");
        return createServerResource(prodBrand, prodVersion, prodVersionTextShort, hotfix);
    }

    protected CitrixResourceBase createServerResource(final String prodBrand, final String prodVersion, final String prodVersionTextShort, final String hotfix) {
        // Xen Cloud Platform group of hypervisors
        if (prodBrand.equals("XCP") && (prodVersion.equals("1.0.0") || prodVersion.equals("1.1.0")
                || prodVersion.equals("5.6.100") || prodVersion.startsWith("1.4") || prodVersion.startsWith("1.6"))) {
            return new XcpServerResource();
        } // Citrix Xenserver group of hypervisors
        else if (prodBrand.equals("XenServer") && prodVersion.equals("5.6.0")) {
            return new XenServer56Resource();
        } else if (prodBrand.equals("XenServer") && prodVersion.equals("6.0.0")) {
            return new XenServer600Resource();
        } else if (prodBrand.equals("XenServer") && prodVersion.equals("6.0.2")) {
            return new XenServer600Resource();
        } else if (prodBrand.equals("XenServer") && prodVersion.equals("6.1.0")) {
            return new XenServer610Resource();
        } else if (prodBrand.equals("XenServer") && prodVersion.equals("6.5.0")) {
            return new XenServer650Resource();
        } else if (prodBrand.equals("XenServer") && prodVersion.equals("6.2.0")) {
            if (hotfix != null && hotfix.equals(XenserverConfigs.XSHotFix62ESP1004)) {
                return new Xenserver625Resource();
            } else if (hotfix != null && hotfix.equals(XenserverConfigs.XSHotFix62ESP1)) {
                return new XenServer620SP1Resource();
            } else {
                return new XenServer620Resource();
            }
        } else if (prodBrand.equals("XenServer") && prodVersion.equals("5.6.100")) {
            if ("5.6 SP2".equals(prodVersionTextShort.trim())) {
                return new XenServer56SP2Resource();
            } else if ("5.6 FP1".equals(prodVersionTextShort.trim())) {
                return new XenServer56FP1Resource();
            }
        } else if (prodBrand.equals("XCP_Kronos")) {
            return new XcpOssResource();
        }
        final String msg =
                "Only support XCP 1.0.0, 1.1.0, 1.4.x, 1.5 beta, 1.6.x; XenServer 5.6,  XenServer 5.6 FP1, XenServer 5.6 SP2, Xenserver 6.0, 6.0.2, 6.1.0, 6.2.0, 6.5.0 but this " +
                        "one is " +
                        prodBrand + " " + prodVersion;
        s_logger.warn(msg);
        throw new RuntimeException(msg);
    }

    @Override
    public void postDiscovery(final List<HostVO> hosts, final long msId) throws DiscoveryException {
        //do nothing
    }

    @Override
    public boolean matchHypervisor(final String hypervisor) {
        if (hypervisor == null) {
            return true;
        }
        return Hypervisor.HypervisorType.XenServer.toString().equalsIgnoreCase(hypervisor);
    }

    @Override
    public Hypervisor.HypervisorType getHypervisorType() {
        return Hypervisor.HypervisorType.XenServer;
    }

    String getPoolUuid(final Connection conn) throws XenAPIException, XmlRpcException {
        final Map<Pool, Pool.Record> pools = Pool.getAllRecords(conn);
        assert pools.size() == 1 : "Pools size is " + pools.size();
        return pools.values().iterator().next().uuid;
    }

    protected void addSamePool(final Connection conn, final Map<CitrixResourceBase, Map<String, String>> resources) throws XenAPIException, XmlRpcException {
        final Map<Pool, Pool.Record> hps = Pool.getAllRecords(conn);
        assert (hps.size() == 1) : "How can it be more than one but it's actually " + hps.size();

        // This is the pool.
        final String poolUuid = hps.values().iterator().next().uuid;

        for (final Map<String, String> details : resources.values()) {
            details.put("pool", poolUuid);
        }
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);
        serverConfig();

        _publicNic = _params.get(Config.XenServerPublicNetwork.key());
        _privateNic = _params.get(Config.XenServerPrivateNetwork.key());

        _storageNic1 = _params.get(Config.XenServerStorageNetwork1.key());
        _storageNic2 = _params.get(Config.XenServerStorageNetwork2.key());

        _guestNic = _params.get(Config.XenServerGuestNetwork.key());

        String value = _params.get(Config.XapiWait.toString());
        _wait = NumbersUtil.parseInt(value, Integer.parseInt(Config.XapiWait.getDefaultValue()));

        _instance = _params.get(Config.InstanceName.key());

        value = _params.get(Config.XenServerSetupMultipath.key());
        Boolean.parseBoolean(value);

        value = _params.get("xenserver.check.hvm");
        _checkHvm = Boolean.parseBoolean(value);
        _connPool = XenServerConnectionPool.getInstance();

        _agentMgr.registerForHostEvents(this, true, false, true);

        createXsToolsISO();
        _resourceMgr.registerResourceStateAdapter(this.getClass().getSimpleName(), this);
        return true;
    }

    protected void serverConfig() {
        final String value = _params.get(Config.XenServerSetupMultipath.key());
        _setupMultipath = Boolean.parseBoolean(value);
    }

    private void createXsToolsISO() {
        final String isoName = "xs-tools.iso";
        final VMTemplateVO tmplt = _tmpltDao.findByTemplateName(isoName);
        final Long id;
        if (tmplt == null) {
            id = _tmpltDao.getNextInSequence(Long.class, "id");
            final VMTemplateVO template =
                    VMTemplateVO.createPreHostIso(id, isoName, isoName, ImageFormat.ISO, true, true, TemplateType.PERHOST, null, null, true, 64, Account.ACCOUNT_ID_SYSTEM,
                            null, "xen-pv-drv-iso", false, 1, false, HypervisorType.XenServer);
            _tmpltDao.persist(template);
        } else {
            id = tmplt.getId();
            tmplt.setTemplateType(TemplateType.PERHOST);
            tmplt.setUrl(null);
            _tmpltDao.update(id, tmplt);
        }
    }

    @Override
    public boolean stop() {
        _resourceMgr.unregisterResourceStateAdapter(this.getClass().getSimpleName());
        return super.stop();
    }

    @Override
    protected HashMap<String, Object> buildConfigParams(final HostVO host) {
        final HashMap<String, Object> params = super.buildConfigParams(host);
        final DataCenterVO zone = _dcDao.findById(host.getDataCenterId());
        if (zone != null) {
            final boolean securityGroupEnabled = zone.isSecurityGroupEnabled();
            params.put("securitygroupenabled", Boolean.toString(securityGroupEnabled));
        }
        return params;
    }

    @Override
    public boolean processAnswers(final long agentId, final long seq, final Answer[] answers) {
        return false;
    }

    @Override
    public boolean processCommands(final long agentId, final long seq, final Command[] commands) {
        return false;
    }

    @Override
    public AgentControlAnswer processControlCommand(final long agentId, final AgentControlCommand cmd) {
        return null;
    }

    @Override
    public void processConnect(final com.cloud.host.Host agent, final StartupCommand cmd, final boolean forRebalance) throws ConnectionException {
        if (!(cmd instanceof StartupRoutingCommand)) {
            return;
        }
        final long agentId = agent.getId();

        final StartupRoutingCommand startup = (StartupRoutingCommand) cmd;
        if (startup.getHypervisorType() != HypervisorType.XenServer) {
            s_logger.debug("Not XenServer so moving on.");
            return;
        }

        final HostVO host = _hostDao.findById(agentId);

        final ClusterVO cluster = _clusterDao.findById(host.getClusterId());
        if (cluster.getGuid() == null) {
            cluster.setGuid(startup.getPool());
            _clusterDao.update(cluster.getId(), cluster);
        } else if (!cluster.getGuid().equals(startup.getPool())) {
            final String msg = "pool uuid for cluster " + cluster.getId() + " changed from " + cluster.getGuid() + " to " + startup.getPool();
            s_logger.warn(msg);
            throw new CloudRuntimeException(msg);
        }

        final Map<String, String> details = startup.getHostDetails();
        final String prodBrand = details.get("product_brand").trim();
        final String prodVersion = details.get("product_version").trim();
        final String hotfix = details.get(XenserverConfigs.XS620HotFix);
        final String prodVersionTextShort = details.get("product_version_text_short");

        final String resource = createServerResource(prodBrand, prodVersion, prodVersionTextShort, hotfix).getClass().getName();

        if (!resource.equals(host.getResource())) {
            final String msg = "host " + host.getPrivateIpAddress() + " changed from " + host.getResource() + " to " + resource;
            s_logger.debug(msg);
            host.setResource(resource);
            host.setSetup(false);
            _hostDao.update(agentId, host);
            throw new HypervisorVersionChangedException(msg);
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Setting up host " + agentId);
        }
        final HostEnvironment env = new HostEnvironment();

        final SetupCommand setup = new SetupCommand(env);
        if (_setupMultipath) {
            setup.setMultipathOn();
        }
        if (!host.isSetup()) {
            setup.setNeedSetup(true);
        }

        try {
            final Answer answer = _agentMgr.send(agentId, setup);
            if (answer != null && answer.getResult() && answer instanceof SetupAnswer) {
                host.setSetup(true);
                host.setLastPinged((System.currentTimeMillis() >> 10) - 5 * 60);
                host.setHypervisorVersion(prodVersion);
                _hostDao.update(host.getId(), host);
                if (((SetupAnswer) answer).needReconnect()) {
                    throw new ConnectionException(false, "Reinitialize agent after setup.");
                }
                return;
            } else {
                s_logger.warn("Unable to setup agent " + agentId + " due to " + ((answer != null) ? answer.getDetails() : "return null"));
            }
        } catch (final AgentUnavailableException e) {
            s_logger.warn("Unable to setup agent " + agentId + " because it became unavailable.", e);
        } catch (final OperationTimedoutException e) {
            s_logger.warn("Unable to setup agent " + agentId + " because it timed out", e);
        }
        throw new ConnectionException(true, "Reinitialize agent after setup.");
    }

    @Override
    public boolean processDisconnect(final long agentId, final Status state) {
        return false;
    }

    @Override
    public boolean isRecurring() {
        return false;
    }

    @Override
    public int getTimeout() {
        return 0;
    }

    @Override
    public boolean processTimeout(final long agentId, final long seq) {
        return false;
    }

    @Override
    public HostVO createHostVOForConnectedAgent(final HostVO host, final StartupCommand[] cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HostVO createHostVOForDirectConnectAgent(final HostVO host, final StartupCommand[] startup, final ServerResource resource, final Map<String, String> details, final
    List<String> hostTags) {
        final StartupCommand firstCmd = startup[0];
        if (!(firstCmd instanceof StartupRoutingCommand)) {
            return null;
        }

        final StartupRoutingCommand ssCmd = ((StartupRoutingCommand) firstCmd);
        if (ssCmd.getHypervisorType() != HypervisorType.XenServer) {
            return null;
        }

        final HostPodVO pod = _podDao.findById(host.getPodId());
        final DataCenterVO dc = _dcDao.findById(host.getDataCenterId());
        s_logger.info("Host: " + host.getName() + " connected with hypervisor type: " + HypervisorType.XenServer + ". Checking CIDR...");
        _resourceMgr.checkCIDR(pod, dc, ssCmd.getPrivateIpAddress(), ssCmd.getPrivateNetmask());
        return _resourceMgr.fillRoutingHostVO(host, ssCmd, HypervisorType.XenServer, details, hostTags);
    }

    @Override
    public DeleteHostAnswer deleteHost(final HostVO host, final boolean isForced, final boolean isForceDeleteStorage) throws UnableDeleteHostException {
        if (host.getType() != com.cloud.host.Host.Type.Routing || host.getHypervisorType() != HypervisorType.XenServer) {
            return null;
        }

        _resourceMgr.deleteRoutingHost(host, isForced, isForceDeleteStorage);
        return new DeleteHostAnswer(true);
    }
}
