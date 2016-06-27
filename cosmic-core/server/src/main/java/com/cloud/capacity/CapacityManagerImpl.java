package com.cloud.capacity;

import com.cloud.agent.AgentManager;
import com.cloud.agent.Listener;
import com.cloud.agent.api.AgentControlAnswer;
import com.cloud.agent.api.AgentControlCommand;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupRoutingCommand;
import com.cloud.capacity.dao.CapacityDao;
import com.cloud.configuration.Config;
import com.cloud.configuration.ConfigurationManager;
import com.cloud.dc.ClusterDetailsDao;
import com.cloud.dc.ClusterDetailsVO;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.deploy.DeploymentClusterPlanner;
import com.cloud.event.UsageEventVO;
import com.cloud.exception.ConnectionException;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.hypervisor.dao.HypervisorCapabilitiesDao;
import com.cloud.offering.ServiceOffering;
import com.cloud.resource.ResourceListener;
import com.cloud.resource.ResourceManager;
import com.cloud.resource.ResourceState;
import com.cloud.resource.ServerResource;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.StorageManager;
import com.cloud.storage.VMTemplateStoragePoolVO;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.VMTemplatePoolDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.utils.DateUtil;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallbackNoReturn;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.fsm.StateListener;
import com.cloud.utils.fsm.StateMachine2;
import com.cloud.vm.UserVmDetailVO;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.Event;
import com.cloud.vm.VirtualMachine.State;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.dao.UserVmDetailsDao;
import com.cloud.vm.dao.VMInstanceDao;
import com.cloud.vm.snapshot.dao.VMSnapshotDao;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreDriver;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreProvider;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreProviderManager;
import org.apache.cloudstack.engine.subsystem.api.storage.PrimaryDataStoreDriver;
import org.apache.cloudstack.framework.config.ConfigDepot;
import org.apache.cloudstack.framework.config.ConfigKey;
import org.apache.cloudstack.framework.config.Configurable;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.framework.messagebus.MessageBus;
import org.apache.cloudstack.framework.messagebus.PublishScope;
import org.apache.cloudstack.storage.datastore.db.StoragePoolVO;

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
    protected UserVmDao _userVMDao;
    @Inject
    protected UserVmDetailsDao _userVmDetailsDao;
    @Inject
    CapacityDao _capacityDao;
    @Inject
    ConfigurationDao _configDao;
    @Inject
    ServiceOfferingDao _offeringsDao;
    @Inject
    HostDao _hostDao;
    @Inject
    VMInstanceDao _vmDao;
    @Inject
    VolumeDao _volumeDao;
    @Inject
    VMTemplatePoolDao _templatePoolDao;
    @Inject
    AgentManager _agentManager;
    @Inject
    ResourceManager _resourceMgr;
    @Inject
    StorageManager _storageMgr;
    @Inject
    ConfigurationManager _configMgr;
    @Inject
    HypervisorCapabilitiesDao _hypervisorCapabilitiesDao;
    @Inject
    ClusterDao _clusterDao;
    @Inject
    ConfigDepot _configDepot;
    @Inject
    DataStoreProviderManager _dataStoreProviderMgr;
    @Inject
    ClusterDetailsDao _clusterDetailsDao;
    long _extraBytesPerVolume = 0;
    @Inject
    MessageBus _messageBus;
    private int _vmCapacityReleaseInterval;
    private ScheduledExecutorService _executor;

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        _vmCapacityReleaseInterval = NumbersUtil.parseInt(_configDao.getValue(Config.CapacitySkipcountingHours.key()), 3600);

        _executor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("HostCapacity-Checker"));
        VirtualMachine.State.getStateMachine().registerListener(this);
        _agentManager.registerForHostEvents(new StorageCapacityListener(_capacityDao, _storageMgr), true, false, false);
        _agentManager.registerForHostEvents(new ComputeCapacityListener(_capacityDao, this), true, false, false);

        return true;
    }

    @Override
    public boolean start() {
        _resourceMgr.registerResourceEvent(ResourceListener.EVENT_PREPARE_MAINTENANCE_AFTER, this);
        _resourceMgr.registerResourceEvent(ResourceListener.EVENT_CANCEL_MAINTENANCE_AFTER, this);
        return true;
    }

    @Override
    public boolean stop() {
        _executor.shutdownNow();
        return true;
    }

    @Override
    public boolean preStateTransitionEvent(final State oldState, final Event event, final State newState, final VirtualMachine vm, final boolean transitionStatus, final Object
            opaque) {
        return true;
    }

    @Override
    public boolean postStateTransitionEvent(final StateMachine2.Transition<State, Event> transition, final VirtualMachine vm, final boolean status, final Object opaque) {
        if (!status) {
            return false;
        }
        final
        Pair<Long, Long> hosts = (Pair<Long, Long>) opaque;
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
            if (vm.getType() == VirtualMachine.Type.User) {

                final UserVmVO userVM = _userVMDao.findById(vm.getId());
                _userVMDao.loadDetails(userVM);
                // free the message sent flag if it exists
                userVM.setDetail(MESSAGE_RESERVED_CAPACITY_FREED_FLAG, "false");
                _userVMDao.saveDetails(userVM);
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

        final ServiceOfferingVO svo = _offeringsDao.findById(vm.getId(), vm.getServiceOfferingId());
        final CapacityVO capacityCpu = _capacityDao.findByHostIdType(hostId, Capacity.CAPACITY_TYPE_CPU);
        final CapacityVO capacityMemory = _capacityDao.findByHostIdType(hostId, Capacity.CAPACITY_TYPE_MEMORY);
        Long clusterId = null;
        if (hostId != null) {
            final HostVO host = _hostDao.findById(hostId);
            if (host == null) {
                s_logger.warn("Host " + hostId + " no long exist anymore!");
                return true;
            }

            clusterId = host.getClusterId();
        }
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
                    final CapacityVO capacityCpu = _capacityDao.lockRow(capacityCpuId, true);
                    final CapacityVO capacityMemory = _capacityDao.lockRow(capacityMemoryId, true);

                    final long usedCpu = capacityCpu.getUsedCapacity();
                    final long usedMem = capacityMemory.getUsedCapacity();
                    final long reservedCpu = capacityCpu.getReservedCapacity();
                    final long reservedMem = capacityMemory.getReservedCapacity();
                    final long actualTotalCpu = capacityCpu.getTotalCapacity();
                    final float cpuOvercommitRatio = Float.parseFloat(_clusterDetailsDao.findDetail(clusterIdFinal, "cpuOvercommitRatio").getValue());
                    final float memoryOvercommitRatio = Float.parseFloat(_clusterDetailsDao.findDetail(clusterIdFinal, "memoryOvercommitRatio").getValue());
                    final int vmCPU = svo.getCpu() * svo.getSpeed();
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

                    _capacityDao.update(capacityCpu.getId(), capacityCpu);
                    _capacityDao.update(capacityMemory.getId(), capacityMemory);
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
        final HostVO host = _hostDao.findById(hostId);
        final long clusterId = host.getClusterId();
        final float cpuOvercommitRatio = Float.parseFloat(_clusterDetailsDao.findDetail(clusterId, "cpuOvercommitRatio").getValue());
        final float memoryOvercommitRatio = Float.parseFloat(_clusterDetailsDao.findDetail(clusterId, "memoryOvercommitRatio").getValue());

        final ServiceOfferingVO svo = _offeringsDao.findById(vm.getId(), vm.getServiceOfferingId());

        final CapacityVO capacityCpu = _capacityDao.findByHostIdType(hostId, Capacity.CAPACITY_TYPE_CPU);
        final CapacityVO capacityMem = _capacityDao.findByHostIdType(hostId, Capacity.CAPACITY_TYPE_MEMORY);

        if (capacityCpu == null || capacityMem == null || svo == null) {
            return;
        }

        final int cpu = svo.getCpu() * svo.getSpeed();
        final long ram = svo.getRamSize() * 1024L * 1024L;

        try {
            final long capacityCpuId = capacityCpu.getId();
            final long capacityMemId = capacityMem.getId();

            Transaction.execute(new TransactionCallbackNoReturn() {
                @Override
                public void doInTransactionWithoutResult(final TransactionStatus status) {
                    final CapacityVO capacityCpu = _capacityDao.lockRow(capacityCpuId, true);
                    final CapacityVO capacityMem = _capacityDao.lockRow(capacityMemId, true);

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

                    _capacityDao.update(capacityCpu.getId(), capacityCpu);
                    _capacityDao.update(capacityMem.getId(), capacityMem);
                }
            });
        } catch (final Exception e) {
            s_logger.error("Exception allocating VM capacity", e);
            return;
        }
    }

    @Override
    public boolean checkIfHostHasCapacity(final long hostId, final Integer cpu, final long ram, final boolean checkFromReservedCapacity, final float cpuOvercommitRatio, final
    float memoryOvercommitRatio,
                                          final boolean considerReservedCapacity) {
        boolean hasCapacity = false;

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Checking if host: " + hostId + " has enough capacity for requested CPU: " + cpu + " and requested RAM: " + ram +
                    " , cpuOverprovisioningFactor: " + cpuOvercommitRatio);
        }

        final CapacityVO capacityCpu = _capacityDao.findByHostIdType(hostId, Capacity.CAPACITY_TYPE_CPU);
        final CapacityVO capacityMem = _capacityDao.findByHostIdType(hostId, Capacity.CAPACITY_TYPE_MEMORY);

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
            final long freeCpu = reservedCpu;
            final long freeMem = reservedMem;

            if (s_logger.isDebugEnabled()) {
                s_logger.debug("We need to allocate to the last host again, so checking if there is enough reserved capacity");
                s_logger.debug("Reserved CPU: " + freeCpu + " , Requested CPU: " + cpu);
                s_logger.debug("Reserved RAM: " + freeMem + " , Requested RAM: " + ram);
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
                    s_logger.debug("considerReservedCapacity is" + considerReservedCapacity + " , not considering reserved capacity for calculating free capacity");
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
        final List<ServiceOfferingVO> offerings = _offeringsDao.listAllIncludingRemoved();
        final Map<Long, ServiceOfferingVO> offeringsMap = new HashMap<>();
        for (final ServiceOfferingVO offering : offerings) {
            offeringsMap.put(offering.getId(), offering);
        }

        long usedCpu = 0;
        long usedMemory = 0;
        long reservedMemory = 0;
        long reservedCpu = 0;
        final CapacityState capacityState = (host.getResourceState() == ResourceState.Enabled) ? CapacityState.Enabled : CapacityState.Disabled;

        final List<VMInstanceVO> vms = _vmDao.listUpByHostId(host.getId());
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Found " + vms.size() + " VMs on host " + host.getId());
        }

        final ClusterVO cluster = _clusterDao.findById(host.getClusterId());
        final ClusterDetailsVO clusterDetailCpu = _clusterDetailsDao.findDetail(cluster.getId(), "cpuOvercommitRatio");
        final ClusterDetailsVO clusterDetailRam = _clusterDetailsDao.findDetail(cluster.getId(), "memoryOvercommitRatio");
        final Float clusterCpuOvercommitRatio = Float.parseFloat(clusterDetailCpu.getValue());
        final Float clusterRamOvercommitRatio = Float.parseFloat(clusterDetailRam.getValue());
        Float cpuOvercommitRatio = 1f;
        Float ramOvercommitRatio = 1f;
        for (final VMInstanceVO vm : vms) {
            final Map<String, String> vmDetails = _userVmDetailsDao.listDetailsKeyPairs(vm.getId());
            final String vmDetailCpu = vmDetails.get("cpuOvercommitRatio");
            final String vmDetailRam = vmDetails.get("memoryOvercommitRatio");
            if (vmDetailCpu != null) {
                //if vmDetail_cpu is not null it means it is running in a overcommited cluster.
                cpuOvercommitRatio = Float.parseFloat(vmDetailCpu);
                ramOvercommitRatio = Float.parseFloat(vmDetailRam);
            }
            final ServiceOffering so = offeringsMap.get(vm.getServiceOfferingId());
            if (so.isDynamic()) {
                usedMemory +=
                        ((Integer.parseInt(vmDetails.get(UsageEventVO.DynamicParameters.memory.name())) * 1024L * 1024L) / ramOvercommitRatio) *
                                clusterRamOvercommitRatio;
                usedCpu +=
                        ((Integer.parseInt(vmDetails.get(UsageEventVO.DynamicParameters.cpuNumber.name())) * Integer.parseInt(vmDetails.get(UsageEventVO.DynamicParameters
                                .cpuSpeed.name()))) / cpuOvercommitRatio) *
                                clusterCpuOvercommitRatio;
            } else {
                usedMemory += ((so.getRamSize() * 1024L * 1024L) / ramOvercommitRatio) * clusterRamOvercommitRatio;
                usedCpu += ((so.getCpu() * so.getSpeed()) / cpuOvercommitRatio) * clusterCpuOvercommitRatio;
            }
        }

        final List<VMInstanceVO> vmsByLastHostId = _vmDao.listByLastHostId(host.getId());
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Found " + vmsByLastHostId.size() + " VM, not running on host " + host.getId());
        }
        for (final VMInstanceVO vm : vmsByLastHostId) {
            final long secondsSinceLastUpdate = (DateUtil.currentGMTTime().getTime() - vm.getUpdateTime().getTime()) / 1000;
            if (secondsSinceLastUpdate < _vmCapacityReleaseInterval) {
                final UserVmDetailVO vmDetailCpu = _userVmDetailsDao.findDetail(vm.getId(), "cpuOvercommitRatio");
                final UserVmDetailVO vmDetailRam = _userVmDetailsDao.findDetail(vm.getId(), "memoryOvercommitRatio");
                if (vmDetailCpu != null) {
                    //if vmDetail_cpu is not null it means it is running in a overcommited cluster.
                    cpuOvercommitRatio = Float.parseFloat(vmDetailCpu.getValue());
                    ramOvercommitRatio = Float.parseFloat(vmDetailRam.getValue());
                }
                final ServiceOffering so = offeringsMap.get(vm.getServiceOfferingId());
                final Map<String, String> vmDetails = _userVmDetailsDao.listDetailsKeyPairs(vm.getId());
                if (so.isDynamic()) {
                    reservedMemory +=
                            ((Integer.parseInt(vmDetails.get(UsageEventVO.DynamicParameters.memory.name())) * 1024L * 1024L) / ramOvercommitRatio) *
                                    clusterRamOvercommitRatio;
                    reservedCpu +=
                            ((Integer.parseInt(vmDetails.get(UsageEventVO.DynamicParameters.cpuNumber.name())) * Integer.parseInt(vmDetails.get(UsageEventVO.DynamicParameters
                                    .cpuSpeed.name()))) / cpuOvercommitRatio) *
                                    clusterCpuOvercommitRatio;
                } else {
                    reservedMemory += ((so.getRamSize() * 1024L * 1024L) / ramOvercommitRatio) * clusterRamOvercommitRatio;
                    reservedCpu += (so.getCpu() * so.getSpeed() / cpuOvercommitRatio) * clusterCpuOvercommitRatio;
                }
            } else {
                // signal if not done already, that the VM has been stopped for skip.counting.hours,
                // hence capacity will not be reserved anymore.
                final UserVmDetailVO messageSentFlag = _userVmDetailsDao.findDetail(vm.getId(), MESSAGE_RESERVED_CAPACITY_FREED_FLAG);
                if (messageSentFlag == null || !Boolean.valueOf(messageSentFlag.getValue())) {
                    _messageBus.publish(_name, "VM_ReservedCapacity_Free", PublishScope.LOCAL, vm);

                    if (vm.getType() == VirtualMachine.Type.User) {
                        final UserVmVO userVM = _userVMDao.findById(vm.getId());
                        _userVMDao.loadDetails(userVM);
                        userVM.setDetail(MESSAGE_RESERVED_CAPACITY_FREED_FLAG, "true");
                        _userVMDao.saveDetails(userVM);
                    }
                }
            }
        }

        final CapacityVO cpuCap = _capacityDao.findByHostIdType(host.getId(), Capacity.CAPACITY_TYPE_CPU);
        final CapacityVO memCap = _capacityDao.findByHostIdType(host.getId(), Capacity.CAPACITY_TYPE_MEMORY);
        if (cpuCap != null && memCap != null) {
            if (host.getTotalMemory() != null) {
                memCap.setTotalCapacity(host.getTotalMemory());
            }
            final long hostTotalCpu = host.getCpus().longValue() * host.getSpeed().longValue();

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
                _capacityDao.update(cpuCap.getId(), cpuCap);
                _capacityDao.update(memCap.getId(), memCap);
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
                    _capacityDao.persist(capacity);

                    capacity =
                            new CapacityVO(host.getId(), host.getDataCenterId(), host.getPodId(), host.getClusterId(), usedCpuFinal, host.getCpus().longValue() *
                                    host.getSpeed().longValue(), Capacity.CAPACITY_TYPE_CPU);
                    capacity.setReservedCapacity(reservedCpuFinal);
                    capacity.setCapacityState(capacityState);
                    _capacityDao.persist(capacity);
                }
            });
        }
    }

    @Override
    public long getAllocatedPoolCapacity(final StoragePoolVO pool, final VMTemplateVO templateForVmCreation) {
        long totalAllocatedSize = 0;

        // if the storage pool is managed, the used bytes can be larger than the sum of the sizes of all of the non-destroyed volumes
        // in this case, call getUsedBytes(StoragePoolVO)
        if (pool.isManaged()) {
            return getUsedBytes(pool);
        } else {
            // Get size for all the non-destroyed volumes
            final Pair<Long, Long> sizes = _volumeDao.getNonDestroyedCountAndTotalByPool(pool.getId());

            totalAllocatedSize = sizes.second() + sizes.first() * _extraBytesPerVolume;
        }

        // Get size for VM Snapshots
        totalAllocatedSize = totalAllocatedSize + _volumeDao.getVMSnapshotSizeByPool(pool.getId());

        // Iterate through all templates on this storage pool
        boolean tmpinstalled = false;
        final List<VMTemplateStoragePoolVO> templatePoolVOs;
        templatePoolVOs = _templatePoolDao.listByPoolId(pool.getId());

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
        final Long vmCount = _vmDao.countActiveByHostId(host.getId());
        final HypervisorType hypervisorType = host.getHypervisorType();
        final String hypervisorVersion = host.getHypervisorVersion();
        final Long maxGuestLimit = _hypervisorCapabilitiesDao.getMaxGuestsLimit(hypervisorType, hypervisorVersion);
        if (vmCount.longValue() >= maxGuestLimit.longValue()) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Host name: " + host.getName() + ", hostId: " + host.getId() + " already reached max Running VMs(count includes system VMs), limit is: " +
                        maxGuestLimit + ",Running VM counts is: " + vmCount.longValue());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean checkIfHostHasCpuCapability(final long hostId, final Integer cpuNum, final Integer cpuSpeed) {

        // Check host can support the Cpu Number and Speed.
        final Host host = _hostDao.findById(hostId);
        final boolean isCpuNumGood = host.getCpus().intValue() >= cpuNum;
        final boolean isCpuSpeedGood = host.getSpeed().intValue() >= cpuSpeed;
        if (isCpuNumGood && isCpuSpeedGood) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Host: " + hostId + " has cpu capability (cpu:" + host.getCpus() + ", speed:" + host.getSpeed() +
                        ") to support requested CPU: " + cpuNum + " and requested speed: " + cpuSpeed);
            }
            return true;
        } else {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Host: " + hostId + " doesn't have cpu capability (cpu:" + host.getCpus() + ", speed:" + host.getSpeed() +
                        ") to support requested CPU: " + cpuNum + " and requested speed: " + cpuSpeed);
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

        final float cpuConsumption = _capacityDao.findClusterConsumption(clusterId, Capacity.CAPACITY_TYPE_CPU, cpuRequested);
        if (cpuConsumption / clusterCpuOverProvisioning > clusterCpuCapacityDisableThreshold) {
            s_logger.debug("Cluster: " + clusterId + " cpu consumption " + cpuConsumption / clusterCpuOverProvisioning
                    + " crosses disable threshold " + clusterCpuCapacityDisableThreshold);
            return true;
        }

        final float memoryConsumption = _capacityDao.findClusterConsumption(clusterId, Capacity.CAPACITY_TYPE_MEMORY, ramRequested);
        if (memoryConsumption / clusterMemoryOverProvisioning > clusterMemoryCapacityDisableThreshold) {
            s_logger.debug("Cluster: " + clusterId + " memory consumption " + memoryConsumption / clusterMemoryOverProvisioning
                    + " crosses disable threshold " + clusterMemoryCapacityDisableThreshold);
            return true;
        }

        return false;
    }

    @Override
    public float getClusterOverProvisioningFactor(final Long clusterId, final short capacityType) {

        String capacityOverProvisioningName = "";
        if (capacityType == Capacity.CAPACITY_TYPE_CPU) {
            capacityOverProvisioningName = "cpuOvercommitRatio";
        } else if (capacityType == Capacity.CAPACITY_TYPE_MEMORY) {
            capacityOverProvisioningName = "memoryOvercommitRatio";
        } else {
            throw new CloudRuntimeException("Invalid capacityType - " + capacityType);
        }

        final ClusterDetailsVO clusterDetailCpu = _clusterDetailsDao.findDetail(clusterId, capacityOverProvisioningName);
        final Float clusterOverProvisioningRatio = Float.parseFloat(clusterDetailCpu.getValue());
        return clusterOverProvisioningRatio;
    }

    @Override
    public long getUsedBytes(final StoragePoolVO pool) {
        final DataStoreProvider storeProvider = _dataStoreProviderMgr.getDataStoreProvider(pool.getStorageProviderName());
        final DataStoreDriver storeDriver = storeProvider.getDataStoreDriver();

        if (storeDriver instanceof PrimaryDataStoreDriver) {
            final PrimaryDataStoreDriver primaryStoreDriver = (PrimaryDataStoreDriver) storeDriver;

            return primaryStoreDriver.getUsedBytes(pool);
        }

        throw new CloudRuntimeException("Storage driver in CapacityManagerImpl.getUsedBytes(StoragePoolVO) is not a PrimaryDataStoreDriver.");
    }

    @Override
    public long getUsedIops(final StoragePoolVO pool) {
        final DataStoreProvider storeProvider = _dataStoreProviderMgr.getDataStoreProvider(pool.getStorageProviderName());
        final DataStoreDriver storeDriver = storeProvider.getDataStoreDriver();

        if (storeDriver instanceof PrimaryDataStoreDriver) {
            final PrimaryDataStoreDriver primaryStoreDriver = (PrimaryDataStoreDriver) storeDriver;

            return primaryStoreDriver.getUsedIops(pool);
        }

        throw new CloudRuntimeException("Storage driver in CapacityManagerImpl.getUsedIops(StoragePoolVO) is not a PrimaryDataStoreDriver.");
    }

    // TODO: Get rid of this case once we've determined that the capacity listeners above have all the changes
    // create capacity entries if none exist for this server
    private void createCapacityEntry(final StartupCommand startup, final HostVO server) {
        final SearchCriteria<CapacityVO> capacitySC = _capacityDao.createSearchCriteria();
        capacitySC.addAnd("hostOrPoolId", SearchCriteria.Op.EQ, server.getId());
        capacitySC.addAnd("dataCenterId", SearchCriteria.Op.EQ, server.getDataCenterId());
        capacitySC.addAnd("podId", SearchCriteria.Op.EQ, server.getPodId());

        if (startup instanceof StartupRoutingCommand) {
            final SearchCriteria<CapacityVO> capacityCPU = _capacityDao.createSearchCriteria();
            capacityCPU.addAnd("hostOrPoolId", SearchCriteria.Op.EQ, server.getId());
            capacityCPU.addAnd("dataCenterId", SearchCriteria.Op.EQ, server.getDataCenterId());
            capacityCPU.addAnd("podId", SearchCriteria.Op.EQ, server.getPodId());
            capacityCPU.addAnd("capacityType", SearchCriteria.Op.EQ, Capacity.CAPACITY_TYPE_CPU);
            final List<CapacityVO> capacityVOCpus = _capacityDao.search(capacitySC, null);
            final Float cpuovercommitratio = Float.parseFloat(_clusterDetailsDao.findDetail(server.getClusterId(), "cpuOvercommitRatio").getValue());
            final Float memoryOvercommitRatio = Float.parseFloat(_clusterDetailsDao.findDetail(server.getClusterId(), "memoryOvercommitRatio").getValue());

            if (capacityVOCpus != null && !capacityVOCpus.isEmpty()) {
                final CapacityVO CapacityVOCpu = capacityVOCpus.get(0);
                final long newTotalCpu = (long) (server.getCpus().longValue() * server.getSpeed().longValue() * cpuovercommitratio);
                if ((CapacityVOCpu.getTotalCapacity() <= newTotalCpu) || ((CapacityVOCpu.getUsedCapacity() + CapacityVOCpu.getReservedCapacity()) <= newTotalCpu)) {
                    CapacityVOCpu.setTotalCapacity(newTotalCpu);
                } else if ((CapacityVOCpu.getUsedCapacity() + CapacityVOCpu.getReservedCapacity() > newTotalCpu) && (CapacityVOCpu.getUsedCapacity() < newTotalCpu)) {
                    CapacityVOCpu.setReservedCapacity(0);
                    CapacityVOCpu.setTotalCapacity(newTotalCpu);
                } else {
                    s_logger.debug("What? new cpu is :" + newTotalCpu + ", old one is " + CapacityVOCpu.getUsedCapacity() + "," + CapacityVOCpu.getReservedCapacity() +
                            "," + CapacityVOCpu.getTotalCapacity());
                }
                _capacityDao.update(CapacityVOCpu.getId(), CapacityVOCpu);
            } else {
                final CapacityVO capacity =
                        new CapacityVO(server.getId(), server.getDataCenterId(), server.getPodId(), server.getClusterId(), 0L, server.getCpus().longValue() *
                                server.getSpeed().longValue(), Capacity.CAPACITY_TYPE_CPU);
                _capacityDao.persist(capacity);
            }

            final SearchCriteria<CapacityVO> capacityMem = _capacityDao.createSearchCriteria();
            capacityMem.addAnd("hostOrPoolId", SearchCriteria.Op.EQ, server.getId());
            capacityMem.addAnd("dataCenterId", SearchCriteria.Op.EQ, server.getDataCenterId());
            capacityMem.addAnd("podId", SearchCriteria.Op.EQ, server.getPodId());
            capacityMem.addAnd("capacityType", SearchCriteria.Op.EQ, Capacity.CAPACITY_TYPE_MEMORY);
            final List<CapacityVO> capacityVOMems = _capacityDao.search(capacityMem, null);

            if (capacityVOMems != null && !capacityVOMems.isEmpty()) {
                final CapacityVO CapacityVOMem = capacityVOMems.get(0);
                final long newTotalMem = (long) ((server.getTotalMemory()) * memoryOvercommitRatio);
                if (CapacityVOMem.getTotalCapacity() <= newTotalMem || (CapacityVOMem.getUsedCapacity() + CapacityVOMem.getReservedCapacity() <= newTotalMem)) {
                    CapacityVOMem.setTotalCapacity(newTotalMem);
                } else if (CapacityVOMem.getUsedCapacity() + CapacityVOMem.getReservedCapacity() > newTotalMem && CapacityVOMem.getUsedCapacity() < newTotalMem) {
                    CapacityVOMem.setReservedCapacity(0);
                    CapacityVOMem.setTotalCapacity(newTotalMem);
                } else {
                    s_logger.debug("What? new cpu is :" + newTotalMem + ", old one is " + CapacityVOMem.getUsedCapacity() + "," + CapacityVOMem.getReservedCapacity() +
                            "," + CapacityVOMem.getTotalCapacity());
                }
                _capacityDao.update(CapacityVOMem.getId(), CapacityVOMem);
            } else {
                final CapacityVO capacity =
                        new CapacityVO(server.getId(), server.getDataCenterId(), server.getPodId(), server.getClusterId(), 0L, server.getTotalMemory(),
                                Capacity.CAPACITY_TYPE_MEMORY);
                _capacityDao.persist(capacity);
            }
        }
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
    public void processConnect(final Host host, final StartupCommand cmd, final boolean forRebalance) throws ConnectionException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean processDisconnect(final long agentId, final Status state) {
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
        updateCapacityForHost(_hostDao.findById(hostId));
    }

    @Override
    public void processPrepareMaintenaceEventBefore(final Long hostId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void processPrepareMaintenaceEventAfter(final Long hostId) {
        _capacityDao.removeBy(Capacity.CAPACITY_TYPE_MEMORY, null, null, null, hostId);
        _capacityDao.removeBy(Capacity.CAPACITY_TYPE_CPU, null, null, null, hostId);
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
