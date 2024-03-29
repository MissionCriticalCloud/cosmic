package com.cloud.vm;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.affinity.AffinityGroupService;
import com.cloud.affinity.AffinityGroupVO;
import com.cloud.affinity.dao.AffinityGroupDao;
import com.cloud.affinity.dao.AffinityGroupVMMapDao;
import com.cloud.agent.AgentManager;
import com.cloud.agent.manager.Commands;
import com.cloud.alert.AlertManager;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiDBUtils;
import com.cloud.api.BaseCmd.HTTPMethod;
import com.cloud.api.command.admin.vm.AssignVMCmd;
import com.cloud.api.command.admin.vm.RecoverVMCmd;
import com.cloud.api.command.user.vm.AddNicToVMCmd;
import com.cloud.api.command.user.vm.DeployVMCmd;
import com.cloud.api.command.user.vm.DestroyVMCmd;
import com.cloud.api.command.user.vm.RebootVMCmd;
import com.cloud.api.command.user.vm.RemoveNicFromVMCmd;
import com.cloud.api.command.user.vm.ResetVMPasswordCmd;
import com.cloud.api.command.user.vm.ResetVMSSHKeyCmd;
import com.cloud.api.command.user.vm.RestoreVMCmd;
import com.cloud.api.command.user.vm.ScaleVMCmd;
import com.cloud.api.command.user.vm.StartVMCmd;
import com.cloud.api.command.user.vm.UpdateDefaultNicForVMCmd;
import com.cloud.api.command.user.vm.UpdateVMCmd;
import com.cloud.api.command.user.vm.UpdateVmNicIpCmd;
import com.cloud.api.command.user.vm.UpgradeVMCmd;
import com.cloud.api.command.user.vmgroup.CreateVMGroupCmd;
import com.cloud.api.command.user.vmgroup.DeleteVMGroupCmd;
import com.cloud.capacity.Capacity;
import com.cloud.capacity.CapacityManager;
import com.cloud.common.managed.context.ManagedContextRunnable;
import com.cloud.configuration.Config;
import com.cloud.configuration.ConfigurationManager;
import com.cloud.context.CallContext;
import com.cloud.dao.EntityManager;
import com.cloud.dao.UUIDManager;
import com.cloud.db.model.Zone;
import com.cloud.db.repository.ZoneRepository;
import com.cloud.dc.DedicatedResourceVO;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.dc.dao.DedicatedResourceDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.deploy.DataCenterDeployment;
import com.cloud.deploy.DeployDestination;
import com.cloud.deploy.DeploymentPlanner;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.deploy.DeploymentPlanningManager;
import com.cloud.deploy.PlannerHostReservationVO;
import com.cloud.deploy.dao.PlannerHostReservationDao;
import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.engine.cloud.entity.api.VirtualMachineEntity;
import com.cloud.engine.cloud.entity.api.db.dao.VMNetworkMapDao;
import com.cloud.engine.orchestration.service.NetworkOrchestrationService;
import com.cloud.engine.orchestration.service.VolumeOrchestrationService;
import com.cloud.engine.service.api.OrchestrationService;
import com.cloud.engine.subsystem.api.storage.DataStore;
import com.cloud.engine.subsystem.api.storage.DataStoreManager;
import com.cloud.engine.subsystem.api.storage.VolumeDataFactory;
import com.cloud.engine.subsystem.api.storage.VolumeInfo;
import com.cloud.event.ActionEvent;
import com.cloud.event.ActionEventUtils;
import com.cloud.event.EventTypes;
import com.cloud.framework.config.ConfigKey;
import com.cloud.framework.config.Configurable;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.gpu.GPU;
import com.cloud.ha.HighAvailabilityManager;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.host.dao.HostTagsDao;
import com.cloud.hypervisor.HypervisorCapabilitiesVO;
import com.cloud.hypervisor.dao.HypervisorCapabilitiesDao;
import com.cloud.legacymodel.acl.ControlledEntity.ACLType;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.GetVmDiskStatsAnswer;
import com.cloud.legacymodel.communication.answer.GetVmStatsAnswer;
import com.cloud.legacymodel.communication.answer.RestoreVMSnapshotAnswer;
import com.cloud.legacymodel.communication.answer.StartAnswer;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.communication.command.DettachCommand;
import com.cloud.legacymodel.communication.command.GetVmDiskStatsCommand;
import com.cloud.legacymodel.communication.command.GetVmIpAddressCommand;
import com.cloud.legacymodel.communication.command.GetVmStatsCommand;
import com.cloud.legacymodel.communication.command.PvlanSetupCommand;
import com.cloud.legacymodel.communication.command.RestoreVMSnapshotCommand;
import com.cloud.legacymodel.configuration.Resource.ResourceType;
import com.cloud.legacymodel.dc.Cluster;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.dc.HostStatus;
import com.cloud.legacymodel.domain.Domain;
import com.cloud.legacymodel.exceptions.AgentUnavailableException;
import com.cloud.legacymodel.exceptions.CloudException;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.legacymodel.exceptions.ExecutionException;
import com.cloud.legacymodel.exceptions.InsufficientAddressCapacityException;
import com.cloud.legacymodel.exceptions.InsufficientCapacityException;
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.legacymodel.exceptions.ManagementServerException;
import com.cloud.legacymodel.exceptions.NoTransitionException;
import com.cloud.legacymodel.exceptions.OperationTimedoutException;
import com.cloud.legacymodel.exceptions.PermissionDeniedException;
import com.cloud.legacymodel.exceptions.ResourceAllocationException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.exceptions.StorageUnavailableException;
import com.cloud.legacymodel.exceptions.VirtualMachineMigrationException;
import com.cloud.legacymodel.network.Network;
import com.cloud.legacymodel.network.Network.IpAddresses;
import com.cloud.legacymodel.network.Network.Service;
import com.cloud.legacymodel.network.Nic;
import com.cloud.legacymodel.resource.ResourceState;
import com.cloud.legacymodel.storage.StoragePool;
import com.cloud.legacymodel.storage.TemplateType;
import com.cloud.legacymodel.storage.VirtualMachineTemplate;
import com.cloud.legacymodel.storage.VmDiskStatsEntry;
import com.cloud.legacymodel.storage.Volume;
import com.cloud.legacymodel.to.DiskTO;
import com.cloud.legacymodel.to.NicTO;
import com.cloud.legacymodel.to.VirtualMachineTO;
import com.cloud.legacymodel.user.Account;
import com.cloud.legacymodel.user.SSHKeyPair;
import com.cloud.legacymodel.user.User;
import com.cloud.legacymodel.utils.Pair;
import com.cloud.legacymodel.vm.VirtualMachine;
import com.cloud.legacymodel.vm.VirtualMachine.State;
import com.cloud.legacymodel.vm.VmStatsEntry;
import com.cloud.model.enumeration.AllocationState;
import com.cloud.model.enumeration.ComplianceStatus;
import com.cloud.model.enumeration.DataStoreRole;
import com.cloud.model.enumeration.DiskControllerType;
import com.cloud.model.enumeration.GuestType;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.model.enumeration.ImageFormat;
import com.cloud.model.enumeration.MaintenancePolicy;
import com.cloud.model.enumeration.NetworkType;
import com.cloud.model.enumeration.OptimiseFor;
import com.cloud.model.enumeration.StoragePoolStatus;
import com.cloud.model.enumeration.TrafficType;
import com.cloud.model.enumeration.VirtualMachineType;
import com.cloud.model.enumeration.VolumeType;
import com.cloud.network.IpAddressManager;
import com.cloud.network.NetworkModel;
import com.cloud.network.PhysicalNetwork;
import com.cloud.network.dao.FirewallRulesDao;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.IPAddressVO;
import com.cloud.network.dao.LoadBalancerVMMapDao;
import com.cloud.network.dao.LoadBalancerVMMapVO;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkServiceMapDao;
import com.cloud.network.dao.NetworkVO;
import com.cloud.network.dao.PhysicalNetworkDao;
import com.cloud.network.element.UserDataServiceProvider;
import com.cloud.network.guru.NetworkGuru;
import com.cloud.network.lb.LoadBalancingRulesManager;
import com.cloud.network.router.VpcVirtualNetworkApplianceManager;
import com.cloud.network.rules.FirewallManager;
import com.cloud.network.rules.FirewallRuleVO;
import com.cloud.network.rules.PortForwardingRuleVO;
import com.cloud.network.rules.RulesManager;
import com.cloud.network.rules.dao.PortForwardingRulesDao;
import com.cloud.network.vpc.VpcManager;
import com.cloud.network.vpc.dao.VpcDao;
import com.cloud.offering.NetworkOffering;
import com.cloud.offering.NetworkOffering.Availability;
import com.cloud.offering.ServiceOffering;
import com.cloud.offerings.NetworkOfferingVO;
import com.cloud.offerings.dao.NetworkOfferingDao;
import com.cloud.projects.ProjectManager;
import com.cloud.resource.ResourceManager;
import com.cloud.server.ManagementServer;
import com.cloud.server.ManagementService;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.service.dao.ServiceOfferingDetailsDao;
import com.cloud.storage.DiskOfferingVO;
import com.cloud.storage.GuestOSCategoryVO;
import com.cloud.storage.GuestOSVO;
import com.cloud.storage.ScopeType;
import com.cloud.storage.SnapshotVO;
import com.cloud.storage.StorageManager;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.VMTemplateZoneVO;
import com.cloud.storage.VolumeApiService;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.DiskOfferingDao;
import com.cloud.storage.dao.GuestOSCategoryDao;
import com.cloud.storage.dao.GuestOSDao;
import com.cloud.storage.dao.SnapshotDao;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.dao.VMTemplateZoneDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.storage.datastore.db.PrimaryDataStoreDao;
import com.cloud.storage.datastore.db.StoragePoolVO;
import com.cloud.storage.datastore.db.TemplateDataStoreDao;
import com.cloud.storage.datastore.db.TemplateDataStoreVO;
import com.cloud.template.TemplateManager;
import com.cloud.user.AccountManager;
import com.cloud.user.AccountService;
import com.cloud.user.ResourceLimitService;
import com.cloud.user.SSHKeyPairVO;
import com.cloud.user.UserVO;
import com.cloud.user.VmDiskStatisticsVO;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.SSHKeyPairDao;
import com.cloud.user.dao.UserDao;
import com.cloud.user.dao.VmDiskStatisticsDao;
import com.cloud.uservm.UserVm;
import com.cloud.utils.DateUtil;
import com.cloud.utils.Journal;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.crypt.DBEncryptionUtil;
import com.cloud.utils.crypt.RSAHelper;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GlobalLock;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallbackNoReturn;
import com.cloud.utils.db.TransactionCallbackWithException;
import com.cloud.utils.db.TransactionCallbackWithExceptionNoReturn;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.dao.DomainRouterDao;
import com.cloud.vm.dao.InstanceGroupDao;
import com.cloud.vm.dao.InstanceGroupVMMapDao;
import com.cloud.vm.dao.NicDao;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.dao.UserVmDetailsDao;
import com.cloud.vm.dao.VMInstanceDao;
import com.cloud.vm.snapshot.VMSnapshotManager;
import com.cloud.vm.snapshot.VMSnapshotVO;
import com.cloud.vm.snapshot.dao.VMSnapshotDao;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserVmManagerImpl extends ManagerBase implements UserVmManager, VirtualMachineGuru, UserVmService, Configurable {
    private static final ConfigKey<Integer> VmIpFetchWaitInterval = new ConfigKey<>("Advanced", Integer.class, "externaldhcp.vmip.retrieval.interval", "180",
            "Wait Interval (in seconds) for shared network vm dhcp ip addr fetch for next iteration ", true);
    private static final ConfigKey<Integer> VmIpFetchTrialMax = new ConfigKey<>("Advanced", Integer.class, "externaldhcp.vmip.max.retry", "10",
            "The max number of retrieval times for shared entwork vm dhcp ip fetch, in case of failures", true);

    // seconds
    private static final ConfigKey<Integer> VmIpFetchThreadPoolMax = new ConfigKey<>("Advanced", Integer.class, "externaldhcp.vmipFetch.threadPool.max", "10",
            "number of threads for fetching vms ip address", true);
    private static final Logger s_logger = LoggerFactory.getLogger(UserVmManagerImpl.class);
    private static final int ACQUIRE_GLOBAL_LOCK_TIMEOUT_FOR_COOPERATION = 3; // 3 seconds
    private static final long GB_TO_BYTES = 1024 * 1024 * 1024;
    private static final int MAX_HTTP_GET_LENGTH = 2 * MAX_USER_DATA_LENGTH_BYTES;
    private static final int MAX_HTTP_POST_LENGTH = 16 * MAX_USER_DATA_LENGTH_BYTES;
    @Inject
    protected HostDao _hostDao = null;
    @Inject
    protected HostTagsDao _hostTagsDao = null;
    @Inject
    protected ServiceOfferingDao _offeringDao = null;
    @Inject
    protected DiskOfferingDao _diskOfferingDao = null;
    @Inject
    protected VMTemplateDao _templateDao = null;
    @Inject
    private VMTemplateZoneDao _templateZoneDao = null;
    @Inject
    protected TemplateDataStoreDao _templateStoreDao;
    @Inject
    protected DomainDao _domainDao = null;
    @Inject
    protected UserVmDao _vmDao = null;
    @Inject
    protected VolumeDao _volsDao = null;
    @Inject
    private FirewallRulesDao _rulesDao = null;
    @Inject
    private LoadBalancerVMMapDao _loadBalancerVMMapDao = null;
    @Inject
    protected PortForwardingRulesDao _portForwardingDao;
    @Inject
    protected IPAddressDao _ipAddressDao = null;
    @Inject
    protected HostPodDao _podDao = null;
    @Inject
    protected NetworkModel _networkModel = null;
    @Inject
    protected NetworkOrchestrationService _networkMgr = null;
    @Inject
    protected StorageManager _storageMgr = null;
    @Inject
    protected AgentManager _agentMgr = null;
    @Inject
    protected ConfigurationManager _configMgr = null;
    @Inject
    protected AccountDao _accountDao = null;
    @Inject
    protected UserDao _userDao = null;
    @Inject
    protected SnapshotDao _snapshotDao = null;
    @Inject
    protected GuestOSDao _guestOSDao = null;
    @Inject
    protected HighAvailabilityManager _haMgr = null;
    @Inject
    protected AlertManager _alertMgr = null;
    @Inject
    protected AccountManager _accountMgr;
    @Inject
    protected AccountService _accountService;
    @Inject
    protected ClusterDao _clusterDao;
    @Inject
    protected PrimaryDataStoreDao _storagePoolDao;
    @Inject
    protected ServiceOfferingDao _serviceOfferingDao;
    @Inject
    protected NetworkOfferingDao _networkOfferingDao;
    @Inject
    protected InstanceGroupDao _vmGroupDao;
    @Inject
    private InstanceGroupVMMapDao _groupVMMapDao;
    @Inject
    protected VirtualMachineManager _itMgr;
    @Inject
    protected NetworkDao _networkDao;
    @Inject
    protected NicDao _nicDao;
    @Inject
    protected VpcDao _vpcDao;
    @Inject
    protected RulesManager _rulesMgr;
    @Inject
    protected LoadBalancingRulesManager _lbMgr;
    @Inject
    private SSHKeyPairDao _sshKeyPairDao;
    @Inject
    protected UserVmDetailsDao _vmDetailsDao;
    @Inject
    protected HypervisorCapabilitiesDao _hypervisorCapabilitiesDao;
    @Inject
    protected CapacityManager _capacityMgr;
    @Inject
    protected VMInstanceDao _vmInstanceDao;
    @Inject
    protected ResourceLimitService _resourceLimitMgr;
    @Inject
    protected FirewallManager _firewallMgr;
    @Inject
    protected ProjectManager _projectMgr;
    @Inject
    protected ResourceManager _resourceMgr;
    @Inject
    protected NetworkServiceMapDao _ntwkSrvcDao;
    @Inject
    protected GuestOSCategoryDao _guestOSCategoryDao;
    @Inject
    protected VMSnapshotDao _vmSnapshotDao;
    @Inject
    private VMSnapshotManager _vmSnapshotMgr;
    @Inject
    private VMNetworkMapDao _vmNetworkMapDao;
    @Inject
    protected IpAddressManager _ipAddrMgr;
    protected ScheduledExecutorService _executor = null;
    private int _expungeInterval;
    private int _expungeDelay;
    private boolean _dailyOrHourly = false;
    protected String _instance;
    protected String _zone;
    protected int _scaleRetry;
    private Map<Long, VmAndCountDetails> vmIdCountMap = new ConcurrentHashMap<>();
    @Inject
    private OrchestrationService _orchSrvc;
    @Inject
    EntityManager _entityMgr;
    @Inject
    private PhysicalNetworkDao _physicalNetworkDao;
    @Inject
    private VpcManager _vpcMgr;
    @Inject
    private TemplateManager _templateMgr;
    @Inject
    private VmDiskStatisticsDao _vmDiskStatsDao;
    @Inject
    private AffinityGroupVMMapDao _affinityGroupVMMapDao;
    @Inject
    private AffinityGroupDao _affinityGroupDao;
    @Inject
    private DedicatedResourceDao _dedicatedDao;
    @Inject
    private AffinityGroupService _affinityGroupService;
    @Inject
    private PlannerHostReservationDao _plannerHostReservationDao;
    @Inject
    private VolumeDataFactory volFactory;
    @Inject
    private UserVmDetailsDao _uservmDetailsDao;
    @Inject
    private UUIDManager _uuidMgr;
    @Inject
    private DeploymentPlanningManager _planningMgr;
    @Inject
    private VolumeApiService _volumeService;
    @Inject
    private DataStoreManager _dataStoreMgr;
    @Inject
    private VpcVirtualNetworkApplianceManager _virtualNetAppliance;
    @Inject
    private DomainRouterDao _routerDao;
    private ExecutorService _vmIpFetchThreadExecutor;
    @Inject
    private ConfigurationDao _configDao;
    @Inject
    VolumeOrchestrationService volumeMgr;
    @Inject
    private ManagementService _mgr;
    @Inject
    private ManagementServer _mgtserver;
    @Inject
    private ServiceOfferingDetailsDao serviceOfferingDetailsDao;
    @Inject
    ZoneRepository zoneRepository;

    private int capacityReleaseInterval;

    UserVmManagerImpl() {
    }

    @Override
    public UserVmVO getVirtualMachine(final long vmId) {
        return _vmDao.findById(vmId);
    }

    @Override
    public boolean stopVirtualMachine(final long userId, final long vmId) {
        boolean status;
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Stopping vm=" + vmId);
        }
        final UserVmVO vm = _vmDao.findById(vmId);
        if (vm == null || vm.getRemoved() != null) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("VM is either removed or deleted.");
            }
            return true;
        }

        _userDao.findById(userId);
        try {
            final VirtualMachineEntity vmEntity = _orchSrvc.getVirtualMachine(vm.getUuid());
            status = vmEntity.stop(Long.toString(userId));
        } catch (final ResourceUnavailableException e) {
            s_logger.debug("Unable to stop due to ", e);
            status = false;
        } catch (final CloudException e) {
            throw new CloudRuntimeException("Unable to contact the agent to stop the virtual machine " + vm, e);
        }
        return status;
    }

    @Override
    public HashMap<Long, VmStatsEntry> getVirtualMachineStatistics(final long hostId, final String hostName, final List<Long> vmIds) throws CloudRuntimeException {
        final HashMap<Long, VmStatsEntry> vmStatsById = new HashMap<>();

        if (vmIds.isEmpty()) {
            return vmStatsById;
        }

        final List<String> vmNames = new ArrayList<>();

        for (final Long vmId : vmIds) {
            final UserVmVO vm = _vmDao.findById(vmId);
            vmNames.add(vm.getInstanceName());
        }

        final Answer answer = _agentMgr.easySend(hostId, new GetVmStatsCommand(vmNames, _hostDao.findById(hostId).getGuid(), hostName));
        if (answer == null || !answer.getResult()) {
            s_logger.warn("Unable to obtain VM statistics.");
            return null;
        } else {
            final HashMap<String, VmStatsEntry> vmStatsByName = ((GetVmStatsAnswer) answer).getVmStatsMap();

            if (vmStatsByName == null) {
                s_logger.warn("Unable to obtain VM statistics.");
                return null;
            }

            for (final Map.Entry<String, VmStatsEntry> entry : vmStatsByName.entrySet()) {
                vmStatsById.put(vmIds.get(vmNames.indexOf(entry.getKey())), entry.getValue());
            }
        }

        return vmStatsById;
    }

    @Override
    public HashMap<Long, List<VmDiskStatsEntry>> getVmDiskStatistics(final long hostId, final String hostName, final List<Long> vmIds) throws CloudRuntimeException {
        final HashMap<Long, List<VmDiskStatsEntry>> vmDiskStatsById = new HashMap<>();

        if (vmIds.isEmpty()) {
            return vmDiskStatsById;
        }

        final List<String> vmNames = new ArrayList<>();

        for (final Long vmId : vmIds) {
            final UserVmVO vm = _vmDao.findById(vmId);
            vmNames.add(vm.getInstanceName());
        }

        final Answer answer = _agentMgr.easySend(hostId, new GetVmDiskStatsCommand(vmNames, _hostDao.findById(hostId).getGuid(), hostName));
        if (answer == null || !answer.getResult()) {
            s_logger.warn("Unable to obtain VM disk statistics.");
            return null;
        } else {
            final HashMap<String, List<VmDiskStatsEntry>> vmDiskStatsByName = ((GetVmDiskStatsAnswer) answer).getVmDiskStatsMap();

            if (vmDiskStatsByName == null) {
                s_logger.warn("Unable to obtain VM disk statistics.");
                return null;
            }

            for (final Map.Entry<String, List<VmDiskStatsEntry>> entry : vmDiskStatsByName.entrySet()) {
                vmDiskStatsById.put(vmIds.get(vmNames.indexOf(entry.getKey())), entry.getValue());
            }
        }

        return vmDiskStatsById;
    }

    @Override
    public boolean deleteVmGroup(final long groupId) {
        // delete all the mappings from group_vm_map table
        final List<InstanceGroupVMMapVO> groupVmMaps = _groupVMMapDao.listByGroupId(groupId);
        for (final InstanceGroupVMMapVO groupMap : groupVmMaps) {
            final SearchCriteria<InstanceGroupVMMapVO> sc = _groupVMMapDao.createSearchCriteria();
            sc.addAnd("instanceId", SearchCriteria.Op.EQ, groupMap.getInstanceId());
            _groupVMMapDao.expunge(sc);
        }

        if (_vmGroupDao.remove(groupId)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    @DB
    public boolean addInstanceToGroup(final long userVmId, final String groupName) {
        final UserVmVO vm = _vmDao.findById(userVmId);

        InstanceGroupVO group = _vmGroupDao.findByAccountAndName(vm.getAccountId(), groupName);
        // Create vm group if the group doesn't exist for this account
        if (group == null) {
            group = createVmGroup(groupName, vm.getAccountId());
        }

        if (group != null) {
            final UserVm userVm = _vmDao.acquireInLockTable(userVmId);
            if (userVm == null) {
                s_logger.warn("Failed to acquire lock on user vm id=" + userVmId);
            }
            try {
                final InstanceGroupVO groupFinal = group;
                Transaction.execute(new TransactionCallbackNoReturn() {
                    @Override
                    public void doInTransactionWithoutResult(final TransactionStatus status) {
                        // don't let the group be deleted when we are assigning vm to
                        // it.
                        final InstanceGroupVO ngrpLock = _vmGroupDao.lockRow(groupFinal.getId(), false);
                        if (ngrpLock == null) {
                            s_logger.warn("Failed to acquire lock on vm group id=" + groupFinal.getId() + " name=" + groupFinal.getName());
                            throw new CloudRuntimeException("Failed to acquire lock on vm group id=" + groupFinal.getId() + " name=" + groupFinal.getName());
                        }

                        // Currently don't allow to assign a vm to more than one group
                        if (_groupVMMapDao.listByInstanceId(userVmId) != null) {
                            // Delete all mappings from group_vm_map table
                            final List<InstanceGroupVMMapVO> groupVmMaps = _groupVMMapDao.listByInstanceId(userVmId);
                            for (final InstanceGroupVMMapVO groupMap : groupVmMaps) {
                                final SearchCriteria<InstanceGroupVMMapVO> sc = _groupVMMapDao.createSearchCriteria();
                                sc.addAnd("instanceId", SearchCriteria.Op.EQ, groupMap.getInstanceId());
                                _groupVMMapDao.expunge(sc);
                            }
                        }
                        final InstanceGroupVMMapVO groupVmMapVO = new InstanceGroupVMMapVO(groupFinal.getId(), userVmId);
                        _groupVMMapDao.persist(groupVmMapVO);
                    }
                });

                return true;
            } finally {
                if (userVm != null) {
                    _vmDao.releaseFromLockTable(userVmId);
                }
            }
        }
        return false;
    }

    @Override
    public InstanceGroupVO getGroupForVm(final long vmId) {
        // TODO - in future releases vm can be assigned to multiple groups; but
        // currently return just one group per vm
        try {
            final List<InstanceGroupVMMapVO> groupsToVmMap = _groupVMMapDao.listByInstanceId(vmId);

            if (groupsToVmMap != null && groupsToVmMap.size() != 0) {
                final InstanceGroupVO group = _vmGroupDao.findById(groupsToVmMap.get(0).getGroupId());
                return group;
            } else {
                return null;
            }
        } catch (final Exception e) {
            s_logger.warn("Error trying to get group for a vm: ", e);
            return null;
        }
    }

    @Override
    public void removeInstanceFromInstanceGroup(final long vmId) {
        try {
            final List<InstanceGroupVMMapVO> groupVmMaps = _groupVMMapDao.listByInstanceId(vmId);
            for (final InstanceGroupVMMapVO groupMap : groupVmMaps) {
                final SearchCriteria<InstanceGroupVMMapVO> sc = _groupVMMapDao.createSearchCriteria();
                sc.addAnd("instanceId", SearchCriteria.Op.EQ, groupMap.getInstanceId());
                _groupVMMapDao.expunge(sc);
            }
        } catch (final Exception e) {
            s_logger.warn("Error trying to remove vm from group: ", e);
        }
    }

    @Override
    public boolean expunge(UserVmVO vm, final long callerUserId, final Account caller) {
        vm = _vmDao.acquireInLockTable(vm.getId());
        if (vm == null) {
            return false;
        }
        try {
            final List<VolumeVO> rootVol = _volsDao.findByInstanceAndType(vm.getId(), VolumeType.ROOT);
            // expunge the vm
            _itMgr.advanceExpunge(vm.getUuid());
            // Update Resource count
            if (vm.getAccountId() != Account.ACCOUNT_ID_SYSTEM && !rootVol.isEmpty()) {
                _resourceLimitMgr.decrementResourceCount(vm.getAccountId(), ResourceType.volume);
                _resourceLimitMgr.recalculateResourceCount(vm.getAccountId(), vm.getDomainId(), ResourceType.primary_storage.getOrdinal());
            }

            // Only if vm is not expunged already, cleanup it's resources
            if (vm.getRemoved() == null) {
                // Cleanup vm resources - all the PF/LB/StaticNat rules
                // associated with vm
                s_logger.debug("Starting cleaning up vm " + vm + " resources...");
                if (cleanupVmResources(vm.getId())) {
                    s_logger.debug("Successfully cleaned up vm " + vm + " resources as a part of expunge process");
                } else {
                    s_logger.warn("Failed to cleanup resources as a part of vm " + vm + " expunge");
                    return false;
                }

                _vmDao.remove(vm.getId());
            }

            return true;
        } catch (final ResourceUnavailableException e) {
            s_logger.warn("Unable to expunge  " + vm, e);
            return false;
        } catch (final OperationTimedoutException e) {
            s_logger.warn("Operation time out on expunging " + vm, e);
            return false;
        } catch (final ConcurrentOperationException e) {
            s_logger.warn("Concurrent operations on expunging " + vm, e);
            return false;
        } finally {
            _vmDao.releaseFromLockTable(vm.getId());
        }
    }

    @Override
    public Pair<UserVmVO, Map<VirtualMachineProfile.Param, Object>> startVirtualMachine(final long vmId, final Long hostId, final Map<VirtualMachineProfile.Param, Object>
            additionalParams, final String deploymentPlannerToUse)
            throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException {
        // Input validation
        final Account callerAccount = CallContext.current().getCallingAccount();
        final UserVO callerUser = _userDao.findById(CallContext.current().getCallingUserId());

        // if account is removed, return error
        if (callerAccount != null && callerAccount.getRemoved() != null) {
            throw new InvalidParameterValueException("The account " + callerAccount.getId() + " is removed");
        }

        final UserVmVO vm = _vmDao.findById(vmId);
        if (vm == null) {
            throw new InvalidParameterValueException("unable to find a virtual machine with id " + vmId);
        }

        _accountMgr.checkAccess(callerAccount, null, true, vm);

        final Account owner = _accountDao.findById(vm.getAccountId());

        if (owner == null) {
            throw new InvalidParameterValueException("The owner of " + vm + " does not exist: " + vm.getAccountId());
        }

        if (owner.getState() == Account.State.disabled) {
            throw new PermissionDeniedException("The owner of " + vm + " is disabled: " + vm.getAccountId());
        }

        Host destinationHost = null;
        if (hostId != null) {
            final Account account = CallContext.current().getCallingAccount();
            if (!_accountService.isRootAdmin(account.getId())) {
                throw new PermissionDeniedException(
                        "Parameter hostid can only be specified by a Root Admin, permission denied");
            }
            destinationHost = _hostDao.findById(hostId);
            if (destinationHost == null) {
                throw new InvalidParameterValueException("Unable to find the host to deploy the VM, host id=" + hostId);
            }
        }

        DataCenterDeployment plan = null;
        boolean deployOnGivenHost = false;
        if (destinationHost != null) {
            s_logger.debug("Destination Host to deploy the VM is specified, specifying a deployment plan to deploy the VM");
            plan = new DataCenterDeployment(vm.getDataCenterId(), destinationHost.getPodId(), destinationHost.getClusterId(), destinationHost.getId(), null, null);
            deployOnGivenHost = true;
        }

        // Set parameters
        Map<VirtualMachineProfile.Param, Object> params = null;
        VMTemplateVO template = null;
        if (vm.isUpdateParameters()) {
            _vmDao.loadDetails(vm);
            // Check that the password was passed in and is valid
            template = _templateDao.findByIdIncludingRemoved(vm.getTemplateId());

            String password = "saved_password";
            if (template.getEnablePassword()) {
                if (vm.getDetail("password") != null) {
                    password = DBEncryptionUtil.decrypt(vm.getDetail("password"));
                } else {
                    password = _mgr.generateRandomPassword();
                }
            }

            if (!validPassword(password)) {
                throw new InvalidParameterValueException("A valid password for this virtual machine was not provided.");
            }

            // Check if an SSH key pair was selected for the instance and if so
            // use it to encrypt & save the vm password
            encryptAndStorePassword(vm, password);

            params = new HashMap<>();
            if (additionalParams != null) {
                params.putAll(additionalParams);
            }
            params.put(VirtualMachineProfile.Param.VmPassword, password);
        }

        final VirtualMachineEntity vmEntity = _orchSrvc.getVirtualMachine(vm.getUuid());

        DeploymentPlanner planner = null;
        if (deploymentPlannerToUse != null) {
            // if set to null, the deployment planner would be later figured out either from global config var, or from
            // the service offering
            planner = _planningMgr.getDeploymentPlannerByName(deploymentPlannerToUse);
            if (planner == null) {
                throw new InvalidParameterValueException("Can't find a planner by name " + deploymentPlannerToUse);
            }
        }

        final String reservationId = vmEntity.reserve(planner, plan, new ExcludeList(), Long.toString(callerUser.getId()));
        vmEntity.deploy(reservationId, Long.toString(callerUser.getId()), params, deployOnGivenHost);

        final Pair<UserVmVO, Map<VirtualMachineProfile.Param, Object>> vmParamPair = new Pair(vm, params);
        if (vm != null && vm.isUpdateParameters()) {
            // Set date and version we start this VM
            vm.setLastStartDateTime(getCurrentLocalDateTimeStamp());
            vm.setLastStartVersion(UserVmManagerImpl.class.getPackage().getImplementationVersion());
            // this value is not being sent to the backend; need only for api
            // display purposes
            if (template.getEnablePassword()) {
                vm.setPassword((String) vmParamPair.second().get(VirtualMachineProfile.Param.VmPassword));
                vm.setUpdateParameters(false);
                if (vm.getDetail("password") != null) {
                    _vmDetailsDao.remove(_vmDetailsDao.findDetail(vm.getId(), "password").getId());
                }
            }
            // Reset VM compliance state
            if (vm.getComplianceStatus() == ComplianceStatus.VMNeedsRestart) {
                vm.setComplianceStatus(ComplianceStatus.Compliant);
            }
            _vmDao.update(vm.getId(), vm);
        }

        return vmParamPair;
    }

    private String getCurrentLocalDateTimeStamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public boolean upgradeVirtualMachine(final Long vmId, final Long newServiceOfferingId, final Map<String, String> customParameters) throws ResourceUnavailableException,
            ConcurrentOperationException, ManagementServerException, VirtualMachineMigrationException {

        // Verify input parameters
        final VMInstanceVO vmInstance = _vmInstanceDao.findById(vmId);

        if (vmInstance != null) {
            // If target VM has associated VM snapshots then don't allow upgrading of VM
            final List<VMSnapshotVO> vmSnapshots = _vmSnapshotDao.findByVm(vmId);
            if (vmSnapshots.size() > 0) {
                throw new InvalidParameterValueException("Unable to scale VM, please remove VM snapshots before scaling VM");
            }
            if (vmInstance.getState().equals(State.Stopped)) {
                upgradeStoppedVirtualMachine(vmId, newServiceOfferingId, customParameters);
                return true;
            }
            if (vmInstance.getState().equals(State.Running)) {
                return upgradeRunningVirtualMachine(vmId, newServiceOfferingId, customParameters);
            }
        }
        return false;
    }

    @Override
    public boolean setupVmForPvlan(final boolean add, final Long hostId, final NicProfile nic) {
        if (!nic.getBroadCastUri().getScheme().equals("pvlan")) {
            return false;
        }
        String op = "add";
        if (!add) {
            op = "delete";
        }
        final Network network = _networkDao.findById(nic.getNetworkId());
        final Host host = _hostDao.findById(hostId);
        final String networkTag = _networkModel.getNetworkTag(host.getHypervisorType(), network);
        final PvlanSetupCommand cmd = PvlanSetupCommand.createVmSetup(op, nic.getBroadCastUri(), networkTag, nic.getMacAddress());
        final Answer answer;
        try {
            answer = _agentMgr.send(hostId, cmd);
        } catch (final OperationTimedoutException e) {
            s_logger.warn("Timed Out", e);
            return false;
        } catch (final AgentUnavailableException e) {
            s_logger.warn("Agent Unavailable ", e);
            return false;
        }

        boolean result = true;
        if (answer == null || !answer.getResult()) {
            result = false;
        }
        return result;
    }

    @Override
    public void collectVmDiskStatistics(final UserVmVO userVm) {
        // support KVM only util 2013.06.25
        if (!userVm.getHypervisorType().equals(HypervisorType.KVM)) {
            return;
        }
        s_logger.debug("Collect vm disk statistics from host before stopping Vm");
        final long hostId = userVm.getHostId();
        final List<String> vmNames = new ArrayList<>();
        vmNames.add(userVm.getInstanceName());
        final HostVO host = _hostDao.findById(hostId);

        final GetVmDiskStatsAnswer diskStatsAnswer;
        try {
            diskStatsAnswer = (GetVmDiskStatsAnswer) _agentMgr.easySend(hostId, new GetVmDiskStatsCommand(vmNames, host.getGuid(), host.getName()));
        } catch (final Exception e) {
            s_logger.warn("Error while collecting disk stats for vm: " + userVm.getInstanceName() + " from host: " + host.getName(), e);
            return;
        }
        if (diskStatsAnswer != null) {
            if (!diskStatsAnswer.getResult()) {
                s_logger.warn("Error while collecting disk stats vm: " + userVm.getInstanceName() + " from host: " + host.getName() + "; details: " + diskStatsAnswer.getDetails());
                return;
            }
            try {
                final GetVmDiskStatsAnswer diskStatsAnswerFinal = diskStatsAnswer;
                Transaction.execute(new TransactionCallbackNoReturn() {
                    @Override
                    public void doInTransactionWithoutResult(final TransactionStatus status) {
                        final HashMap<String, List<VmDiskStatsEntry>> vmDiskStatsByName = diskStatsAnswerFinal.getVmDiskStatsMap();
                        if (vmDiskStatsByName == null) {
                            return;
                        }
                        final List<VmDiskStatsEntry> vmDiskStats = vmDiskStatsByName.get(userVm.getInstanceName());
                        if (vmDiskStats == null) {
                            return;
                        }

                        for (final VmDiskStatsEntry vmDiskStat : vmDiskStats) {
                            final SearchCriteria<VolumeVO> sc_volume = _volsDao.createSearchCriteria();
                            sc_volume.addAnd("path", SearchCriteria.Op.EQ, vmDiskStat.getPath());
                            final List<VolumeVO> volumes = _volsDao.search(sc_volume, null);
                            if (volumes == null || volumes.size() == 0) {
                                break;
                            }
                            final VolumeVO volume = volumes.get(0);
                            final VmDiskStatisticsVO previousVmDiskStats = _vmDiskStatsDao.findBy(userVm.getAccountId(), userVm.getDataCenterId(), userVm.getId(), volume.getId());
                            final VmDiskStatisticsVO vmDiskStat_lock = _vmDiskStatsDao.lock(userVm.getAccountId(), userVm.getDataCenterId(), userVm.getId(), volume.getId());

                            if (vmDiskStat.getIORead() == 0 && vmDiskStat.getIOWrite() == 0 && vmDiskStat.getBytesRead() == 0 && vmDiskStat.getBytesWrite() == 0) {
                                s_logger.debug("Read/Write of IO and Bytes are both 0. Not updating vm_disk_statistics");
                                continue;
                            }

                            if (vmDiskStat_lock == null) {
                                s_logger.warn("unable to find vm disk stats from host for account: " + userVm.getAccountId() + " with vmId: " + userVm.getId() + " and volumeId:"
                                        + volume.getId());
                                continue;
                            }

                            if (previousVmDiskStats != null
                                    && (previousVmDiskStats.getCurrentIORead() != vmDiskStat_lock.getCurrentIORead() || previousVmDiskStats.getCurrentIOWrite() != vmDiskStat_lock
                                    .getCurrentIOWrite()
                                    || previousVmDiskStats.getCurrentBytesRead() != vmDiskStat_lock.getCurrentBytesRead() || previousVmDiskStats
                                    .getCurrentBytesWrite() != vmDiskStat_lock.getCurrentBytesWrite())) {
                                s_logger.debug("vm disk stats changed from the time GetVmDiskStatsCommand was sent. " + "Ignoring current answer. Host: " + host.getName()
                                        + " . VM: " + vmDiskStat.getVmName() + " IO Read: " + vmDiskStat.getIORead() + " IO Write: " + vmDiskStat.getIOWrite() + " Bytes Read: "
                                        + vmDiskStat.getBytesRead() + " Bytes Write: " + vmDiskStat.getBytesWrite());
                                continue;
                            }

                            if (vmDiskStat_lock.getCurrentIORead() > vmDiskStat.getIORead()) {
                                if (s_logger.isDebugEnabled()) {
                                    s_logger.debug("Read # of IO that's less than the last one.  " + "Assuming something went wrong and persisting it. Host: " + host.getName()
                                            + " . VM: " + vmDiskStat.getVmName() + " Reported: " + vmDiskStat.getIORead() + " Stored: " + vmDiskStat_lock.getCurrentIORead());
                                }
                                vmDiskStat_lock.setNetIORead(vmDiskStat_lock.getNetIORead() + vmDiskStat_lock.getCurrentIORead());
                            }
                            vmDiskStat_lock.setCurrentIORead(vmDiskStat.getIORead());
                            if (vmDiskStat_lock.getCurrentIOWrite() > vmDiskStat.getIOWrite()) {
                                if (s_logger.isDebugEnabled()) {
                                    s_logger.debug("Write # of IO that's less than the last one.  " + "Assuming something went wrong and persisting it. Host: " + host.getName()
                                            + " . VM: " + vmDiskStat.getVmName() + " Reported: " + vmDiskStat.getIOWrite() + " Stored: " + vmDiskStat_lock.getCurrentIOWrite());
                                }
                                vmDiskStat_lock.setNetIOWrite(vmDiskStat_lock.getNetIOWrite() + vmDiskStat_lock.getCurrentIOWrite());
                            }
                            vmDiskStat_lock.setCurrentIOWrite(vmDiskStat.getIOWrite());
                            if (vmDiskStat_lock.getCurrentBytesRead() > vmDiskStat.getBytesRead()) {
                                if (s_logger.isDebugEnabled()) {
                                    s_logger.debug("Read # of Bytes that's less than the last one.  " + "Assuming something went wrong and persisting it. Host: " + host.getName()
                                            + " . VM: " + vmDiskStat.getVmName() + " Reported: " + vmDiskStat.getBytesRead() + " Stored: " + vmDiskStat_lock.getCurrentBytesRead());
                                }
                                vmDiskStat_lock.setNetBytesRead(vmDiskStat_lock.getNetBytesRead() + vmDiskStat_lock.getCurrentBytesRead());
                            }
                            vmDiskStat_lock.setCurrentBytesRead(vmDiskStat.getBytesRead());
                            if (vmDiskStat_lock.getCurrentBytesWrite() > vmDiskStat.getBytesWrite()) {
                                if (s_logger.isDebugEnabled()) {
                                    s_logger.debug("Write # of Bytes that's less than the last one.  " + "Assuming something went wrong and persisting it. Host: " + host.getName()
                                            + " . VM: " + vmDiskStat.getVmName() + " Reported: " + vmDiskStat.getBytesWrite() + " Stored: "
                                            + vmDiskStat_lock.getCurrentBytesWrite());
                                }
                                vmDiskStat_lock.setNetBytesWrite(vmDiskStat_lock.getNetBytesWrite() + vmDiskStat_lock.getCurrentBytesWrite());
                            }
                            vmDiskStat_lock.setCurrentBytesWrite(vmDiskStat.getBytesWrite());

                            if (!_dailyOrHourly) {
                                //update agg bytes
                                vmDiskStat_lock.setAggIORead(vmDiskStat_lock.getNetIORead() + vmDiskStat_lock.getCurrentIORead());
                                vmDiskStat_lock.setAggIOWrite(vmDiskStat_lock.getNetIOWrite() + vmDiskStat_lock.getCurrentIOWrite());
                                vmDiskStat_lock.setAggBytesRead(vmDiskStat_lock.getNetBytesRead() + vmDiskStat_lock.getCurrentBytesRead());
                                vmDiskStat_lock.setAggBytesWrite(vmDiskStat_lock.getNetBytesWrite() + vmDiskStat_lock.getCurrentBytesWrite());
                            }

                            _vmDiskStatsDao.update(vmDiskStat_lock.getId(), vmDiskStat_lock);
                        }
                    }
                });
            } catch (final Exception e) {
                s_logger.warn("Unable to update vm disk statistics for vm: " + userVm.getId() + " from host: " + hostId, e);
            }
        }
    }

    @Override
    public UserVm updateVirtualMachine(final long id, String displayName, final String group, Boolean ha, Boolean isDisplayVmEnabled, Long osTypeId, String userData,
                                       Boolean isDynamicallyScalable, final HTTPMethod httpMethod, final String customId, String hostName, final String instanceName,
                                       final String manufacturerString, final OptimiseFor optimiseFor, final Boolean requiresRestart, final MaintenancePolicy maintenancePolicy,
                                       final Long bootMenuTimeout, final String bootOrder, final ComplianceStatus complianceStatus
    ) throws ResourceUnavailableException, InsufficientCapacityException {

        final UserVmVO vm = _vmDao.findById(id);
        if (vm == null) {
            throw new CloudRuntimeException("Unable to find virual machine with id " + id);
        }

        if (instanceName != null) {
            final VMInstanceVO vmInstance = _vmInstanceDao.findVMByInstanceName(instanceName);
            if (vmInstance != null && vmInstance.getId() != id) {
                throw new CloudRuntimeException("Instance name : " + instanceName + " is not unique");
            }
        }

        if (vm.getState() == State.Error || vm.getState() == State.Expunging) {
            s_logger.error("vm is not in the right state: " + id);
            throw new InvalidParameterValueException("Vm with id " + id + " is not in the right state");
        }

        if (vm.getMaintenancePolicy() != maintenancePolicy && MaintenancePolicy.LiveMigrate.equals(maintenancePolicy)) {
            final List<VolumeVO> volumes = _volsDao.findByInstance(vm.getId());
            for (final VolumeVO volume : volumes) {
                final StoragePoolVO storagePool = _storagePoolDao.findById(volume.getPoolId());
                if (ScopeType.HOST.equals(storagePool.getScope())) {
                    throw new InvalidParameterValueException("Vm " + vm.getInstanceName() + " has volume(s) with scope HOST, so MaintenancePolicy "
                            + maintenancePolicy.toString() + " is not allowed.");
                }
            }
        }

        if (displayName == null) {
            displayName = vm.getDisplayName();
        }

        if (ha == null) {
            ha = vm.isHaEnabled();
        }

        final ServiceOffering offering = _serviceOfferingDao.findById(vm.getId(), vm.getServiceOfferingId());
        if (!offering.getOfferHA() && ha) {
            throw new InvalidParameterValueException("Can't enable ha for the vm as it's created from the Service offering having HA disabled");
        }

        if (isDisplayVmEnabled == null) {
            isDisplayVmEnabled = vm.isDisplayVm();
        }

        boolean updateUserdata = false;
        if (userData != null) {
            // check and replace newlines
            userData = userData.replace("\\n", "");
            if (!Base64.isBase64(userData)) {
                throw new InvalidParameterValueException("Unsupported encoding");
            }
            validateUserData(userData, httpMethod);
            // update userData on domain router.
            updateUserdata = true;
        } else {
            userData = vm.getUserData();
        }

        if (isDynamicallyScalable == null) {
            isDynamicallyScalable = vm.isDynamicallyScalable();
        }

        if (osTypeId == null) {
            osTypeId = vm.getGuestOSId();
        }

        if (group != null) {
            addInstanceToGroup(id, group);
        }

        if (isDynamicallyScalable == null) {
            isDynamicallyScalable = vm.isDynamicallyScalable();
        }

        if (hostName != null) {
            // Check is hostName is RFC compliant
            checkNameForRFCCompliance(hostName);

            if (vm.getHostName().equalsIgnoreCase(hostName)) {
                s_logger.debug("Vm " + vm + " is already set with the hostName specified: " + hostName);
                hostName = null;
            }

            // Verify that vm's hostName is unique
            final List<NetworkVO> vmNtwks = new ArrayList<>();
            final List<? extends Nic> nics = _nicDao.listByVmId(vm.getId());
            for (final Nic nic : nics) {
                vmNtwks.add(_networkDao.findById(nic.getNetworkId()));
            }
            checkIfHostNameUniqueInNtwkDomain(hostName, vmNtwks);
        }

        _vmDao.updateVM(id, displayName, ha, osTypeId, userData, isDisplayVmEnabled, isDynamicallyScalable, customId, hostName, instanceName, manufacturerString, optimiseFor,
                requiresRestart, maintenancePolicy, bootMenuTimeout, bootOrder, complianceStatus);

        if (updateUserdata) {
            final boolean result = updateUserDataInternal(_vmDao.findById(id));
            if (result) {
                s_logger.debug("User data successfully updated for vm id=" + id);
            } else {
                throw new CloudRuntimeException("Failed to reset userdata for the virtual machine ");
            }
        }

        return _vmDao.findById(id);
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        _name = name;

        if (_configDao == null) {
            throw new ConfigurationException("Unable to get the configuration dao.");
        }

        final Map<String, String> configs = _configDao.getConfiguration("AgentManager", params);

        _instance = configs.get("instance.name");
        if (_instance == null) {
            _instance = "DEFAULT";
        }

        final String workers = configs.get("expunge.workers");
        final int wrks = NumbersUtil.parseInt(workers, 10);
        capacityReleaseInterval = NumbersUtil.parseInt(_configDao.getValue(Config.CapacitySkipcountingHours.key()), 3600);

        String time = configs.get("expunge.interval");
        _expungeInterval = NumbersUtil.parseInt(time, 86400);
        time = configs.get("expunge.delay");
        _expungeDelay = NumbersUtil.parseInt(time, _expungeInterval);

        _executor = Executors.newScheduledThreadPool(wrks, new NamedThreadFactory("UserVm-Scavenger"));

        final String aggregationRange = configs.get("usage.stats.job.aggregation.range");
        final int _usageAggregationRange = NumbersUtil.parseInt(aggregationRange, 1440);
        final int HOURLY_TIME = 60;
        final int DAILY_TIME = 60 * 24;
        if (_usageAggregationRange == DAILY_TIME) {
            _dailyOrHourly = true;
        } else if (_usageAggregationRange == HOURLY_TIME) {
            _dailyOrHourly = true;
        } else {
            _dailyOrHourly = false;
        }

        _itMgr.registerGuru(VirtualMachineType.User, this);

        VirtualMachine.State.getStateMachine().registerListener(new UserVmStateListener(_networkDao, _nicDao, _offeringDao, _vmDao, this, _configDao));

        final String value = _configDao.getValue(Config.SetVmInternalNameUsingDisplayName.key());

        _scaleRetry = NumbersUtil.parseInt(configs.get(Config.ScaleRetry.key()), 2);

        s_logger.info("User VM Manager is configured.");

        return true;
    }

    @Override
    public boolean start() {
        _executor.scheduleWithFixedDelay(new ExpungeTask(), _expungeInterval, _expungeInterval, TimeUnit.SECONDS);
        _executor.scheduleWithFixedDelay(new VmIpFetchTask(), VmIpFetchWaitInterval.value(), VmIpFetchWaitInterval.value(), TimeUnit.SECONDS);
        loadVmDetailsInMapForExternalDhcpIp();
        return true;
    }

    private void loadVmDetailsInMapForExternalDhcpIp() {

        final List<NetworkVO> networks = _networkDao.listByGuestType(GuestType.Shared);

        for (final NetworkVO network : networks) {
            if (_networkModel.isSharedNetworkWithoutServices(network.getId())) {
                final List<NicVO> nics = _nicDao.listByNetworkId(network.getId());

                for (final NicVO nic : nics) {

                    if (nic.getIPv4Address() == null) {
                        final long nicId = nic.getId();
                        final long vmId = nic.getInstanceId();
                        final VMInstanceVO vmInstance = _vmInstanceDao.findById(vmId);

                        // only load running vms. For stopped vms get loaded on starting
                        if (vmInstance.getState() == State.Running) {
                            final VmAndCountDetails vmAndCount = new VmAndCountDetails(vmId, VmIpFetchTrialMax.value());
                            vmIdCountMap.put(nicId, vmAndCount);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean stop() {
        _executor.shutdown();
        return true;
    }

    public String getRandomPrivateTemplateName() {
        return UUID.randomUUID().toString();
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_DESTROY, eventDescription = "destroying Vm", async = true)
    public UserVm destroyVm(final DestroyVMCmd cmd) throws ResourceUnavailableException, ConcurrentOperationException {
        final CallContext ctx = CallContext.current();
        final long vmId = cmd.getId();
        final boolean expunge = cmd.getExpunge();

        // When trying to expunge, permission is denied when the caller is not an admin and the AllowUserExpungeRecoverVm is false for the caller.
        if (expunge && !_accountMgr.isAdmin(ctx.getCallingAccount().getId()) && !AllowUserExpungeRecoverVm.valueIn(cmd.getEntityOwnerId())) {
            throw new PermissionDeniedException("Parameter " + ApiConstants.EXPUNGE + " can be passed by Admin only. Or when the allow.user.expunge.recover.vm key is set.");
        }

        final UserVm destroyedVm = destroyVm(vmId);
        if (expunge) {
            final UserVmVO vm = _vmDao.findById(vmId);
            if (!expunge(vm, ctx.getCallingUserId(), ctx.getCallingAccount())) {
                throw new CloudRuntimeException("Failed to expunge vm " + destroyedVm);
            }
        }

        return destroyedVm;
    }

    private void resourceCountDecrement(final long accountId, final Boolean displayVm, final Long cpu, final Long memory) {
        _resourceLimitMgr.decrementResourceCount(accountId, ResourceType.user_vm, displayVm);
        _resourceLimitMgr.decrementResourceCount(accountId, ResourceType.cpu, displayVm, cpu);
        _resourceLimitMgr.decrementResourceCount(accountId, ResourceType.memory, displayVm, memory);
    }

    private boolean cleanupVmResources(final long vmId) {
        boolean success = true;

        // Remove vm from instance group
        removeInstanceFromInstanceGroup(vmId);

        // cleanup firewall rules
        if (_firewallMgr.revokeFirewallRulesForVm(vmId)) {
            s_logger.debug("Firewall rules are removed successfully as a part of vm id=" + vmId + " expunge");
        } else {
            success = false;
            s_logger.warn("Fail to remove firewall rules as a part of vm id=" + vmId + " expunge");
        }

        // cleanup port forwarding rules
        if (_rulesMgr.revokePortForwardingRulesForVm(vmId)) {
            s_logger.debug("Port forwarding rules are removed successfully as a part of vm id=" + vmId + " expunge");
        } else {
            success = false;
            s_logger.warn("Fail to remove port forwarding rules as a part of vm id=" + vmId + " expunge");
        }

        // cleanup load balancer rules
        if (_lbMgr.removeVmFromLoadBalancers(vmId)) {
            s_logger.debug("Removed vm id=" + vmId + " from all load balancers as a part of expunge process");
        } else {
            success = false;
            s_logger.warn("Fail to remove vm id=" + vmId + " from load balancers as a part of expunge process");
        }

        // If vm is assigned to static nat, disable static nat for the ip
        // address and disassociate ip if elasticIP is enabled
        final List<IPAddressVO> ips = _ipAddressDao.findAllByAssociatedVmId(vmId);

        for (final IPAddressVO ip : ips) {
            try {
                if (_rulesMgr.disableStaticNat(ip.getId(), _accountMgr.getAccount(Account.ACCOUNT_ID_SYSTEM), User.UID_SYSTEM, true)) {
                    s_logger.debug("Disabled 1-1 nat for ip address " + ip + " as a part of vm id=" + vmId + " expunge");
                } else {
                    s_logger.warn("Failed to disable static nat for ip address " + ip + " as a part of vm id=" + vmId + " expunge");
                    success = false;
                }
            } catch (final ResourceUnavailableException e) {
                success = false;
                s_logger.warn("Failed to disable static nat for ip address " + ip + " as a part of vm id=" + vmId + " expunge because resource is unavailable", e);
            }
        }

        return success;
    }

    @Override
    public UserVm destroyVm(final long vmId) throws ResourceUnavailableException, ConcurrentOperationException {
        // Account caller = CallContext.current().getCallingAccount();
        // Long userId = CallContext.current().getCallingUserId();
        final Long userId = 2L;

        // Verify input parameters
        final UserVmVO vm = _vmDao.findById(vmId);
        if (vm == null || vm.getRemoved() != null) {
            final InvalidParameterValueException ex = new InvalidParameterValueException("Unable to find a virtual machine with specified vmId");
            throw ex;
        }

        if (vm.getState() == State.Destroyed || vm.getState() == State.Expunging) {
            s_logger.trace("Vm id=" + vmId + " is already destroyed");
            return vm;
        }

        final boolean status;
        final State vmState = vm.getState();

        try {
            final VirtualMachineEntity vmEntity = _orchSrvc.getVirtualMachine(vm.getUuid());
            status = vmEntity.destroy(Long.toString(userId));
        } catch (final CloudException e) {
            final CloudRuntimeException ex = new CloudRuntimeException("Unable to destroy with specified vmId", e);
            ex.addProxyObject(vm.getUuid(), "vmId");
            throw ex;
        }

        if (status) {
            // Mark the account's volumes as destroyed
            final List<VolumeVO> volumes = _volsDao.findByInstance(vmId);

            if (vmState != State.Error) {
                // Get serviceOffering for Virtual Machine
                final ServiceOfferingVO offering = _serviceOfferingDao.findByIdIncludingRemoved(vm.getId(), vm.getServiceOfferingId());

                //Update Resource Count for the given account
                resourceCountDecrement(vm.getAccountId(), vm.isDisplayVm(), new Long(offering.getCpu()), new Long(offering.getRamSize()));
            }
            return _vmDao.findById(vmId);
        } else {
            final CloudRuntimeException ex = new CloudRuntimeException("Failed to destroy vm with specified vmId");
            ex.addProxyObject(vm.getUuid(), "vmId");
            throw ex;
        }
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_RESETPASSWORD, eventDescription = "resetting Vm password", async = true)
    public UserVm resetVMPassword(final ResetVMPasswordCmd cmd, final String password) throws ResourceUnavailableException, InsufficientCapacityException {
        final Account caller = CallContext.current().getCallingAccount();
        final Long vmId = cmd.getId();
        final UserVmVO userVm = _vmDao.findById(cmd.getId());

        // Do parameters input validation
        if (userVm == null) {
            throw new InvalidParameterValueException("unable to find a virtual machine with id " + cmd.getId());
        }

        _vmDao.loadDetails(userVm);

        final VMTemplateVO template = _templateDao.findByIdIncludingRemoved(userVm.getTemplateId());
        if (template == null || !template.getEnablePassword()) {
            throw new InvalidParameterValueException("Fail to reset password for the virtual machine, the template is not password enabled");
        }

        if (userVm.getState() == State.Error || userVm.getState() == State.Expunging) {
            s_logger.error("vm is not in the right state: " + vmId);
            throw new InvalidParameterValueException("Vm with id " + vmId + " is not in the right state");
        }

        _accountMgr.checkAccess(caller, null, true, userVm);

        final boolean result = resetVMPasswordInternal(vmId, password);

        if (result) {
            userVm.setPassword(password);
            // update the password in vm_details table too
            // Check if an SSH key pair was selected for the instance and if so
            // use it to encrypt & save the vm password
            encryptAndStorePassword(userVm, password);
        } else {
            throw new CloudRuntimeException("Failed to reset password for the virtual machine ");
        }

        return userVm;
    }

    private boolean resetVMPasswordInternal(final Long vmId, final String password) throws ResourceUnavailableException, InsufficientCapacityException {
        final Long userId = CallContext.current().getCallingUserId();
        final VMInstanceVO vmInstance = _vmDao.findById(vmId);

        if (password == null || password.equals("")) {
            return false;
        }

        final VMTemplateVO template = _templateDao.findByIdIncludingRemoved(vmInstance.getTemplateId());
        if (template.getEnablePassword()) {
            final Nic defaultNic = _networkModel.getDefaultNic(vmId);
            if (defaultNic == null) {
                s_logger.error("Unable to reset password for vm " + vmInstance + " as the instance doesn't have default nic");
                return false;
            }

            final Network defaultNetwork = _networkDao.findById(defaultNic.getNetworkId());
            final NicProfile defaultNicProfile =
                    new NicProfile(defaultNic, defaultNetwork, null, null, null, _networkModel.getNetworkTag(template.getHypervisorType(), defaultNetwork));
            final VirtualMachineProfile vmProfile = new VirtualMachineProfileImpl(vmInstance);
            vmProfile.setParameter(VirtualMachineProfile.Param.VmPassword, password);

            final UserDataServiceProvider element = _networkMgr.getPasswordResetProvider(defaultNetwork);
            if (element == null) {
                throw new CloudRuntimeException("Can't find network element for " + Service.UserData.getName() + " provider needed for password reset");
            }

            final boolean result = element.savePassword(defaultNetwork, defaultNicProfile, vmProfile);

            // Need to reboot the virtual machine so that the password gets
            // redownloaded from the DomR, and reset on the VM
            if (!result) {
                s_logger.debug("Failed to reset password for the virutal machine; no need to reboot the vm");
                return false;
            } else {
                if (vmInstance.getState() == State.Stopped) {
                    s_logger.debug("Vm " + vmInstance + " is stopped, not rebooting it as a part of password reset");
                    return true;
                }

                if (rebootVirtualMachine(userId, vmId) == null) {
                    s_logger.warn("Failed to reboot the vm " + vmInstance);
                    return false;
                } else {
                    s_logger.debug("Vm " + vmInstance + " is rebooted successfully as a part of password reset");
                    return true;
                }
            }
        } else {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Reset password called for a vm that is not using a password enabled template");
            }
            return false;
        }
    }

    private void encryptAndStorePassword(final UserVmVO vm, final String password) {
        final String sshPublicKey = vm.getDetail("SSH.PublicKey");
        if (sshPublicKey != null && !sshPublicKey.equals("") && password != null && !password.equals("saved_password")) {
            if (!sshPublicKey.startsWith("ssh-rsa")) {
                s_logger.warn("Only RSA public keys can be used to encrypt a vm password.");
                return;
            }
            final String encryptedPasswd = RSAHelper.encryptWithSSHPublicKey(sshPublicKey, password);
            if (encryptedPasswd == null) {
                throw new CloudRuntimeException("Error encrypting password");
            }

            vm.setDetail("Encrypted.Password", encryptedPasswd);
            _vmDao.saveDetails(vm);
        }
    }

    private UserVm rebootVirtualMachine(final long userId, final long vmId) throws InsufficientCapacityException, ResourceUnavailableException {
        final UserVmVO vm = _vmDao.findById(vmId);

        if (vm == null || vm.getState() == State.Destroyed || vm.getState() == State.Expunging || vm.getRemoved() != null) {
            s_logger.warn("Vm id=" + vmId + " doesn't exist");
            return null;
        }

        if (vm.getState() == State.Running && vm.getHostId() != null) {
            collectVmDiskStatistics(vm);
            final Zone zone = zoneRepository.findById(vm.getDataCenterId()).orElse(null);
            try {
                if (zone.getNetworkType() == NetworkType.Advanced) {
                    //List all networks of vm
                    final List<Long> vmNetworks = _vmNetworkMapDao.getNetworks(vmId);
                    final List<DomainRouterVO> routers = new ArrayList<>();
                    //List the stopped routers
                    for (final long vmNetworkId : vmNetworks) {
                        final List<DomainRouterVO> router = _routerDao.listStopped(vmNetworkId);
                        routers.addAll(router);
                    }
                    //A vm may not have many nics attached and even fewer routers might be stopped (only in exceptional cases)
                    //Safe to start the stopped router serially, this is consistent with the way how multiple networks are added to vm during deploy
                    //and routers are started serially ,may revisit to make this process parallel
                    for (final DomainRouterVO routerToStart : routers) {
                        s_logger.warn("Trying to start router " + routerToStart.getInstanceName() + " as part of vm: " + vm.getInstanceName() + " reboot");
                        _virtualNetAppliance.startRouter(routerToStart.getId(), true);
                    }
                }
            } catch (final ConcurrentOperationException e) {
                throw new CloudRuntimeException("Concurrent operations on starting router. " + e);
            } catch (final Exception ex) {
                throw new CloudRuntimeException("Router start failed due to" + ex);
            } finally {
                s_logger.info("Rebooting vm " + vm.getInstanceName());
                _itMgr.reboot(vm.getUuid(), null);
            }
            return _vmDao.findById(vmId);
        } else {
            s_logger.error("Vm id=" + vmId + " is not in Running state, failed to reboot");
            return null;
        }
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_RESETSSHKEY, eventDescription = "resetting Vm SSHKey", async = true)
    public UserVm resetVMSSHKey(final ResetVMSSHKeyCmd cmd) throws ResourceUnavailableException, InsufficientCapacityException {

        final Account caller = CallContext.current().getCallingAccount();
        final Account owner = _accountMgr.finalizeOwner(caller, cmd.getAccountName(), cmd.getDomainId(), cmd.getProjectId());
        final Long vmId = cmd.getId();

        final UserVmVO userVm = _vmDao.findById(cmd.getId());
        if (userVm == null) {
            throw new InvalidParameterValueException("unable to find a virtual machine by id" + cmd.getId());
        }

        _vmDao.loadDetails(userVm);
        final VMTemplateVO template = _templateDao.findByIdIncludingRemoved(userVm.getTemplateId());

        // Do parameters input validation

        if (userVm.getState() == State.Error || userVm.getState() == State.Expunging) {
            s_logger.error("vm is not in the right state: " + vmId);
            throw new InvalidParameterValueException("Vm with specified id is not in the right state");
        }
        if (userVm.getState() != State.Stopped) {
            s_logger.error("vm is not in the right state: " + vmId);
            throw new InvalidParameterValueException("Vm " + userVm + " should be stopped to do SSH Key reset");
        }

        final SSHKeyPairVO s = _sshKeyPairDao.findByName(owner.getAccountId(), owner.getDomainId(), cmd.getName());
        if (s == null) {
            throw new InvalidParameterValueException("A key pair with name '" + cmd.getName() + "' does not exist for account " + owner.getAccountName()
                    + " in specified domain id");
        }

        _accountMgr.checkAccess(caller, null, true, userVm);
        String password = null;
        final String sshPublicKey = s.getPublicKey();
        if (template != null && template.getEnablePassword()) {
            password = _mgr.generateRandomPassword();
        }

        final boolean result = resetVMSSHKeyInternal(vmId, sshPublicKey, password);

        if (result) {
            userVm.setDetail("SSH.PublicKey", sshPublicKey);
            if (template != null && template.getEnablePassword()) {
                userVm.setPassword(password);
                //update the encrypted password in vm_details table too
                encryptAndStorePassword(userVm, password);
            }
            _vmDao.saveDetails(userVm);
        } else {
            throw new CloudRuntimeException("Failed to reset SSH Key for the virtual machine ");
        }
        return userVm;
    }

    private boolean resetVMSSHKeyInternal(final Long vmId, final String sshPublicKey, final String password) throws ResourceUnavailableException, InsufficientCapacityException {
        final Long userId = CallContext.current().getCallingUserId();
        final VMInstanceVO vmInstance = _vmDao.findById(vmId);

        final VMTemplateVO template = _templateDao.findByIdIncludingRemoved(vmInstance.getTemplateId());
        final Nic defaultNic = _networkModel.getDefaultNic(vmId);
        if (defaultNic == null) {
            s_logger.error("Unable to reset SSH Key for vm " + vmInstance + " as the instance doesn't have default nic");
            return false;
        }

        final Network defaultNetwork = _networkDao.findById(defaultNic.getNetworkId());
        final NicProfile defaultNicProfile =
                new NicProfile(defaultNic, defaultNetwork, null, null, null, _networkModel.getNetworkTag(template.getHypervisorType(), defaultNetwork));

        final VirtualMachineProfile vmProfile = new VirtualMachineProfileImpl(vmInstance);

        if (template.getEnablePassword()) {
            vmProfile.setParameter(VirtualMachineProfile.Param.VmPassword, password);
        }

        final UserDataServiceProvider element = _networkMgr.getSSHKeyResetProvider(defaultNetwork);
        if (element == null) {
            throw new CloudRuntimeException("Can't find network element for " + Service.UserData.getName() + " provider needed for SSH Key reset");
        }
        final boolean result = element.saveSSHKey(defaultNetwork, defaultNicProfile, vmProfile, sshPublicKey);

        // Need to reboot the virtual machine so that the password gets redownloaded from the DomR, and reset on the VM
        if (!result) {
            s_logger.debug("Failed to reset SSH Key for the virutal machine; no need to reboot the vm");
            return false;
        } else {
            if (vmInstance.getState() == State.Stopped) {
                s_logger.debug("Vm " + vmInstance + " is stopped, not rebooting it as a part of SSH Key reset");
                return true;
            }
            if (rebootVirtualMachine(userId, vmId) == null) {
                s_logger.warn("Failed to reboot the vm " + vmInstance);
                return false;
            } else {
                s_logger.debug("Vm " + vmInstance + " is rebooted successfully as a part of SSH Key reset");
                return true;
            }
        }
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_START, eventDescription = "starting Vm", async = true)
    public UserVm startVirtualMachine(final StartVMCmd cmd) throws ExecutionException, ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException {
        return startVirtualMachine(cmd.getId(), cmd.getHostId(), null, cmd.getDeploymentPlanner()).first();
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_REBOOT, eventDescription = "rebooting Vm", async = true)
    public UserVm rebootVirtualMachine(final RebootVMCmd cmd) throws InsufficientCapacityException, ResourceUnavailableException {
        final Account caller = CallContext.current().getCallingAccount();
        final Long vmId = cmd.getId();

        // Verify input parameters
        final UserVmVO vmInstance = _vmDao.findById(vmId);
        if (vmInstance == null) {
            throw new InvalidParameterValueException("unable to find a virtual machine with id " + vmId);
        }

        _accountMgr.checkAccess(caller, null, true, vmInstance);

        // If the VM is Volatile in nature, on reboot discard the VM's root disk and create a new root disk for it: by calling restoreVM
        final long serviceOfferingId = vmInstance.getServiceOfferingId();
        final ServiceOfferingVO offering = _serviceOfferingDao.findById(vmInstance.getId(), serviceOfferingId);
        if (offering != null && offering.getRemoved() == null) {
            if (offering.getVolatileVm()) {
                return restoreVMInternal(caller, vmInstance, null);
            }
        } else {
            throw new InvalidParameterValueException("Unable to find service offering: " + serviceOfferingId + " corresponding to the vm");
        }

        return rebootVirtualMachine(CallContext.current().getCallingUserId(), vmId);
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_UPDATE, eventDescription = "updating Vm")
    public UserVm updateVirtualMachine(final UpdateVMCmd cmd) throws ResourceUnavailableException, InsufficientCapacityException {
        final String displayName = cmd.getDisplayName();
        final String group = cmd.getGroup();
        final Boolean ha = cmd.getHaEnable();
        final Boolean isDisplayVm = cmd.getDisplayVm();
        final Long id = cmd.getId();
        final Long osTypeId = cmd.getOsTypeId();
        final String userData = cmd.getUserData();
        final Boolean isDynamicallyScalable = cmd.isDynamicallyScalable();
        final String hostName = cmd.getHostName();
        final Map<String, String> details = cmd.getDetails();
        final Account caller = CallContext.current().getCallingAccount();
        final OptimiseFor optimiseFor = cmd.getOptimiseFor();
        final String manufacturerString = cmd.getManufacturerString();
        final Boolean requiresRestart = cmd.getRequiresRestart();
        final MaintenancePolicy maintenancePolicy = cmd.getMaintenancePolicy();
        final Long bootMenuTimeout = cmd.getBootMenuTimeout();
        final String bootOrder = cmd.getBootOrder();
        final ComplianceStatus complianceStatus = cmd.getComplianceStatus();

        // Input validation and permission checks
        final UserVmVO vmInstance = _vmDao.findById(id);
        if (vmInstance == null) {
            throw new InvalidParameterValueException("unable to find virtual machine with id " + id);
        }

        _accountMgr.checkAccess(caller, null, true, vmInstance);

        //If the flag is specified and is changed
        if (isDisplayVm != null && isDisplayVm != vmInstance.isDisplayVm()) {

            //update vm
            vmInstance.setDisplayVm(isDisplayVm);

            // Resource limit changes
            final ServiceOffering offering = _serviceOfferingDao.findByIdIncludingRemoved(vmInstance.getServiceOfferingId());
            _resourceLimitMgr.changeResourceCount(vmInstance.getAccountId(), ResourceType.user_vm, isDisplayVm);
            _resourceLimitMgr.changeResourceCount(vmInstance.getAccountId(), ResourceType.cpu, isDisplayVm, new Long(offering.getCpu()));
            _resourceLimitMgr.changeResourceCount(vmInstance.getAccountId(), ResourceType.memory, isDisplayVm, new Long(offering.getRamSize()));

            // take care of the root volume as well.
            final List<VolumeVO> rootVols = _volsDao.findByInstanceAndType(id, VolumeType.ROOT);
            if (!rootVols.isEmpty()) {
                _volumeService.updateDisplay(rootVols.get(0), isDisplayVm);
            }

            // take care of the data volumes as well.
            final List<VolumeVO> dataVols = _volsDao.findByInstanceAndType(id, VolumeType.DATADISK);
            for (final Volume dataVol : dataVols) {
                _volumeService.updateDisplay(dataVol, isDisplayVm);
            }
        }

        if (details != null && !details.isEmpty()) {
            _vmDao.loadDetails(vmInstance);

            for (final Map.Entry<String, String> entry : details.entrySet()) {
                if (entry instanceof Map.Entry) {
                    vmInstance.setDetail(entry.getKey(), entry.getValue());
                }
            }
            _vmDao.saveDetails(vmInstance);
        }

        return updateVirtualMachine(id, displayName, group, ha, isDisplayVm, osTypeId, userData, isDynamicallyScalable, cmd.getHttpMethod(), cmd.getCustomId(), hostName,
                cmd.getInstanceName(), manufacturerString, optimiseFor, requiresRestart, maintenancePolicy, bootMenuTimeout, bootOrder, complianceStatus);
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_NIC_CREATE, eventDescription = "Creating Nic", async = true)
    public UserVm addNicToVirtualMachine(final AddNicToVMCmd cmd) throws InvalidParameterValueException, PermissionDeniedException, CloudRuntimeException {
        final Long vmId = cmd.getVmId();
        final Long networkId = cmd.getNetworkId();
        final String ipAddress = cmd.getIpAddress();
        final String macAddress = cmd.getMacAddress();
        final Account caller = CallContext.current().getCallingAccount();

        final UserVmVO vmInstance = _vmDao.findById(vmId);
        if (vmInstance == null) {
            throw new InvalidParameterValueException("unable to find a virtual machine with id " + vmId);
        }

        // Check that Vm does not have VM Snapshots
        if (_vmSnapshotDao.findByVm(vmId).size() > 0) {
            throw new InvalidParameterValueException("NIC cannot be added to VM with VM Snapshots");
        }

        final NetworkVO network = _networkDao.findById(networkId);
        if (network == null) {
            throw new InvalidParameterValueException("unable to find a network with id " + networkId);
        }

        // Root admin may plug anything, Domain admin is allowed to plug into the public network
        if (caller.getType() != Account.ACCOUNT_TYPE_ADMIN) {
            if (!(network.getGuestType() == GuestType.Shared && network.getAclType() == ACLType.Domain)
                    && !(network.getAclType() == ACLType.Account && network.getAccountId() == vmInstance.getAccountId())
                    && !(caller.getType() == Account.ACCOUNT_TYPE_DOMAIN_ADMIN && TrafficType.Public.equals(network.getTrafficType()))) {
                throw new InvalidParameterValueException("only shared network or isolated network with the same account_id can be added to vmId: " + vmId);
            }
        }

        final List<NicVO> allNics = _nicDao.listByVmId(vmInstance.getId());
        for (final NicVO nic : allNics) {
            if (nic.getNetworkId() == network.getId()) {
                throw new CloudRuntimeException("A NIC already exists for VM:" + vmInstance.getInstanceName() + " in network: " + network.getUuid());
            }
        }

        if (_nicDao.findByNetworkIdAndMacAddress(networkId, macAddress) != null) {
            throw new CloudRuntimeException("A NIC with this MAC-Address already exists for network: " + network.getUuid());
        }

        NicProfile profile = new NicProfile(ipAddress, null, macAddress);
        if (ipAddress != null) {
            if (!(NetUtils.isValidIp4(ipAddress) || NetUtils.isValidIp6(ipAddress))) {
                throw new InvalidParameterValueException("Invalid format for IP address parameter: " + ipAddress);
            }
        }

        // Perform permission check on VM
        _accountMgr.checkAccess(caller, null, true, vmInstance);

        // Verify that zone is not Basic
        final Zone zone = zoneRepository.findById(vmInstance.getDataCenterId()).orElse(null);
        if (zone.getNetworkType() == NetworkType.Basic) {
            throw new CloudRuntimeException("Zone " + vmInstance.getDataCenterId() + ", has a NetworkType of Basic. Can't add a new NIC to a VM on a Basic Network");
        }

        // Perform account permission check on network
        _accountMgr.checkAccess(caller, AccessType.UseEntry, false, network);

        //ensure network belongs in zone
        if (network.getDataCenterId() != vmInstance.getDataCenterId()) {
            throw new CloudRuntimeException(vmInstance + " is in zone:" + vmInstance.getDataCenterId() + " but " + network + " is in zone:" + network.getDataCenterId());
        }

        // Get all vms hostNames in the network
        final List<String> hostNames = _vmInstanceDao.listDistinctHostNames(network.getId());
        // verify that there are no duplicates, listDistictHostNames could return hostNames even if the NIC
        //in the network is removed, so also check if the NIC is present and then throw an exception.
        //This will also check if there are multiple nics of same vm in the network
        if (hostNames.contains(vmInstance.getHostName())) {
            for (final String hostName : hostNames) {
                final VMInstanceVO vm = _vmInstanceDao.findVMByHostName(hostName);
                if (_networkModel.getNicInNetwork(vm.getId(), network.getId()) != null && vm.getHostName().equals(vmInstance.getHostName())) {
                    throw new CloudRuntimeException(network + " already has a vm with host name: " + vmInstance.getHostName());
                }
            }
        }

        NicProfile guestNic = null;
        boolean cleanUp = true;

        try {
            guestNic = _itMgr.addVmToNetwork(vmInstance, network, profile);
            cleanUp = false;
        } catch (final ResourceUnavailableException e) {
            throw new CloudRuntimeException("Unable to add NIC to " + vmInstance + ": " + e);
        } catch (final InsufficientCapacityException e) {
            throw new CloudRuntimeException("Insufficient capacity when adding NIC to " + vmInstance + ": " + e);
        } catch (final ConcurrentOperationException e) {
            throw new CloudRuntimeException("Concurrent operations on adding NIC to " + vmInstance + ": " + e);
        } finally {
            if (cleanUp) {
                try {
                    _itMgr.removeVmFromNetwork(vmInstance, network, null);
                } catch (final ResourceUnavailableException e) {
                    throw new CloudRuntimeException("Error while cleaning up NIC " + e);
                }
            }
        }

        if (guestNic == null) {
            throw new CloudRuntimeException("Unable to add NIC to " + vmInstance);
        }
        CallContext.current().putContextParameter(Nic.class, guestNic.getUuid());
        s_logger.debug("Successful addition of " + network + " from " + vmInstance);
        return _vmDao.findById(vmInstance.getId());
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_NIC_DELETE, eventDescription = "Removing Nic", async = true)
    public UserVm removeNicFromVirtualMachine(final RemoveNicFromVMCmd cmd) throws InvalidParameterValueException, PermissionDeniedException, CloudRuntimeException {
        final Long vmId = cmd.getVmId();
        final Long nicId = cmd.getNicId();
        final Account caller = CallContext.current().getCallingAccount();

        final UserVmVO vmInstance = _vmDao.findById(vmId);
        if (vmInstance == null) {
            throw new InvalidParameterValueException("Unable to find a virtual machine with id " + vmId);
        }

        // Check that Vm does not have VM Snapshots
        if (_vmSnapshotDao.findByVm(vmId).size() > 0) {
            throw new InvalidParameterValueException("NIC cannot be removed from VM with VM Snapshots");
        }

        final NicVO nic = _nicDao.findById(nicId);
        if (nic == null) {
            throw new InvalidParameterValueException("Unable to find a nic with id " + nicId);
        }

        final NetworkVO network = _networkDao.findById(nic.getNetworkId());
        if (network == null) {
            throw new InvalidParameterValueException("Unable to find a network with id " + nic.getNetworkId());
        }

        // Perform permission check on VM
        _accountMgr.checkAccess(caller, null, true, vmInstance);

        // Verify that zone is not Basic
        final Zone zone = zoneRepository.findById(vmInstance.getDataCenterId()).orElse(null);
        if (zone.getNetworkType() == NetworkType.Basic) {
            throw new InvalidParameterValueException("Zone " + vmInstance.getDataCenterId() + ", has a NetworkType of Basic. Can't remove a NIC from a VM on a Basic Network");
        }

        // check to see if nic is attached to VM
        if (nic.getInstanceId() != vmId) {
            throw new InvalidParameterValueException(nic + " is not a nic on " + vmInstance);
        }

        // Perform account permission check on network
        _accountMgr.checkAccess(caller, AccessType.UseEntry, false, network);

        // don't delete default NIC on a user VM
        if (nic.isDefaultNic() && vmInstance.getType() == VirtualMachineType.User) {
            throw new InvalidParameterValueException("Unable to remove nic from " + vmInstance + " in " + network + ", nic is default.");
        }

        // if specified nic is associated with PF/LB/Static NAT
        if (_rulesMgr.listAssociatedRulesForGuestNic(nic).size() > 0) {
            throw new InvalidParameterValueException("Unable to remove nic from " + vmInstance + " in " + network + ", nic has associated Port forwarding or Load balancer or " +
                    "Static NAT rules.");
        }

        final boolean nicremoved;
        try {
            nicremoved = _itMgr.removeNicFromVm(vmInstance, nic);
        } catch (final ResourceUnavailableException e) {
            throw new CloudRuntimeException("Unable to remove " + network + " from " + vmInstance + ": " + e);
        } catch (final ConcurrentOperationException e) {
            throw new CloudRuntimeException("Concurrent operations on removing " + network + " from " + vmInstance + ": " + e);
        }

        if (!nicremoved) {
            throw new CloudRuntimeException("Unable to remove " + network + " from " + vmInstance);
        }

        s_logger.debug("Successful removal of " + network + " from " + vmInstance);
        return _vmDao.findById(vmInstance.getId());
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_NIC_UPDATE, eventDescription = "Creating Nic", async = true)
    public UserVm updateDefaultNicForVirtualMachine(final UpdateDefaultNicForVMCmd cmd) throws InvalidParameterValueException, CloudRuntimeException {
        final Long vmId = cmd.getVmId();
        final Long nicId = cmd.getNicId();
        final Account caller = CallContext.current().getCallingAccount();

        final UserVmVO vmInstance = _vmDao.findById(vmId);
        if (vmInstance == null) {
            throw new InvalidParameterValueException("unable to find a virtual machine with id " + vmId);
        }

        // Check that Vm does not have VM Snapshots
        if (_vmSnapshotDao.findByVm(vmId).size() > 0) {
            throw new InvalidParameterValueException("NIC cannot be updated for VM with VM Snapshots");
        }

        NicVO nic = _nicDao.findById(nicId);
        if (nic == null) {
            throw new InvalidParameterValueException("unable to find a nic with id " + nicId);
        }
        final NetworkVO network = _networkDao.findById(nic.getNetworkId());
        if (network == null) {
            throw new InvalidParameterValueException("unable to find a network with id " + nic.getNetworkId());
        }

        // Perform permission check on VM
        _accountMgr.checkAccess(caller, null, true, vmInstance);

        // Verify that zone is not Basic
        final Zone zone = zoneRepository.findById(vmInstance.getDataCenterId()).orElse(null);
        if (zone.getNetworkType() == NetworkType.Basic) {
            throw new CloudRuntimeException("Zone " + vmInstance.getDataCenterId() + ", has a NetworkType of Basic. Can't change default NIC on a Basic Network");
        }

        // no need to check permissions for network, we'll enumerate the ones they already have access to
        final Network existingdefaultnet = _networkModel.getDefaultNetworkForVm(vmId);

        //check to see if nic is attached to VM
        if (nic.getInstanceId() != vmId) {
            throw new InvalidParameterValueException(nic + " is not a nic on  " + vmInstance);
        }
        // if current default equals chosen new default, Throw an exception
        if (nic.isDefaultNic()) {
            throw new CloudRuntimeException("refusing to set default nic because chosen nic is already the default");
        }

        //make sure the VM is Running or Stopped
        if (vmInstance.getState() != State.Running && vmInstance.getState() != State.Stopped) {
            throw new CloudRuntimeException("refusing to set default " + vmInstance + " is not Running or Stopped");
        }

        NicProfile existing = null;
        final List<NicProfile> nicProfiles = _networkMgr.getNicProfiles(vmInstance);
        for (final NicProfile nicProfile : nicProfiles) {
            if (nicProfile.isDefaultNic() && existingdefaultnet != null && nicProfile.getNetworkId() == existingdefaultnet.getId()) {
                existing = nicProfile;
            }
        }

        if (existing == null) {
            s_logger.warn("Failed to update default nic, no nic profile found for existing default network");
            throw new CloudRuntimeException("Failed to find a nic profile for the existing default network. This is bad and probably means some sort of configuration corruption");
        }

        NicVO existingVO = _nicDao.findById(existing.id);

        nic.setDefaultNic(true);
        existingVO.setDefaultNic(false);

        nic = _nicDao.persist(nic);
        existingVO = _nicDao.persist(existingVO);

        Network newdefault = _networkModel.getDefaultNetworkForVm(vmId);

        if (newdefault == null) {
            nic.setDefaultNic(false);
            existingVO.setDefaultNic(true);

            nic = _nicDao.persist(nic);
            _nicDao.persist(existingVO);

            newdefault = _networkModel.getDefaultNetworkForVm(vmId);
            if (newdefault.getId() == existingdefaultnet.getId()) {
                throw new CloudRuntimeException("Setting a default nic failed, and we had no default nic, but we were able to set it back to the original");
            }
            throw new CloudRuntimeException("Failed to change default nic to " + nic + " and now we have no default");
        } else if (newdefault.getId() == nic.getNetworkId()) {
            s_logger.debug("successfully set default network to " + network + " for " + vmInstance);
            return _vmDao.findById(vmInstance.getId());
        }

        throw new CloudRuntimeException("something strange happened, new default network(" + newdefault.getId() + ") is not null, and is not equal to the network("
                + nic.getNetworkId() + ") of the chosen nic");
    }

    @Override
    public UserVm updateNicIpForVirtualMachine(final UpdateVmNicIpCmd cmd) {
        final Long nicId = cmd.getNicId();
        String ipaddr = cmd.getIpaddress();
        final Account caller = CallContext.current().getCallingAccount();

        //check whether the nic belongs to user vm.
        final NicVO nicVO = _nicDao.findById(nicId);
        if (nicVO == null) {
            throw new InvalidParameterValueException("There is no nic for the " + nicId);
        }

        if (nicVO.getVmType() != VirtualMachineType.User) {
            throw new InvalidParameterValueException("The nic is not belongs to user vm");
        }

        final UserVm vm = _vmDao.findById(nicVO.getInstanceId());
        if (vm == null) {
            throw new InvalidParameterValueException("There is no vm with the nic");
        }

        final Network network = _networkDao.findById(nicVO.getNetworkId());
        if (network == null) {
            throw new InvalidParameterValueException("There is no network with the nic");
        }
        // Don't allow to update vm nic ip if network is not in Implemented/Setup/Allocated state
        if (!(network.getState() == Network.State.Allocated || network.getState() == Network.State.Implemented || network.getState() == Network.State.Setup)) {
            throw new InvalidParameterValueException("Network is not in the right state to update vm nic ip. Correct states are: " + Network.State.Allocated + ", " + Network
                    .State.Implemented + ", "
                    + Network.State.Setup);
        }

        final NetworkOfferingVO offering = _networkOfferingDao.findByIdIncludingRemoved(network.getNetworkOfferingId());
        if (offering == null) {
            throw new InvalidParameterValueException("There is no network offering with the network");
        }
        if (!_networkModel.listNetworkOfferingServices(offering.getId()).isEmpty() && vm.getState() != State.Stopped) {
            final InvalidParameterValueException ex = new InvalidParameterValueException(
                    "VM is not Stopped, unable to update the vm nic having the specified id");
            ex.addProxyObject(vm.getUuid(), "vmId");
            throw ex;
        }

        // verify permissions
        _accountMgr.checkAccess(caller, null, true, vm);
        final Account ipOwner = _accountDao.findByIdIncludingRemoved(vm.getAccountId());

        // verify ip address
        s_logger.debug("Calling the ip allocation ...");
        final Zone zone = zoneRepository.findById(network.getDataCenterId()).orElse(null);
        if (zone == null) {
            throw new InvalidParameterValueException("There is no dc with the nic");
        }
        if (zone.getNetworkType() == NetworkType.Advanced && (network.getGuestType() == GuestType.Isolated || network.getGuestType() == GuestType.Private)) {
            try {
                ipaddr = _ipAddrMgr.allocateGuestIP(network, ipaddr);
            } catch (final InsufficientAddressCapacityException e) {
                throw new InvalidParameterValueException("Allocating ip to guest nic " + nicVO.getUuid() + " failed, for insufficient address capacity");
            }
            if (ipaddr == null) {
                throw new InvalidParameterValueException("Allocating ip to guest nic " + nicVO.getUuid() + " failed, please choose another ip");
            }

            if (_networkModel.areServicesSupportedInNetwork(network.getId(), Service.StaticNat)) {
                final IPAddressVO oldIP = _ipAddressDao.findByAssociatedVmId(vm.getId());
                if (oldIP != null) {
                    oldIP.setVmIp(ipaddr);
                    _ipAddressDao.persist(oldIP);
                }
            }
            // implementing the network elements and resources as a part of vm nic ip update if network has services and it is in Implemented state
            if (!_networkModel.listNetworkOfferingServices(offering.getId()).isEmpty() && network.getState() == Network.State.Implemented) {
                final User callerUser = _accountMgr.getActiveUser(CallContext.current().getCallingUserId());
                final ReservationContext context = new ReservationContextImpl(null, null, callerUser, caller);
                final DeployDestination dest = new DeployDestination(zoneRepository.findById(network.getDataCenterId()).orElse(null), null, null, null);

                s_logger.debug("Implementing the network " + network + " elements and resources as a part of vm nic ip update");
                try {
                    // implement the network elements and rules again
                    _networkMgr.implementNetworkElementsAndResources(dest, context, network, offering);
                } catch (final Exception ex) {
                    s_logger.warn("Failed to implement network " + network + " elements and resources as a part of vm nic ip update due to ", ex);
                    final CloudRuntimeException e = new CloudRuntimeException("Failed to implement network (with specified id) elements and resources as a part of vm nic ip " +
                            "update");
                    e.addProxyObject(network.getUuid(), "networkId");
                    // restore to old ip address
                    if (_networkModel.areServicesSupportedInNetwork(network.getId(), Service.StaticNat)) {
                        final IPAddressVO oldIP = _ipAddressDao.findByAssociatedVmId(vm.getId());
                        if (oldIP != null) {
                            oldIP.setVmIp(nicVO.getIPv4Address());
                            _ipAddressDao.persist(oldIP);
                        }
                    }
                    throw e;
                }
            }
        } else if (zone.getNetworkType() == NetworkType.Basic || network.getGuestType() == GuestType.Shared) {
            //handle the basic networks here
            //for basic zone, need to provide the podId to ensure proper ip alloation
            Long podId = null;
            if (zone.getNetworkType() == NetworkType.Basic) {
                podId = vm.getPodIdToDeployIn();
                if (podId == null) {
                    throw new InvalidParameterValueException("vm pod id is null in Basic zone; can't decide the range for ip allocation");
                }
            }

            try {
                ipaddr = _ipAddrMgr.allocatePublicIpForGuestNic(network, podId, ipOwner, ipaddr);
                if (ipaddr == null) {
                    throw new InvalidParameterValueException("Allocating ip to guest nic " + nicVO.getUuid() + " failed, please choose another ip");
                }
                final IPAddressVO ip = _ipAddressDao.findByIpAndSourceNetworkId(nicVO.getNetworkId(), nicVO.getIPv4Address());
                if (ip != null) {
                    Transaction.execute(new TransactionCallbackNoReturn() {
                        @Override
                        public void doInTransactionWithoutResult(final TransactionStatus status) {
                            _ipAddrMgr.markIpAsUnavailable(ip.getId());
                            _ipAddressDao.unassignIpAddress(ip.getId());
                        }
                    });
                }
            } catch (final InsufficientAddressCapacityException e) {
                s_logger.error("Allocating ip to guest nic " + nicVO.getUuid() + " failed, for insufficient address capacity");
                return null;
            }
        } else {
            s_logger.error("UpdateVmNicIpCmd is not supported in this network...");
            return null;
        }

        // update nic ipaddress
        nicVO.setIPv4Address(ipaddr);
        _nicDao.persist(nicVO);

        return vm;
    }

    @Override
    @DB
    public UserVm recoverVirtualMachine(final RecoverVMCmd cmd) throws ResourceAllocationException, CloudRuntimeException {

        final Long vmId = cmd.getId();
        final Account caller = CallContext.current().getCallingAccount();
        final Long userId = caller.getAccountId();

        // Verify input parameters
        final UserVmVO vm = _vmDao.findById(vmId);

        if (vm == null) {
            throw new InvalidParameterValueException("unable to find a virtual machine with id " + vmId);
        }

        // When trying to expunge, permission is denied when the caller is not an admin and the AllowUserExpungeRecoverVm is false for the caller.
        if (!_accountMgr.isAdmin(userId) && !AllowUserExpungeRecoverVm.valueIn(userId)) {
            throw new PermissionDeniedException("Recovering a vm can only be done by an Admin. Or when the allow.user.expunge.recover.vm key is set.");
        }

        if (vm.getRemoved() != null) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Unable to find vm or vm is removed: " + vmId);
            }
            throw new InvalidParameterValueException("Unable to find vm by id " + vmId);
        }

        if (vm.getState() != State.Destroyed) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("vm is not in the right state: " + vmId);
            }
            throw new InvalidParameterValueException("Vm with id " + vmId + " is not in the right state");
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Recovering vm " + vmId);
        }

        Transaction.execute(new TransactionCallbackWithExceptionNoReturn<ResourceAllocationException>() {
            @Override
            public void doInTransactionWithoutResult(final TransactionStatus status) throws ResourceAllocationException {

                final Account account = _accountDao.lockRow(vm.getAccountId(), true);

                // if the account is deleted, throw error
                if (account.getRemoved() != null) {
                    throw new CloudRuntimeException("Unable to recover VM as the account is deleted");
                }

                // Get serviceOffering for Virtual Machine
                final ServiceOfferingVO serviceOffering = _serviceOfferingDao.findById(vm.getId(), vm.getServiceOfferingId());

                // First check that the maximum number of UserVMs, CPU and Memory limit for the given
                // accountId will not be exceeded
                resourceLimitCheck(account, vm.isDisplayVm(), new Long(serviceOffering.getCpu()), new Long(serviceOffering.getRamSize()));

                _haMgr.cancelDestroy(vm, vm.getHostId());

                try {
                    if (!_itMgr.stateTransitTo(vm, VirtualMachine.Event.RecoveryRequested, null)) {
                        s_logger.debug("Unable to recover the vm because it is not in the correct state: " + vmId);
                        throw new InvalidParameterValueException("Unable to recover the vm because it is not in the correct state: " + vmId);
                    }
                } catch (final NoTransitionException e) {
                    throw new InvalidParameterValueException("Unable to recover the vm because it is not in the correct state: " + vmId);
                }

                // Recover the VM's disks
                final List<VolumeVO> volumes = _volsDao.findByInstance(vmId);
                for (final VolumeVO volume : volumes) {
                    if (volume.getVolumeType().equals(VolumeType.ROOT)) {
                        // Create an event
                        final Long templateId = volume.getTemplateId();
                        final Long diskOfferingId = volume.getDiskOfferingId();
                        Long offeringId = null;
                        if (diskOfferingId != null) {
                            final DiskOfferingVO offering = _diskOfferingDao.findById(diskOfferingId);
                            if (offering != null && offering.getType() == DiskOfferingVO.Type.Disk) {
                                offeringId = offering.getId();
                            }
                        }
                    }
                }

                //Update Resource Count for the given account
                resourceCountIncrement(account.getId(), vm.isDisplayVm(), new Long(serviceOffering.getCpu()), new Long(serviceOffering.getRamSize()));
            }
        });

        return _vmDao.findById(vmId);
    }

    private void resourceLimitCheck(final Account owner, final Boolean displayVm, final Long cpu, final Long memory) throws ResourceAllocationException {
        _resourceLimitMgr.checkResourceLimit(owner, ResourceType.user_vm, displayVm);
        _resourceLimitMgr.checkResourceLimit(owner, ResourceType.cpu, displayVm, cpu);
        _resourceLimitMgr.checkResourceLimit(owner, ResourceType.memory, displayVm, memory);
    }

    private void resourceCountIncrement(final long accountId, final Boolean displayVm, final Long cpu, final Long memory) {
        _resourceLimitMgr.incrementResourceCount(accountId, ResourceType.user_vm, displayVm);
        _resourceLimitMgr.incrementResourceCount(accountId, ResourceType.cpu, displayVm, cpu);
        _resourceLimitMgr.incrementResourceCount(accountId, ResourceType.memory, displayVm, memory);
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_CREATE, eventDescription = "deploying Vm", create = true)
    public UserVm createAdvancedVirtualMachine(final Zone zone, final ServiceOffering serviceOffering, final VirtualMachineTemplate template, final List<Long>
            networkIdList, final Account owner, final String hostName, final String displayName, final Long diskOfferingId, final Long diskSize, final String group,
                                               final HypervisorType hypervisor, final HTTPMethod httpmethod, final String userData, final String sshKeyPair,
                                               final Map<Long, IpAddresses> requestedIps, final IpAddresses defaultIps, final Boolean displayvm, final String keyboard,
                                               final List<Long> affinityGroupIdList, final Map<String, String> customParametrs, final String customId,
                                               final DiskControllerType diskControllerType, final Long bootMenuTimeout, MaintenancePolicy maintenancePolicy,
                                               OptimiseFor optimiseFor, String manufacturerString, String bootOrder)
            throws InsufficientCapacityException, ConcurrentOperationException, ResourceUnavailableException, StorageUnavailableException, ResourceAllocationException {

        final Account caller = CallContext.current().getCallingAccount();
        final List<NetworkVO> networkList = new ArrayList<>();

        // Verify that caller can perform actions in behalf of vm owner
        _accountMgr.checkAccess(caller, null, true, owner);

        // Verify that owner can use the service offering
        _accountMgr.checkAccess(owner, serviceOffering);
        _accountMgr.checkAccess(owner, _diskOfferingDao.findById(diskOfferingId));

        checkHypervisorEnabled(zone, template);

        if (networkIdList == null || networkIdList.isEmpty()) {
            throw new InvalidParameterValueException("Please provide at least one network ID");
        }

        final List<HypervisorType> vpcSupportedHTypes = _vpcMgr.getSupportedVpcHypervisors();
        for (final Long networkId : networkIdList) {
            final NetworkVO network = _networkDao.findById(networkId);
            if (network == null) {
                throw new InvalidParameterValueException("Unable to find network by id " + networkIdList.get(0));
            }
            if (GuestType.Private.equals(network.getGuestType()) && !_accountMgr.isRootAdmin(caller.getId())) {
                throw new InvalidParameterValueException("Deploying VMs in a network of type " + GuestType.Private + " is not possible.");
            }
            if (network.getVpcId() != null) {
                // Only ISOs, XenServer, KVM and template types are
                // supported for vpc networks
                if (template.getFormat() != ImageFormat.ISO && !vpcSupportedHTypes.contains(template.getHypervisorType())) {
                    throw new InvalidParameterValueException("Can't create vm from template with hypervisor " + template.getHypervisorType() + " in vpc network " + network);
                } else if (template.getFormat() == ImageFormat.ISO && !vpcSupportedHTypes.contains(hypervisor)) {
                    // Only XenServer and KVM hypervisors are supported for vpc networks
                    throw new InvalidParameterValueException("Can't create vm of hypervisor type " + hypervisor + " in vpc network");
                }
            }

            _networkModel.checkNetworkPermissions(owner, network);

            // don't allow to use system networks
            final NetworkOffering networkOffering = _entityMgr.findById(NetworkOffering.class, network.getNetworkOfferingId());
            if (networkOffering.isSystemOnly()) {
                throw new InvalidParameterValueException("Network id=" + networkId + " is system only and can't be used for vm deployment");
            }
            networkList.add(network);
        }

        return createVirtualMachine(zone, serviceOffering, template, hostName, displayName, owner, diskOfferingId, diskSize, networkList, group, httpmethod, userData, sshKeyPair,
                hypervisor, caller, requestedIps, defaultIps, displayvm, keyboard, affinityGroupIdList, customParametrs, customId, diskControllerType, bootMenuTimeout,
                maintenancePolicy, optimiseFor, manufacturerString, bootOrder);
    }

    private void checkHypervisorEnabled(final Zone zone, final VirtualMachineTemplate template) {
        final boolean xenServerDeploymentsEnabled = _mgtserver.getXenserverDeploymentsEnabled();
        final boolean kvmDeploymentsEnabled = _mgtserver.getKvmDeploymentsEnabled();

        if (template.getHypervisorType() != null && template.getHypervisorType().equals(HypervisorType.XenServer) && !xenServerDeploymentsEnabled) {
            throw new CloudRuntimeException("Deploying to hypervisor " + HypervisorType.XenServer + " is disabled in zone " + zone.getName());
        }

        if (template.getHypervisorType() != null && template.getHypervisorType().equals(HypervisorType.KVM) && !kvmDeploymentsEnabled) {
            throw new CloudRuntimeException("Deploying to hypervisor " + HypervisorType.KVM + " is disabled in zone " + zone.getName());
        }
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_CREATE, eventDescription = "starting Vm", async = true)
    public UserVm startVirtualMachine(final DeployVMCmd cmd) throws ResourceUnavailableException, InsufficientCapacityException, ConcurrentOperationException {
        return startVirtualMachine(cmd, null, cmd.getDeploymentPlanner());
    }

    @Override
    @DB
    public InstanceGroupVO createVmGroup(final CreateVMGroupCmd cmd) {
        final Account caller = CallContext.current().getCallingAccount();
        final Long domainId = cmd.getDomainId();
        final String accountName = cmd.getAccountName();
        final String groupName = cmd.getGroupName();
        final Long projectId = cmd.getProjectId();

        final Account owner = _accountMgr.finalizeOwner(caller, accountName, domainId, projectId);
        final long accountId = owner.getId();

        // Check if name is already in use by this account
        final boolean isNameInUse = _vmGroupDao.isNameInUse(accountId, groupName);

        if (isNameInUse) {
            throw new InvalidParameterValueException("Unable to create vm group, a group with name " + groupName + " already exists for account " + accountId);
        }

        return createVmGroup(groupName, accountId);
    }

    @Override
    public boolean deleteVmGroup(final DeleteVMGroupCmd cmd) {
        final Account caller = CallContext.current().getCallingAccount();
        final Long groupId = cmd.getId();

        // Verify input parameters
        final InstanceGroupVO group = _vmGroupDao.findById(groupId);
        if (group == null || group.getRemoved() != null) {
            throw new InvalidParameterValueException("unable to find a vm group with id " + groupId);
        }

        _accountMgr.checkAccess(caller, null, true, group);

        return deleteVmGroup(groupId);
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_UPGRADE, eventDescription = "upgrading Vm")
    /*
     * TODO: cleanup eventually - Refactored API call
     */
    // This method will be deprecated as we use ScaleVMCmd for both stopped VMs and running VMs
    public UserVm upgradeVirtualMachine(final UpgradeVMCmd cmd) throws ResourceAllocationException {
        final Long vmId = cmd.getId();
        final Long svcOffId = cmd.getServiceOfferingId();
        final Account caller = CallContext.current().getCallingAccount();

        // Verify input parameters
        //UserVmVO vmInstance = _vmDao.findById(vmId);
        final VMInstanceVO vmInstance = _vmInstanceDao.findById(vmId);
        if (vmInstance == null) {
            throw new InvalidParameterValueException("unable to find a virtual machine with id " + vmId);
        } else if (!vmInstance.getState().equals(State.Stopped)) {
            throw new InvalidParameterValueException("Unable to upgrade virtual machine " + vmInstance.toString() + " " + " in state " + vmInstance.getState()
                    + "; make sure the virtual machine is stopped");
        }

        // If target VM has associated VM snapshots then don't allow upgrading of VM
        final List<VMSnapshotVO> vmSnapshots = _vmSnapshotDao.findByVm(vmId);
        if (vmSnapshots.size() > 0) {
            throw new InvalidParameterValueException("Unable to change service offering for VM, please remove VM snapshots before changing service offering of VM");
        }

        _accountMgr.checkAccess(caller, null, true, vmInstance);

        // Check resource limits for CPU and Memory.
        final Map<String, String> customParameters = cmd.getDetails();
        ServiceOfferingVO newServiceOffering = _offeringDao.findById(svcOffId);

        final ServiceOfferingVO currentServiceOffering = _offeringDao.findByIdIncludingRemoved(vmInstance.getId(), vmInstance.getServiceOfferingId());

        final int newCpu = newServiceOffering.getCpu();
        final int newMemory = newServiceOffering.getRamSize();
        final int currentCpu = currentServiceOffering.getCpu();
        final int currentMemory = currentServiceOffering.getRamSize();

        if (newCpu > currentCpu) {
            _resourceLimitMgr.checkResourceLimit(caller, ResourceType.cpu, newCpu - currentCpu);
        }
        if (newMemory > currentMemory) {
            _resourceLimitMgr.checkResourceLimit(caller, ResourceType.memory, newMemory - currentMemory);
        }

        // Check that the specified service offering ID is valid
        _itMgr.checkIfCanUpgrade(vmInstance, newServiceOffering);

        _itMgr.upgradeVmDb(vmId, svcOffId);

        // Increment or decrement CPU and Memory count accordingly.
        if (newCpu > currentCpu) {
            _resourceLimitMgr.incrementResourceCount(caller.getAccountId(), ResourceType.cpu, new Long(newCpu - currentCpu));
        } else if (currentCpu > newCpu) {
            _resourceLimitMgr.decrementResourceCount(caller.getAccountId(), ResourceType.cpu, new Long(currentCpu - newCpu));
        }
        if (newMemory > currentMemory) {
            _resourceLimitMgr.incrementResourceCount(caller.getAccountId(), ResourceType.memory, new Long(newMemory - currentMemory));
        } else if (currentMemory > newMemory) {
            _resourceLimitMgr.decrementResourceCount(caller.getAccountId(), ResourceType.memory, new Long(currentMemory - newMemory));
        }

        return _vmDao.findById(vmId);
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_STOP, eventDescription = "stopping Vm", async = true)
    public UserVm stopVirtualMachine(final long vmId, final boolean forced) throws ConcurrentOperationException {
        // Input validation
        final Account caller = CallContext.current().getCallingAccount();
        final Long userId = CallContext.current().getCallingUserId();

        // if account is removed, return error
        if (caller != null && caller.getRemoved() != null) {
            throw new PermissionDeniedException("The account " + caller.getUuid() + " is removed");
        }

        final UserVmVO vm = _vmDao.findById(vmId);
        if (vm == null) {
            throw new InvalidParameterValueException("unable to find a virtual machine with id " + vmId);
        }

        _userDao.findById(userId);
        final boolean status;
        try {
            final VirtualMachineEntity vmEntity = _orchSrvc.getVirtualMachine(vm.getUuid());

            if (forced || vm.getState() == State.Paused) {
                status = vmEntity.stopForced(Long.toString(userId));
            } else {
                status = vmEntity.stop(Long.toString(userId));
            }

            if (status) {
                return _vmDao.findById(vmId);
            } else {
                return null;
            }
        } catch (final ResourceUnavailableException e) {
            throw new CloudRuntimeException("Unable to contact the agent to stop the virtual machine " + vm, e);
        } catch (final CloudException e) {
            throw new CloudRuntimeException("Unable to contact the agent to stop the virtual machine " + vm, e);
        }
    }

    @Override
    public UserVm getUserVm(final long vmId) {
        return _vmDao.findById(vmId);
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_MIGRATE, eventDescription = "migrating VM", async = true)
    public VirtualMachine migrateVirtualMachine(final Long vmId, final Host destinationHost) throws ResourceUnavailableException, ConcurrentOperationException,
            ManagementServerException,
            VirtualMachineMigrationException {
        // access check - only root admin can migrate VM
        final Account caller = CallContext.current().getCallingAccount();
        if (!_accountMgr.isRootAdmin(caller.getId())) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Caller is not a root admin, permission denied to migrate the VM");
            }
            throw new PermissionDeniedException("No permission to migrate VM, Only Root Admin can migrate a VM!");
        }

        final VMInstanceVO vm = _vmInstanceDao.findById(vmId);
        if (vm == null) {
            throw new InvalidParameterValueException("Unable to find the VM by id=" + vmId);
        }
        // business logic
        if (vm.getState() != State.Running) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("VM is not Running, unable to migrate the vm " + vm);
            }
            final InvalidParameterValueException ex = new InvalidParameterValueException("VM is not Running, unable to migrate the vm with specified id");
            ex.addProxyObject(vm.getUuid(), "vmId");
            throw ex;
        }

        if (serviceOfferingDetailsDao.findDetail(vm.getServiceOfferingId(), GPU.Keys.pciDevice.toString()) != null) {
            throw new InvalidParameterValueException("Live Migration of GPU enabled VM is not supported");
        }

        if (!vm.getHypervisorType().equals(HypervisorType.XenServer) && !vm.getHypervisorType().equals(HypervisorType.KVM)) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug(vm + " is not XenServer/KVM, cannot migrate this VM.");
            }
            throw new InvalidParameterValueException("Unsupported Hypervisor Type for VM migration, we support XenServer/KVM only");
        }

        if (isVMUsingLocalStorage(vm)) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug(vm + " is using Local Storage, cannot migrate this VM.");
            }
            throw new InvalidParameterValueException("Unsupported operation, VM uses Local storage, cannot migrate");
        }

        // check if migrating to same host
        final long srcHostId = vm.getHostId();
        if (destinationHost.getId() == srcHostId) {
            throw new InvalidParameterValueException("Cannot migrate VM, VM is already presnt on this host, please specify valid destination host to migrate the VM");
        }

        // check if host is UP
        if (destinationHost.getState() != HostStatus.Up || destinationHost.getResourceState() != ResourceState.Enabled) {
            throw new InvalidParameterValueException("Cannot migrate VM, destination host is not in correct state, has status: " + destinationHost.getState() + ", state: "
                    + destinationHost.getResourceState());
        }

        if (vm.getType() != VirtualMachineType.User) {
            // for System VMs check that the destination host is within the same
            // cluster
            final HostVO srcHost = _hostDao.findById(srcHostId);
            if (srcHost != null && srcHost.getClusterId() != null && destinationHost.getClusterId() != null) {
                if (srcHost.getClusterId().longValue() != destinationHost.getClusterId().longValue()) {
                    throw new InvalidParameterValueException("Cannot migrate the VM, destination host is not in the same cluster as current host of the VM");
                }
            }
        }

        checkHostsDedication(vm, srcHostId, destinationHost.getId());

        // call to core process
        final Zone zone = zoneRepository.findById(destinationHost.getDataCenterId()).orElse(null);
        final HostPodVO pod = _podDao.findById(destinationHost.getPodId());
        final Cluster cluster = _clusterDao.findById(destinationHost.getClusterId());
        final DeployDestination dest = new DeployDestination(zone, pod, cluster, destinationHost);

        // check max guest vm limit for the destinationHost
        final HostVO destinationHostVO = _hostDao.findById(destinationHost.getId());
        if (_capacityMgr.checkIfHostReachMaxGuestLimit(destinationHostVO)) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Host name: " + destinationHost.getName() + ": " + destinationHost.getName()
                        + " already has max Running VMs(count includes system VMs), cannot migrate to this host");
            }
            throw new VirtualMachineMigrationException("Destination host: " + destinationHost.getName()
                    + " already has max Running VMs(count includes system VMs), cannot migrate to this host");
        }

        // Check host tags
        final ServiceOffering serviceOffering = _serviceOfferingDao.findById(vm.getServiceOfferingId());
        if (serviceOffering != null) {
            final String requiredHostTag = serviceOffering.getHostTag();
            final List<String> hostTags = _hostTagsDao.gethostTags(destinationHost.getId());

            boolean foundRequiredHostTag = false;
            for (final String hostTag : hostTags) {
                if (hostTag.equals(requiredHostTag)) {
                    foundRequiredHostTag = true;
                    break;
                }
            }
            if (!foundRequiredHostTag && requiredHostTag != null) {
                throw new VirtualMachineMigrationException("Destination host: " + destinationHost.getName() + " does not have required host tag " + requiredHostTag);
            }
        }

        final UserVmVO uservm = _vmDao.findById(vmId);
        if (uservm != null) {
            collectVmDiskStatistics(uservm);
        }
        _itMgr.migrate(vm.getUuid(), srcHostId, dest);
        final VMInstanceVO vmInstance = _vmInstanceDao.findById(vmId);
        if (vmInstance.getType().equals(VirtualMachineType.User)) {
            return _vmDao.findById(vmId);
        } else {
            return vmInstance;
        }
    }

    private boolean isVMUsingLocalStorage(final VMInstanceVO vm) {
        boolean usesLocalStorage = false;
        final ServiceOfferingVO svcOffering = _serviceOfferingDao.findById(vm.getId(), vm.getServiceOfferingId());
        if (svcOffering.getUseLocalStorage()) {
            usesLocalStorage = true;
        } else {
            final List<VolumeVO> volumes = _volsDao.findByInstanceAndType(vm.getId(), VolumeType.DATADISK);
            for (final VolumeVO vol : volumes) {
                final DiskOfferingVO diskOffering = _diskOfferingDao.findById(vol.getDiskOfferingId());
                if (diskOffering.getUseLocalStorage()) {
                    usesLocalStorage = true;
                    break;
                }
            }
        }
        return usesLocalStorage;
    }

    private void checkHostsDedication(final VMInstanceVO vm, final long srcHostId, final long destHostId) {
        final HostVO srcHost = _hostDao.findById(srcHostId);
        final HostVO destHost = _hostDao.findById(destHostId);
        final boolean srcExplDedicated = checkIfHostIsDedicated(srcHost);
        final boolean destExplDedicated = checkIfHostIsDedicated(destHost);
        //if srcHost is explicitly dedicated and destination Host is not
        if (srcExplDedicated && !destExplDedicated) {
            //raise an alert
            final String msg = "VM " + vm.getInstanceName() + " is being migrated from a explicitly dedicated host " + srcHost.getName() + " to non-dedicated host " + destHost
                    .getName();
            s_logger.info(msg);
        }
        //if srcHost is non dedicated but destination Host is explicitly dedicated
        if (!srcExplDedicated && destExplDedicated) {
            //raise an alert
            final String msg = "VM " + vm.getInstanceName() + " is being migrated from a non dedicated host " + srcHost.getName() + " to a explicitly dedicated host " + destHost
                    .getName();
            s_logger.info(msg);
        }

        // Don't allow it when hosts are dedicated to different account/domains
        if (srcExplDedicated && destExplDedicated) {
            if (!(accountOfDedicatedHost(srcHost) == null || accountOfDedicatedHost(srcHost).equals(accountOfDedicatedHost(destHost)))) {
                final String msg = "Cannot migrate VM " + vm.getInstanceName() + " from host " + srcHost.getName() + " explicitly dedicated to account " + accountOfDedicatedHost
                        (srcHost) + " to host "
                        + destHost.getName() + " explicitly dedicated to account " + accountOfDedicatedHost(destHost);
                s_logger.error(msg);
                throw new InvalidParameterValueException(msg);
            }
            if (!(domainOfDedicatedHost(srcHost) == null || domainOfDedicatedHost(srcHost).equals(domainOfDedicatedHost(destHost)))) {
                final Domain srcDomain = _domainDao.findById(domainOfDedicatedHost(srcHost));
                String srcDomainName = domainOfDedicatedHost(srcHost).toString();
                if (srcDomain != null) {
                    srcDomainName = srcDomain.getName();
                }
                final Domain destDomain = _domainDao.findById(domainOfDedicatedHost(destHost));
                String destDomainName = domainOfDedicatedHost(destHost).toString();
                if (destDomain != null) {
                    destDomainName = destDomain.getName();
                }
                final String msg = "Cannot migrate VM " + vm.getInstanceName() + " from host " + srcHost.getName() + " explicitly dedicated to domain " + srcDomainName + " to " +
                        "host "
                        + destHost.getName() + " explicitly dedicated to domain " + destDomainName;
                s_logger.error(msg);
                throw new InvalidParameterValueException(msg);
            }
        }

        // Checks for implicitly dedicated hosts
        final ServiceOfferingVO deployPlanner = _offeringDao.findById(vm.getId(), vm.getServiceOfferingId());
        if (deployPlanner.getDeploymentPlanner() != null && deployPlanner.getDeploymentPlanner().equals("ImplicitDedicationPlanner")) {
            //VM is deployed using implicit planner
            final long accountOfVm = vm.getAccountId();
            String msg = "VM of account " + accountOfVm + " with implicit deployment planner being migrated to host " + destHost.getName();
            //Get all vms on destination host
            boolean emptyDestination = false;
            final List<VMInstanceVO> vmsOnDest = getVmsOnHost(destHostId);
            if (vmsOnDest == null || vmsOnDest.isEmpty()) {
                emptyDestination = true;
            }

            if (!emptyDestination) {
                //Check if vm is deployed using strict implicit planner
                if (!isServiceOfferingUsingPlannerInPreferredMode(vm.getServiceOfferingId())) {
                    //Check if all vms on destination host are created using strict implicit mode
                    if (!checkIfAllVmsCreatedInStrictMode(accountOfVm, vmsOnDest)) {
                        msg = "VM of account " + accountOfVm + " with strict implicit deployment planner being migrated to host " + destHost.getName()
                                + " not having all vms strict implicitly dedicated to account " + accountOfVm;
                    }
                } else {
                    //If vm is deployed using preferred implicit planner, check if all vms on destination host must be
                    //using implicit planner and must belong to same account
                    for (final VMInstanceVO vmsDest : vmsOnDest) {
                        final ServiceOfferingVO destPlanner = _offeringDao.findById(vm.getId(), vmsDest.getServiceOfferingId());
                        if (!(destPlanner.getDeploymentPlanner() != null && destPlanner.getDeploymentPlanner().equals("ImplicitDedicationPlanner") && vmsDest.getAccountId() ==
                                accountOfVm)) {
                            msg = "VM of account " + accountOfVm + " with preffered implicit deployment planner being migrated to host " + destHost.getName()
                                    + " not having all vms implicitly dedicated to account " + accountOfVm;
                        }
                    }
                }
            }
            _alertMgr.sendAlert(AlertManager.AlertType.ALERT_TYPE_USERVM, vm.getDataCenterId(), vm.getPodIdToDeployIn(), msg, msg);
            s_logger.warn(msg);
        } else {
            //VM is not deployed using implicit planner, check if it migrated between dedicated hosts
            final List<PlannerHostReservationVO> reservedHosts = _plannerHostReservationDao.listAllDedicatedHosts();
            boolean srcImplDedicated = false;
            boolean destImplDedicated = false;
            final String msg;
            for (final PlannerHostReservationVO reservedHost : reservedHosts) {
                if (reservedHost.getHostId() == srcHostId) {
                    srcImplDedicated = true;
                }
                if (reservedHost.getHostId() == destHostId) {
                    destImplDedicated = true;
                }
            }
            if (srcImplDedicated) {
                if (destImplDedicated) {
                    msg = "VM is being migrated from implicitly dedicated host " + srcHost.getName() + " to another implicitly dedicated host " + destHost.getName();
                } else {
                    msg = "VM is being migrated from implicitly dedicated host " + srcHost.getName() + " to shared host " + destHost.getName();
                }
                _alertMgr.sendAlert(AlertManager.AlertType.ALERT_TYPE_USERVM, vm.getDataCenterId(), vm.getPodIdToDeployIn(), msg, msg);
                s_logger.warn(msg);
            } else {
                if (destImplDedicated) {
                    msg = "VM is being migrated from shared host " + srcHost.getName() + " to implicitly dedicated host " + destHost.getName();
                    _alertMgr.sendAlert(AlertManager.AlertType.ALERT_TYPE_USERVM, vm.getDataCenterId(), vm.getPodIdToDeployIn(), msg, msg);
                    s_logger.warn(msg);
                }
            }
        }
    }

    private boolean checkIfHostIsDedicated(final HostVO host) {
        final long hostId = host.getId();
        final DedicatedResourceVO dedicatedHost = _dedicatedDao.findByHostId(hostId);
        final DedicatedResourceVO dedicatedClusterOfHost = _dedicatedDao.findByClusterId(host.getClusterId());
        final DedicatedResourceVO dedicatedPodOfHost = _dedicatedDao.findByPodId(host.getPodId());
        if (dedicatedHost != null || dedicatedClusterOfHost != null || dedicatedPodOfHost != null) {
            return true;
        } else {
            return false;
        }
    }

    private Long accountOfDedicatedHost(final HostVO host) {
        final long hostId = host.getId();
        final DedicatedResourceVO dedicatedHost = _dedicatedDao.findByHostId(hostId);
        final DedicatedResourceVO dedicatedClusterOfHost = _dedicatedDao.findByClusterId(host.getClusterId());
        final DedicatedResourceVO dedicatedPodOfHost = _dedicatedDao.findByPodId(host.getPodId());
        if (dedicatedHost != null) {
            return dedicatedHost.getAccountId();
        }
        if (dedicatedClusterOfHost != null) {
            return dedicatedClusterOfHost.getAccountId();
        }
        if (dedicatedPodOfHost != null) {
            return dedicatedPodOfHost.getAccountId();
        }
        return null;
    }

    private Long domainOfDedicatedHost(final HostVO host) {
        final long hostId = host.getId();
        final DedicatedResourceVO dedicatedHost = _dedicatedDao.findByHostId(hostId);
        final DedicatedResourceVO dedicatedClusterOfHost = _dedicatedDao.findByClusterId(host.getClusterId());
        final DedicatedResourceVO dedicatedPodOfHost = _dedicatedDao.findByPodId(host.getPodId());
        if (dedicatedHost != null) {
            return dedicatedHost.getDomainId();
        }
        if (dedicatedClusterOfHost != null) {
            return dedicatedClusterOfHost.getDomainId();
        }
        if (dedicatedPodOfHost != null) {
            return dedicatedPodOfHost.getDomainId();
        }
        return null;
    }

    private List<VMInstanceVO> getVmsOnHost(final long hostId) {
        final List<VMInstanceVO> vms = _vmInstanceDao.listUpByHostId(hostId);
        final List<VMInstanceVO> vmsByLastHostId = _vmInstanceDao.listByLastHostId(hostId);
        if (vmsByLastHostId.size() > 0) {
            // check if any VMs are within skip.counting.hours, if yes we have to consider the host.
            for (final VMInstanceVO stoppedVM : vmsByLastHostId) {
                final long secondsSinceLastUpdate = (DateUtil.currentGMTTime().getTime() - stoppedVM.getUpdateTime().getTime()) / 1000;
                if (secondsSinceLastUpdate < capacityReleaseInterval) {
                    vms.add(stoppedVM);
                }
            }
        }

        return vms;
    }

    private boolean isServiceOfferingUsingPlannerInPreferredMode(final long serviceOfferingId) {
        boolean preferred = false;
        final Map<String, String> details = serviceOfferingDetailsDao.listDetailsKeyPairs(serviceOfferingId);
        if (details != null && !details.isEmpty()) {
            final String preferredAttribute = details.get("ImplicitDedicationMode");
            if (preferredAttribute != null && preferredAttribute.equals("Preferred")) {
                preferred = true;
            }
        }
        return preferred;
    }

    private boolean checkIfAllVmsCreatedInStrictMode(final Long accountId, final List<VMInstanceVO> allVmsOnHost) {
        boolean createdByImplicitStrict = true;
        if (allVmsOnHost.isEmpty()) {
            return false;
        }
        for (final VMInstanceVO vm : allVmsOnHost) {
            if (!isImplicitPlannerUsedByOffering(vm.getServiceOfferingId()) || vm.getAccountId() != accountId) {
                s_logger.info("Host " + vm.getHostId() + " found to be running a vm created by a planner other" + " than implicit, or running vms of other account");
                createdByImplicitStrict = false;
                break;
            } else if (isServiceOfferingUsingPlannerInPreferredMode(vm.getServiceOfferingId()) || vm.getAccountId() != accountId) {
                s_logger.info("Host " + vm.getHostId() + " found to be running a vm created by an implicit planner" + " in preferred mode, or running vms of other account");
                createdByImplicitStrict = false;
                break;
            }
        }
        return createdByImplicitStrict;
    }

    private boolean isImplicitPlannerUsedByOffering(final long offeringId) {
        boolean implicitPlannerUsed = false;
        final ServiceOfferingVO offering = _serviceOfferingDao.findByIdIncludingRemoved(offeringId);
        if (offering == null) {
            s_logger.error("Couldn't retrieve the offering by the given id : " + offeringId);
        } else {
            final String plannerName = offering.getDeploymentPlanner();
            if (plannerName != null) {
                if (plannerName.equals("ImplicitDedicationPlanner")) {
                    implicitPlannerUsed = true;
                }
            }
        }

        return implicitPlannerUsed;
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_MIGRATE, eventDescription = "migrating VM", async = true)
    public VirtualMachine migrateVirtualMachineWithVolume(final Long vmId, final Host destinationHost, final Map<String, String> volumeToPool) throws ResourceUnavailableException,
            ConcurrentOperationException, ManagementServerException, VirtualMachineMigrationException {
        // Access check - only root administrator can migrate VM.
        final Account caller = CallContext.current().getCallingAccount();
        if (!_accountMgr.isRootAdmin(caller.getId())) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Caller is not a root admin, permission denied to migrate the VM");
            }
            throw new PermissionDeniedException("No permission to migrate VM, Only Root Admin can migrate a VM!");
        }

        final VMInstanceVO vm = _vmInstanceDao.findById(vmId);
        if (vm == null) {
            throw new InvalidParameterValueException("Unable to find the vm by id " + vmId);
        }

        if (vm.getState() != State.Running) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("VM is not Running, unable to migrate the vm " + vm);
            }
            final CloudRuntimeException ex = new CloudRuntimeException("VM is not Running, unable to migrate the vm with" + " specified id");
            ex.addProxyObject(vm.getUuid(), "vmId");
            throw ex;
        }

        if (serviceOfferingDetailsDao.findDetail(vm.getServiceOfferingId(), GPU.Keys.pciDevice.toString()) != null) {
            throw new InvalidParameterValueException("Live Migration of GPU enabled VM is not supported");
        }

        if (!vm.getHypervisorType().equals(HypervisorType.XenServer) && !vm.getHypervisorType().equals(HypervisorType.KVM)) {
            throw new InvalidParameterValueException("Unsupported hypervisor type for vm migration, we support" + " XenServer/KVM only");
        }

        final long srcHostId = vm.getHostId();
        final Host srcHost = _resourceMgr.getHost(srcHostId);

        if (srcHost == null) {
            throw new InvalidParameterValueException("Cannot migrate VM, there is not Host with id: " + srcHostId);
        }

        // Check if src and destination hosts are valid and migrating to same host
        if (destinationHost.getId() == srcHostId) {
            throw new InvalidParameterValueException("Cannot migrate VM, VM is already present on this host, please" + " specify valid destination host to migrate the VM");
        }

        // Check if the source and destination hosts are of the same type and support storage motion.
        if (!srcHost.getHypervisorType().equals(destinationHost.getHypervisorType())) {
            throw new CloudRuntimeException("The source and destination hosts are not of the same type " + "Source hypervisor type and version: "
                    + srcHost.getHypervisorType().toString() + " " + srcHost.getHypervisorVersion() + ", Destination hypervisor type and version: "
                    + destinationHost.getHypervisorType().toString() + " " + destinationHost.getHypervisorVersion());
        }
        if (srcHost.getHypervisorVersion() != null && !srcHost.getHypervisorVersion().equals(destinationHost.getHypervisorVersion())) {
            throw new CloudRuntimeException("The source and destination hosts are not of the same version. " + "Source hypervisor type and version: "
                    + srcHost.getHypervisorType().toString() + " " + srcHost.getHypervisorVersion() + ", Destination hypervisor type and version: "
                    + destinationHost.getHypervisorType().toString() + " " + destinationHost.getHypervisorVersion());
        }

        final HypervisorCapabilitiesVO capabilities = _hypervisorCapabilitiesDao.findByHypervisorTypeAndVersion(srcHost.getHypervisorType(), srcHost.getHypervisorVersion());
        if (!capabilities.isStorageMotionSupported()) {
            throw new CloudRuntimeException("Migration with storage isn't supported on hypervisor " + srcHost.getHypervisorType() + " of version " + srcHost.getHypervisorVersion
                    ());
        }

        // Check if destination host is up.
        if (destinationHost.getState() != HostStatus.Up || destinationHost.getResourceState() != ResourceState.Enabled) {
            throw new CloudRuntimeException("Cannot migrate VM, destination host is not in correct state, has " + "status: " + destinationHost.getState() + ", state: "
                    + destinationHost.getResourceState());
        }

        final List<VolumeVO> vmVolumes = _volsDao.findUsableVolumesForInstance(vm.getId());
        final Map<Long, Long> volToPoolObjectMap = new HashMap<>();
        if (!isVMUsingLocalStorage(vm) && destinationHost.getClusterId().equals(srcHost.getClusterId())) {
            if (volumeToPool.isEmpty()) {
                // If the destination host is in the same cluster and volumes do not have to be migrated across pools
                // then fail the call. migrateVirtualMachine api should have been used.
                throw new InvalidParameterValueException("Migration of the vm " + vm + "from host " + srcHost + " to destination host " + destinationHost
                        + " doesn't involve migrating the volumes.");
            }
        }

        if (!volumeToPool.isEmpty()) {
            // Check if all the volumes and pools passed as parameters are valid.
            for (final Map.Entry<String, String> entry : volumeToPool.entrySet()) {
                final VolumeVO volume = _volsDao.findByUuid(entry.getKey());
                final StoragePoolVO pool = _storagePoolDao.findByUuid(entry.getValue());
                if (volume == null) {
                    throw new InvalidParameterValueException("There is no volume present with the given id " + entry.getKey());
                } else if (pool == null) {
                    throw new InvalidParameterValueException("There is no storage pool present with the given id " + entry.getValue());
                } else if (pool.isInMaintenance()) {
                    throw new InvalidParameterValueException("Cannot migrate volume " + volume + "to the destination storage pool " + pool.getName() +
                            " as the storage pool is in maintenance mode.");
                } else {
                    // Verify the volume given belongs to the vm.
                    if (!vmVolumes.contains(volume)) {
                        throw new InvalidParameterValueException("There volume " + volume + " doesn't belong to " + "the virtual machine " + vm + " that has to be migrated");
                    }
                    volToPoolObjectMap.put(volume.getId(), pool.getId());
                }
            }
        }

        // Check if all the volumes are in the correct state.
        for (final VolumeVO volume : vmVolumes) {
            if (volume.getState() != Volume.State.Ready) {
                throw new CloudRuntimeException("Volume " + volume + " of the VM is not in Ready state. Cannot " + "migrate the vm with its volumes.");
            }
        }

        // Check max guest vm limit for the destinationHost.
        final HostVO destinationHostVO = _hostDao.findById(destinationHost.getId());
        if (_capacityMgr.checkIfHostReachMaxGuestLimit(destinationHostVO)) {
            throw new VirtualMachineMigrationException("Host name: " + destinationHost.getName() + ", hostId: " + destinationHost.getId()
                    + " already has max running vms (count includes system VMs). Cannot" + " migrate to this host");
        }

        checkHostsDedication(vm, srcHostId, destinationHost.getId());

        _itMgr.migrateWithStorage(vm.getUuid(), srcHostId, destinationHost.getId(), volToPoolObjectMap);
        return _vmDao.findById(vm.getId());
    }

    @DB
    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_MOVE, eventDescription = "move VM to another user", async = false)
    public UserVm moveVMToUser(final AssignVMCmd cmd) throws ResourceAllocationException, ConcurrentOperationException, ResourceUnavailableException,
            InsufficientCapacityException {
        // VERIFICATIONS and VALIDATIONS

        // VV 1: verify the two users
        final Account caller = CallContext.current().getCallingAccount();
        if (!_accountMgr.isRootAdmin(caller.getId())
                && !_accountMgr.isDomainAdmin(caller.getId())) { // only
            // root
            // admin
            // can
            // assign
            // VMs
            throw new InvalidParameterValueException("Only domain admins are allowed to assign VMs and not " + caller.getType());
        }

        // get and check the valid VM
        final UserVmVO vm = _vmDao.findById(cmd.getVmId());
        if (vm == null) {
            throw new InvalidParameterValueException("There is no vm by that id " + cmd.getVmId());
        } else if (vm.getState() == State.Running) { // VV 3: check if vm is
            // running
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("VM is Running, unable to move the vm " + vm);
            }
            final InvalidParameterValueException ex = new InvalidParameterValueException("VM is Running, unable to move the vm with specified vmId");
            ex.addProxyObject(vm.getUuid(), "vmId");
            throw ex;
        }

        final Account oldAccount = _accountService.getActiveAccountById(vm.getAccountId());
        if (oldAccount == null) {
            throw new InvalidParameterValueException("Invalid account for VM " + vm.getAccountId() + " in domain.");
        }
        // don't allow to move the vm from the project
        if (oldAccount.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            final InvalidParameterValueException ex = new InvalidParameterValueException("Specified Vm id belongs to the project and can't be moved");
            ex.addProxyObject(vm.getUuid(), "vmId");
            throw ex;
        }
        final Account newAccount = _accountService.getActiveAccountByName(cmd.getAccountName(), cmd.getDomainId());
        if (newAccount == null || newAccount.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            throw new InvalidParameterValueException("Invalid accountid=" + cmd.getAccountName() + " in domain " + cmd.getDomainId());
        }

        if (newAccount.getState() == Account.State.disabled) {
            throw new InvalidParameterValueException("The new account owner " + cmd.getAccountName() + " is disabled.");
        }

        //check caller has access to both the old and new account
        _accountMgr.checkAccess(caller, null, true, oldAccount);
        _accountMgr.checkAccess(caller, null, true, newAccount);

        // make sure the accounts are not same
        if (oldAccount.getAccountId() == newAccount.getAccountId()) {
            throw new InvalidParameterValueException("The new account is the same as the old account. Account id =" + oldAccount.getAccountId());
        }

        // don't allow to move the vm if there are existing PF/LB/Static Nat
        // rules, or vm is assigned to static Nat ip
        final List<PortForwardingRuleVO> pfrules = _portForwardingDao.listByVm(cmd.getVmId());
        if (pfrules != null && pfrules.size() > 0) {
            throw new InvalidParameterValueException("Remove the Port forwarding rules for this VM before assigning to another user.");
        }
        final List<FirewallRuleVO> snrules = _rulesDao.listStaticNatByVmId(vm.getId());
        if (snrules != null && snrules.size() > 0) {
            throw new InvalidParameterValueException("Remove the StaticNat rules for this VM before assigning to another user.");
        }
        final List<LoadBalancerVMMapVO> maps = _loadBalancerVMMapDao.listByInstanceId(vm.getId());
        if (maps != null && maps.size() > 0) {
            throw new InvalidParameterValueException("Remove the load balancing rules for this VM before assigning to another user.");
        }
        // check for one on one nat
        final List<IPAddressVO> ips = _ipAddressDao.findAllByAssociatedVmId(cmd.getVmId());
        for (final IPAddressVO ip : ips) {
            if (ip.isOneToOneNat()) {
                throw new InvalidParameterValueException("Remove the one to one nat rule for this VM for ip " + ip.toString());
            }
        }

        final Zone zone = zoneRepository.findById(vm.getDataCenterId()).orElse(null);

        // Get serviceOffering and Volumes for Virtual Machine
        final ServiceOfferingVO offering = _serviceOfferingDao.findByIdIncludingRemoved(vm.getId(), vm.getServiceOfferingId());
        final List<VolumeVO> volumes = _volsDao.findByInstance(cmd.getVmId());

        //Remove vm from instance group
        removeInstanceFromInstanceGroup(cmd.getVmId());

        // VV 2: check if account/domain is with in resource limits to create a new vm
        resourceLimitCheck(newAccount, vm.isDisplayVm(), new Long(offering.getCpu()), new Long(offering.getRamSize()));

        // VV 3: check if volumes and primary storage space are with in resource limits
        _resourceLimitMgr.checkResourceLimit(newAccount, ResourceType.volume, _volsDao.findByInstance(cmd.getVmId()).size());
        Long totalVolumesSize = (long) 0;
        for (final VolumeVO volume : volumes) {
            totalVolumesSize += volume.getSize();
        }
        _resourceLimitMgr.checkResourceLimit(newAccount, ResourceType.primary_storage, totalVolumesSize);

        // VV 4: Check if new owner can use the vm template
        final VirtualMachineTemplate template = _templateDao.findById(vm.getTemplateId());
        if (!template.isPublicTemplate()) {
            final Account templateOwner = _accountMgr.getAccount(template.getAccountId());
            _accountMgr.checkAccess(newAccount, null, true, templateOwner);
        }

        // VV 5: check the new account can create vm in the domain
        final DomainVO domain = _domainDao.findById(cmd.getDomainId());
        _accountMgr.checkAccess(newAccount, domain);

        Transaction.execute(new TransactionCallbackNoReturn() {
            @Override
            public void doInTransactionWithoutResult(final TransactionStatus status) {
                // update resource counts for old account
                resourceCountDecrement(oldAccount.getAccountId(), vm.isDisplayVm(), new Long(offering.getCpu()), new Long(offering.getRamSize()));

                // OWNERSHIP STEP 1: update the vm owner
                vm.setAccountId(newAccount.getAccountId());
                vm.setDomainId(cmd.getDomainId());
                _vmDao.persist(vm);

                // OS 2: update volume
                for (final VolumeVO volume : volumes) {
                    _resourceLimitMgr.decrementResourceCount(oldAccount.getAccountId(), ResourceType.volume);
                    _resourceLimitMgr.decrementResourceCount(oldAccount.getAccountId(), ResourceType.primary_storage, new Long(volume.getSize()));
                    volume.setAccountId(newAccount.getAccountId());
                    volume.setDomainId(newAccount.getDomainId());
                    _volsDao.persist(volume);
                    _resourceLimitMgr.incrementResourceCount(newAccount.getAccountId(), ResourceType.volume);
                    _resourceLimitMgr.incrementResourceCount(newAccount.getAccountId(), ResourceType.primary_storage, new Long(volume.getSize()));
                    //snapshots: mark these removed in db
                    final List<SnapshotVO> snapshots = _snapshotDao.listByVolumeIdIncludingRemoved(volume.getId());
                    for (final SnapshotVO snapshot : snapshots) {
                        _snapshotDao.remove(snapshot.getId());
                    }
                }

                //update resource count of new account
                resourceCountIncrement(newAccount.getAccountId(), vm.isDisplayVm(), new Long(offering.getCpu()), new Long(offering.getRamSize()));
            }
        });

        final VirtualMachine vmoi = _itMgr.findById(vm.getId());
        final VirtualMachineProfileImpl vmOldProfile = new VirtualMachineProfileImpl(vmoi);

        // OS 3: update the network
        final List<Long> networkIdList = cmd.getNetworkIds();

        if (zone.getNetworkType() == NetworkType.Basic) {
            if (networkIdList != null && !networkIdList.isEmpty()) {
                throw new InvalidParameterValueException("Can't move vm with network Ids; this is a basic zone VM");
            }
            // cleanup the network for the oldOwner
            _networkMgr.cleanupNics(vmOldProfile);
            _networkMgr.expungeNics(vmOldProfile);
            // security groups will be recreated for the new account, when the
            // VM is started
            final List<NetworkVO> networkList = new ArrayList<>();

            // Get default guest network in Basic zone
            final Network defaultNetwork = _networkModel.getExclusiveGuestNetwork(zone.getId());

            if (defaultNetwork == null) {
                throw new InvalidParameterValueException("Unable to find a default network to start a vm");
            } else {
                networkList.add(_networkDao.findById(defaultNetwork.getId()));
            }

            final LinkedHashMap<Network, List<? extends NicProfile>> networks = new LinkedHashMap<>();
            final NicProfile profile = new NicProfile();
            profile.setDefaultNic(true);
            networks.put(networkList.get(0), new ArrayList<>(Arrays.asList(profile)));

            final VirtualMachine vmi = _itMgr.findById(vm.getId());
            final VirtualMachineProfileImpl vmProfile = new VirtualMachineProfileImpl(vmi);
            _networkMgr.allocate(vmProfile, networks);
        } else {
            // cleanup the network for the oldOwner
            _networkMgr.cleanupNics(vmOldProfile);
            _networkMgr.expungeNics(vmOldProfile);

            final Set<NetworkVO> applicableNetworks = new HashSet<>();

            if (networkIdList != null && !networkIdList.isEmpty()) {
                // add any additional networks
                for (final Long networkId : networkIdList) {
                    final NetworkVO network = _networkDao.findById(networkId);
                    if (network == null) {
                        final InvalidParameterValueException ex = new InvalidParameterValueException("Unable to find specified network id");
                        ex.addProxyObject(networkId.toString(), "networkId");
                        throw ex;
                    }

                    _networkModel.checkNetworkPermissions(newAccount, network);

                    // don't allow to use system networks
                    final NetworkOffering networkOffering = _entityMgr.findById(NetworkOffering.class, network.getNetworkOfferingId());
                    if (networkOffering.isSystemOnly()) {
                        final InvalidParameterValueException ex = new InvalidParameterValueException("Specified Network id is system only and can't be used for vm deployment");
                        ex.addProxyObject(network.getUuid(), "networkId");
                        throw ex;
                    }
                    applicableNetworks.add(network);
                }
            } else {
                final NetworkVO defaultNetwork;
                final List<NetworkOfferingVO> requiredOfferings = _networkOfferingDao.listByAvailability(Availability.Required, false);
                if (requiredOfferings.size() < 1) {
                    throw new InvalidParameterValueException("Unable to find network offering with availability=" + Availability.Required
                            + " to automatically create the network as a part of vm creation");
                }
                if (requiredOfferings.get(0).getState() == NetworkOffering.State.Enabled) {
                    // get Virtual networks
                    final List<? extends Network> virtualNetworks = _networkModel.listNetworksForAccount(newAccount.getId(), zone.getId(), GuestType.Isolated);
                    if (virtualNetworks.isEmpty()) {
                        final long physicalNetworkId = _networkModel.findPhysicalNetworkId(zone.getId(), requiredOfferings.get(0).getTags(), requiredOfferings.get(0)
                                                                                                                                                              .getTrafficType
                                                                                                                                                                      ());
                        // Validate physical network
                        final PhysicalNetwork physicalNetwork = _physicalNetworkDao.findById(physicalNetworkId);
                        if (physicalNetwork == null) {
                            throw new InvalidParameterValueException("Unable to find physical network with id: " + physicalNetworkId + " and tag: "
                                    + requiredOfferings.get(0).getTags());
                        }
                        s_logger.debug("Creating network for account " + newAccount + " from the network offering id=" + requiredOfferings.get(0).getId()
                                + " as a part of deployVM process");
                        Network newNetwork = _networkMgr.createGuestNetwork(requiredOfferings.get(0).getId(), newAccount.getAccountName() + "-network",
                                newAccount.getAccountName() + "-network", null, null, null, null, newAccount, null, physicalNetwork, zone.getId(), ACLType.Account, null, null,
                                null, null, true, null, null, null, null, null, null);
                        // if the network offering has persistent set to true, implement the network
                        if (requiredOfferings.get(0).getIsPersistent()) {
                            final DeployDestination dest = new DeployDestination(zone, null, null, null);
                            final UserVO callerUser = _userDao.findById(CallContext.current().getCallingUserId());
                            final Journal journal = new Journal.LogJournal("Implementing " + newNetwork, s_logger);
                            final ReservationContext context = new ReservationContextImpl(UUID.randomUUID().toString(), journal, callerUser, caller);
                            s_logger.debug("Implementing the network for account" + newNetwork + " as a part of" + " network provision for persistent networks");
                            try {
                                final Pair<? extends NetworkGuru, ? extends Network> implementedNetwork = _networkMgr.implementNetwork(newNetwork.getId(), dest, context);
                                if (implementedNetwork == null || implementedNetwork.first() == null) {
                                    s_logger.warn("Failed to implement the network " + newNetwork);
                                }
                                newNetwork = implementedNetwork.second();
                            } catch (final Exception ex) {
                                s_logger.warn("Failed to implement network " + newNetwork + " elements and"
                                        + " resources as a part of network provision for persistent network due to ", ex);
                                final CloudRuntimeException e = new CloudRuntimeException("Failed to implement network"
                                        + " (with specified id) elements and resources as a part of network provision");
                                e.addProxyObject(newNetwork.getUuid(), "networkId");
                                throw e;
                            }
                        }
                        defaultNetwork = _networkDao.findById(newNetwork.getId());
                    } else if (virtualNetworks.size() > 1) {
                        throw new InvalidParameterValueException("More than 1 default Isolated networks are found " + "for account " + newAccount
                                + "; please specify networkIds");
                    } else {
                        defaultNetwork = _networkDao.findById(virtualNetworks.get(0).getId());
                    }
                } else {
                    throw new InvalidParameterValueException("Required network offering id=" + requiredOfferings.get(0).getId() + " is not in " + NetworkOffering.State
                            .Enabled);
                }

                applicableNetworks.add(defaultNetwork);
            }

            // add the new nics
            final LinkedHashMap<Network, List<? extends NicProfile>> networks = new LinkedHashMap<>();
            int toggle = 0;
            for (final NetworkVO appNet : applicableNetworks) {
                final NicProfile defaultNic = new NicProfile();
                if (toggle == 0) {
                    defaultNic.setDefaultNic(true);
                    toggle++;
                }
                networks.put(appNet, new ArrayList<>(Arrays.asList(defaultNic)));
            }
            final VirtualMachine vmi = _itMgr.findById(vm.getId());
            final VirtualMachineProfileImpl vmProfile = new VirtualMachineProfileImpl(vmi);
            _networkMgr.allocate(vmProfile, networks);
            s_logger.debug("AssignVM: Advance virtual, adding networks no " + networks.size() + " to " + vm.getInstanceName());
        } // END IF ADVANCED
        s_logger.info("AssignVM: vm " + vm.getInstanceName() + " now belongs to account " + cmd.getAccountName());
        return vm;
    }

    @Override
    public VirtualMachine vmStorageMigration(final Long vmId, final StoragePool destPool) {
        // access check - only root admin can migrate VM
        final Account caller = CallContext.current().getCallingAccount();
        if (!_accountMgr.isRootAdmin(caller.getId())) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Caller is not a root admin, permission denied to migrate the VM");
            }
            throw new PermissionDeniedException("No permission to migrate VM, Only Root Admin can migrate a VM!");
        }

        final VMInstanceVO vm = _vmInstanceDao.findById(vmId);
        if (vm == null) {
            throw new InvalidParameterValueException("Unable to find the VM by id=" + vmId);
        }

        if (vm.getState() != State.Stopped) {
            final InvalidParameterValueException ex = new InvalidParameterValueException("VM is not Stopped, unable to migrate the vm having the specified id");
            ex.addProxyObject(vm.getUuid(), "vmId");
            throw ex;
        }

        if (vm.getType() != VirtualMachineType.User) {
            throw new InvalidParameterValueException("can only do storage migration on user vm");
        }

        final List<VolumeVO> vols = _volsDao.findByInstance(vm.getId());
        if (vols.size() > 1) {
            throw new InvalidParameterValueException("Data disks attached to the vm, can not migrate. Need to dettach data disks at first");
        }

        // Check that Vm does not have VM Snapshots
        if (_vmSnapshotDao.findByVm(vmId).size() > 0) {
            throw new InvalidParameterValueException("VM's disk cannot be migrated, please remove all the VM Snapshots for this VM");
        }

        HypervisorType destHypervisorType = destPool.getHypervisor();
        if (destHypervisorType == null) {
            destHypervisorType = _clusterDao.findById(
                    destPool.getClusterId()).getHypervisorType();
        }

        if (vm.getHypervisorType() != destHypervisorType) {
            throw new InvalidParameterValueException("hypervisor is not compatible: dest: " + destHypervisorType.toString() + ", vm: " + vm.getHypervisorType().toString());
        }
        _itMgr.storageMigration(vm.getUuid(), destPool);
        return _vmDao.findById(vm.getId());
    }

    @Override
    public UserVm restoreVM(final RestoreVMCmd cmd) throws InsufficientCapacityException, ResourceUnavailableException {
        // Input validation
        final Account caller = CallContext.current().getCallingAccount();

        final long vmId = cmd.getVmId();
        final Long newTemplateId = cmd.getTemplateId();

        final UserVmVO vm = _vmDao.findById(vmId);
        if (vm == null) {
            final InvalidParameterValueException ex = new InvalidParameterValueException("Cannot find VM with ID " + vmId);
            ex.addProxyObject(String.valueOf(vmId), "vmId");
            throw ex;
        }

        final Zone zone = zoneRepository.findById(vm.getDataCenterId()).orElse(null);
        final VirtualMachineTemplate template = _entityMgr.findByIdIncludingRemoved(VirtualMachineTemplate.class, vm.getTemplateId());

        checkHypervisorEnabled(zone, template);

        _accountMgr.checkAccess(caller, null, true, vm);

        return restoreVMInternal(caller, vm, newTemplateId);
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_UPGRADE, eventDescription = "Upgrading VM", async = true)
    public UserVm upgradeVirtualMachine(final ScaleVMCmd cmd) throws ResourceUnavailableException, ConcurrentOperationException, ManagementServerException,
            VirtualMachineMigrationException {

        final Long vmId = cmd.getId();
        final Long newServiceOfferingId = cmd.getServiceOfferingId();
        CallContext.current().setEventDetails("Vm Id: " + vmId);

        final boolean result = upgradeVirtualMachine(vmId, newServiceOfferingId, cmd.getDetails());
        if (result) {
            return _vmDao.findById(vmId);
        } else {
            throw new CloudRuntimeException("Failed to scale the VM");
        }
    }

    private UserVm upgradeStoppedVirtualMachine(final Long vmId, final Long svcOffId, final Map<String, String> customParameters) throws ResourceAllocationException {
        final Account caller = CallContext.current().getCallingAccount();

        // Verify input parameters
        //UserVmVO vmInstance = _vmDao.findById(vmId);
        final VMInstanceVO vmInstance = _vmInstanceDao.findById(vmId);
        if (vmInstance == null) {
            throw new InvalidParameterValueException("unable to find a virtual machine with id " + vmId);
        }

        _accountMgr.checkAccess(caller, null, true, vmInstance);

        // Check resource limits for CPU and Memory.
        ServiceOfferingVO newServiceOffering = _offeringDao.findById(svcOffId);

        final ServiceOfferingVO currentServiceOffering = _offeringDao.findByIdIncludingRemoved(vmInstance.getId(), vmInstance.getServiceOfferingId());

        final int newCpu = newServiceOffering.getCpu();
        final int newMemory = newServiceOffering.getRamSize();
        final int currentCpu = currentServiceOffering.getCpu();
        final int currentMemory = currentServiceOffering.getRamSize();

        if (newCpu > currentCpu) {
            _resourceLimitMgr.checkResourceLimit(caller, ResourceType.cpu, newCpu - currentCpu);
        }
        if (newMemory > currentMemory) {
            _resourceLimitMgr.checkResourceLimit(caller, ResourceType.memory, newMemory - currentMemory);
        }

        // Check that the specified service offering ID is valid
        _itMgr.checkIfCanUpgrade(vmInstance, newServiceOffering);

        // Check if the new service offering can be applied to vm instance
        final ServiceOffering newSvcOffering = _offeringDao.findById(svcOffId);
        final Account owner = _accountMgr.getActiveAccountById(vmInstance.getAccountId());
        _accountMgr.checkAccess(owner, newSvcOffering);

        _itMgr.upgradeVmDb(vmId, svcOffId);

        // Increment or decrement CPU and Memory count accordingly.
        if (newCpu > currentCpu) {
            _resourceLimitMgr.incrementResourceCount(caller.getAccountId(), ResourceType.cpu, new Long(newCpu - currentCpu));
        } else if (currentCpu > newCpu) {
            _resourceLimitMgr.decrementResourceCount(caller.getAccountId(), ResourceType.cpu, new Long(currentCpu - newCpu));
        }
        if (newMemory > currentMemory) {
            _resourceLimitMgr.incrementResourceCount(caller.getAccountId(), ResourceType.memory, new Long(newMemory - currentMemory));
        } else if (currentMemory > newMemory) {
            _resourceLimitMgr.decrementResourceCount(caller.getAccountId(), ResourceType.memory, new Long(currentMemory - newMemory));
        }

        return _vmDao.findById(vmInstance.getId());
    }

    private boolean upgradeRunningVirtualMachine(final Long vmId, final Long newServiceOfferingId, final Map<String, String> customParameters) throws ResourceUnavailableException,
            ConcurrentOperationException, ManagementServerException, VirtualMachineMigrationException {

        final Account caller = CallContext.current().getCallingAccount();
        VMInstanceVO vmInstance = _vmInstanceDao.findById(vmId);
        if (vmInstance.getHypervisorType() != HypervisorType.XenServer) {
            s_logger.info("Scaling the VM dynamically is not supported for VMs running on Hypervisor " + vmInstance.getHypervisorType());
            throw new InvalidParameterValueException("Scaling the VM dynamically is not supported for VMs running on Hypervisor " + vmInstance.getHypervisorType());
        }

        _accountMgr.checkAccess(caller, null, true, vmInstance);

        //Check if its a scale "up"
        ServiceOfferingVO newServiceOffering = _offeringDao.findById(newServiceOfferingId);

        // Check that the specified service offering ID is valid
        _itMgr.checkIfCanUpgrade(vmInstance, newServiceOffering);

        final ServiceOffering currentServiceOffering = _offeringDao.findByIdIncludingRemoved(vmInstance.getId(), vmInstance.getServiceOfferingId());
        final int newCpu = newServiceOffering.getCpu();
        final int newMemory = newServiceOffering.getRamSize();
        final int currentCpu = currentServiceOffering.getCpu();
        final int currentMemory = currentServiceOffering.getRamSize();
        final int memoryDiff = newMemory - currentMemory;
        final int cpuDiff = newCpu - currentCpu;

        // Don't allow to scale when (Any of the new values less than current values) OR (All current and new values are same)
        if (newMemory < currentMemory || newCpu < currentCpu || newMemory == currentMemory && newCpu == currentCpu) {
            throw new InvalidParameterValueException("Only scaling up the vm is supported, new service offering(cpu=" + newCpu + ",memory=," + newMemory
                    + ")" + " should have at least one value(cpu/ram) greater than old value and no resource value less than older(cpu=" + currentCpu
                    + ",memory=," + currentMemory + ")");
        }

        // Check resource limits
        if (newCpu > currentCpu) {
            _resourceLimitMgr.checkResourceLimit(caller, ResourceType.cpu, newCpu - currentCpu);
        }
        if (newMemory > currentMemory) {
            _resourceLimitMgr.checkResourceLimit(caller, ResourceType.memory, newMemory - currentMemory);
        }

        // Dynamically upgrade the running vms
        boolean success = false;
        if (vmInstance.getState().equals(State.Running)) {
            int retry = _scaleRetry;
            final ExcludeList excludes = new ExcludeList();

            // Check zone wide flag
            final boolean enableDynamicallyScaleVm = EnableDynamicallyScaleVm.valueIn(vmInstance.getDataCenterId());
            if (!enableDynamicallyScaleVm) {
                throw new PermissionDeniedException("Dynamically scaling virtual machines is disabled for this zone, please contact your admin");
            }

            // Check vm flag
            if (!vmInstance.isDynamicallyScalable()) {
                throw new CloudRuntimeException("Unable to Scale the vm: " + vmInstance.getUuid() + " as vm does not have tools to support dynamic scaling");
            }

            // Check disable threshold for cluster is not crossed
            final HostVO host = _hostDao.findById(vmInstance.getHostId());
            if (_capacityMgr.checkIfClusterCrossesThreshold(host.getClusterId(), cpuDiff, memoryDiff)) {
                throw new CloudRuntimeException("Unable to scale vm: " + vmInstance.getUuid() + " due to insufficient resources");
            }

            while (retry-- != 0) { // It's != so that it can match -1.
                try {
                    boolean existingHostHasCapacity = false;

                    // Increment CPU and Memory count accordingly.
                    if (newCpu > currentCpu) {
                        _resourceLimitMgr.incrementResourceCount(caller.getAccountId(), ResourceType.cpu, new Long(newCpu - currentCpu));
                    }

                    if (memoryDiff > 0) {
                        _resourceLimitMgr.incrementResourceCount(caller.getAccountId(), ResourceType.memory, new Long(memoryDiff));
                    }

                    // #1 Check existing host has capacity
                    if (!excludes.shouldAvoid(ApiDBUtils.findHostById(vmInstance.getHostId()))) {
                        existingHostHasCapacity = _capacityMgr.checkIfHostHasCpuCapability(vmInstance.getHostId(), newCpu)
                                && _capacityMgr.checkIfHostHasCapacity(vmInstance.getHostId(), cpuDiff, memoryDiff * 1024L * 1024L, false,
                                _capacityMgr.getClusterOverProvisioningFactor(host.getClusterId(), Capacity.CAPACITY_TYPE_CPU),
                                _capacityMgr.getClusterOverProvisioningFactor(host.getClusterId(), Capacity.CAPACITY_TYPE_MEMORY), false);
                        excludes.addHost(vmInstance.getHostId());
                    }

                    // #2 migrate the vm if host doesn't have capacity or is in avoid set
                    if (!existingHostHasCapacity) {
                        _itMgr.findHostAndMigrate(vmInstance.getUuid(), newServiceOfferingId, excludes);
                    }

                    // #3 scale the vm now
                    _itMgr.upgradeVmDb(vmId, newServiceOfferingId);

                    vmInstance = _vmInstanceDao.findById(vmId);
                    _itMgr.reConfigureVm(vmInstance.getUuid(), currentServiceOffering, existingHostHasCapacity);
                    success = true;

                    return success;
                } catch (final InsufficientCapacityException e) {
                    s_logger.warn("Received exception while scaling ", e);
                } catch (final ResourceUnavailableException e) {
                    s_logger.warn("Received exception while scaling ", e);
                } catch (final ConcurrentOperationException e) {
                    s_logger.warn("Received exception while scaling ", e);
                } catch (final Exception e) {
                    s_logger.warn("Received exception while scaling ", e);
                } finally {
                    if (!success) {
                        _itMgr.upgradeVmDb(vmId, currentServiceOffering.getId()); // rollback

                        // Decrement CPU and Memory count accordingly.
                        if (newCpu > currentCpu) {
                            _resourceLimitMgr.decrementResourceCount(caller.getAccountId(), ResourceType.cpu, new Long(newCpu - currentCpu));
                        }

                        if (memoryDiff > 0) {
                            _resourceLimitMgr.decrementResourceCount(caller.getAccountId(), ResourceType.memory, new Long(memoryDiff));
                        }
                    }
                }
            }
        }
        return success;
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_EXPUNGE, eventDescription = "expunging Vm", async = true)
    public UserVm expungeVm(final long vmId) throws ResourceUnavailableException, ConcurrentOperationException {
        final Account caller = CallContext.current().getCallingAccount();
        final Long userId = caller.getId();

        // Verify input parameters
        final UserVmVO vm = _vmDao.findById(vmId);
        if (vm == null) {
            final InvalidParameterValueException ex = new InvalidParameterValueException("Unable to find a virtual machine with specified vmId");
            ex.addProxyObject(String.valueOf(vmId), "vmId");
            throw ex;
        }

        if (vm.getRemoved() != null) {
            s_logger.trace("Vm id=" + vmId + " is already expunged");
            return vm;
        }

        if (!(vm.getState() == State.Destroyed || vm.getState() == State.Expunging || vm.getState() == State.Error)) {
            final CloudRuntimeException ex = new CloudRuntimeException("Please destroy vm with specified vmId before expunge");
            ex.addProxyObject(String.valueOf(vmId), "vmId");
            throw ex;
        }

        // When trying to expunge, permission is denied when the caller is not an admin and the AllowUserExpungeRecoverVm is false for the caller.
        if (!_accountMgr.isAdmin(userId) && !AllowUserExpungeRecoverVm.valueIn(userId)) {
            throw new PermissionDeniedException("Expunging a vm can only be done by an Admin. Or when the allow.user.expunge.recover.vm key is set.");
        }

        final boolean status;

        status = expunge(vm, userId, caller);
        if (status) {
            return _vmDao.findByIdIncludingRemoved(vmId);
        } else {
            final CloudRuntimeException ex = new CloudRuntimeException("Failed to expunge vm with specified vmId");
            ex.addProxyObject(String.valueOf(vmId), "vmId");
            throw ex;
        }
    }

    @Override
    public String getVmUserData(final long vmId) {
        final UserVmVO vm = _vmDao.findById(vmId);
        if (vm == null) {
            throw new InvalidParameterValueException("Unable to find virual machine with id " + vmId);
        }

        //check permissions
        _accountMgr.checkAccess(CallContext.current().getCallingAccount(), null, true, vm);
        return vm.getUserData();
    }

    public UserVm restoreVMInternal(final Account caller, UserVmVO vm, final Long newTemplateId) throws InsufficientCapacityException, ResourceUnavailableException {

        final Long userId = caller.getId();
        final Account owner = _accountDao.findById(vm.getAccountId());
        _userDao.findById(userId);
        final long vmId = vm.getId();
        boolean needRestart = false;

        // Input validation
        if (owner == null) {
            throw new InvalidParameterValueException("The owner of " + vm + " does not exist: " + vm.getAccountId());
        }

        if (owner.getState() == Account.State.disabled) {
            throw new PermissionDeniedException("The owner of " + vm + " is disabled: " + vm.getAccountId());
        }

        if (vm.getState() != VirtualMachine.State.Running && vm.getState() != VirtualMachine.State.Stopped) {
            throw new CloudRuntimeException("Vm " + vm.getUuid() + " currently in " + vm.getState() + " state, restore vm can only execute when VM in Running or Stopped");
        }

        if (vm.getState() == VirtualMachine.State.Running) {
            needRestart = true;
        }

        final List<VolumeVO> rootVols = _volsDao.findByInstanceAndType(vmId, VolumeType.ROOT);
        if (rootVols.isEmpty()) {
            final InvalidParameterValueException ex = new InvalidParameterValueException("Can not find root volume for VM " + vm.getUuid());
            ex.addProxyObject(vm.getUuid(), "vmId");
            throw ex;
        }
        if (rootVols.size() > 1) {
            final InvalidParameterValueException ex = new InvalidParameterValueException("There are " + rootVols.size() + " root volumes for VM " + vm.getUuid());
            ex.addProxyObject(vm.getUuid(), "vmId");
            throw ex;
        }
        final VolumeVO root = rootVols.get(0);
        if (!Volume.State.Allocated.equals(root.getState()) || newTemplateId != null) {
            Long templateId = root.getTemplateId();
            boolean isISO = false;
            if (templateId == null) {
                // Assuming that for a vm deployed using ISO, template ID is set to NULL
                isISO = true;
                templateId = vm.getIsoId();
            }

            // If target VM has associated VM snapshots then don't allow restore of VM
            final List<VMSnapshotVO> vmSnapshots = _vmSnapshotDao.findByVm(vmId);
            if (vmSnapshots.size() > 0) {
                throw new InvalidParameterValueException("Unable to restore VM, please remove VM snapshots before restoring VM");
            }

            final VMTemplateVO template;
            //newTemplateId can be either template or ISO id. In the following snippet based on the vm deployment (from template or ISO) it is handled accordingly
            if (newTemplateId != null) {
                template = _templateDao.findById(newTemplateId);
                _accountMgr.checkAccess(caller, null, true, template);
                if (isISO) {
                    if (!template.getFormat().equals(ImageFormat.ISO)) {
                        throw new InvalidParameterValueException("Invalid ISO id provided to restore the VM ");
                    }
                } else {
                    if (template.getFormat().equals(ImageFormat.ISO)) {
                        throw new InvalidParameterValueException("Invalid template id provided to restore the VM ");
                    }
                }
            } else {
                if (isISO && templateId == null) {
                    throw new CloudRuntimeException("Cannot restore the VM since there is no ISO attached to VM");
                }
                template = _templateDao.findById(templateId);
                if (template == null) {
                    final InvalidParameterValueException ex = new InvalidParameterValueException("Cannot find template/ISO for specified volumeid and vmId");
                    ex.addProxyObject(vm.getUuid(), "vmId");
                    ex.addProxyObject(root.getUuid(), "volumeId");
                    throw ex;
                }
            }
            final TemplateDataStoreVO tmplStore = _templateStoreDao.findByTemplateZoneReady(template.getId(), vm.getDataCenterId());
            if (tmplStore == null) {
                throw new InvalidParameterValueException("Cannot restore the vm as the template " + template.getUuid() + " isn't available in the zone");
            }

            if (needRestart) {
                try {
                    _itMgr.stop(vm.getUuid());
                } catch (final ResourceUnavailableException e) {
                    s_logger.debug("Stop vm " + vm.getUuid() + " failed", e);
                    final CloudRuntimeException ex = new CloudRuntimeException("Stop vm failed for specified vmId");
                    ex.addProxyObject(vm.getUuid(), "vmId");
                    throw ex;
                }
            }

            /* If new template/ISO is provided allocate a new volume from new template/ISO otherwise allocate new volume from original template/ISO */
            final Volume newVol;
            if (newTemplateId != null) {
                if (isISO) {
                    newVol = volumeMgr.allocateDuplicateVolume(root, null);
                    vm.setIsoId(newTemplateId);
                    vm.setGuestOSId(template.getGuestOSId());
                    vm.setTemplateId(newTemplateId);
                    _vmDao.update(vmId, vm);
                } else {
                    newVol = volumeMgr.allocateDuplicateVolume(root, newTemplateId);
                    vm.setGuestOSId(template.getGuestOSId());
                    vm.setTemplateId(newTemplateId);
                    _vmDao.update(vmId, vm);
                }
            } else {
                newVol = volumeMgr.allocateDuplicateVolume(root, null);
            }

            // 1. Save usage event and update resource count for user vm volumes
            _resourceLimitMgr.incrementResourceCount(newVol.getAccountId(), ResourceType.volume, newVol.isDisplay());
            _resourceLimitMgr.incrementResourceCount(newVol.getAccountId(), ResourceType.primary_storage, newVol.isDisplay(), new Long(newVol.getSize()));

            handleManagedStorage(vm, root);

            _volsDao.attachVolume(newVol.getId(), vmId, newVol.getDeviceId(), newVol.getDiskController());

            // Detach, destroy and create the usage event for the old root volume.
            _volsDao.detachVolume(root.getId());
            volumeMgr.destroyVolume(root);

            Map<VirtualMachineProfile.Param, Object> params = null;
            String password = null;

            if (template.getEnablePassword()) {
                password = _mgr.generateRandomPassword();
                final boolean result = resetVMPasswordInternal(vmId, password);
                if (result) {
                    vm.setPassword(password);
                    _vmDao.loadDetails(vm);
                    // update the password in vm_details table too
                    // Check if an SSH key pair was selected for the instance and if so
                    // use it to encrypt & save the vm password
                    encryptAndStorePassword(vm, password);
                } else {
                    throw new CloudRuntimeException("VM reset is completed but failed to reset password for the virtual machine ");
                }
            }

            if (needRestart) {
                try {
                    if (vm.getDetail("password") != null) {
                        params = new HashMap<>();
                        params.put(VirtualMachineProfile.Param.VmPassword, password);
                    }
                    _itMgr.start(vm.getUuid(), params);
                    vm = _vmDao.findById(vmId);
                    if (template.getEnablePassword()) {
                        // this value is not being sent to the backend; need only for api
                        // display purposes
                        vm.setPassword(password);
                        if (vm.isUpdateParameters()) {
                            vm.setUpdateParameters(false);
                            _vmDao.loadDetails(vm);
                            if (vm.getDetail("password") != null) {
                                _vmDetailsDao.remove(_vmDetailsDao.findDetail(vm.getId(), "password").getId());
                            }
                            _vmDao.update(vm.getId(), vm);
                        }
                    }
                } catch (final Exception e) {
                    s_logger.debug("Unable to start VM " + vm.getUuid(), e);
                    final CloudRuntimeException ex = new CloudRuntimeException("Unable to start VM with specified id" + e.getMessage());
                    ex.addProxyObject(vm.getUuid(), "vmId");
                    throw ex;
                }
            }
        }

        s_logger.debug("Restore VM " + vmId + " done successfully");
        return vm;
    }

    private void handleManagedStorage(final UserVmVO vm, final VolumeVO root) {
        if (Volume.State.Allocated.equals(root.getState())) {
            return;
        }
        final StoragePoolVO storagePool = _storagePoolDao.findById(root.getPoolId());

        if (storagePool != null && storagePool.isManaged()) {
            final Long hostId = vm.getHostId() != null ? vm.getHostId() : vm.getLastHostId();

            if (hostId != null) {
                final VolumeInfo volumeInfo = volFactory.getVolume(root.getId());
                final Host host = _hostDao.findById(hostId);

                final Command cmd;

                if (host.getHypervisorType() == HypervisorType.XenServer) {
                    final DiskTO disk = new DiskTO(volumeInfo.getTO(), root.getDeviceId(), root.getPath(), root.getVolumeType());

                    // it's OK in this case to send a detach command to the host for a root volume as this
                    // will simply lead to the SR that supports the root volume being removed
                    cmd = new DettachCommand(disk, vm.getInstanceName());

                    final DettachCommand detachCommand = (DettachCommand) cmd;

                    detachCommand.setManaged(true);

                    detachCommand.setStorageHost(storagePool.getHostAddress());
                    detachCommand.setStoragePort(storagePool.getPort());

                    detachCommand.set_iScsiName(root.get_iScsiName());
                } else {
                    throw new CloudRuntimeException("This hypervisor type is not supported on managed storage for this command.");
                }

                final Commands cmds = new Commands(Command.OnError.Stop);

                cmds.addCommand(cmd);

                try {
                    _agentMgr.send(hostId, cmds);
                } catch (final Exception ex) {
                    throw new CloudRuntimeException(ex.getMessage());
                }

                if (!cmds.isSuccessful()) {
                    for (final Answer answer : cmds.getAnswers()) {
                        if (!answer.getResult()) {
                            s_logger.warn("Failed to reset vm due to: " + answer.getDetails());

                            throw new CloudRuntimeException("Unable to reset " + vm + " due to " + answer.getDetails());
                        }
                    }
                }

                // root.getPoolId() should be null if the VM we are detaching the disk from has never been started before
                final DataStore dataStore = root.getPoolId() != null ? _dataStoreMgr.getDataStore(root.getPoolId(), DataStoreRole.Primary) : null;
                volumeMgr.revokeAccess(volFactory.getVolume(root.getId()), host, dataStore);
            }
        }
    }

    protected UserVm startVirtualMachine(final DeployVMCmd cmd, final Map<VirtualMachineProfile.Param, Object> additonalParams, final String deploymentPlannerToUse) throws
            ResourceUnavailableException,
            InsufficientCapacityException, ConcurrentOperationException {

        final long vmId = cmd.getEntityId();
        final Long hostId = cmd.getHostId();
        UserVmVO vm = _vmDao.findById(vmId);

        Pair<UserVmVO, Map<VirtualMachineProfile.Param, Object>> vmParamPair = null;
        try {
            vmParamPair = startVirtualMachine(vmId, hostId, additonalParams, deploymentPlannerToUse);
            vm = vmParamPair.first();

            // At this point VM should be in "Running" state
            final UserVmVO tmpVm = _vmDao.findById(vm.getId());
            if (!tmpVm.getState().equals(State.Running)) {
                // Some other thread changed state of VM, possibly vmsync
                s_logger.error("VM " + tmpVm + " unexpectedly went to " + tmpVm.getState() + " state");
                throw new ConcurrentOperationException("Failed to deploy VM " + vm);
            }
        } finally {
            updateVmStateForFailedVmCreation(vm.getId(), hostId);
        }

        // Check that the password was passed in and is valid
        final VMTemplateVO template = _templateDao.findByIdIncludingRemoved(vm.getTemplateId());
        if (template.getEnablePassword()) {
            // this value is not being sent to the backend; need only for api
            // display purposes
            vm.setPassword((String) vmParamPair.second().get(VirtualMachineProfile.Param.VmPassword));
        }

        return vm;
    }

    // used for vm transitioning to error state
    private void updateVmStateForFailedVmCreation(final Long vmId, final Long hostId) {

        final UserVmVO vm = _vmDao.findById(vmId);

        if (vm != null) {
            if (vm.getState().equals(State.Stopped)) {
                s_logger.debug("Destroying vm " + vm + " as it failed to create on Host with Id:" + hostId);
                try {
                    _itMgr.stateTransitTo(vm, VirtualMachine.Event.OperationFailedToError, null);
                } catch (final NoTransitionException e1) {
                    s_logger.warn(e1.getMessage());
                }
                // destroy associated volumes for vm in error state
                // get all volumes in non destroyed state
                final List<VolumeVO> volumesForThisVm = _volsDao.findUsableVolumesForInstance(vm.getId());
                for (final VolumeVO volume : volumesForThisVm) {
                    if (volume.getState() != Volume.State.Destroy) {
                        volumeMgr.destroyVolume(volume);
                    }
                }
                final String msg = "Failed to deploy Vm with Id: " + vmId + ", on Host with Id: " + hostId;
                _alertMgr.sendAlert(AlertManager.AlertType.ALERT_TYPE_USERVM, vm.getDataCenterId(), vm.getPodIdToDeployIn(), msg, msg);

                // Get serviceOffering for Virtual Machine
                final ServiceOfferingVO offering = _serviceOfferingDao.findById(vm.getId(), vm.getServiceOfferingId());

                // Update Resource Count for the given account
                resourceCountDecrement(vm.getAccountId(), vm.isDisplayVm(), new Long(offering.getCpu()), new Long(offering.getRamSize()));
            }
        }
    }

    @DB
    private UserVm createVirtualMachine(final Zone zone, final ServiceOffering serviceOffering, final VirtualMachineTemplate tmplt, String hostName, final String displayName,
                                        final Account owner,
                                        final Long diskOfferingId, final Long diskSize, final List<NetworkVO> networkList, final String group, final HTTPMethod httpmethod,
                                        final String userData,
                                        final String sshKeyPair, final HypervisorType hypervisor, final Account caller, final Map<Long, IpAddresses> requestedIps,
                                        final IpAddresses defaultIps,
                                        final Boolean isDisplayVm, final String keyboard, final List<Long> affinityGroupIdList, final Map<String, String> customParameters,
                                        final String customId,
                                        final DiskControllerType diskControllerType, final Long bootMenuTimeout, MaintenancePolicy maintenancePolicy, OptimiseFor optimiseFor,
                                        String manufacturerString, String bootOrder)
            throws InsufficientCapacityException, ResourceUnavailableException, ConcurrentOperationException, StorageUnavailableException, ResourceAllocationException {

        _accountMgr.checkAccess(caller, null, true, owner);

        if (owner.getState() == Account.State.disabled) {
            throw new PermissionDeniedException("The owner of vm to deploy is disabled: " + owner);
        }
        final VMTemplateVO template = _templateDao.findById(tmplt.getId());
        if (template != null) {
            _templateDao.loadDetails(template);
        }

        if (optimiseFor == null) {
            if (template.getOptimiseFor() != null) {
                optimiseFor = template.getOptimiseFor();
            } else {
                optimiseFor = OptimiseFor.Generic;
            }
        }

        if (manufacturerString == null) {
            manufacturerString = template.getManufacturerString();
        }

        if (maintenancePolicy == null) {
            maintenancePolicy = template.getMaintenancePolicy();
        }

        Boolean macLarning = template.getMacLearning();
        String cpuFlags = template.getCpuFlags();

        final long accountId = owner.getId();

        assert !(requestedIps != null && (defaultIps.getIp4Address() != null || defaultIps.getIp6Address() != null)) : "requestedIp list and defaultNetworkIp should never be " +
                "specified together";

        if (AllocationState.Disabled == zone.getAllocationState()
                && !_accountMgr.isRootAdmin(caller.getId())) {
            throw new PermissionDeniedException(
                    "Cannot perform this operation, Zone is currently disabled: "
                            + zone.getId());
        }

        // check if zone is dedicated
        final DedicatedResourceVO dedicatedZone = _dedicatedDao.findByZoneId(zone.getId());
        if (dedicatedZone != null) {
            final DomainVO domain = _domainDao.findById(dedicatedZone.getDomainId());
            if (domain == null) {
                throw new CloudRuntimeException("Unable to find the domain " + zone.getDomainId() + " for the zone: " + zone);
            }
            // check that caller can operate with domain
            _configMgr.checkZoneAccess(caller, zone);
            // check that vm owner can create vm in the domain
            _configMgr.checkZoneAccess(owner, zone);
        }

        ServiceOfferingVO offering = _serviceOfferingDao.findById(serviceOffering.getId());

        // check if account/domain is with in resource limits to create a new vm
        final boolean isIso = ImageFormat.ISO == template.getFormat();
        long size = 0;

        // custom root disk size, resizes base template to larger size
        if (customParameters.containsKey("rootdisksize")) {
            final Long rootDiskSize = NumbersUtil.parseLong(customParameters.get("rootdisksize"), -1);
            if (rootDiskSize <= 0) {
                throw new InvalidParameterValueException("Root disk size should be a positive number.");
            }
            size = rootDiskSize * GB_TO_BYTES;
        }

        if (diskOfferingId != null) {
            final DiskOfferingVO diskOffering = _diskOfferingDao.findById(diskOfferingId);
            if (diskOffering != null && diskOffering.isCustomized()) {
                if (diskSize == null) {
                    throw new InvalidParameterValueException("This disk offering requires a custom size specified");
                }
                final Long customDiskOfferingMaxSize = VolumeOrchestrationService.CustomDiskOfferingMaxSize.value();
                final Long customDiskOfferingMinSize = VolumeOrchestrationService.CustomDiskOfferingMinSize.value();
                if (diskSize < customDiskOfferingMinSize || diskSize > customDiskOfferingMaxSize) {
                    throw new InvalidParameterValueException("VM Creation failed. Volume size: " + diskSize + "GB is out of allowed range. Max: " + customDiskOfferingMaxSize
                            + " Min:" + customDiskOfferingMinSize);
                }
                size += diskSize * GB_TO_BYTES;
            }
            size += _diskOfferingDao.findById(diskOfferingId).getDiskSize();
        }
        resourceLimitCheck(owner, isDisplayVm, new Long(offering.getCpu()), new Long(offering.getRamSize()));

        _resourceLimitMgr.checkResourceLimit(owner, ResourceType.volume, isIso || diskOfferingId == null ? 1 : 2);
        _resourceLimitMgr.checkResourceLimit(owner, ResourceType.primary_storage, size);

        // check that the affinity groups exist
        if (affinityGroupIdList != null) {
            for (final Long affinityGroupId : affinityGroupIdList) {
                final AffinityGroupVO ag = _affinityGroupDao.findById(affinityGroupId);
                if (ag == null) {
                    throw new InvalidParameterValueException("Unable to find affinity group " + ag);
                } else if (!_affinityGroupService.isAffinityGroupProcessorAvailable(ag.getType())) {
                    throw new InvalidParameterValueException("Affinity group type is not supported for group: " + ag + " ,type: " + ag.getType()
                            + " , Please try again after removing the affinity group");
                } else {
                    // verify permissions
                    if (ag.getAclType() == ACLType.Domain) {
                        _accountMgr.checkAccess(caller, null, false, owner, ag);
                        // Root admin has access to both VM and AG by default,
                        // but
                        // make sure the owner of these entities is same
                        if (caller.getId() == Account.ACCOUNT_ID_SYSTEM || _accountMgr.isRootAdmin(caller.getId())) {
                            if (!_affinityGroupService.isAffinityGroupAvailableInDomain(ag.getId(), owner.getDomainId())) {
                                throw new PermissionDeniedException("Affinity Group " + ag + " does not belong to the VM's domain");
                            }
                        }
                    } else {
                        _accountMgr.checkAccess(caller, null, true, owner, ag);
                        // Root admin has access to both VM and AG by default,
                        // but
                        // make sure the owner of these entities is same
                        if (caller.getId() == Account.ACCOUNT_ID_SYSTEM || _accountMgr.isRootAdmin(caller.getId())) {
                            if (ag.getDomainId() != owner.getDomainId()) {
                                throw new PermissionDeniedException("Affinity Group " + ag + " does not belong to the VM's domain");
                            }
                        }
                    }
                }
            }
        }

        final HypervisorType hypervisorType;
        if (template.getHypervisorType() == null || template.getHypervisorType() == HypervisorType.None) {
            if (hypervisor == null || hypervisor == HypervisorType.None) {
                throw new InvalidParameterValueException("hypervisor parameter is needed to deploy VM or the hypervisor parameter value passed is invalid");
            }
            hypervisorType = hypervisor;
        } else {
            if (hypervisor != null && hypervisor != HypervisorType.None && hypervisor != template.getHypervisorType()) {
                throw new InvalidParameterValueException("Hypervisor passed to the deployVm call, is different from the hypervisor type of the template");
            }
            hypervisorType = template.getHypervisorType();
        }

        // check if we have available pools for vm deployment
        final long availablePools = _storagePoolDao.countPoolsByStatus(StoragePoolStatus.Up);
        if (availablePools < 1) {
            throw new StorageUnavailableException("There are no available pools in the UP state for vm deployment", -1);
        }

        if (template.getTemplateType().equals(TemplateType.SYSTEM)) {
            throw new InvalidParameterValueException("Unable to use system template " + template.getId() + " to deploy a user vm");
        }
        final List<VMTemplateZoneVO> listZoneTemplate = _templateZoneDao.listByZoneTemplate(zone.getId(), template.getId());
        if (listZoneTemplate == null || listZoneTemplate.isEmpty()) {
            throw new InvalidParameterValueException("The template " + template.getId() + " is not available for use");
        }

        if (isIso && !template.isBootable()) {
            throw new InvalidParameterValueException("Installing from ISO requires an ISO that is bootable: " + template.getId());
        }

        // Check templates permissions
        _accountMgr.checkAccess(owner, AccessType.UseEntry, false, template);

        // check if the user data is correct
        validateUserData(userData, httpmethod);

        // Find an SSH public key corresponding to the key pair name, if one is
        // given
        String sshPublicKey = null;
        if (sshKeyPair != null && !sshKeyPair.equals("")) {
            final SSHKeyPair pair = _sshKeyPairDao.findByName(owner.getAccountId(), owner.getDomainId(), sshKeyPair);
            if (pair == null) {
                throw new InvalidParameterValueException("A key pair with name '" + sshKeyPair + "' was not found.");
            }

            sshPublicKey = pair.getPublicKey();
        }

        final List<Pair<NetworkVO, NicProfile>> networks = new ArrayList<>();

        final LinkedHashMap<String, NicProfile> networkNicMap = new LinkedHashMap<>();

        short defaultNetworkNumber = 0;
        boolean vpcNetwork = false;
        for (final NetworkVO network : networkList) {
            if (network.getDataCenterId() != zone.getId()) {
                if (!network.isStrechedL2Network()) {
                    throw new InvalidParameterValueException("Network id=" + network.getId() +
                            " doesn't belong to zone " + zone.getId());
                }

                final NetworkOffering ntwkOffering = _networkOfferingDao.findById(network.getNetworkOfferingId());
                final Long physicalNetworkId = _networkModel.findPhysicalNetworkId(zone.getId(), ntwkOffering.getTags(),
                        ntwkOffering.getTrafficType());
                if (physicalNetworkId == null) {
                    throw new InvalidParameterValueException("Network in which is VM getting deployed could not be" +
                            " streched to the zone, as we could not find a valid physical network");
                }

                final String provider = _ntwkSrvcDao.getProviderForServiceInNetwork(network.getId(), Service.Connectivity);
                if (!_networkModel.isProviderEnabledInPhysicalNetwork(physicalNetworkId, provider)) {
                    throw new InvalidParameterValueException("Network in which is VM getting deployed could not be" +
                            " streched to the zone, as we could not find a valid physical network");
                }
            }

            //relax the check if the caller is admin account
            if (caller.getType() != Account.ACCOUNT_TYPE_ADMIN) {
                if (!(network.getGuestType() == GuestType.Shared && network.getAclType() == ACLType.Domain)
                        && !(network.getAclType() == ACLType.Account && network.getAccountId() == accountId)) {
                    throw new InvalidParameterValueException("only shared network or isolated network with the same account_id can be added to vm");
                }
            }

            IpAddresses requestedIpPair = null;
            if (requestedIps != null && !requestedIps.isEmpty()) {
                requestedIpPair = requestedIps.get(network.getId());
            }

            if (requestedIpPair == null) {
                requestedIpPair = new IpAddresses(null, null);
            } else {
                _networkModel.checkRequestedIpAddresses(network.getId(), requestedIpPair);
            }

            NicProfile profile = new NicProfile(requestedIpPair.getIp4Address(), requestedIpPair.getIp6Address(), requestedIpPair.getMacAddress());

            if (defaultNetworkNumber == 0) {
                defaultNetworkNumber++;
                // if user requested specific ip for default network, add it
                if (defaultIps.getIp4Address() != null || defaultIps.getIp6Address() != null) {
                    _networkModel.checkRequestedIpAddresses(network.getId(), defaultIps);
                    profile = new NicProfile(defaultIps.getIp4Address(), defaultIps.getIp6Address());
                } else if (defaultIps.getMacAddress() != null) {
                    profile = new NicProfile(null, null, defaultIps.getMacAddress());
                }

                profile.setDefaultNic(true);
                if (!_networkModel.areServicesSupportedInNetwork(network.getId(), new Service[]{Service.UserData})) {
                    if (userData != null && !userData.isEmpty()) {
                        throw new InvalidParameterValueException("Unable to deploy VM as UserData is provided while deploying the VM, but there is no support for " + Network
                                .Service.UserData.getName() + " service in the default network " + network.getId());
                    }

                    if (sshPublicKey != null && !sshPublicKey.isEmpty()) {
                        throw new InvalidParameterValueException("Unable to deploy VM as SSH keypair is provided while deploying the VM, but there is no support for " + Network
                                .Service.UserData.getName() + " service in the default network " + network.getId());
                    }

                    if (template.getEnablePassword()) {
                        throw new InvalidParameterValueException("Unable to deploy VM as template " + template.getId() + " is password enabled, but there is no support for " +
                                Network.Service.UserData.getName() + " service in the default network " + network.getId());
                    }
                }
            }

            networks.add(new Pair<>(network, profile));

            networkNicMap.put(network.getUuid(), profile);
        }

        // Verify network information - network default network has to be set;
        // and vm can't have more than one default network
        // This is a part of business logic because default network is required
        // by Agent Manager in order to configure default
        // gateway for the vm
        if (defaultNetworkNumber == 0) {
            throw new InvalidParameterValueException("At least 1 default network has to be specified for the vm");
        } else if (defaultNetworkNumber > 1) {
            throw new InvalidParameterValueException("Only 1 default network per vm is supported");
        }

        final long id = _vmDao.getNextInSequence(Long.class, "id");

        if (hostName != null) {
            // Check is hostName is RFC compliant
            checkNameForRFCCompliance(hostName);
        }

        final String instanceName;
        final String uuidName = _uuidMgr.generateUuid(UserVm.class, customId);
        if (hostName == null) {
            //Generate name using uuid and instance.name global config
            hostName = generateHostName(uuidName);
        }

        if (hostName != null) {
            // Check is hostName is RFC compliant
            checkNameForRFCCompliance(hostName);
        }
        instanceName = VirtualMachineName.getVmName(id, owner.getId(), _instance);

        // Check if VM with instanceName already exists.
        final VMInstanceVO vmObj = _vmInstanceDao.findVMByInstanceName(instanceName);
        if (vmObj != null && vmObj.getState() != VirtualMachine.State.Expunging) {
            throw new InvalidParameterValueException("There already exists a VM by the display name supplied");
        }

        checkIfHostNameUniqueInNtwkDomain(hostName, networkList);

        long userId = CallContext.current().getCallingUserId();
        if (CallContext.current().getCallingAccount().getId() != owner.getId()) {
            final List<UserVO> userVOs = _userDao.listByAccount(owner.getAccountId());
            if (!userVOs.isEmpty()) {
                userId = userVOs.get(0).getId();
            }
        }

        final UserVmVO vm = commitUserVm(zone, template, hostName, displayName, owner, diskOfferingId, diskSize, userData, caller, isDisplayVm, keyboard, accountId, userId,
                offering, isIso, sshPublicKey, networkNicMap, id, instanceName, uuidName, hypervisorType, customParameters, diskControllerType, manufacturerString, optimiseFor,
                macLarning, cpuFlags, maintenancePolicy, bootMenuTimeout, bootOrder);

        // Assign instance to the group
        try {
            if (group != null) {
                final boolean addToGroup = addInstanceToGroup(Long.valueOf(id), group);
                if (!addToGroup) {
                    throw new CloudRuntimeException("Unable to assign Vm to the group " + group);
                }
            }
        } catch (final Exception ex) {
            throw new CloudRuntimeException("Unable to assign Vm to the group " + group);
        }

        if (affinityGroupIdList != null && !affinityGroupIdList.isEmpty()) {
            _affinityGroupVMMapDao.updateMap(vm.getId(), affinityGroupIdList);
        }

        CallContext.current().putContextParameter(VirtualMachine.class, vm.getUuid());
        return vm;
    }

    private String generateHostName(final String uuidName) {
        return _instance + "-" + uuidName;
    }

    private UserVmVO commitUserVm(final Zone zone, final VirtualMachineTemplate template, final String hostName, final String displayName, final Account owner,
                                  final Long diskOfferingId, final Long diskSize, final String userData, final Account caller, final Boolean isDisplayVm, final String keyboard,
                                  final long accountId, final long userId, final ServiceOfferingVO offering, final boolean isIso, final String sshPublicKey,
                                  final LinkedHashMap<String, NicProfile> networkNicMap, final long id, final String instanceName, final String uuidName,
                                  final HypervisorType hypervisorType, final Map<String, String> customParameters, final DiskControllerType diskControllerType,
                                  final String manufacturerString, final OptimiseFor optimiseFor, final Boolean macLearning, final String cpuFlags,
                                  final MaintenancePolicy maintenancePolicy, final Long bootMenuTimeout, final String bootOrder)
            throws InsufficientCapacityException {
        return Transaction.execute(new TransactionCallbackWithException<UserVmVO, InsufficientCapacityException>() {
            @Override
            public UserVmVO doInTransaction(final TransactionStatus status) throws InsufficientCapacityException {
                final UserVmVO vm = new UserVmVO(id, instanceName, displayName, template.getId(), hypervisorType, template.getGuestOSId(),
                        offering.getOfferHA(), offering.getLimitCpuUse(), owner.getDomainId(), owner.getId(), userId, offering.getId(),
                        userData, hostName, diskOfferingId, manufacturerString, optimiseFor, macLearning, cpuFlags, maintenancePolicy, bootMenuTimeout, bootOrder);
                vm.setUuid(uuidName);
                vm.setDynamicallyScalable(template.isDynamicallyScalable());

                final Map<String, String> details = template.getDetails();
                if (details != null && !details.isEmpty()) {
                    vm.details.putAll(details);
                }

                if (sshPublicKey != null) {
                    vm.setDetail("SSH.PublicKey", sshPublicKey);
                }

                if (keyboard != null && !keyboard.isEmpty()) {
                    vm.setDetail(VmDetailConstants.KEYBOARD, keyboard);
                }

                if (isIso) {
                    vm.setIsoId(template.getId());
                }
                Long rootDiskSize = null;
                // custom root disk size, resizes base template to larger size
                if (customParameters.containsKey("rootdisksize")) {

                    rootDiskSize = Long.parseLong(customParameters.get("rootdisksize"));

                    // rootdisksize must be larger than template
                    final VMTemplateVO templateVO = _templateDao.findById(template.getId());
                    if (templateVO == null) {
                        throw new InvalidParameterValueException("Unable to look up template by id " + template.getId());
                    }

                    if (rootDiskSize << 30 < templateVO.getSize()) {
                        final Long templateVOSizeGB = templateVO.getSize() / GB_TO_BYTES;
                        throw new InvalidParameterValueException("unsupported: rootdisksize override is smaller than template size " + templateVO.getSize()
                                + "B (" + templateVOSizeGB + "GB)");
                    } else {
                        s_logger.debug("rootdisksize of " + (rootDiskSize << 30) + " was larger than template size of " + templateVO.getSize());
                    }

                    s_logger.debug("found root disk size of " + rootDiskSize);
                    customParameters.remove("rootdisksize");
                }

                if (isDisplayVm != null) {
                    vm.setDisplayVm(isDisplayVm);
                } else {
                    vm.setDisplayVm(true);
                }

                final long guestOSId = template.getGuestOSId();
                final GuestOSVO guestOS = _guestOSDao.findById(guestOSId);
                final long guestOSCategoryId = guestOS.getCategoryId();
                final GuestOSCategoryVO guestOSCategory = _guestOSCategoryDao.findById(guestOSCategoryId);

                // Set date and version we start this VM
                vm.setLastStartDateTime(getCurrentLocalDateTimeStamp());
                vm.setLastStartVersion(UserVmManagerImpl.class.getPackage().getImplementationVersion());

                _vmDao.persist(vm);
                for (final String key : customParameters.keySet()) {
                    vm.setDetail(key, customParameters.get(key));
                }
                _vmDao.saveDetails(vm);

                s_logger.debug("Allocating in the DB for vm");
                final DataCenterDeployment plan = new DataCenterDeployment(zone.getId());

                final List<String> computeTags = new ArrayList<>();
                computeTags.add(offering.getHostTag());

                final List<String> rootDiskTags = new ArrayList<>();
                rootDiskTags.add(offering.getTags());

                if (isIso) {
                    _orchSrvc.createVirtualMachineFromScratch(vm.getUuid(), Long.toString(owner.getAccountId()), vm.getIsoId().toString(), hostName, displayName,
                            hypervisorType.name(), guestOSCategory.getName(), offering.getCpu(), offering.getRamSize(), diskSize, computeTags, rootDiskTags,
                            networkNicMap, plan, diskControllerType);
                } else {
                    _orchSrvc.createVirtualMachine(vm.getUuid(), Long.toString(owner.getAccountId()), Long.toString(template.getId()), hostName, displayName, hypervisorType.name(),
                            offering.getCpu(), offering.getRamSize(), diskSize, computeTags, rootDiskTags, networkNicMap, plan, rootDiskSize, diskControllerType);
                }

                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Successfully allocated DB entry for " + vm);
                }
                CallContext.current().setEventDetails("Vm Id: " + vm.getId());

                //Update Resource Count for the given account
                resourceCountIncrement(accountId, isDisplayVm, new Long(offering.getCpu()), new Long(offering.getRamSize()));
                return vm;
            }
        });
    }

    private void validateUserData(final String userData, final HTTPMethod httpmethod) {
        byte[] decodedUserData = null;
        if (userData != null) {
            if (!Base64.isBase64(userData)) {
                throw new InvalidParameterValueException("User data is not base64 encoded");
            }
            // If GET, use 4K. If POST, support upto 32K.
            if (httpmethod.equals(HTTPMethod.GET)) {
                if (userData.length() >= MAX_HTTP_GET_LENGTH) {
                    throw new InvalidParameterValueException("User data is too long for an http GET request");
                }
                decodedUserData = Base64.decodeBase64(userData.getBytes());
                if (decodedUserData.length > MAX_HTTP_GET_LENGTH) {
                    throw new InvalidParameterValueException("User data is too long for GET request");
                }
            } else if (httpmethod.equals(HTTPMethod.POST)) {
                if (userData.length() >= MAX_HTTP_POST_LENGTH) {
                    throw new InvalidParameterValueException("User data is too long for an http POST request");
                }
                decodedUserData = Base64.decodeBase64(userData.getBytes());
                if (decodedUserData.length > MAX_HTTP_POST_LENGTH) {
                    throw new InvalidParameterValueException("User data is too long for POST request");
                }
            }

            if (decodedUserData == null || decodedUserData.length < 1) {
                throw new InvalidParameterValueException("User data is too short");
            }
        }
    }

    private void checkNameForRFCCompliance(final String name) {
        if (!NetUtils.verifyDomainNameLabel(name, true)) {
            throw new InvalidParameterValueException("Invalid name. Vm name can contain ASCII letters 'a' through 'z', the digits '0' through '9', "
                    + "and the hyphen ('-'), must be between 1 and 63 characters long, and can't start or end with \"-\" and can't start with digit");
        }
    }

    private void checkIfHostNameUniqueInNtwkDomain(final String hostName, final List<? extends Network> networkList) {
        // Check that hostName is unique in the network domain
        final Map<String, List<Long>> ntwkDomains = new HashMap<>();
        for (final Network network : networkList) {
            final String ntwkDomain = network.getNetworkDomain();
            if (!ntwkDomains.containsKey(ntwkDomain)) {
                final List<Long> ntwkIds = new ArrayList<>();
                ntwkIds.add(network.getId());
                ntwkDomains.put(ntwkDomain, ntwkIds);
            } else {
                final List<Long> ntwkIds = ntwkDomains.get(ntwkDomain);
                ntwkIds.add(network.getId());
                ntwkDomains.put(ntwkDomain, ntwkIds);
            }
        }

        for (final Entry<String, List<Long>> ntwkDomain : ntwkDomains.entrySet()) {
            for (final Long ntwkId : ntwkDomain.getValue()) {
                // * get all vms hostNames in the network
                final List<String> hostNames = _vmInstanceDao.listDistinctHostNames(ntwkId);
                // * verify that there are no duplicates
                if (hostNames.contains(hostName)) {
                    throw new InvalidParameterValueException("The vm with hostName " + hostName + " already exists in the network domain: " + ntwkDomain.getKey() + "; network="
                            + _networkModel.getNetwork(ntwkId));
                }
            }
        }
    }

    private boolean updateUserDataInternal(final UserVm vm) throws ResourceUnavailableException, InsufficientCapacityException {
        final VMTemplateVO template = _templateDao.findByIdIncludingRemoved(vm.getTemplateId());

        final List<? extends Nic> nics = _nicDao.listByVmId(vm.getId());
        if (nics == null || nics.isEmpty()) {
            s_logger.error("unable to find any nics for vm " + vm.getUuid());
            return false;
        }

        for (final Nic nic : nics) {
            final Network network = _networkDao.findById(nic.getNetworkId());
            final NicProfile nicProfile = new NicProfile(nic, network, null, null, null, _networkModel.getNetworkTag(
                    template.getHypervisorType(), network));

            final VirtualMachineProfile vmProfile = new VirtualMachineProfileImpl(vm);

            final UserDataServiceProvider element = _networkModel.getUserDataUpdateProvider(network);
            if (element == null) {
                throw new CloudRuntimeException("Can't find network element for " + Service.UserData.getName() + " provider needed for UserData update");
            }
            final boolean result = element.saveUserData(network, nicProfile, vmProfile);
            if (!result) {
                s_logger.error("Failed to update userdata for vm " + vm + " and nic " + nic);
            }
        }

        return true;
    }

    @DB
    private InstanceGroupVO createVmGroup(final String groupName, final long accountId) {
        Account account = null;
        try {
            account = _accountDao.acquireInLockTable(accountId); // to ensure
            // duplicate
            // vm group
            // names are
            // not
            // created.
            if (account == null) {
                s_logger.warn("Failed to acquire lock on account");
                return null;
            }
            InstanceGroupVO group = _vmGroupDao.findByAccountAndName(accountId, groupName);
            if (group == null) {
                group = new InstanceGroupVO(groupName, accountId);
                group = _vmGroupDao.persist(group);
            }
            return group;
        } finally {
            if (account != null) {
                _accountDao.releaseFromLockTable(accountId);
            }
        }
    }

    private boolean validPassword(final String password) {
        if (password == null || password.length() == 0) {
            return false;
        }
        for (int i = 0; i < password.length(); i++) {
            if (password.charAt(i) == ' ') {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean finalizeVirtualMachineProfile(final VirtualMachineProfile profile, final DeployDestination dest, final ReservationContext context) {
        final UserVmVO vm = _vmDao.findById(profile.getId());
        final Map<String, String> details = _vmDetailsDao.listDetailsKeyPairs(vm.getId());
        vm.setDetails(details);

        // add userdata info into vm profile
        final Nic defaultNic = _networkModel.getDefaultNic(vm.getId());
        if (defaultNic != null) {
            final Network network = _networkModel.getNetwork(defaultNic.getNetworkId());
            if (_networkModel.isSharedNetworkWithoutServices(network.getId())) {
                final String serviceOffering = _serviceOfferingDao.findByIdIncludingRemoved(vm.getId(), vm.getServiceOfferingId()).getDisplayText();
                final String zoneName = zoneRepository.findById(vm.getDataCenterId()).orElse(null).getName();
                final boolean isWindows = _guestOSCategoryDao.findById(_guestOSDao.findById(vm.getGuestOSId()).getCategoryId()).getName().equalsIgnoreCase("Windows");

                final List<String[]> vmData = _networkModel.generateVmData(vm.getUserData(), serviceOffering, zoneName, vm.getInstanceName(), vm.getId(),
                        (String) profile.getParameter(VirtualMachineProfile.Param.VmSshPubKey), (String) profile.getParameter(VirtualMachineProfile.Param.VmPassword), isWindows,
                        network);
                final String vmName = vm.getInstanceName();
                final String configDriveIsoRootFolder = "/tmp";
                final String isoFile = configDriveIsoRootFolder + "/" + vmName + "/configDrive/" + vmName + ".iso";
                profile.setVmData(vmData);
                profile.setConfigDriveLabel(VirtualMachineManager.VmConfigDriveLabel.value());
                profile.setConfigDriveIsoRootFolder(configDriveIsoRootFolder);
                profile.setConfigDriveIsoFile(isoFile);
            }
        }

        _templateMgr.prepareIsoForVmProfile(profile);
        return true;
    }

    @Override
    public boolean finalizeDeployment(final Commands cmds, final VirtualMachineProfile profile, final DeployDestination dest, final ReservationContext context) {
        final UserVmVO userVm = _vmDao.findById(profile.getId());
        final List<NicVO> nics = _nicDao.listByVmId(userVm.getId());
        for (final NicVO nic : nics) {
            final NetworkVO network = _networkDao.findById(nic.getNetworkId());
            if (network.getTrafficType() == TrafficType.Guest || network.getTrafficType() == TrafficType.Public) {
                userVm.setPrivateIpAddress(nic.getIPv4Address());
                userVm.setPrivateMacAddress(nic.getMacAddress());
                _vmDao.update(userVm.getId(), userVm);
            }
        }

        final List<VolumeVO> volumes = _volsDao.findByInstance(userVm.getId());
        VmDiskStatisticsVO diskstats;
        for (final VolumeVO volume : volumes) {
            diskstats = _vmDiskStatsDao.findBy(userVm.getAccountId(), userVm.getDataCenterId(), userVm.getId(), volume.getId());
            if (diskstats == null) {
                diskstats = new VmDiskStatisticsVO(userVm.getAccountId(), userVm.getDataCenterId(), userVm.getId(), volume.getId());
                _vmDiskStatsDao.persist(diskstats);
            }
        }

        finalizeCommandsOnStart(cmds, profile);
        return true;
    }

    @Override
    public boolean finalizeStart(final VirtualMachineProfile profile, final long hostId, final Commands cmds, final ReservationContext context) {
        final UserVmVO vm = _vmDao.findById(profile.getId());

        final Answer[] answersToCmds = cmds.getAnswers();
        if (answersToCmds == null) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Returning from finalizeStart() since there are no answers to read");
            }
            return true;
        }
        final Answer startAnswer = cmds.getAnswer(StartAnswer.class);
        String returnedIp = null;
        String originalIp = null;
        if (startAnswer != null) {
            final StartAnswer startAns = (StartAnswer) startAnswer;
            final VirtualMachineTO vmTO = startAns.getVirtualMachine();
            for (final NicTO nicTO : vmTO.getNics()) {
                if (nicTO.getType() == TrafficType.Guest) {
                    returnedIp = nicTO.getIp();
                }
            }
        }

        final List<NicVO> nics = _nicDao.listByVmId(vm.getId());
        NicVO guestNic = null;
        NetworkVO guestNetwork = null;
        for (final NicVO nic : nics) {
            final NetworkVO network = _networkDao.findById(nic.getNetworkId());
            final long isDefault = nic.isDefaultNic() ? 1 : 0;

            if (network.getTrafficType() == TrafficType.Guest) {
                originalIp = nic.getIPv4Address();
                guestNic = nic;
                guestNetwork = network;
                if (nic.getBroadcastUri().getScheme().equals("pvlan")) {
                    final NicProfile nicProfile = new NicProfile(nic, network, nic.getBroadcastUri(), nic.getIsolationUri(), 0, "pvlan-nic");
                    if (!setupVmForPvlan(true, hostId, nicProfile)) {
                        return false;
                    }
                }
            }
        }
        boolean ipChanged = false;
        if (originalIp != null && !originalIp.equalsIgnoreCase(returnedIp)) {
            if (returnedIp != null && guestNic != null) {
                guestNic.setIPv4Address(returnedIp);
                ipChanged = true;
            }
        }
        if (returnedIp != null && !returnedIp.equalsIgnoreCase(originalIp)) {
            if (guestNic != null) {
                guestNic.setIPv4Address(returnedIp);
                ipChanged = true;
            }
        }

        // get system ip and create static nat rule for the vm
        try {
            _rulesMgr.getSystemIpAndEnableStaticNatForVm(profile.getVirtualMachine(), false);
        } catch (final Exception ex) {
            s_logger.warn("Failed to get system ip and enable static nat for the vm " + profile.getVirtualMachine() + " due to exception ", ex);
            return false;
        }

        final Answer answer = cmds.getAnswer("restoreVMSnapshot");
        if (answer != null && answer instanceof RestoreVMSnapshotAnswer) {
            final RestoreVMSnapshotAnswer restoreVMSnapshotAnswer = (RestoreVMSnapshotAnswer) answer;
            if (!restoreVMSnapshotAnswer.getResult()) {
                s_logger.warn("Unable to restore the vm snapshot from image file to the VM: " + restoreVMSnapshotAnswer.getDetails());
            }
        }

        return true;
    }

    @Override
    public boolean finalizeCommandsOnStart(final Commands cmds, final VirtualMachineProfile profile) {
        final UserVmVO vm = _vmDao.findById(profile.getId());
        final List<VMSnapshotVO> vmSnapshots = _vmSnapshotDao.findByVm(vm.getId());
        final RestoreVMSnapshotCommand command = _vmSnapshotMgr.createRestoreCommand(vm, vmSnapshots);
        if (command != null) {
            cmds.addCommand("restoreVMSnapshot", command);
        }
        return true;
    }

    @Override
    public void finalizeStop(final VirtualMachineProfile profile, final Answer answer) {
        final VirtualMachine vm = profile.getVirtualMachine();
        // release elastic IP here
        final IPAddressVO ip = _ipAddressDao.findByAssociatedVmId(profile.getId());
        if (ip != null && ip.getSystem()) {
            final CallContext ctx = CallContext.current();
            try {
                final long networkId = ip.getAssociatedWithNetworkId();
                final Network guestNetwork = _networkDao.findById(networkId);
                final NetworkOffering offering = _entityMgr.findById(NetworkOffering.class, guestNetwork.getNetworkOfferingId());
                assert offering.getAssociatePublicIP() : "User VM should not have system owned public IP associated with it when offering configured not to associate " +
                        "public IP.";
                _rulesMgr.disableStaticNat(ip.getId(), ctx.getCallingAccount(), ctx.getCallingUserId(), true);
            } catch (final Exception ex) {
                s_logger.warn("Failed to disable static nat and release system ip " + ip + " as a part of vm " + profile.getVirtualMachine() + " stop due to exception ", ex);
            }
        }

        final List<NicVO> nics = _nicDao.listByVmId(vm.getId());
        for (final NicVO nic : nics) {
            final NetworkVO network = _networkDao.findById(nic.getNetworkId());
            if (network.getTrafficType() == TrafficType.Guest) {
                if (nic.getBroadcastUri() != null && nic.getBroadcastUri().getScheme().equals("pvlan")) {
                    final NicProfile nicProfile = new NicProfile(nic, network, nic.getBroadcastUri(), nic.getIsolationUri(), 0, "pvlan-nic");
                    setupVmForPvlan(false, vm.getHostId(), nicProfile);
                }
            }
        }
    }

    @Override
    public void finalizeExpunge(final VirtualMachine vm) {
    }

    @Override
    public void prepareStop(final VirtualMachineProfile profile) {
        final UserVmVO vm = _vmDao.findById(profile.getId());
        if (vm != null && vm.getState() == State.Stopping) {
            collectVmDiskStatistics(vm);
        }
    }

    @Override
    public String getConfigComponentName() {
        return UserVmManager.class.getSimpleName();
    }

    @Override
    public ConfigKey<?>[] getConfigKeys() {
        return new ConfigKey<?>[]{EnableDynamicallyScaleVm, AllowUserExpungeRecoverVm, VmIpFetchWaitInterval, VmIpFetchTrialMax, VmIpFetchThreadPoolMax};
    }

    public enum UserVmCloneType {
        full, linked
    }

    public class VmAndCountDetails {
        long vmId;
        int retrievalCount = VmIpFetchTrialMax.value();

        public VmAndCountDetails() {
        }

        VmAndCountDetails(final long vmId, final int retrievalCount) {
            this.vmId = vmId;
            this.retrievalCount = retrievalCount;
        }

        public VmAndCountDetails(final long vmId) {
            this.vmId = vmId;
        }

        public int getRetrievalCount() {
            return retrievalCount;
        }

        public void setRetrievalCount(final int retrievalCount) {
            this.retrievalCount = retrievalCount;
        }

        public long getVmId() {
            return vmId;
        }

        public void setVmId(final long vmId) {
            this.vmId = vmId;
        }

        public void decrementCount() {
            retrievalCount--;
        }
    }

    protected class VmIpAddrFetchThread extends ManagedContextRunnable {

        long nicId;
        long vmId;
        String vmName;
        boolean isWindows;
        Long hostId;
        String networkCidr;

        public VmIpAddrFetchThread() {
        }

        VmIpAddrFetchThread(final long vmId, final long nicId, final String instanceName, final boolean windows, final Long hostId, final String networkCidr) {
            this.vmId = vmId;
            this.nicId = nicId;
            vmName = instanceName;
            isWindows = windows;
            this.hostId = hostId;
            this.networkCidr = networkCidr;
        }

        @Override
        protected void runInContext() {
            final GetVmIpAddressCommand cmd = new GetVmIpAddressCommand(vmName, networkCidr, isWindows);
            boolean decrementCount = true;

            try {
                s_logger.debug("Trying for vm " + vmId + " nic Id " + nicId + " ip retrieval ...");
                final Answer answer = _agentMgr.send(hostId, cmd);
                final NicVO nic = _nicDao.findById(nicId);
                if (answer.getResult()) {
                    final String vmIp = answer.getDetails();

                    if (NetUtils.isValidIp4(vmIp)) {
                        // set this vm ip addr in vm nic.
                        if (nic != null) {
                            nic.setIPv4Address(vmIp);
                            _nicDao.update(nicId, nic);
                            s_logger.debug("Vm " + vmId + " IP " + vmIp + " got retrieved successfully");
                            vmIdCountMap.remove(nicId);
                            decrementCount = false;
                            ActionEventUtils.onActionEvent(User.UID_SYSTEM, Account.ACCOUNT_ID_SYSTEM,
                                    Domain.ROOT_DOMAIN, EventTypes.EVENT_NETWORK_EXTERNAL_DHCP_VM_IPFETCH,
                                    "VM " + vmId + " nic id " + nicId + " ip address " + vmIp + " got fetched successfully");
                        }
                    }
                } else {
                    //previously vm has ip and nic table has ip address. After vm restart or stop/start
                    //if vm doesnot get the ip then set the ip in nic table to null
                    if (nic.getIPv4Address() != null) {
                        nic.setIPv4Address(null);
                        _nicDao.update(nicId, nic);
                    }
                    if (answer.getDetails() != null) {
                        s_logger.debug("Failed to get vm ip for Vm " + vmId + answer.getDetails());
                    }
                }
            } catch (final OperationTimedoutException e) {
                s_logger.warn("Timed Out", e);
            } catch (final AgentUnavailableException e) {
                s_logger.warn("Agent Unavailable ", e);
            } finally {
                if (decrementCount) {
                    final VmAndCountDetails vmAndCount = vmIdCountMap.get(nicId);
                    vmAndCount.decrementCount();
                    s_logger.debug("Ip is not retrieved for VM " + vmId + " nic " + nicId + " ... decremented count to " + vmAndCount.getRetrievalCount());
                    vmIdCountMap.put(nicId, vmAndCount);
                }
            }
        }
    }

    protected class VmIpFetchTask extends ManagedContextRunnable {

        VmIpFetchTask() {
            GlobalLock.getInternLock("vmIpFetch");
        }

        @Override
        protected void runInContext() {
            final GlobalLock scanLock = GlobalLock.getInternLock("vmIpFetch");

            try {
                if (scanLock.lock(ACQUIRE_GLOBAL_LOCK_TIMEOUT_FOR_COOPERATION)) {
                    try {

                        for (final Entry<Long, VmAndCountDetails> entry : vmIdCountMap.entrySet()) {
                            final long nicId = entry.getKey();
                            final VmAndCountDetails vmIdAndCount = entry.getValue();
                            final long vmId = vmIdAndCount.getVmId();

                            if (vmIdAndCount.getRetrievalCount() <= 0) {
                                vmIdCountMap.remove(nicId);
                                s_logger.debug("Vm " + vmId + " nic " + nicId + " count is zero .. removing vm nic from map ");

                                ActionEventUtils.onActionEvent(User.UID_SYSTEM, Account.ACCOUNT_ID_SYSTEM,
                                        Domain.ROOT_DOMAIN, EventTypes.EVENT_NETWORK_EXTERNAL_DHCP_VM_IPFETCH,
                                        "VM " + vmId + " nic id " + nicId + " ip addr fetch failed ");

                                continue;
                            }

                            final UserVm userVm = _vmDao.findById(vmId);
                            final VMInstanceVO vmInstance = _vmInstanceDao.findById(vmId);
                            final NicVO nicVo = _nicDao.findById(nicId);
                            final NetworkVO network = _networkDao.findById(nicVo.getNetworkId());

                            final VirtualMachineProfile vmProfile = new VirtualMachineProfileImpl(userVm);
                            final VirtualMachine vm = vmProfile.getVirtualMachine();
                            final boolean isWindows = _guestOSCategoryDao.findById(_guestOSDao.findById(vm.getGuestOSId()).getCategoryId()).getName().equalsIgnoreCase("Windows");

                            _vmIpFetchThreadExecutor.execute(new VmIpAddrFetchThread(vmId, nicId, vmInstance.getInstanceName(),
                                    isWindows, vm.getHostId(), network.getCidr()));
                        }
                    } catch (final Exception e) {
                        s_logger.error("Caught the Exception in VmIpFetchTask", e);
                    } finally {
                        scanLock.unlock();
                    }
                }
            } finally {
                scanLock.releaseRef();
            }
        }
    }

    protected class ExpungeTask extends ManagedContextRunnable {
        ExpungeTask() {
        }

        @Override
        protected void runInContext() {
            final GlobalLock scanLock = GlobalLock.getInternLock("UserVMExpunge");
            try {
                if (scanLock.lock(ACQUIRE_GLOBAL_LOCK_TIMEOUT_FOR_COOPERATION)) {
                    try {
                        final List<UserVmVO> vms = _vmDao.findDestroyedVms(new Date(System.currentTimeMillis() - ((long) _expungeDelay << 10)));
                        if (s_logger.isInfoEnabled()) {
                            if (vms.size() == 0) {
                                s_logger.trace("Found " + vms.size() + " vms to expunge.");
                            } else {
                                s_logger.info("Found " + vms.size() + " vms to expunge.");
                            }
                        }
                        for (final UserVmVO vm : vms) {
                            try {
                                expungeVm(vm.getId());
                            } catch (final Exception e) {
                                s_logger.warn("Unable to expunge " + vm, e);
                            }
                        }
                    } catch (final Exception e) {
                        s_logger.error("Caught the following Exception", e);
                    } finally {
                        scanLock.unlock();
                    }
                }
            } finally {
                scanLock.releaseRef();
            }
        }
    }
}
