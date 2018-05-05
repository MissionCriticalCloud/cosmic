package com.cloud.capacity;

import com.cloud.agent.AgentManager;
import com.cloud.agent.Listener;
import com.cloud.capacity.dao.CapacityDao;
import com.cloud.common.resource.ServerResource;
import com.cloud.configuration.Config;
import com.cloud.dc.ClusterDetailsDao;
import com.cloud.dc.ClusterDetailsVO;
import com.cloud.deploy.DeploymentClusterPlanner;
import com.cloud.engine.subsystem.api.storage.DataStoreDriver;
import com.cloud.engine.subsystem.api.storage.DataStoreProvider;
import com.cloud.engine.subsystem.api.storage.DataStoreProviderManager;
import com.cloud.engine.subsystem.api.storage.PrimaryDataStoreDriver;
import com.cloud.framework.config.ConfigKey;
import com.cloud.framework.config.Configurable;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.framework.messagebus.MessageBus;
import com.cloud.framework.messagebus.PublishScope;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.dao.HypervisorCapabilitiesDao;
import com.cloud.legacymodel.communication.answer.AgentControlAnswer;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.AgentControlCommand;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.communication.command.StartupCommand;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.dc.HostStatus;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.resource.ResourceState;
import com.cloud.legacymodel.statemachine.StateListener;
import com.cloud.legacymodel.statemachine.Transition;
import com.cloud.legacymodel.utils.Pair;
import com.cloud.legacymodel.vm.VirtualMachine;
import com.cloud.legacymodel.vm.VirtualMachine.Event;
import com.cloud.legacymodel.vm.VirtualMachine.State;
import com.cloud.model.enumeration.CapacityState;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.model.enumeration.VirtualMachineType;
import com.cloud.offering.ServiceOffering;
import com.cloud.resource.ResourceListener;
import com.cloud.resource.ResourceManager;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.StorageManager;
import com.cloud.storage.VMTemplateStoragePoolVO;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.VMTemplatePoolDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.storage.datastore.db.StoragePoolVO;
import com.cloud.utils.DateUtil;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallbackNoReturn;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.vm.UserVmDetailVO;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.dao.UserVmDetailsDao;
import com.cloud.vm.dao.VMInstanceDao;
import com.cloud.vm.snapshot.dao.VMSnapshotDao;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CapacityManagerImpl extends ManagerBase implements CapacityManager, StateListener<State, VirtualMachine.Event, VirtualMachine>, Listener, ResourceListener,
        Configurable {
    private static final Logger s_logger = LoggerFactory.getLogger(CapacityManagerImpl.class);
    private static final String MESSAGE_RESERVED_CAPACITY_FREED_FLAG = "Message.ReservedCapacityFreed.Flag";
    @Inject
    protected VMSnapshotDao _vmSnapshotDao;
    @Inject
    private UserVmDao _userVMDao;
    @Inject
    private UserVmDetailsDao _userVmDetailsDao;
    @Inject
    private CapacityDao _capacityDao;
    @Inject
    private ConfigurationDao _configDao;
    @Inject
    private ServiceOfferingDao _offeringsDao;
    @Inject
    private HostDao _hostDao;
    @Inject
    private VMInstanceDao _vmDao;
    @Inject
    private VolumeDao _volumeDao;
    @Inject
    private VMTemplatePoolDao _templatePoolDao;
    @Inject
    private AgentManager _agentManager;
    @Inject
    private ResourceManager _resourceMgr;
    @Inject
    private StorageManager _storageMgr;
    @Inject
    private HypervisorCapabilitiesDao _hypervisorCapabilitiesDao;
    @Inject
    private DataStoreProviderManager _dataStoreProviderMgr;
    @Inject
    private ClusterDetailsDao _clusterDetailsDao;
    @Inject
    private MessageBus _messageBus;
    private int _vmCapacityReleaseInterval;
    private ScheduledExecutorService _executor;

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        this._vmCapacityReleaseInterval = NumbersUtil.parseInt(this._configDao.getValue(Config.CapacitySkipcountingHours.key()), 3600);

        this._executor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("HostCapacity-Checker"));
        VirtualMachine.State.getStateMachine().registerListener(this);
        this._agentManager.registerForHostEvents(new StorageCapacityListener(this._capacityDao, this._storageMgr), true, false, false);
        this._agentManager.registerForHostEvents(new ComputeCapacityListener(this._capacityDao, this), true, false, false);

        return true;
    }

    @Override
    public boolean start() {
        this._resourceMgr.registerResourceEvent(ResourceListener.EVENT_PREPARE_MAINTENANCE_AFTER, this);
        this._resourceMgr.registerResourceEvent(ResourceListener.EVENT_CANCEL_MAINTENANCE_AFTER, this);
        return true;
    }

    @Override
    public boolean stop() {
        this._executor.shutdownNow();
        return true;
    }

    @Override
    public boolean preStateTransitionEvent(final State oldState, final Event event, final State newState, final VirtualMachine vm, final boolean transitionStatus, final Object
            opaque) {
        return true;
    }

    @Override
    public boolean postStateTransitionEvent(final Transition<State, Event> transition, final VirtualMachine vm, final boolean status, final Object opaque) {
        if (!status) {
            return false;
        }
        final Pair<Long, Long> hosts = (Pair<Long, Long>) opaque;
        final Long oldHostId = hosts.first();

        final State oldState = transition.getCurrentState();
        final State newState = transition.getToState();
        final Event event = transition.getEvent();
        s_logger.debug("VM state transitted from :" + oldState + " to " + newState + " with event: " + event + "vm's original host id: " + vm.getLastHostId() +
                " new host id: " + vm.getHostId() + " host id before state transition: " + oldHostId);

        if (oldState == State.Starting) {
            if (newState != State.Running) {
                releaseVmCapacity(vm, false, false, oldHostId);
            }
        } else if (oldState == State.Running) {
            if (event == Event.AgentReportStopped) {
                releaseVmCapacity(vm, false, true, oldHostId);
            } else if (event == Event.AgentReportMigrated) {
                releaseVmCapacity(vm, false, false, oldHostId);
            }
        } else if (oldState == State.Migrating) {
            if (event == Event.AgentReportStopped) {
                /* Release capacity from original host */
                releaseVmCapacity(vm, false, false, vm.getLastHostId());
                releaseVmCapacity(vm, false, false, oldHostId);
            } else if (event == Event.OperationFailed) {
                /* Release from dest host */
                releaseVmCapacity(vm, false, false, oldHostId);
            } else if (event == Event.OperationSucceeded) {
                releaseVmCapacity(vm, false, false, vm.getLastHostId());
            }
        } else if (oldState == State.Stopping) {
            if (event == Event.OperationSucceeded) {
                releaseVmCapacity(vm, false, true, oldHostId);
            } else if (event == Event.AgentReportStopped) {
                releaseVmCapacity(vm, false, false, oldHostId);
            } else if (event == Event.AgentReportMigrated) {
                releaseVmCapacity(vm, false, false, oldHostId);
            }
        } else if (oldState == State.Stopped) {
            if (event == Event.DestroyRequested || event == Event.ExpungeOperation) {
                releaseVmCapacity(vm, true, false, vm.getLastHostId());
            } else if (event == Event.AgentReportMigrated) {
                releaseVmCapacity(vm, false, false, oldHostId);
            }
        }

        if ((newState == State.Starting || newState == State.Migrating || event == Event.AgentReportMigrated) && vm.getHostId() != null) {
            boolean fromLastHost = false;
            if (vm.getHostId().equals(vm.getLastHostId())) {
                s_logger.debug("VM starting again on the last host it was stopped on");
                fromLastHost = true;
            }
            allocateVmCapacity(vm, fromLastHost);
        }

        if (newState == State.Stopped) {
            if (vm.getType() == VirtualMachineType.User) {

                final UserVmVO userVM = this._userVMDao.findById(vm.getId());
                this._userVMDao.loadDetails(userVM);
                // free the message sent flag if it exists
                userVM.setDetail(MESSAGE_RESERVED_CAPACITY_FREED_FLAG, "false");
                this._userVMDao.saveDetails(userVM);
            }
        }

        return true;
    }

    @DB
    @Override
    public boolean releaseVmCapacity(final VirtualMachine vm, final boolean moveFromReserved, final boolean moveToReservered, final Long hostId) {
        if (hostId == null) {
            return true;
        }

        final ServiceOfferingVO svo = this._offeringsDao.findById(vm.getId(), vm.getServiceOfferingId());
        final CapacityVO capacityCpu = this._capacityDao.findByHostIdType(hostId, Capacity.CAPACITY_TYPE_CPU);
        final CapacityVO capacityMemory = this._capacityDao.findByHostIdType(hostId, Capacity.CAPACITY_TYPE_MEMORY);
        final Long clusterId;
        final HostVO host = this._hostDao.findById(hostId);
        if (host == null) {
            s_logger.warn("Host " + hostId + " no long exist anymore!");
            return true;
        }

        clusterId = host.getClusterId();
        if (capacityCpu == null || capacityMemory == null || svo == null) {
            return false;
        }

        try {
            final Long clusterIdFinal = clusterId;
            final long capacityCpuId = capacityCpu.getId();
            final long capacityMemoryId = capacityMemory.getId();
            Transaction.execute(new TransactionCallbackNoReturn() {
                @Override
                public void doInTransactionWithoutResult(final TransactionStatus status) {
                    final CapacityVO capacityCpu = CapacityManagerImpl.this._capacityDao.lockRow(capacityCpuId, true);
                    final CapacityVO capacityMemory = CapacityManagerImpl.this._capacityDao.lockRow(capacityMemoryId, true);

                    final long usedCpu = capacityCpu.getUsedCapacity();
                    final long usedMem = capacityMemory.getUsedCapacity();
                    final long reservedCpu = capacityCpu.getReservedCapacity();
                    final long reservedMem = capacityMemory.getReservedCapacity();
                    final long actualTotalCpu = capacityCpu.getTotalCapacity();
                    final float cpuOvercommitRatio = Float.parseFloat(CapacityManagerImpl.this._clusterDetailsDao.findDetail(clusterIdFinal, "cpuOvercommitRatio").getValue());
                    final float memoryOvercommitRatio = Float.parseFloat(CapacityManagerImpl.this._clusterDetailsDao.findDetail(clusterIdFinal, "memoryOvercommitRatio").getValue());
                    final int vmCPU = svo.getCpu();
                    final long vmMem = svo.getRamSize() * 1024L * 1024L;
                    final long actualTotalMem = capacityMemory.getTotalCapacity();
                    final long totalMem = (long) (actualTotalMem * memoryOvercommitRatio);
                    final long totalCpu = (long) (actualTotalCpu * cpuOvercommitRatio);
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Hosts's actual total CPU: " + actualTotalCpu + " and CPU after applying overprovisioning: " + totalCpu);
                        s_logger.debug("Hosts's actual total RAM: " + actualTotalMem + " and RAM after applying overprovisioning: " + totalMem);
                    }

                    if (!moveFromReserved) {
                        /* move resource from used */
                        if (usedCpu >= vmCPU) {
                            capacityCpu.setUsedCapacity(usedCpu - vmCPU);
                        }
                        if (usedMem >= vmMem) {
                            capacityMemory.setUsedCapacity(usedMem - vmMem);
                        }

                        if (moveToReservered) {
                            if (reservedCpu + vmCPU <= totalCpu) {
                                capacityCpu.setReservedCapacity(reservedCpu + vmCPU);
                            }
                            if (reservedMem + vmMem <= totalMem) {
                                capacityMemory.setReservedCapacity(reservedMem + vmMem);
                            }
                        }
                    } else {
                        if (reservedCpu >= vmCPU) {
                            capacityCpu.setReservedCapacity(reservedCpu - vmCPU);
                        }
                        if (reservedMem >= vmMem) {
                            capacityMemory.setReservedCapacity(reservedMem - vmMem);
                        }
                    }

                    s_logger.debug("release cpu from host: " + hostId + ", old used: " + usedCpu + ",reserved: " + reservedCpu + ", actual total: " + actualTotalCpu +
                            ", total with overprovisioning: " + totalCpu + "; new used: " + capacityCpu.getUsedCapacity() + ",reserved:" + capacityCpu.getReservedCapacity() +
                            "; movedfromreserved: " + moveFromReserved + ",moveToReservered" + moveToReservered);

                    s_logger.debug("release mem from host: " + hostId + ", old used: " + usedMem + ",reserved: " + reservedMem + ", total: " + totalMem + "; new used: " +
                            capacityMemory.getUsedCapacity() + ",reserved:" + capacityMemory.getReservedCapacity() + "; movedfromreserved: " + moveFromReserved +
                            ",moveToReservered" + moveToReservered);

                    CapacityManagerImpl.this._capacityDao.update(capacityCpu.getId(), capacityCpu);
                    CapacityManagerImpl.this._capacityDao.update(capacityMemory.getId(), capacityMemory);
                }
            });

            return true;
        } catch (final Exception e) {
            s_logger.debug("Failed to transit vm's state, due to " + e.getMessage());
            return false;
        }
    }

    @DB
    @Override
    public void allocateVmCapacity(final VirtualMachine vm, final boolean fromLastHost) {

        final long hostId = vm.getHostId();
        final HostVO host = this._hostDao.findById(hostId);
        final long clusterId = host.getClusterId();
        final float cpuOvercommitRatio = Float.parseFloat(this._clusterDetailsDao.findDetail(clusterId, "cpuOvercommitRatio").getValue());
        final float memoryOvercommitRatio = Float.parseFloat(this._clusterDetailsDao.findDetail(clusterId, "memoryOvercommitRatio").getValue());

        final ServiceOfferingVO svo = this._offeringsDao.findById(vm.getId(), vm.getServiceOfferingId());

        final CapacityVO capacityCpu = this._capacityDao.findByHostIdType(hostId, Capacity.CAPACITY_TYPE_CPU);
        final CapacityVO capacityMem = this._capacityDao.findByHostIdType(hostId, Capacity.CAPACITY_TYPE_MEMORY);

        if (capacityCpu == null || capacityMem == null || svo == null) {
            return;
        }

        final int cpu = svo.getCpu();
        final long ram = svo.getRamSize() * 1024L * 1024L;

        try {
            final long capacityCpuId = capacityCpu.getId();
            final long capacityMemId = capacityMem.getId();

            Transaction.execute(new TransactionCallbackNoReturn() {
                @Override
                public void doInTransactionWithoutResult(final TransactionStatus status) {
                    final CapacityVO capacityCpu = CapacityManagerImpl.this._capacityDao.lockRow(capacityCpuId, true);
                    final CapacityVO capacityMem = CapacityManagerImpl.this._capacityDao.lockRow(capacityMemId, true);

                    final long usedCpu = capacityCpu.getUsedCapacity();
                    final long usedMem = capacityMem.getUsedCapacity();
                    final long reservedCpu = capacityCpu.getReservedCapacity();
                    final long reservedMem = capacityMem.getReservedCapacity();
                    final long actualTotalCpu = capacityCpu.getTotalCapacity();
                    final long actualTotalMem = capacityMem.getTotalCapacity();
                    final long totalCpu = (long) (actualTotalCpu * cpuOvercommitRatio);
                    final long totalMem = (long) (actualTotalMem * memoryOvercommitRatio);
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Hosts's actual total CPU: " + actualTotalCpu + " and CPU after applying overprovisioning: " + totalCpu);
                    }

                    final long freeCpu = totalCpu - (reservedCpu + usedCpu);
                    final long freeMem = totalMem - (reservedMem + usedMem);

                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("We are allocating VM, increasing the used capacity of this host:" + hostId);
                        s_logger.debug("Current Used CPU: " + usedCpu + " , Free CPU:" + freeCpu + " ,Requested CPU: " + cpu);
                        s_logger.debug("Current Used RAM: " + usedMem + " , Free RAM:" + freeMem + " ,Requested RAM: " + ram);
                    }
                    capacityCpu.setUsedCapacity(usedCpu + cpu);
                    capacityMem.setUsedCapacity(usedMem + ram);

                    if (fromLastHost) {
                        /* alloc from reserved */
                        if (s_logger.isDebugEnabled()) {
                            s_logger.debug("We are allocating VM to the last host again, so adjusting the reserved capacity if it is not less than required");
                            s_logger.debug("Reserved CPU: " + reservedCpu + " , Requested CPU: " + cpu);
                            s_logger.debug("Reserved RAM: " + reservedMem + " , Requested RAM: " + ram);
                        }
                        if (reservedCpu >= cpu && reservedMem >= ram) {
                            capacityCpu.setReservedCapacity(reservedCpu - cpu);
                            capacityMem.setReservedCapacity(reservedMem - ram);
                        }
                    } else {
                        /* alloc from free resource */
                        if (!((reservedCpu + usedCpu + cpu <= totalCpu) && (reservedMem + usedMem + ram <= totalMem))) {
                            if (s_logger.isDebugEnabled()) {
                                s_logger.debug("Host doesnt seem to have enough free capacity, but increasing the used capacity anyways, " +
                                        "since the VM is already starting on this host ");
                            }
                        }
                    }

                    s_logger.debug("CPU STATS after allocation: for host: " + hostId + ", old used: " + usedCpu + ", old reserved: " + reservedCpu + ", actual total: " +
                            actualTotalCpu + ", total with overprovisioning: " + totalCpu + "; new used:" + capacityCpu.getUsedCapacity() + ", reserved:" +
                            capacityCpu.getReservedCapacity() + "; requested cpu:" + cpu + ",alloc_from_last:" + fromLastHost);

                    s_logger.debug("RAM STATS after allocation: for host: " + hostId + ", old used: " + usedMem + ", old reserved: " + reservedMem + ", total: " +
                            totalMem + "; new used: " + capacityMem.getUsedCapacity() + ", reserved: " + capacityMem.getReservedCapacity() + "; requested mem: " + ram +
                            ",alloc_from_last:" + fromLastHost);

                    CapacityManagerImpl.this._capacityDao.update(capacityCpu.getId(), capacityCpu);
                    CapacityManagerImpl.this._capacityDao.update(capacityMem.getId(), capacityMem);
                }
            });
        } catch (final Exception e) {
            s_logger.error("Exception allocating VM capacity", e);
        }
    }

    @Override
    public boolean checkIfHostHasCapacity(final long hostId, final Integer cpu, final long ram, final boolean checkFromReservedCapacity, final float cpuOvercommitRatio, final
    float memoryOvercommitRatio, final boolean considerReservedCapacity) {
        boolean hasCapacity = false;

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Checking if host: " + hostId + " has enough capacity for requested CPU: " + cpu + " and requested RAM: " + ram +
                    " , cpuOverprovisioningFactor: " + cpuOvercommitRatio);
        }

        final CapacityVO capacityCpu = this._capacityDao.findByHostIdType(hostId, Capacity.CAPACITY_TYPE_CPU);
        final CapacityVO capacityMem = this._capacityDao.findByHostIdType(hostId, Capacity.CAPACITY_TYPE_MEMORY);

        if (capacityCpu == null || capacityMem == null) {
            if (capacityCpu == null) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Cannot checkIfHostHasCapacity, Capacity entry for CPU not found in Db, for hostId: " + hostId);
                }
            }
            if (capacityMem == null) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Cannot checkIfHostHasCapacity, Capacity entry for RAM not found in Db, for hostId: " + hostId);
                }
            }

            return false;
        }

        final long usedCpu = capacityCpu.getUsedCapacity();
        final long usedMem = capacityMem.getUsedCapacity();
        final long reservedCpu = capacityCpu.getReservedCapacity();
        final long reservedMem = capacityMem.getReservedCapacity();
        final long actualTotalCpu = capacityCpu.getTotalCapacity();
        final long actualTotalMem = capacityMem.getTotalCapacity();
        final long totalCpu = (long) (actualTotalCpu * cpuOvercommitRatio);
        final long totalMem = (long) (actualTotalMem * memoryOvercommitRatio);
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Hosts's actual total CPU: " + actualTotalCpu + " and CPU after applying overprovisioning: " + totalCpu);
        }

        String failureReason = "";
        if (checkFromReservedCapacity) {

            if (s_logger.isDebugEnabled()) {
                s_logger.debug("We need to allocate to the last host again, so checking if there is enough reserved capacity");
                s_logger.debug("Reserved CPU: " + reservedCpu + " , Requested CPU: " + cpu);
                s_logger.debug("Reserved RAM: " + reservedMem + " , Requested RAM: " + ram);
            }
            /* alloc from reserved */
            if (reservedCpu >= cpu) {
                if (reservedMem >= ram) {
                    hasCapacity = true;
                } else {
                    failureReason = "Host does not have enough reserved RAM available";
                }
            } else {
                failureReason = "Host does not have enough reserved CPU available";
            }
        } else {

            long reservedCpuValueToUse = reservedCpu;
            long reservedMemValueToUse = reservedMem;

            if (!considerReservedCapacity) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("considerReservedCapacity is false, not considering reserved capacity for calculating free capacity");
                }
                reservedCpuValueToUse = 0;
                reservedMemValueToUse = 0;
            }
            final long freeCpu = totalCpu - (reservedCpuValueToUse + usedCpu);
            final long freeMem = totalMem - (reservedMemValueToUse + usedMem);

            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Free CPU: " + freeCpu + " , Requested CPU: " + cpu);
                s_logger.debug("Free RAM: " + freeMem + " , Requested RAM: " + ram);
            }
            /* alloc from free resource */
            if ((reservedCpuValueToUse + usedCpu + cpu <= totalCpu)) {
                if ((reservedMemValueToUse + usedMem + ram <= totalMem)) {
                    hasCapacity = true;
                } else {
                    failureReason = "Host does not have enough RAM available";
                }
            } else {
                failureReason = "Host does not have enough CPU available";
            }
        }

        if (hasCapacity) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Host has enough CPU and RAM available");
            }

            s_logger.debug("STATS: Can alloc CPU from host: " + hostId + ", used: " + usedCpu + ", reserved: " + reservedCpu + ", actual total: " + actualTotalCpu +
                    ", total with overprovisioning: " + totalCpu + "; requested cpu:" + cpu + ",alloc_from_last_host?:" + checkFromReservedCapacity +
                    " ,considerReservedCapacity?: " + considerReservedCapacity);

            s_logger.debug("STATS: Can alloc MEM from host: " + hostId + ", used: " + usedMem + ", reserved: " + reservedMem + ", total: " + totalMem +
                    "; requested mem: " + ram + ",alloc_from_last_host?:" + checkFromReservedCapacity + " ,considerReservedCapacity?: " + considerReservedCapacity);
        } else {

            if (checkFromReservedCapacity) {
                s_logger.debug("STATS: Failed to alloc resource from host: " + hostId + " reservedCpu: " + reservedCpu + ", requested cpu: " + cpu + ", reservedMem: " +
                        reservedMem + ", requested mem: " + ram);
            } else {
                s_logger.debug("STATS: Failed to alloc resource from host: " + hostId + " reservedCpu: " + reservedCpu + ", used cpu: " + usedCpu + ", requested cpu: " +
                        cpu + ", actual total cpu: " + actualTotalCpu + ", total cpu with overprovisioning: " + totalCpu + ", reservedMem: " + reservedMem + ", used Mem: " +
                        usedMem + ", requested mem: " + ram + ", total Mem:" + totalMem + " ,considerReservedCapacity?: " + considerReservedCapacity);
            }

            if (s_logger.isDebugEnabled()) {
                s_logger.debug(failureReason + ", cannot allocate to this host.");
            }
        }

        return hasCapacity;
    }

    @DB
    @Override
    public void updateCapacityForHost(final Host host) {
        // prepare the service offerings
        final List<ServiceOfferingVO> offerings = this._offeringsDao.listAllIncludingRemoved();
        final Map<Long, ServiceOfferingVO> offeringsMap = new HashMap<>();
        for (final ServiceOfferingVO offering : offerings) {
            offeringsMap.put(offering.getId(), offering);
        }

        long usedCpu = 0;
        long usedMemory = 0;
        long reservedMemory = 0;
        long reservedCpu = 0;
        final CapacityState capacityState = (host.getResourceState() == ResourceState.Enabled) ? CapacityState.Enabled : CapacityState.Disabled;

        final List<VMInstanceVO> vms = this._vmDao.listUpByHostId(host.getId());
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Found " + vms.size() + " VMs on host " + host.getId());
        }

        for (final VMInstanceVO vm : vms) {
            final ServiceOffering so = offeringsMap.get(vm.getServiceOfferingId());
            usedMemory += (so.getRamSize() * 1024L * 1024L);
            usedCpu += so.getCpu();
        }

        final List<VMInstanceVO> vmsByLastHostId = this._vmDao.listByLastHostId(host.getId());
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Found " + vmsByLastHostId.size() + " VM, not running on host " + host.getId());
        }
        for (final VMInstanceVO vm : vmsByLastHostId) {
            final long secondsSinceLastUpdate = (DateUtil.currentGMTTime().getTime() - vm.getUpdateTime().getTime()) / 1000;
            if (secondsSinceLastUpdate < this._vmCapacityReleaseInterval) {
                final ServiceOffering so = offeringsMap.get(vm.getServiceOfferingId());

                reservedMemory += (so.getRamSize() * 1024L * 1024L);
                reservedCpu += so.getCpu();
            } else {
                // signal if not done already, that the VM has been stopped for skip.counting.hours,
                // hence capacity will not be reserved anymore.
                final UserVmDetailVO messageSentFlag = this._userVmDetailsDao.findDetail(vm.getId(), MESSAGE_RESERVED_CAPACITY_FREED_FLAG);
                if (messageSentFlag == null || !Boolean.valueOf(messageSentFlag.getValue())) {
                    this._messageBus.publish(this._name, "VM_ReservedCapacity_Free", PublishScope.LOCAL, vm);

                    if (vm.getType() == VirtualMachineType.User) {
                        final UserVmVO userVM = this._userVMDao.findById(vm.getId());
                        this._userVMDao.loadDetails(userVM);
                        userVM.setDetail(MESSAGE_RESERVED_CAPACITY_FREED_FLAG, "true");
                        this._userVMDao.saveDetails(userVM);
                    }
                }
            }
        }

        final CapacityVO cpuCap = this._capacityDao.findByHostIdType(host.getId(), Capacity.CAPACITY_TYPE_CPU);
        final CapacityVO memCap = this._capacityDao.findByHostIdType(host.getId(), Capacity.CAPACITY_TYPE_MEMORY);
        if (cpuCap != null && memCap != null) {
            if (host.getTotalMemory() != null) {
                memCap.setTotalCapacity(host.getTotalMemory());
            }
            final long hostTotalCpu = host.getCpus().longValue();

            if (cpuCap.getTotalCapacity() != hostTotalCpu) {
                s_logger.debug("Calibrate total cpu for host: " + host.getId() + " old total CPU:" + cpuCap.getTotalCapacity() + " new total CPU:" + hostTotalCpu);
                cpuCap.setTotalCapacity(hostTotalCpu);
            }
            // Set the capacity state as per the host allocation state.
            if (capacityState != cpuCap.getCapacityState()) {
                s_logger.debug("Calibrate cpu capacity state for host: " + host.getId() + " old capacity state:" + cpuCap.getTotalCapacity() + " new capacity state:" +
                        hostTotalCpu);
                cpuCap.setCapacityState(capacityState);
            }
            memCap.setCapacityState(capacityState);

            if (cpuCap.getUsedCapacity() == usedCpu && cpuCap.getReservedCapacity() == reservedCpu) {
                s_logger.debug("No need to calibrate cpu capacity, host:" + host.getId() + " usedCpu: " + cpuCap.getUsedCapacity() + " reservedCpu: " +
                        cpuCap.getReservedCapacity());
            } else {
                if (cpuCap.getReservedCapacity() != reservedCpu) {
                    s_logger.debug("Calibrate reserved cpu for host: " + host.getId() + " old reservedCpu:" + cpuCap.getReservedCapacity() + " new reservedCpu:" +
                            reservedCpu);
                    cpuCap.setReservedCapacity(reservedCpu);
                }
                if (cpuCap.getUsedCapacity() != usedCpu) {
                    s_logger.debug("Calibrate used cpu for host: " + host.getId() + " old usedCpu:" + cpuCap.getUsedCapacity() + " new usedCpu:" + usedCpu);
                    cpuCap.setUsedCapacity(usedCpu);
                }
            }

            if (memCap.getTotalCapacity() != host.getTotalMemory()) {
                s_logger.debug("Calibrate total memory for host: " + host.getId() + " old total memory:" + memCap.getTotalCapacity() + " new total memory:" +
                        host.getTotalMemory());
                memCap.setTotalCapacity(host.getTotalMemory());
            }
            // Set the capacity state as per the host allocation state.
            if (capacityState != memCap.getCapacityState()) {
                s_logger.debug("Calibrate memory capacity state for host: " + host.getId() + " old capacity state:" + memCap.getTotalCapacity() + " new capacity state:" +
                        hostTotalCpu);
                memCap.setCapacityState(capacityState);
            }

            if (memCap.getUsedCapacity() == usedMemory && memCap.getReservedCapacity() == reservedMemory) {
                s_logger.debug("No need to calibrate memory capacity, host:" + host.getId() + " usedMem: " + memCap.getUsedCapacity() + " reservedMem: " +
                        memCap.getReservedCapacity());
            } else {
                if (memCap.getReservedCapacity() != reservedMemory) {
                    s_logger.debug("Calibrate reserved memory for host: " + host.getId() + " old reservedMem:" + memCap.getReservedCapacity() + " new reservedMem:" +
                            reservedMemory);
                    memCap.setReservedCapacity(reservedMemory);
                }
                if (memCap.getUsedCapacity() != usedMemory) {
                    /*
                     * Didn't calibrate for used memory, because VMs can be in
                     * state(starting/migrating) that I don't know on which host
                     * they are allocated
                     */
                    s_logger.debug("Calibrate used memory for host: " + host.getId() + " old usedMem: " + memCap.getUsedCapacity() + " new usedMem: " + usedMemory);
                    memCap.setUsedCapacity(usedMemory);
                }
            }

            try {
                this._capacityDao.update(cpuCap.getId(), cpuCap);
                this._capacityDao.update(memCap.getId(), memCap);
            } catch (final Exception e) {
                s_logger.error("Caught exception while updating cpu/memory capacity for the host " + host.getId(), e);
            }
        } else {
            final long usedMemoryFinal = usedMemory;
            final long reservedMemoryFinal = reservedMemory;
            final long usedCpuFinal = usedCpu;
            final long reservedCpuFinal = reservedCpu;
            Transaction.execute(new TransactionCallbackNoReturn() {
                @Override
                public void doInTransactionWithoutResult(final TransactionStatus status) {
                    CapacityVO capacity =
                            new CapacityVO(host.getId(), host.getDataCenterId(), host.getPodId(), host.getClusterId(), usedMemoryFinal, host.getTotalMemory(),
                                    Capacity.CAPACITY_TYPE_MEMORY);
                    capacity.setReservedCapacity(reservedMemoryFinal);
                    capacity.setCapacityState(capacityState);
                    CapacityManagerImpl.this._capacityDao.persist(capacity);

                    capacity =
                            new CapacityVO(host.getId(), host.getDataCenterId(), host.getPodId(), host.getClusterId(), usedCpuFinal, host.getCpus().longValue(), Capacity.CAPACITY_TYPE_CPU);
                    capacity.setReservedCapacity(reservedCpuFinal);
                    capacity.setCapacityState(capacityState);
                    CapacityManagerImpl.this._capacityDao.persist(capacity);
                }
            });
        }
    }

    @Override
    public long getAllocatedPoolCapacity(final StoragePoolVO pool, final VMTemplateVO templateForVmCreation) {
        long totalAllocatedSize;

        // if the storage pool is managed, the used bytes can be larger than the sum of the sizes of all of the non-destroyed volumes
        // in this case, call getUsedBytes(StoragePoolVO)
        final long _extraBytesPerVolume = 0;
        if (pool.isManaged()) {
            return getUsedBytes(pool);
        } else {
            // Get size for all the non-destroyed volumes
            final Pair<Long, Long> sizes = this._volumeDao.getNonDestroyedCountAndTotalByPool(pool.getId());

            totalAllocatedSize = sizes.second() + sizes.first() * _extraBytesPerVolume;
        }

        // Get size for VM Snapshots
        totalAllocatedSize = totalAllocatedSize + this._volumeDao.getVMSnapshotSizeByPool(pool.getId());

        // Iterate through all templates on this storage pool
        boolean tmpinstalled = false;
        final List<VMTemplateStoragePoolVO> templatePoolVOs;
        templatePoolVOs = this._templatePoolDao.listByPoolId(pool.getId());

        for (final VMTemplateStoragePoolVO templatePoolVO : templatePoolVOs) {
            if ((templateForVmCreation != null) && !tmpinstalled && (templatePoolVO.getTemplateId() == templateForVmCreation.getId())) {
                tmpinstalled = true;
            }
            final long templateSize = templatePoolVO.getTemplateSize();
            totalAllocatedSize += templateSize + _extraBytesPerVolume;
        }

        return totalAllocatedSize;
    }

    @Override
    public boolean checkIfHostReachMaxGuestLimit(final Host host) {
        final Long vmCount = this._vmDao.countActiveByHostId(host.getId());
        final HypervisorType hypervisorType = host.getHypervisorType();
        final String hypervisorVersion = host.getHypervisorVersion();
        final Long maxGuestLimit = this._hypervisorCapabilitiesDao.getMaxGuestsLimit(hypervisorType, hypervisorVersion);
        if (vmCount >= maxGuestLimit) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Host name: " + host.getName() + ", hostId: " + host.getId() + " already reached max Running VMs(count includes system VMs), limit is: " +
                        maxGuestLimit + ",Running VM counts is: " + vmCount);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean checkIfHostHasCpuCapability(final long hostId, final Integer cpuNum) {

        // Check host can support the Cpu Number.
        final Host host = this._hostDao.findById(hostId);
        final boolean isCpuNumGood = host.getCpus() >= cpuNum;
        if (isCpuNumGood) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Host: " + hostId + " has cpu capability (cpu:" + host.getCpus() + ") to support requested CPU: " + cpuNum);
            }
            return true;
        } else {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Host: " + hostId + " doesn't have cpu capability (cpu:" + host.getCpus() + ") to support requested CPU: " + cpuNum);
            }
            return false;
        }
    }

    @Override
    public boolean checkIfClusterCrossesThreshold(final Long clusterId, final Integer cpuRequested, final long ramRequested) {

        final Float clusterCpuOverProvisioning = getClusterOverProvisioningFactor(clusterId, Capacity.CAPACITY_TYPE_CPU);
        final Float clusterMemoryOverProvisioning = getClusterOverProvisioningFactor(clusterId, Capacity.CAPACITY_TYPE_MEMORY);
        final Float clusterCpuCapacityDisableThreshold = DeploymentClusterPlanner.ClusterCPUCapacityDisableThreshold.valueIn(clusterId);
        final Float clusterMemoryCapacityDisableThreshold = DeploymentClusterPlanner.ClusterMemoryCapacityDisableThreshold.valueIn(clusterId);

        final float cpuConsumption = this._capacityDao.findClusterConsumption(clusterId, Capacity.CAPACITY_TYPE_CPU, cpuRequested);
        if (cpuConsumption / clusterCpuOverProvisioning > clusterCpuCapacityDisableThreshold) {
            s_logger.debug("Cluster: " + clusterId + " cpu consumption " + cpuConsumption / clusterCpuOverProvisioning
                    + " crosses disable threshold " + clusterCpuCapacityDisableThreshold);
            return true;
        }

        final float memoryConsumption = this._capacityDao.findClusterConsumption(clusterId, Capacity.CAPACITY_TYPE_MEMORY, ramRequested);
        if (memoryConsumption / clusterMemoryOverProvisioning > clusterMemoryCapacityDisableThreshold) {
            s_logger.debug("Cluster: " + clusterId + " memory consumption " + memoryConsumption / clusterMemoryOverProvisioning
                    + " crosses disable threshold " + clusterMemoryCapacityDisableThreshold);
            return true;
        }

        return false;
    }

    @Override
    public float getClusterOverProvisioningFactor(final Long clusterId, final short capacityType) {

        final String capacityOverProvisioningName;
        if (capacityType == Capacity.CAPACITY_TYPE_CPU) {
            capacityOverProvisioningName = "cpuOvercommitRatio";
        } else if (capacityType == Capacity.CAPACITY_TYPE_MEMORY) {
            capacityOverProvisioningName = "memoryOvercommitRatio";
        } else {
            throw new CloudRuntimeException("Invalid capacityType - " + capacityType);
        }

        final ClusterDetailsVO clusterDetailCpu = this._clusterDetailsDao.findDetail(clusterId, capacityOverProvisioningName);
        return Float.parseFloat(clusterDetailCpu.getValue());
    }

    @Override
    public long getUsedBytes(final StoragePoolVO pool) {
        final DataStoreProvider storeProvider = this._dataStoreProviderMgr.getDataStoreProvider(pool.getStorageProviderName());
        final DataStoreDriver storeDriver = storeProvider.getDataStoreDriver();

        if (storeDriver instanceof PrimaryDataStoreDriver) {
            final PrimaryDataStoreDriver primaryStoreDriver = (PrimaryDataStoreDriver) storeDriver;

            return primaryStoreDriver.getUsedBytes(pool);
        }

        throw new CloudRuntimeException("Storage driver in CapacityManagerImpl.getUsedBytes(StoragePoolVO) is not a PrimaryDataStoreDriver.");
    }

    @Override
    public long getUsedIops(final StoragePoolVO pool) {
        final DataStoreProvider storeProvider = this._dataStoreProviderMgr.getDataStoreProvider(pool.getStorageProviderName());
        final DataStoreDriver storeDriver = storeProvider.getDataStoreDriver();

        if (storeDriver instanceof PrimaryDataStoreDriver) {
            final PrimaryDataStoreDriver primaryStoreDriver = (PrimaryDataStoreDriver) storeDriver;

            return primaryStoreDriver.getUsedIops(pool);
        }

        throw new CloudRuntimeException("Storage driver in CapacityManagerImpl.getUsedIops(StoragePoolVO) is not a PrimaryDataStoreDriver.");
    }

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
        // TODO Auto-generated method stub

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
    public void processDiscoverEventBefore(final Long dcid, final Long podId, final Long clusterId, final URI uri, final String username, final String password, final
    List<String> hostTags) {
        // TODO Auto-generated method stub

    }

    @Override
    public void processDiscoverEventAfter(final Map<? extends ServerResource, Map<String, String>> resources) {
        // TODO Auto-generated method stub

    }

    @Override
    public void processDeleteHostEventBefore(final Host host) {
        // TODO Auto-generated method stub

    }

    @Override
    public void processDeletHostEventAfter(final Host host) {
        // TODO Auto-generated method stub

    }

    @Override
    public void processCancelMaintenaceEventBefore(final Long hostId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void processCancelMaintenaceEventAfter(final Long hostId) {
        updateCapacityForHost(this._hostDao.findById(hostId));
    }

    @Override
    public void processPrepareMaintenaceEventBefore(final Long hostId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void processPrepareMaintenaceEventAfter(final Long hostId) {
        this._capacityDao.removeBy(Capacity.CAPACITY_TYPE_MEMORY, null, null, null, hostId);
        this._capacityDao.removeBy(Capacity.CAPACITY_TYPE_CPU, null, null, null, hostId);
    }

    @Override
    public String getConfigComponentName() {
        return CapacityManager.class.getSimpleName();
    }

    @Override
    public ConfigKey<?>[] getConfigKeys() {
        return new ConfigKey<?>[]{CpuOverprovisioningFactor, MemOverprovisioningFactor, StorageCapacityDisableThreshold, StorageOverprovisioningFactor,
                StorageAllocatedCapacityDisableThreshold};
    }
}
