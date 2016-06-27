package com.cloud.consoleproxy;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.ConsoleProxyLoadReportCommand;
import com.cloud.agent.api.RebootCommand;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupProxyCommand;
import com.cloud.agent.api.check.CheckSshAnswer;
import com.cloud.agent.api.check.CheckSshCommand;
import com.cloud.agent.api.proxy.ConsoleProxyLoadAnswer;
import com.cloud.agent.manager.Commands;
import com.cloud.cluster.ClusterManager;
import com.cloud.configuration.Config;
import com.cloud.configuration.ConfigurationManagerImpl;
import com.cloud.configuration.ZoneConfig;
import com.cloud.dc.DataCenter;
import com.cloud.dc.DataCenter.NetworkType;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.deploy.DataCenterDeployment;
import com.cloud.deploy.DeployDestination;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.exception.StorageUnavailableException;
import com.cloud.host.Host;
import com.cloud.host.Host.Type;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.info.ConsoleProxyConnectionInfo;
import com.cloud.info.ConsoleProxyInfo;
import com.cloud.info.ConsoleProxyLoadInfo;
import com.cloud.info.ConsoleProxyStatus;
import com.cloud.info.RunningHostCountInfo;
import com.cloud.info.RunningHostInfoAgregator;
import com.cloud.info.RunningHostInfoAgregator.ZoneHostInfo;
import com.cloud.network.Network;
import com.cloud.network.NetworkModel;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.IPAddressVO;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkVO;
import com.cloud.network.rules.RulesManager;
import com.cloud.offering.NetworkOffering;
import com.cloud.offering.ServiceOffering;
import com.cloud.offerings.dao.NetworkOfferingDao;
import com.cloud.resource.ResourceManager;
import com.cloud.resource.ResourceStateAdapter;
import com.cloud.resource.ServerResource;
import com.cloud.resource.UnableDeleteHostException;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.Storage;
import com.cloud.storage.StoragePoolStatus;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.utils.DateUtil;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GlobalLock;
import com.cloud.utils.db.QueryBuilder;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallbackNoReturn;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.utils.events.SubscriptionMgr;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.ConsoleProxyVO;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.SystemVmLoadScanHandler;
import com.cloud.vm.SystemVmLoadScanner;
import com.cloud.vm.SystemVmLoadScanner.AfterScanAction;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.State;
import com.cloud.vm.VirtualMachineGuru;
import com.cloud.vm.VirtualMachineManager;
import com.cloud.vm.VirtualMachineName;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.dao.ConsoleProxyDao;
import com.cloud.vm.dao.UserVmDetailsDao;
import com.cloud.vm.dao.VMInstanceDao;
import org.apache.cloudstack.config.ApiServiceConfiguration;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.engine.orchestration.service.NetworkOrchestrationService;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.framework.security.keys.KeysManager;
import org.apache.cloudstack.framework.security.keystore.KeystoreDao;
import org.apache.cloudstack.framework.security.keystore.KeystoreManager;
import org.apache.cloudstack.framework.security.keystore.KeystoreVO;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.StoragePoolVO;
import org.apache.cloudstack.storage.datastore.db.TemplateDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.TemplateDataStoreVO;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//
// Possible console proxy state transition cases
//        Stopped --> Starting -> Running
//        HA -> Stopped -> Starting -> Running
//        Migrating -> Running    (if previous state is Running before it enters into Migrating state
//        Migrating -> Stopped    (if previous state is not Running before it enters into Migrating state)
//        Running -> HA            (if agent lost connection)
//        Stopped -> Destroyed
//
// Starting, HA, Migrating, Running state are all counted as "Open" for available capacity calculation
// because sooner or later, it will be driven into Running state
//
public class ConsoleProxyManagerImpl extends ManagerBase implements ConsoleProxyManager, VirtualMachineGuru, SystemVmLoadScanHandler<Long>, ResourceStateAdapter {
    private static final Logger s_logger = LoggerFactory.getLogger(ConsoleProxyManagerImpl.class);

    private static final int DEFAULT_CAPACITY_SCAN_INTERVAL = 30000; // 30 seconds
    private static final int ACQUIRE_GLOBAL_LOCK_TIMEOUT_FOR_SYNC = 180; // 3 minutes

    private static final int STARTUP_DELAY = 60000; // 60 seconds
    private final GlobalLock _allocProxyLock = GlobalLock.getInternLock(getAllocProxyLockName());
    private int _consoleProxyPort = ConsoleProxyManager.DEFAULT_PROXY_VNC_PORT;
    private int _mgmtPort = 8250;
    private List<ConsoleProxyAllocator> _consoleProxyAllocators;
    @Inject
    private ConsoleProxyDao _consoleProxyDao;
    @Inject
    private DataCenterDao _dcDao;
    @Inject
    private VMTemplateDao _templateDao;
    @Inject
    private HostPodDao _podDao;
    @Inject
    private HostDao _hostDao;
    @Inject
    private ConfigurationDao _configDao;
    @Inject
    private VMInstanceDao _instanceDao;
    @Inject
    private TemplateDataStoreDao _vmTemplateStoreDao;
    @Inject
    private AgentManager _agentMgr;
    @Inject
    private NetworkOrchestrationService _networkMgr;
    @Inject
    private NetworkModel _networkModel;
    @Inject
    private AccountManager _accountMgr;
    @Inject
    private ServiceOfferingDao _offeringDao;
    @Inject
    private NetworkOfferingDao _networkOfferingDao;
    @Inject
    private PrimaryDataStoreDao _storagePoolDao;
    @Inject
    private UserVmDetailsDao _vmDetailsDao;
    @Inject
    private ResourceManager _resourceMgr;
    @Inject
    private NetworkDao _networkDao;
    @Inject
    private RulesManager _rulesMgr;
    @Inject
    private IPAddressDao _ipAddressDao;
    @Inject
    private KeysManager _keysMgr;
    @Inject
    private VirtualMachineManager _itMgr;
    private ConsoleProxyListener _listener;
    private ServiceOfferingVO _serviceOffering;
    /*
     * private final ExecutorService _requestHandlerScheduler = Executors.newCachedThreadPool(new
     * NamedThreadFactory("Request-handler"));
     */
    private long _capacityScanInterval = DEFAULT_CAPACITY_SCAN_INTERVAL;
    private int _capacityPerProxy = ConsoleProxyManager.DEFAULT_PROXY_CAPACITY;
    private int _standbyCapacity = ConsoleProxyManager.DEFAULT_STANDBY_CAPACITY;
    private boolean _useStorageVm;
    private boolean _disableRpFilter = false;
    private String _instance;
    private int _proxySessionTimeoutValue = DEFAULT_PROXY_SESSION_TIMEOUT;
    private boolean _sslEnabled = true;
    private String _consoleProxyUrlDomain;
    // global load picture at zone basis
    private SystemVmLoadScanner<Long> _loadScanner;
    private Map<Long, ZoneHostInfo> _zoneHostInfoMap; // map <zone id, info about running host in zone>
    private Map<Long, ConsoleProxyLoadInfo> _zoneProxyCountMap; // map <zone id, info about proxy VMs count in zone>
    private Map<Long, ConsoleProxyLoadInfo> _zoneVmCountMap; // map <zone id, info about running VMs count in zone>
    private String _staticPublicIp;
    private int _staticPort;
    @Inject
    private KeystoreDao _ksDao;
    @Inject
    private KeystoreManager _ksMgr;

    protected ConsoleProxyManagerImpl() {
    }

    @Override
    public ConsoleProxyInfo assignProxy(final long dataCenterId, final long vmId) {
        final ConsoleProxyVO proxy = doAssignProxy(dataCenterId, vmId);
        if (proxy == null) {
            return null;
        }

        if (proxy.getPublicIpAddress() == null) {
            s_logger.warn("Assigned console proxy does not have a valid public IP address");
            return null;
        }

        final KeystoreVO ksVo = _ksDao.findByName(ConsoleProxyManager.CERTIFICATE_NAME);
        if (proxy.isSslEnabled() && ksVo == null) {
            s_logger.warn("SSL enabled for console proxy but no server certificate found in database");
        }

        if (_staticPublicIp == null) {
            return new ConsoleProxyInfo(proxy.isSslEnabled(), proxy.getPublicIpAddress(), _consoleProxyPort, proxy.getPort(), _consoleProxyUrlDomain);
        } else {
            return new ConsoleProxyInfo(proxy.isSslEnabled(), _staticPublicIp, _consoleProxyPort, _staticPort, _consoleProxyUrlDomain);
        }
    }

    public ConsoleProxyVO doAssignProxy(final long dataCenterId, final long vmId) {
        ConsoleProxyVO proxy = null;
        final VMInstanceVO vm = _instanceDao.findById(vmId);

        if (vm == null) {
            s_logger.warn("VM " + vmId + " no longer exists, return a null proxy for vm:" + vmId);
            return null;
        }

        if (vm != null && vm.getState() != State.Running) {
            if (s_logger.isInfoEnabled()) {
                s_logger.info("Detected that vm : " + vmId + " is not currently at running state, we will fail the proxy assignment for it");
            }
            return null;
        }

        if (_allocProxyLock.lock(ACQUIRE_GLOBAL_LOCK_TIMEOUT_FOR_SYNC)) {
            try {
                if (vm.getProxyId() != null) {
                    proxy = _consoleProxyDao.findById(vm.getProxyId());

                    if (proxy != null) {
                        if (!isInAssignableState(proxy)) {
                            if (s_logger.isInfoEnabled()) {
                                s_logger.info("A previous assigned proxy is not assignable now, reassign console proxy for user vm : " + vmId);
                            }
                            proxy = null;
                        } else {
                            if (_consoleProxyDao.getProxyActiveLoad(proxy.getId()) < _capacityPerProxy || hasPreviousSession(proxy, vm)) {
                                if (s_logger.isTraceEnabled()) {
                                    s_logger.trace("Assign previous allocated console proxy for user vm : " + vmId);
                                }

                                if (proxy.getActiveSession() >= _capacityPerProxy) {
                                    s_logger.warn("Assign overloaded proxy to user VM as previous session exists, user vm : " + vmId);
                                }
                            } else {
                                proxy = null;
                            }
                        }
                    }
                }

                if (proxy == null) {
                    proxy = assignProxyFromRunningPool(dataCenterId);
                }
            } finally {
                _allocProxyLock.unlock();
            }
        } else {
            s_logger.error("Unable to acquire synchronization lock to get/allocate proxy resource for vm :" + vmId +
                    ". Previous console proxy allocation is taking too long");
        }

        if (proxy == null) {
            s_logger.warn("Unable to find or allocate console proxy resource");
            return null;
        }

        // if it is a new assignment or a changed assignment, update the record
        if (vm.getProxyId() == null || vm.getProxyId().longValue() != proxy.getId()) {
            _instanceDao.updateProxyId(vmId, proxy.getId(), DateUtil.currentGMTTime());
        }

        proxy.setSslEnabled(_sslEnabled);
        if (_sslEnabled) {
            proxy.setPort(443);
        } else {
            proxy.setPort(80);
        }

        return proxy;
    }

    private static boolean isInAssignableState(final ConsoleProxyVO proxy) {
        // console proxies that are in states of being able to serve user VM
        final State state = proxy.getState();
        if (state == State.Running) {
            return true;
        }

        return false;
    }

    private boolean hasPreviousSession(final ConsoleProxyVO proxy, final VMInstanceVO vm) {

        ConsoleProxyStatus status = null;
        try {
            final GsonBuilder gb = new GsonBuilder();
            gb.setVersion(1.3);
            final Gson gson = gb.create();

            final byte[] details = proxy.getSessionDetails();
            status = gson.fromJson(details != null ? new String(details, Charset.forName("US-ASCII")) : null, ConsoleProxyStatus.class);
        } catch (final Throwable e) {
            s_logger.warn("Unable to parse proxy session details : " + Arrays.toString(proxy.getSessionDetails()));
        }

        if (status != null && status.getConnections() != null) {
            final ConsoleProxyConnectionInfo[] connections = status.getConnections();
            for (int i = 0; i < connections.length; i++) {
                long taggedVmId = 0;
                if (connections[i].tag != null) {
                    try {
                        taggedVmId = Long.parseLong(connections[i].tag);
                    } catch (final NumberFormatException e) {
                        s_logger.warn("Unable to parse console proxy connection info passed through tag: " + connections[i].tag, e);
                    }
                }
                if (taggedVmId == vm.getId()) {
                    return true;
                }
            }

            //
            // even if we are not in the list, it may because we haven't
            // received load-update yet
            // wait until session time
            //
            if (DateUtil.currentGMTTime().getTime() - vm.getProxyAssignTime().getTime() < _proxySessionTimeoutValue) {
                return true;
            }

            return false;
        } else {
            s_logger.error("No proxy load info on an overloaded proxy ?");
            return false;
        }
    }

    public ConsoleProxyVO assignProxyFromRunningPool(final long dataCenterId) {

        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Assign console proxy from running pool for request from data center : " + dataCenterId);
        }

        final ConsoleProxyAllocator allocator = getCurrentAllocator();
        assert (allocator != null);
        final List<ConsoleProxyVO> runningList = _consoleProxyDao.getProxyListInStates(dataCenterId, State.Running);
        if (runningList != null && runningList.size() > 0) {
            final Iterator<ConsoleProxyVO> it = runningList.iterator();
            while (it.hasNext()) {
                final ConsoleProxyVO proxy = it.next();
                if (proxy.getActiveSession() >= _capacityPerProxy) {
                    it.remove();
                }
            }
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Running proxy pool size : " + runningList.size());
                for (final ConsoleProxyVO proxy : runningList) {
                    s_logger.trace("Running proxy instance : " + proxy.getHostName());
                }
            }

            final List<Pair<Long, Integer>> l = _consoleProxyDao.getProxyLoadMatrix();
            final Map<Long, Integer> loadInfo = new HashMap<>();
            if (l != null) {
                for (final Pair<Long, Integer> p : l) {
                    loadInfo.put(p.first(), p.second());

                    if (s_logger.isTraceEnabled()) {
                        s_logger.trace("Running proxy instance allocation load { proxy id : " + p.first() + ", load : " + p.second() + "}");
                    }
                }
            }
            final Long allocated = allocator.allocProxy(runningList, loadInfo, dataCenterId);
            if (allocated == null) {
                s_logger.debug("Unable to find a console proxy ");
                return null;
            }
            return _consoleProxyDao.findById(allocated);
        } else {
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Empty running proxy pool for now in data center : " + dataCenterId);
            }
        }
        return null;
    }

    private ConsoleProxyAllocator getCurrentAllocator() {
        // for now, only one adapter is supported
        for (final ConsoleProxyAllocator allocator : _consoleProxyAllocators) {
            return allocator;
        }

        return null;
    }

    public void onLoadAnswer(final ConsoleProxyLoadAnswer answer) {
        if (answer.getDetails() == null) {
            return;
        }

        ConsoleProxyStatus status = null;
        try {
            final GsonBuilder gb = new GsonBuilder();
            gb.setVersion(1.3);
            final Gson gson = gb.create();
            status = gson.fromJson(answer.getDetails(), ConsoleProxyStatus.class);
        } catch (final Throwable e) {
            s_logger.warn("Unable to parse load info from proxy, proxy vm id : " + answer.getProxyVmId() + ", info : " + answer.getDetails());
        }

        if (status != null) {
            int count = 0;
            if (status.getConnections() != null) {
                count = status.getConnections().length;
            }

            byte[] details = null;
            if (answer.getDetails() != null) {
                details = answer.getDetails().getBytes(Charset.forName("US-ASCII"));
            }
            _consoleProxyDao.update(answer.getProxyVmId(), count, DateUtil.currentGMTTime(), details);
        } else {
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Unable to get console proxy load info, id : " + answer.getProxyVmId());
            }

            _consoleProxyDao.update(answer.getProxyVmId(), 0, DateUtil.currentGMTTime(), null);
            // TODO : something is wrong with the VM, restart it?
        }
    }

    public void handleAgentDisconnect(final long agentId, final com.cloud.host.Status state) {
        if (state == com.cloud.host.Status.Alert || state == com.cloud.host.Status.Disconnected) {
            // be it either in alert or in disconnected state, the agent process
            // may be gone in the VM,
            // we will be reacting to stop the corresponding VM and let the scan
            // process to
            final HostVO host = _hostDao.findById(agentId);
            if (host.getType() == Type.ConsoleProxy) {
                final String name = host.getName();
                if (s_logger.isInfoEnabled()) {
                    s_logger.info("Console proxy agent disconnected, proxy: " + name);
                }
                if (name != null && name.startsWith("v-")) {
                    final String[] tokens = name.split("-");
                    long proxyVmId = 0;
                    try {
                        proxyVmId = Long.parseLong(tokens[1]);
                    } catch (final NumberFormatException e) {
                        s_logger.error("Unexpected exception " + e.getMessage(), e);
                        return;
                    }

                    final ConsoleProxyVO proxy = _consoleProxyDao.findById(proxyVmId);
                    if (proxy != null) {

                        // Disable this feature for now, as it conflicts with
                        // the case of allowing user to reboot console proxy
                        // when rebooting happens, we will receive disconnect
                        // here and we can't enter into stopping process,
                        // as when the rebooted one comes up, it will kick off a
                        // newly started one and trigger the process
                        // continue on forever

                        /*
                         * _capacityScanScheduler.execute(new Runnable() { public void run() {
                         * if(s_logger.isInfoEnabled())
                         * s_logger.info("Stop console proxy " + proxy.getName() +
                         * " VM because of that the agent running inside it has disconnected" );
                         * stopProxy(proxy.getId()); } });
                         */
                    } else {
                        if (s_logger.isInfoEnabled()) {
                            s_logger.info("Console proxy agent disconnected but corresponding console proxy VM no longer exists in DB, proxy: " + name);
                        }
                    }
                } else {
                    assert (false) : "Invalid console proxy name: " + name;
                }
            }
        }
    }

    private String getAllocProxyLockName() {
        return "consoleproxy.alloc";
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Start configuring console proxy manager : " + name);
        }

        final Map<String, String> configs = _configDao.getConfiguration("management-server", params);

        String value = configs.get(Config.ConsoleProxyCmdPort.key());
        value = configs.get("consoleproxy.sslEnabled");
        if (value != null && value.equalsIgnoreCase("true")) {
            _sslEnabled = true;
        }

        _consoleProxyUrlDomain = configs.get(Config.ConsoleProxyUrlDomain.key());
        if (_sslEnabled && (_consoleProxyUrlDomain == null || _consoleProxyUrlDomain.isEmpty())) {
            s_logger.warn("Empty console proxy domain, explicitly disabling SSL");
            _sslEnabled = false;
        }

        value = configs.get(Config.ConsoleProxyCapacityScanInterval.key());
        _capacityScanInterval = NumbersUtil.parseLong(value, DEFAULT_CAPACITY_SCAN_INTERVAL);

        _capacityPerProxy = NumbersUtil.parseInt(configs.get("consoleproxy.session.max"), DEFAULT_PROXY_CAPACITY);
        _standbyCapacity = NumbersUtil.parseInt(configs.get("consoleproxy.capacity.standby"), DEFAULT_STANDBY_CAPACITY);
        _proxySessionTimeoutValue = NumbersUtil.parseInt(configs.get("consoleproxy.session.timeout"), DEFAULT_PROXY_SESSION_TIMEOUT);

        value = configs.get("consoleproxy.port");
        if (value != null) {
            _consoleProxyPort = NumbersUtil.parseInt(value, ConsoleProxyManager.DEFAULT_PROXY_VNC_PORT);
        }

        value = configs.get(Config.ConsoleProxyDisableRpFilter.key());
        if (value != null && value.equalsIgnoreCase("true")) {
            _disableRpFilter = true;
        }

        value = configs.get("secondary.storage.vm");
        if (value != null && value.equalsIgnoreCase("true")) {
            _useStorageVm = true;
        }

        if (s_logger.isInfoEnabled()) {
            s_logger.info("Console proxy max session soft limit : " + _capacityPerProxy);
            s_logger.info("Console proxy standby capacity : " + _standbyCapacity);
        }

        _instance = configs.get("instance.name");
        if (_instance == null) {
            _instance = "DEFAULT";
        }

        final Map<String, String> agentMgrConfigs = _configDao.getConfiguration("AgentManager", params);

        value = agentMgrConfigs.get("port");
        _mgmtPort = NumbersUtil.parseInt(value, 8250);

        _listener = new ConsoleProxyListener(new VmBasedAgentHook(_instanceDao, _hostDao, _configDao, _ksMgr, _agentMgr, _keysMgr));
        _agentMgr.registerForHostEvents(_listener, true, true, false);

        _itMgr.registerGuru(VirtualMachine.Type.ConsoleProxy, this);

        //check if there is a default service offering configured
        final String cpvmSrvcOffIdStr = configs.get(Config.ConsoleProxyServiceOffering.key());
        if (cpvmSrvcOffIdStr != null) {
            _serviceOffering = _offeringDao.findByUuid(cpvmSrvcOffIdStr);
            if (_serviceOffering == null) {
                try {
                    _serviceOffering = _offeringDao.findById(Long.parseLong(cpvmSrvcOffIdStr));
                } catch (final NumberFormatException ex) {
                    s_logger.debug("The system service offering specified by global config is not id, but uuid=" + cpvmSrvcOffIdStr + " for console proxy vm");
                }
            }
            if (_serviceOffering == null) {
                s_logger.warn("Can't find system service offering specified by global config, uuid=" + cpvmSrvcOffIdStr + " for console proxy vm");
            }
        }

        if (_serviceOffering == null || !_serviceOffering.getSystemUse()) {
            final int ramSize = NumbersUtil.parseInt(_configDao.getValue("console.ram.size"), DEFAULT_PROXY_VM_RAMSIZE);
            final int cpuFreq = NumbersUtil.parseInt(_configDao.getValue("console.cpu.mhz"), DEFAULT_PROXY_VM_CPUMHZ);
            final List<ServiceOfferingVO> offerings = _offeringDao.createSystemServiceOfferings("System Offering For Console Proxy",
                    ServiceOffering.consoleProxyDefaultOffUniqueName, 1, ramSize, cpuFreq, 0, 0, false, null,
                    Storage.ProvisioningType.THIN, true, null, true, VirtualMachine.Type.ConsoleProxy, true);
            // this can sometimes happen, if DB is manually or programmatically manipulated
            if (offerings == null || offerings.size() < 2) {
                final String msg = "Data integrity problem : System Offering For Console Proxy has been removed?";
                s_logger.error(msg);
                throw new ConfigurationException(msg);
            }
        }

        _loadScanner = new SystemVmLoadScanner<>(this);
        _loadScanner.initScan(STARTUP_DELAY, _capacityScanInterval);
        _resourceMgr.registerResourceStateAdapter(this.getClass().getSimpleName(), this);

        _staticPublicIp = _configDao.getValue("consoleproxy.static.publicIp");
        if (_staticPublicIp != null) {
            _staticPort = NumbersUtil.parseInt(_configDao.getValue("consoleproxy.static.port"), 8443);
        }

        if (s_logger.isInfoEnabled()) {
            s_logger.info("Console Proxy Manager is configured.");
        }
        return true;
    }

    @Override
    public boolean start() {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Start console proxy manager");
        }

        return true;
    }

    @Override
    public boolean stop() {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Stop console proxy manager");
        }

        _loadScanner.stop();
        _allocProxyLock.releaseRef();
        _resourceMgr.unregisterResourceStateAdapter(this.getClass().getSimpleName());
        return true;
    }

    @Override
    public boolean finalizeVirtualMachineProfile(final VirtualMachineProfile profile, final DeployDestination dest, final ReservationContext context) {

        final ConsoleProxyVO vm = _consoleProxyDao.findById(profile.getId());
        final Map<String, String> details = _vmDetailsDao.listDetailsKeyPairs(vm.getId());
        vm.setDetails(details);

        final StringBuilder buf = profile.getBootArgsBuilder();
        buf.append(" template=domP type=consoleproxy");
        buf.append(" host=").append(ApiServiceConfiguration.ManagementHostIPAdr.value());
        buf.append(" port=").append(_mgmtPort);
        buf.append(" name=").append(profile.getVirtualMachine().getHostName());
        if (_sslEnabled) {
            buf.append(" premium=true");
        }
        buf.append(" zone=").append(dest.getDataCenter().getId());
        buf.append(" pod=").append(dest.getPod().getId());
        buf.append(" guid=Proxy.").append(profile.getId());
        buf.append(" proxy_vm=").append(profile.getId());
        if (_disableRpFilter) {
            buf.append(" disable_rp_filter=true");
        }

        boolean externalDhcp = false;
        final String externalDhcpStr = _configDao.getValue("direct.attach.network.externalIpAllocator.enabled");
        if (externalDhcpStr != null && externalDhcpStr.equalsIgnoreCase("true")) {
            externalDhcp = true;
        }

        if (Boolean.valueOf(_configDao.getValue("system.vm.random.password"))) {
            buf.append(" vmpassword=").append(_configDao.getValue("system.vm.password"));
        }

        for (final NicProfile nic : profile.getNics()) {
            final int deviceId = nic.getDeviceId();
            if (nic.getIPv4Address() == null) {
                buf.append(" eth").append(deviceId).append("ip=").append("0.0.0.0");
                buf.append(" eth").append(deviceId).append("mask=").append("0.0.0.0");
            } else {
                buf.append(" eth").append(deviceId).append("ip=").append(nic.getIPv4Address());
                buf.append(" eth").append(deviceId).append("mask=").append(nic.getIPv4Netmask());
            }

            if (nic.isDefaultNic()) {
                buf.append(" gateway=").append(nic.getIPv4Gateway());
            }

            if (nic.getTrafficType() == TrafficType.Management) {
                final String mgmt_cidr = _configDao.getValue(Config.ManagementNetwork.key());
                if (NetUtils.isValidCIDR(mgmt_cidr)) {
                    buf.append(" mgmtcidr=").append(mgmt_cidr);
                }
                buf.append(" localgw=").append(dest.getPod().getGateway());
            }
        }

        /* External DHCP mode */
        if (externalDhcp) {
            buf.append(" bootproto=dhcp");
        }
        final DataCenterVO dc = _dcDao.findById(profile.getVirtualMachine().getDataCenterId());
        buf.append(" internaldns1=").append(dc.getInternalDns1());
        if (dc.getInternalDns2() != null) {
            buf.append(" internaldns2=").append(dc.getInternalDns2());
        }
        buf.append(" dns1=").append(dc.getDns1());
        if (dc.getDns2() != null) {
            buf.append(" dns2=").append(dc.getDns2());
        }

        final String bootArgs = buf.toString();
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Boot Args for " + profile + ": " + bootArgs);
        }

        return true;
    }

    @Override
    public boolean finalizeDeployment(final Commands cmds, final VirtualMachineProfile profile, final DeployDestination dest, final ReservationContext context) {

        finalizeCommandsOnStart(cmds, profile);

        final ConsoleProxyVO proxy = _consoleProxyDao.findById(profile.getId());
        final DataCenter dc = dest.getDataCenter();
        final List<NicProfile> nics = profile.getNics();
        for (final NicProfile nic : nics) {
            if ((nic.getTrafficType() == TrafficType.Public && dc.getNetworkType() == NetworkType.Advanced) ||
                    (nic.getTrafficType() == TrafficType.Guest && (dc.getNetworkType() == NetworkType.Basic || dc.isSecurityGroupEnabled()))) {
                proxy.setPublicIpAddress(nic.getIPv4Address());
                proxy.setPublicNetmask(nic.getIPv4Netmask());
                proxy.setPublicMacAddress(nic.getMacAddress());
            } else if (nic.getTrafficType() == TrafficType.Management) {
                proxy.setPrivateIpAddress(nic.getIPv4Address());
                proxy.setPrivateMacAddress(nic.getMacAddress());
            }
        }
        _consoleProxyDao.update(proxy.getId(), proxy);
        return true;
    }

    @Override
    public boolean finalizeStart(final VirtualMachineProfile profile, final long hostId, final Commands cmds, final ReservationContext context) {
        final CheckSshAnswer answer = (CheckSshAnswer) cmds.getAnswer("checkSsh");
        if (answer == null || !answer.getResult()) {
            if (answer != null) {
                s_logger.warn("Unable to ssh to the VM: " + answer.getDetails());
            } else {
                s_logger.warn("Unable to ssh to the VM: null answer");
            }
            return false;
        }

        try {
            //get system ip and create static nat rule for the vm in case of basic networking with EIP/ELB
            _rulesMgr.getSystemIpAndEnableStaticNatForVm(profile.getVirtualMachine(), false);
            final IPAddressVO ipaddr = _ipAddressDao.findByAssociatedVmId(profile.getVirtualMachine().getId());
            if (ipaddr != null && ipaddr.getSystem()) {
                final ConsoleProxyVO consoleVm = _consoleProxyDao.findById(profile.getId());
                // override CPVM guest IP with EIP, so that console url's will be prepared with EIP
                consoleVm.setPublicIpAddress(ipaddr.getAddress().addr());
                _consoleProxyDao.update(consoleVm.getId(), consoleVm);
            }
        } catch (final Exception ex) {
            s_logger.warn("Failed to get system ip and enable static nat for the vm " + profile.getVirtualMachine() + " due to exception ", ex);
            return false;
        }

        return true;
    }

    @Override
    public boolean finalizeCommandsOnStart(final Commands cmds, final VirtualMachineProfile profile) {

        NicProfile managementNic = null;
        NicProfile controlNic = null;
        for (final NicProfile nic : profile.getNics()) {
            if (nic.getTrafficType() == TrafficType.Management) {
                managementNic = nic;
            } else if (nic.getTrafficType() == TrafficType.Control && nic.getIPv4Address() != null) {
                controlNic = nic;
            }
        }

        if (controlNic == null) {
            if (managementNic == null) {
                s_logger.error("Management network doesn't exist for the console proxy vm " + profile.getVirtualMachine());
                return false;
            }
            controlNic = managementNic;
        }

        final CheckSshCommand check = new CheckSshCommand(profile.getInstanceName(), controlNic.getIPv4Address(), 3922);
        cmds.addCommand("checkSsh", check);

        return true;
    }

    @Override
    public void finalizeStop(final VirtualMachineProfile profile, final Answer answer) {
        //release elastic IP here if assigned
        final IPAddressVO ip = _ipAddressDao.findByAssociatedVmId(profile.getId());
        if (ip != null && ip.getSystem()) {
            final CallContext ctx = CallContext.current();
            try {
                _rulesMgr.disableStaticNat(ip.getId(), ctx.getCallingAccount(), ctx.getCallingUserId(), true);
            } catch (final Exception ex) {
                s_logger.warn("Failed to disable static nat and release system ip " + ip + " as a part of vm " + profile.getVirtualMachine() + " stop due to exception ",
                        ex);
            }
        }
    }

    @Override
    public void finalizeExpunge(final VirtualMachine vm) {
        final ConsoleProxyVO proxy = _consoleProxyDao.findById(vm.getId());
        proxy.setPublicIpAddress(null);
        proxy.setPublicMacAddress(null);
        proxy.setPublicNetmask(null);
        proxy.setPrivateMacAddress(null);
        proxy.setPrivateIpAddress(null);
        _consoleProxyDao.update(proxy.getId(), proxy);
    }

    @Override
    public void prepareStop(final VirtualMachineProfile profile) {
    }

    @Override
    public String getScanHandlerName() {
        return "consoleproxy";
    }

    @Override
    public boolean canScan() {
        // take the chance to do management-state management
        scanManagementState();

        if (!reserveStandbyCapacity()) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Reserving standby capacity is disabled, skip capacity scan");
            }
            return false;
        }

        final List<StoragePoolVO> upPools = _storagePoolDao.listByStatus(StoragePoolStatus.Up);
        if (upPools == null || upPools.size() == 0) {
            s_logger.debug("Skip capacity scan as there is no Primary Storage in 'Up' state");
            return false;
        }

        return true;
    }

    @Override
    public void onScanStart() {
        // to reduce possible number of DB queries for capacity scan, we run following aggregated queries in preparation
        // stage
        _zoneHostInfoMap = getZoneHostInfo();

        _zoneProxyCountMap = new HashMap<>();
        final List<ConsoleProxyLoadInfo> listProxyCounts = _consoleProxyDao.getDatacenterProxyLoadMatrix();
        for (final ConsoleProxyLoadInfo info : listProxyCounts) {
            _zoneProxyCountMap.put(info.getId(), info);
        }

        _zoneVmCountMap = new HashMap<>();
        final List<ConsoleProxyLoadInfo> listVmCounts = _consoleProxyDao.getDatacenterSessionLoadMatrix();
        for (final ConsoleProxyLoadInfo info : listVmCounts) {
            _zoneVmCountMap.put(info.getId(), info);
        }
    }

    private synchronized Map<Long, ZoneHostInfo> getZoneHostInfo() {
        final Date cutTime = DateUtil.currentGMTTime();
        final List<RunningHostCountInfo> l = _hostDao.getRunningHostCounts(new Date(cutTime.getTime() - ClusterManager.HeartbeatThreshold.value()));

        final RunningHostInfoAgregator aggregator = new RunningHostInfoAgregator();
        if (l.size() > 0) {
            for (final RunningHostCountInfo countInfo : l) {
                aggregator.aggregate(countInfo);
            }
        }

        return aggregator.getZoneHostInfoMap();
    }

    @Override
    public Long[] getScannablePools() {
        final List<DataCenterVO> zones = _dcDao.listEnabledZones();

        final Long[] dcIdList = new Long[zones.size()];
        int i = 0;
        for (final DataCenterVO dc : zones) {
            dcIdList[i++] = dc.getId();
        }

        return dcIdList;
    }

    @Override
    public boolean isPoolReadyForScan(final Long pool) {
        // pool is at zone basis
        final long dataCenterId = pool.longValue();

        if (!isZoneReady(_zoneHostInfoMap, dataCenterId)) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Zone " + dataCenterId + " is not ready to launch console proxy yet");
            }
            return false;
        }

        final List<ConsoleProxyVO> l = _consoleProxyDao.getProxyListInStates(VirtualMachine.State.Starting, VirtualMachine.State.Stopping);
        if (l.size() > 0) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Zone " + dataCenterId + " has " + l.size() + " console proxy VM(s) in transition state");
            }

            return false;
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Zone " + dataCenterId + " is ready to launch console proxy");
        }
        return true;
    }

    public boolean isZoneReady(final Map<Long, ZoneHostInfo> zoneHostInfoMap, final long dataCenterId) {
        final ZoneHostInfo zoneHostInfo = zoneHostInfoMap.get(dataCenterId);
        if (zoneHostInfo != null && isZoneHostReady(zoneHostInfo)) {
            final VMTemplateVO template = _templateDao.findSystemVMReadyTemplate(dataCenterId, HypervisorType.Any);
            if (template == null) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("System vm template is not ready at data center " + dataCenterId + ", wait until it is ready to launch console proxy vm");
                }
                return false;
            }
            final TemplateDataStoreVO templateHostRef = _vmTemplateStoreDao.findByTemplateZoneDownloadStatus(template.getId(), dataCenterId, Status.DOWNLOADED);

            if (templateHostRef != null) {
                boolean useLocalStorage = false;
                final Boolean useLocal = ConfigurationManagerImpl.SystemVMUseLocalStorage.valueIn(dataCenterId);
                if (useLocal != null) {
                    useLocalStorage = useLocal.booleanValue();
                }
                final List<Pair<Long, Integer>> l = _consoleProxyDao.getDatacenterStoragePoolHostInfo(dataCenterId, useLocalStorage);
                if (l != null && l.size() > 0 && l.get(0).second().intValue() > 0) {
                    return true;
                } else {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Primary storage is not ready, wait until it is ready to launch console proxy");
                    }
                }
            } else {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Zone host is ready, but console proxy template: " + template.getId() + " is not ready on secondary storage.");
                }
            }
        }
        return false;
    }

    private boolean isZoneHostReady(final ZoneHostInfo zoneHostInfo) {
        int expectedFlags = 0;
        if (_useStorageVm) {
            expectedFlags = RunningHostInfoAgregator.ZoneHostInfo.ROUTING_HOST_MASK;
        } else {
            expectedFlags = RunningHostInfoAgregator.ZoneHostInfo.ALL_HOST_MASK;
        }

        return (zoneHostInfo.getFlags() & expectedFlags) == expectedFlags;
    }

    @Override
    public Pair<AfterScanAction, Object> scanPool(final Long pool) {
        final long dataCenterId = pool.longValue();

        final ConsoleProxyLoadInfo proxyInfo = _zoneProxyCountMap.get(dataCenterId);
        if (proxyInfo == null) {
            return new Pair<>(AfterScanAction.nop, null);
        }

        ConsoleProxyLoadInfo vmInfo = _zoneVmCountMap.get(dataCenterId);
        if (vmInfo == null) {
            vmInfo = new ConsoleProxyLoadInfo();
        }

        if (!checkCapacity(proxyInfo, vmInfo)) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Expand console proxy standby capacity for zone " + proxyInfo.getName());
            }

            return new Pair<>(AfterScanAction.expand, null);
        }

        return new Pair<>(AfterScanAction.nop, null);
    }

    private boolean checkCapacity(final ConsoleProxyLoadInfo proxyCountInfo, final ConsoleProxyLoadInfo vmCountInfo) {

        if (proxyCountInfo.getCount() * _capacityPerProxy - vmCountInfo.getCount() <= _standbyCapacity) {
            return false;
        }

        return true;
    }

    @Override
    public void expandPool(final Long pool, final Object actionArgs) {
        final long dataCenterId = pool.longValue();
        allocCapacity(dataCenterId);
    }

    private void allocCapacity(final long dataCenterId) {
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Allocate console proxy standby capacity for data center : " + dataCenterId);
        }

        ConsoleProxyVO proxy = null;
        String errorString = null;
        try {
            boolean consoleProxyVmFromStoppedPool = false;
            proxy = assignProxyFromStoppedPool(dataCenterId);
            if (proxy == null) {
                if (s_logger.isInfoEnabled()) {
                    s_logger.info("No stopped console proxy is available, need to allocate a new console proxy");
                }

                if (_allocProxyLock.lock(ACQUIRE_GLOBAL_LOCK_TIMEOUT_FOR_SYNC)) {
                    try {
                        proxy = startNew(dataCenterId);
                    } catch (final ConcurrentOperationException e) {
                        s_logger.info("Concurrent operation exception caught " + e);
                    } finally {
                        _allocProxyLock.unlock();
                    }
                } else {
                    if (s_logger.isInfoEnabled()) {
                        s_logger.info("Unable to acquire synchronization lock for console proxy vm allocation, wait for next scan");
                    }
                }
            } else {
                if (s_logger.isInfoEnabled()) {
                    s_logger.info("Found a stopped console proxy, starting it. Vm id : " + proxy.getId());
                }
                consoleProxyVmFromStoppedPool = true;
            }

            if (proxy != null) {
                final long proxyVmId = proxy.getId();
                proxy = startProxy(proxyVmId, false);

                if (proxy != null) {
                    if (s_logger.isInfoEnabled()) {
                        s_logger.info("Console proxy " + proxy.getHostName() + " is started");
                    }
                    SubscriptionMgr.getInstance().notifySubscribers(ConsoleProxyManager.ALERT_SUBJECT, this,
                            new ConsoleProxyAlertEventArgs(ConsoleProxyAlertEventArgs.PROXY_UP, dataCenterId, proxy.getId(), proxy, null));
                } else {
                    if (s_logger.isInfoEnabled()) {
                        s_logger.info("Unable to start console proxy vm for standby capacity, vm id : " + proxyVmId + ", will recycle it and start a new one");
                    }

                    if (consoleProxyVmFromStoppedPool) {
                        destroyProxy(proxyVmId);
                    }
                }
            }
        } catch (final Exception e) {
            errorString = e.getMessage();
            throw e;
        } finally {
            // TODO - For now put all the alerts as creation failure. Distinguish between creation vs start failure in future.
            // Also add failure reason since startvm masks some of them.
            if (proxy == null || proxy.getState() != State.Running) {
                SubscriptionMgr.getInstance().notifySubscribers(ConsoleProxyManager.ALERT_SUBJECT, this,
                        new ConsoleProxyAlertEventArgs(ConsoleProxyAlertEventArgs.PROXY_CREATE_FAILURE, dataCenterId, 0l, null, errorString));
            }
        }
    }

    public ConsoleProxyVO assignProxyFromStoppedPool(final long dataCenterId) {

        // practically treat all console proxy VM that is not in Running state but can be entering into Running state as
        // candidates
        // this is to prevent launching unneccessary console proxy VMs because of temporarily unavailable state
        final List<ConsoleProxyVO> l = _consoleProxyDao.getProxyListInStates(dataCenterId, State.Starting, State.Stopped, State.Migrating, State.Stopping);
        if (l != null && l.size() > 0) {
            return l.get(0);
        }

        return null;
    }

    public ConsoleProxyVO startNew(final long dataCenterId) throws ConcurrentOperationException {

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Assign console proxy from a newly started instance for request from data center : " + dataCenterId);
        }

        if (!allowToLaunchNew(dataCenterId)) {
            s_logger.warn("The number of launched console proxy on zone " + dataCenterId + " has reached to limit");
            return null;
        }

        VMTemplateVO template = null;
        final HypervisorType availableHypervisor = _resourceMgr.getAvailableHypervisor(dataCenterId);
        template = _templateDao.findSystemVMReadyTemplate(dataCenterId, availableHypervisor);
        if (template == null) {
            throw new CloudRuntimeException("Not able to find the System templates or not downloaded in zone " + dataCenterId);
        }

        final Map<String, Object> context = createProxyInstance(dataCenterId, template);

        final long proxyVmId = (Long) context.get("proxyVmId");
        if (proxyVmId == 0) {
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Creating proxy instance failed, data center id : " + dataCenterId);
            }
            return null;
        }

        final ConsoleProxyVO proxy = _consoleProxyDao.findById(proxyVmId);
        if (proxy != null) {
            SubscriptionMgr.getInstance().notifySubscribers(ConsoleProxyManager.ALERT_SUBJECT, this,
                    new ConsoleProxyAlertEventArgs(ConsoleProxyAlertEventArgs.PROXY_CREATED, dataCenterId, proxy.getId(), proxy, null));
            return proxy;
        } else {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Unable to allocate console proxy storage, remove the console proxy record from DB, proxy id: " + proxyVmId);
            }
        }
        return null;
    }

    private boolean allowToLaunchNew(final long dcId) {
        if (!isConsoleProxyVmRequired(dcId)) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Console proxy vm not required in zone " + dcId + " not launching");
            }
            return false;
        }
        final List<ConsoleProxyVO> l =
                _consoleProxyDao.getProxyListInStates(dcId, VirtualMachine.State.Starting, VirtualMachine.State.Running, VirtualMachine.State.Stopping,
                        VirtualMachine.State.Stopped, VirtualMachine.State.Migrating, VirtualMachine.State.Shutdowned, VirtualMachine.State.Unknown);

        final String value = _configDao.getValue(Config.ConsoleProxyLaunchMax.key());
        final int launchLimit = NumbersUtil.parseInt(value, 10);
        return l.size() < launchLimit;
    }

    protected Map<String, Object> createProxyInstance(final long dataCenterId, final VMTemplateVO template) throws ConcurrentOperationException {

        final long id = _consoleProxyDao.getNextInSequence(Long.class, "id");
        final String name = VirtualMachineName.getConsoleProxyName(id, _instance);
        final DataCenterVO dc = _dcDao.findById(dataCenterId);
        final Account systemAcct = _accountMgr.getSystemAccount();

        final DataCenterDeployment plan = new DataCenterDeployment(dataCenterId);

        final NetworkVO defaultNetwork = getDefaultNetworkForCreation(dc);

        final List<? extends NetworkOffering> offerings =
                _networkModel.getSystemAccountNetworkOfferings(NetworkOffering.SystemControlNetwork, NetworkOffering.SystemManagementNetwork);
        final LinkedHashMap<Network, List<? extends NicProfile>> networks = new LinkedHashMap<>(offerings.size() + 1);
        final NicProfile defaultNic = new NicProfile();
        defaultNic.setDefaultNic(true);
        defaultNic.setDeviceId(2);

        networks.put(_networkMgr.setupNetwork(systemAcct, _networkOfferingDao.findById(defaultNetwork.getNetworkOfferingId()), plan, null, null, false).get(0),
                new ArrayList<>(Arrays.asList(defaultNic)));

        for (final NetworkOffering offering : offerings) {
            networks.put(_networkMgr.setupNetwork(systemAcct, offering, plan, null, null, false).get(0), new ArrayList<>());
        }

        ServiceOfferingVO serviceOffering = _serviceOffering;
        if (serviceOffering == null) {
            serviceOffering = _offeringDao.findDefaultSystemOffering(ServiceOffering.consoleProxyDefaultOffUniqueName, ConfigurationManagerImpl.SystemVMUseLocalStorage.valueIn
                    (dataCenterId));
        }
        ConsoleProxyVO proxy =
                new ConsoleProxyVO(id, serviceOffering.getId(), name, template.getId(), template.getHypervisorType(), template.getGuestOSId(), dataCenterId,
                        systemAcct.getDomainId(), systemAcct.getId(), _accountMgr.getSystemUser().getId(), 0, serviceOffering.getOfferHA());
        proxy.setDynamicallyScalable(template.isDynamicallyScalable());
        proxy = _consoleProxyDao.persist(proxy);
        try {
            _itMgr.allocate(name, template, serviceOffering, networks, plan, null);
        } catch (final InsufficientCapacityException e) {
            s_logger.warn("InsufficientCapacity", e);
            throw new CloudRuntimeException("Insufficient capacity exception", e);
        }

        final Map<String, Object> context = new HashMap<>();
        context.put("dc", dc);
        final HostPodVO pod = _podDao.findById(proxy.getPodIdToDeployIn());
        context.put("pod", pod);
        context.put("proxyVmId", proxy.getId());

        return context;
    }

    private boolean isConsoleProxyVmRequired(final long dcId) {
        final DataCenterVO dc = _dcDao.findById(dcId);
        _dcDao.loadDetails(dc);
        final String cpvmReq = dc.getDetail(ZoneConfig.EnableConsoleProxyVm.key());
        if (cpvmReq != null) {
            return Boolean.parseBoolean(cpvmReq);
        }
        return true;
    }

    /**
     * Get the default network for the console proxy VM, based on the zone it is in. Delegates to
     * either {@link #getDefaultNetworkForZone(DataCenter)} or {@link #getDefaultNetworkForAdvancedSGZone(DataCenter)},
     * depending on the zone network type and whether or not security groups are enabled in the zone.
     *
     * @param dc - The zone (DataCenter) of the console proxy VM.
     * @return The default network for use with the console proxy VM.
     */
    protected NetworkVO getDefaultNetworkForCreation(final DataCenter dc) {
        if (dc.getNetworkType() == NetworkType.Advanced) {
            return getDefaultNetworkForAdvancedZone(dc);
        } else {
            return getDefaultNetworkForBasicZone(dc);
        }
    }

    /**
     * Get default network for a console proxy VM starting up in an advanced zone. If the zone
     * is security group-enabled, the first network found that supports SG services is returned.
     * If the zone is not SG-enabled, the Public network is returned.
     *
     * @param dc - The zone.
     * @return The selected default network.
     * @throws CloudRuntimeException - If the zone is not a valid choice or a network couldn't be found.
     */
    protected NetworkVO getDefaultNetworkForAdvancedZone(final DataCenter dc) {
        if (dc.getNetworkType() != NetworkType.Advanced) {
            throw new CloudRuntimeException("Zone " + dc + " is not advanced.");
        }

        if (dc.isSecurityGroupEnabled()) {
            final List<NetworkVO> networks = _networkDao.listByZoneSecurityGroup(dc.getId());
            if (CollectionUtils.isEmpty(networks)) {
                throw new CloudRuntimeException("Can not found security enabled network in SG Zone " + dc);
            }

            return networks.get(0);
        } else {
            final TrafficType defaultTrafficType = TrafficType.Public;
            final List<NetworkVO> defaultNetworks = _networkDao.listByZoneAndTrafficType(dc.getId(), defaultTrafficType);

            // api should never allow this situation to happen
            if (defaultNetworks.size() != 1) {
                throw new CloudRuntimeException("Found " + defaultNetworks.size() + " networks of type " + defaultTrafficType + " when expect to find 1");
            }

            return defaultNetworks.get(0);
        }
    }

    /**
     * Get default network for console proxy VM for starting up in a basic zone. Basic zones select
     * the Guest network whether or not the zone is SG-enabled.
     *
     * @param dc - The zone.
     * @return The default network according to the zone's network selection rules.
     * @throws CloudRuntimeException - If the zone is not a valid choice or a network couldn't be found.
     */
    protected NetworkVO getDefaultNetworkForBasicZone(final DataCenter dc) {
        if (dc.getNetworkType() != NetworkType.Basic) {
            throw new CloudRuntimeException("Zone " + dc + "is not basic.");
        }

        final TrafficType defaultTrafficType = TrafficType.Guest;
        final List<NetworkVO> defaultNetworks = _networkDao.listByZoneAndTrafficType(dc.getId(), defaultTrafficType);

        // api should never allow this situation to happen
        if (defaultNetworks.size() != 1) {
            throw new CloudRuntimeException("Found " + defaultNetworks.size() + " networks of type " + defaultTrafficType + " when expect to find 1");
        }

        return defaultNetworks.get(0);
    }

    @Override
    public void shrinkPool(final Long pool, final Object actionArgs) {
    }

    @Override
    public void onScanEnd() {
    }

    private void scanManagementState() {
        final ConsoleProxyManagementState state = getManagementState();
        if (state != null) {
            switch (state) {
                case Auto:
                case Manual:
                case Suspending:
                    break;

                case ResetSuspending:
                    handleResetSuspending();
                    break;

                default:
                    assert (false);
            }
        }
    }

    private boolean reserveStandbyCapacity() {
        final ConsoleProxyManagementState state = getManagementState();
        if (state == null || state != ConsoleProxyManagementState.Auto) {
            return false;
        }

        return true;
    }

    @Override
    public ConsoleProxyManagementState getManagementState() {
        final String value = _configDao.getValue(Config.ConsoleProxyManagementState.key());
        if (value != null) {
            final ConsoleProxyManagementState state = ConsoleProxyManagementState.valueOf(value);

            if (state == null) {
                s_logger.error("Invalid console proxy management state: " + value);
            }
            return state;
        }

        s_logger.error("Invalid console proxy management state: " + value);
        return null;
    }

    @Override
    @DB
    public void setManagementState(final ConsoleProxyManagementState state) {
        try {
            final ConsoleProxyManagementState lastState = getManagementState();
            if (lastState == null) {
                return;
            }

            if (lastState != state) {
                Transaction.execute(new TransactionCallbackNoReturn() {
                    @Override
                    public void doInTransactionWithoutResult(final TransactionStatus status) {
                        _configDao.update(Config.ConsoleProxyManagementLastState.key(), Config.ConsoleProxyManagementLastState.getCategory(), lastState.toString());
                        _configDao.update(Config.ConsoleProxyManagementState.key(), Config.ConsoleProxyManagementState.getCategory(), state.toString());
                    }
                });
            }
        } catch (final Throwable e) {
            s_logger.error("Failed to set managment state", e);
        }
    }

    @Override
    @DB
    public void resumeLastManagementState() {
        try {
            final ConsoleProxyManagementState state = getManagementState();
            final ConsoleProxyManagementState lastState = getLastManagementState();
            if (lastState == null) {
                return;
            }

            if (lastState != state) {
                _configDao.update(Config.ConsoleProxyManagementState.key(), Config.ConsoleProxyManagementState.getCategory(), lastState.toString());
            }
        } catch (final Throwable e) {
            s_logger.error("Failed to resume last management state", e);
        }
    }

    @Override
    public ConsoleProxyVO startProxy(final long proxyVmId, final boolean ignoreRestartSetting) {
        try {
            ConsoleProxyVO proxy = _consoleProxyDao.findById(proxyVmId);
            if (proxy.getState() == VirtualMachine.State.Running) {
                return proxy;
            }

            final String restart = _configDao.getValue(Config.ConsoleProxyRestart.key());
            if (!ignoreRestartSetting && restart != null && restart.equalsIgnoreCase("false")) {
                return null;
            }

            if (proxy.getState() == VirtualMachine.State.Stopped) {
                _itMgr.advanceStart(proxy.getUuid(), null, null);
                proxy = _consoleProxyDao.findById(proxy.getId());
                return proxy;
            }

            // For VMs that are in Stopping, Starting, Migrating state, let client to wait by returning null
            // as sooner or later, Starting/Migrating state will be transited to Running and Stopping will be transited
            // to Stopped to allow Starting of it
            s_logger.warn("Console proxy is not in correct state to be started: " + proxy.getState());
            return null;
        } catch (final StorageUnavailableException e) {
            s_logger.warn("Exception while trying to start console proxy", e);
            return null;
        } catch (final InsufficientCapacityException e) {
            s_logger.warn("Exception while trying to start console proxy", e);
            return null;
        } catch (final ResourceUnavailableException e) {
            s_logger.warn("Exception while trying to start console proxy", e);
            return null;
        } catch (final ConcurrentOperationException e) {
            s_logger.warn("Runtime Exception while trying to start console proxy", e);
            return null;
        } catch (final CloudRuntimeException e) {
            s_logger.warn("Runtime Exception while trying to start console proxy", e);
            return null;
        } catch (final OperationTimedoutException e) {
            s_logger.warn("Runtime Exception while trying to start console proxy", e);
            return null;
        }
    }

    @Override
    public boolean stopProxy(final long proxyVmId) {
        final ConsoleProxyVO proxy = _consoleProxyDao.findById(proxyVmId);
        if (proxy == null) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Stopping console proxy failed: console proxy " + proxyVmId + " no longer exists");
            }
            return false;
        }

        try {
            _itMgr.stop(proxy.getUuid());
            return true;
        } catch (final ResourceUnavailableException e) {
            s_logger.warn("Stopping console proxy " + proxy.getHostName() + " failed : exception ", e);
            return false;
        } catch (final CloudRuntimeException e) {
            s_logger.warn("Unable to stop proxy ", e);
            return false;
        }
    }

    @Override
    public boolean rebootProxy(final long proxyVmId) {
        final ConsoleProxyVO proxy = _consoleProxyDao.findById(proxyVmId);

        if (proxy == null || proxy.getState() == State.Destroyed) {
            return false;
        }

        if (proxy.getState() == State.Running && proxy.getHostId() != null) {
            final RebootCommand cmd = new RebootCommand(proxy.getInstanceName());
            final Answer answer = _agentMgr.easySend(proxy.getHostId(), cmd);

            if (answer != null && answer.getResult()) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Successfully reboot console proxy " + proxy.getHostName());
                }

                SubscriptionMgr.getInstance().notifySubscribers(ConsoleProxyManager.ALERT_SUBJECT, this,
                        new ConsoleProxyAlertEventArgs(ConsoleProxyAlertEventArgs.PROXY_REBOOTED, proxy.getDataCenterId(), proxy.getId(), proxy, null));

                return true;
            } else {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("failed to reboot console proxy : " + proxy.getHostName());
                }

                return false;
            }
        } else {
            return startProxy(proxyVmId, false) != null;
        }
    }

    @Override
    public boolean destroyProxy(final long vmId) {
        final ConsoleProxyVO proxy = _consoleProxyDao.findById(vmId);
        try {
            //expunge the vm
            _itMgr.expunge(proxy.getUuid());
            proxy.setPublicIpAddress(null);
            proxy.setPublicMacAddress(null);
            proxy.setPublicNetmask(null);
            proxy.setPrivateMacAddress(null);
            proxy.setPrivateIpAddress(null);
            _consoleProxyDao.update(proxy.getId(), proxy);
            _consoleProxyDao.remove(vmId);
            final HostVO host = _hostDao.findByTypeNameAndZoneId(proxy.getDataCenterId(), proxy.getHostName(), Host.Type.ConsoleProxy);
            if (host != null) {
                s_logger.debug("Removing host entry for proxy id=" + vmId);
                return _hostDao.remove(host.getId());
            }

            return true;
        } catch (final ResourceUnavailableException e) {
            s_logger.warn("Unable to expunge " + proxy, e);
            return false;
        }
    }

    private void handleResetSuspending() {
        final List<ConsoleProxyVO> runningProxies = _consoleProxyDao.getProxyListInStates(State.Running);
        for (final ConsoleProxyVO proxy : runningProxies) {
            s_logger.info("Stop console proxy " + proxy.getId() + " because of we are currently in ResetSuspending management mode");
            stopProxy(proxy.getId());
        }

        // check if it is time to resume
        final List<ConsoleProxyVO> proxiesInTransition = _consoleProxyDao.getProxyListInStates(State.Running, State.Starting, State.Stopping);
        if (proxiesInTransition.size() == 0) {
            s_logger.info("All previous console proxy VMs in transition mode ceased the mode, we will now resume to last management state");
            resumeLastManagementState();
        }
    }

    private ConsoleProxyManagementState getLastManagementState() {
        final String value = _configDao.getValue(Config.ConsoleProxyManagementLastState.key());
        if (value != null) {
            final ConsoleProxyManagementState state = ConsoleProxyManagementState.valueOf(value);

            if (state == null) {
                s_logger.error("Invalid console proxy management state: " + value);
            }
            return state;
        }

        s_logger.error("Invalid console proxy management state: " + value);
        return null;
    }

    @Override
    public HostVO createHostVOForConnectedAgent(final HostVO host, final StartupCommand[] cmd) {
        if (!(cmd[0] instanceof StartupProxyCommand)) {
            return null;
        }

        host.setType(com.cloud.host.Host.Type.ConsoleProxy);
        return host;
    }

    @Override
    public HostVO createHostVOForDirectConnectAgent(final HostVO host, final StartupCommand[] startup, final ServerResource resource, final Map<String, String> details, final
    List<String> hostTags) {
        return null;
    }

    @Override
    public DeleteHostAnswer deleteHost(final HostVO host, final boolean isForced, final boolean isForceDeleteStorage) throws UnableDeleteHostException {
        return null;
    }

    protected HostVO findConsoleProxyHostByName(final String name) {
        final QueryBuilder<HostVO> sc = QueryBuilder.create(HostVO.class);
        sc.and(sc.entity().getType(), Op.EQ, Host.Type.ConsoleProxy);
        sc.and(sc.entity().getName(), Op.EQ, name);
        return sc.find();
    }

    public List<ConsoleProxyAllocator> getConsoleProxyAllocators() {
        return _consoleProxyAllocators;
    }

    @Inject
    public void setConsoleProxyAllocators(final List<ConsoleProxyAllocator> consoleProxyAllocators) {
        _consoleProxyAllocators = consoleProxyAllocators;
    }

    public class VmBasedAgentHook extends AgentHookBase {

        public VmBasedAgentHook(final VMInstanceDao instanceDao, final HostDao hostDao, final ConfigurationDao cfgDao, final KeystoreManager ksMgr, final AgentManager agentMgr,
                                final KeysManager keysMgr) {
            super(instanceDao, hostDao, cfgDao, ksMgr, agentMgr, keysMgr);
        }

        @Override
        public void onLoadReport(final ConsoleProxyLoadReportCommand cmd) {
            if (cmd.getLoadInfo() == null) {
                return;
            }

            ConsoleProxyStatus status = null;
            try {
                final GsonBuilder gb = new GsonBuilder();
                gb.setVersion(1.3);
                final Gson gson = gb.create();
                status = gson.fromJson(cmd.getLoadInfo(), ConsoleProxyStatus.class);
            } catch (final Throwable e) {
                s_logger.warn("Unable to parse load info from proxy, proxy vm id : " + cmd.getProxyVmId() + ", info : " + cmd.getLoadInfo());
            }

            if (status != null) {
                int count = 0;
                if (status.getConnections() != null) {
                    count = status.getConnections().length;
                }

                byte[] details = null;
                if (cmd.getLoadInfo() != null) {
                    details = cmd.getLoadInfo().getBytes(Charset.forName("US-ASCII"));
                }
                _consoleProxyDao.update(cmd.getProxyVmId(), count, DateUtil.currentGMTTime(), details);
            } else {
                if (s_logger.isTraceEnabled()) {
                    s_logger.trace("Unable to get console proxy load info, id : " + cmd.getProxyVmId());
                }

                _consoleProxyDao.update(cmd.getProxyVmId(), 0, DateUtil.currentGMTTime(), null);
            }
        }

        @Override
        public void onAgentDisconnect(final long agentId, final com.cloud.host.Status state) {

            if (state == com.cloud.host.Status.Alert || state == com.cloud.host.Status.Disconnected) {
                // be it either in alert or in disconnected state, the agent
                // process
                // may be gone in the VM,
                // we will be reacting to stop the corresponding VM and let the
                // scan
                // process to
                final HostVO host = _hostDao.findById(agentId);
                if (host.getType() == Type.ConsoleProxy) {
                    final String name = host.getName();
                    if (s_logger.isInfoEnabled()) {
                        s_logger.info("Console proxy agent disconnected, proxy: " + name);
                    }
                    if (name != null && name.startsWith("v-")) {
                        final String[] tokens = name.split("-");
                        long proxyVmId = 0;
                        try {
                            proxyVmId = Long.parseLong(tokens[1]);
                        } catch (final NumberFormatException e) {
                            s_logger.error("Unexpected exception " + e.getMessage(), e);
                            return;
                        }

                        final ConsoleProxyVO proxy = _consoleProxyDao.findById(proxyVmId);
                        if (proxy != null) {

                            // Disable this feature for now, as it conflicts
                            // with
                            // the case of allowing user to reboot console proxy
                            // when rebooting happens, we will receive
                            // disconnect
                            // here and we can't enter into stopping process,
                            // as when the rebooted one comes up, it will kick
                            // off a
                            // newly started one and trigger the process
                            // continue on forever

                            /*
                             * _capacityScanScheduler.execute(new Runnable() {
                             * public void run() { if(s_logger.isInfoEnabled())
                             * s_logger.info("Stop console proxy " +
                             * proxy.getName() +
                             * " VM because of that the agent running inside it has disconnected"
                             * ); stopProxy(proxy.getId()); } });
                             */
                        } else {
                            if (s_logger.isInfoEnabled()) {
                                s_logger.info("Console proxy agent disconnected but corresponding console proxy VM no longer exists in DB, proxy: " + name);
                            }
                        }
                    } else {
                        assert (false) : "Invalid console proxy name: " + name;
                    }
                }
            }
        }

        @Override
        protected HostVO findConsoleProxyHost(final StartupProxyCommand startupCmd) {
            final long proxyVmId = startupCmd.getProxyVmId();
            final ConsoleProxyVO consoleProxy = _consoleProxyDao.findById(proxyVmId);
            if (consoleProxy == null) {
                s_logger.info("Proxy " + proxyVmId + " is no longer in DB, skip sending startup command");
                return null;
            }

            assert (consoleProxy != null);
            return findConsoleProxyHostByName(consoleProxy.getHostName());
        }
    }
}
