package org.apache.cloudstack.engine.cloud.entity.api;

import com.cloud.dc.ClusterVO;
import com.cloud.dc.DataCenter;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.deploy.DataCenterDeployment;
import com.cloud.deploy.DeployDestination;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.deploy.DeploymentPlanningManager;
import com.cloud.exception.AffinityConflictException;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InsufficientServerCapacityException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.dao.NetworkDao;
import com.cloud.org.Cluster;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.StoragePool;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.DiskOfferingDao;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserDao;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineManager;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.VirtualMachineProfileImpl;
import com.cloud.vm.dao.VMInstanceDao;
import org.apache.cloudstack.affinity.dao.AffinityGroupVMMapDao;
import org.apache.cloudstack.engine.cloud.entity.api.db.VMEntityVO;
import org.apache.cloudstack.engine.cloud.entity.api.db.VMReservationVO;
import org.apache.cloudstack.engine.cloud.entity.api.db.dao.VMEntityDao;
import org.apache.cloudstack.engine.cloud.entity.api.db.dao.VMReservationDao;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VMEntityManagerImpl implements VMEntityManager {

    private static final Logger s_logger = LoggerFactory.getLogger(VMEntityManagerImpl.class);

    @Inject
    protected VMInstanceDao _vmDao;
    @Inject
    protected VMTemplateDao _templateDao = null;

    @Inject
    protected ServiceOfferingDao _serviceOfferingDao;

    @Inject
    protected DiskOfferingDao _diskOfferingDao = null;

    @Inject
    protected NetworkDao _networkDao;

    @Inject
    protected AccountDao _accountDao = null;

    @Inject
    protected UserDao _userDao = null;

    @Inject
    protected ClusterDao _clusterDao;

    @Inject
    protected VMEntityDao _vmEntityDao;

    @Inject
    protected VMReservationDao _reservationDao;

    @Inject
    protected VirtualMachineManager _itMgr;

    protected List<DeploymentPlanner> _planners;

    @Inject
    protected VolumeDao _volsDao;

    @Inject
    protected PrimaryDataStoreDao _storagePoolDao;
    @Inject
    protected AffinityGroupVMMapDao _affinityGroupVMMapDao;
    @Inject
    DataStoreManager dataStoreMgr;
    @Inject
    DeploymentPlanningManager _dpMgr;
    @Inject
    DeploymentPlanningManager _planningMgr;

    @Override
    public VMEntityVO loadVirtualMachine(final String vmId) {
        // TODO Auto-generated method stub
        return _vmEntityDao.findByUuid(vmId);
    }

    @Override
    public void saveVirtualMachine(final VMEntityVO entity) {
        _vmEntityDao.persist(entity);
    }

    @Override
    public String reserveVirtualMachine(final VMEntityVO vmEntityVO, final DeploymentPlanner plannerToUse, final DeploymentPlan planToDeploy, final ExcludeList exclude)
            throws InsufficientCapacityException, ResourceUnavailableException {

        //call planner and get the deployDestination.
        //load vm instance and offerings and call virtualMachineManagerImpl
        //FIXME: profile should work on VirtualMachineEntity
        final VMInstanceVO vm = _vmDao.findByUuid(vmEntityVO.getUuid());
        final VirtualMachineProfileImpl vmProfile = new VirtualMachineProfileImpl(vm);
        vmProfile.setServiceOffering(_serviceOfferingDao.findByIdIncludingRemoved(vm.getId(), vm.getServiceOfferingId()));
        DataCenterDeployment plan = new DataCenterDeployment(vm.getDataCenterId(), vm.getPodIdToDeployIn(), null, null, null, null);
        if (planToDeploy != null && planToDeploy.getDataCenterId() != 0) {
            plan =
                    new DataCenterDeployment(planToDeploy.getDataCenterId(), planToDeploy.getPodId(), planToDeploy.getClusterId(), planToDeploy.getHostId(),
                            planToDeploy.getPoolId(), planToDeploy.getPhysicalNetworkId());
        }

        boolean planChangedByReadyVolume = false;
        final List<VolumeVO> vols = _volsDao.findReadyRootVolumesByInstance(vm.getId());
        if (!vols.isEmpty()) {
            final VolumeVO vol = vols.get(0);
            final StoragePool pool = (StoragePool) dataStoreMgr.getPrimaryDataStore(vol.getPoolId());

            if (!pool.isInMaintenance()) {
                final long rootVolDcId = pool.getDataCenterId();
                final Long rootVolPodId = pool.getPodId();
                final Long rootVolClusterId = pool.getClusterId();
                if (planToDeploy != null && planToDeploy.getDataCenterId() != 0) {
                    final Long clusterIdSpecified = planToDeploy.getClusterId();
                    if (clusterIdSpecified != null && rootVolClusterId != null) {
                        checkIfPlanIsDeployable(vm, rootVolClusterId, clusterIdSpecified);
                    }
                    plan =
                            new DataCenterDeployment(planToDeploy.getDataCenterId(), planToDeploy.getPodId(), planToDeploy.getClusterId(), planToDeploy.getHostId(),
                                    vol.getPoolId(), null, null);
                } else {
                    plan = new DataCenterDeployment(rootVolDcId, rootVolPodId, rootVolClusterId, null, vol.getPoolId(), null, null);
                    planChangedByReadyVolume = true;
                }
            }
        }

        while (true) {
            DeployDestination dest = null;
            try {
                dest = _dpMgr.planDeployment(vmProfile, plan, exclude, plannerToUse);
            } catch (final AffinityConflictException e) {
                throw new CloudRuntimeException("Unable to create deployment, affinity rules associated to the VM conflict");
            }

            if (dest != null) {
                final String reservationId = _dpMgr.finalizeReservation(dest, vmProfile, plan, exclude, plannerToUse);
                if (reservationId != null) {
                    return reservationId;
                } else {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Cannot finalize the VM reservation for this destination found, retrying");
                    }
                    exclude.addHost(dest.getHost().getId());
                    continue;
                }
            } else if (planChangedByReadyVolume) {
                // we could not reserve in the Volume's cluster - let the deploy
                // call retry it.
                return UUID.randomUUID().toString();
            } else {
                throw new InsufficientServerCapacityException("Unable to create a deployment for " + vmProfile, DataCenter.class, plan.getDataCenterId(),
                        areAffinityGroupsAssociated(vmProfile));
            }
        }
    }

    private void checkIfPlanIsDeployable(final VMInstanceVO vm, final Long rootVolClusterId, final Long clusterIdSpecified) throws ResourceUnavailableException {
        if (rootVolClusterId.longValue() != clusterIdSpecified.longValue()) {
            // cannot satisfy the plan passed in to the planner
            final ClusterVO volumeCluster = _clusterDao.findById(rootVolClusterId);
            final ClusterVO vmCluster = _clusterDao.findById(clusterIdSpecified);

            final String errorMsg;
            errorMsg = String.format("Root volume is ready in cluster '%s' while VM is to be started in cluster '%s'. Make sure these match. Unable to create a deployment for %s",
                    volumeCluster.getName(), vmCluster.getName(), vm);

            if (s_logger.isDebugEnabled()) {
                s_logger.debug(errorMsg);
            }
            throw new ResourceUnavailableException(errorMsg, Cluster.class, clusterIdSpecified);
        }
    }

    protected boolean areAffinityGroupsAssociated(final VirtualMachineProfile vmProfile) {
        final VirtualMachine vm = vmProfile.getVirtualMachine();
        final long vmGroupCount = _affinityGroupVMMapDao.countAffinityGroupsForVm(vm.getId());

        if (vmGroupCount > 0) {
            return true;
        }
        return false;
    }

    @Override
    public void deployVirtualMachine(final String reservationId, final VMEntityVO vmEntityVO, final String caller, final Map<VirtualMachineProfile.Param, Object> params)
            throws InsufficientCapacityException, ResourceUnavailableException {
        //grab the VM Id and destination using the reservationId.

        final VMInstanceVO vm = _vmDao.findByUuid(vmEntityVO.getUuid());

        final VMReservationVO vmReservation = _reservationDao.findByReservationId(reservationId);
        if (vmReservation != null) {

            final DataCenterDeployment reservedPlan =
                    new DataCenterDeployment(vm.getDataCenterId(), vmReservation.getPodId(), vmReservation.getClusterId(), vmReservation.getHostId(), null, null);
            try {
                _itMgr.start(vm.getUuid(), params, reservedPlan, _planningMgr.getDeploymentPlannerByName(vmReservation.getDeploymentPlanner()));
            } catch (final Exception ex) {
                // Retry the deployment without using the reservation plan
                final DataCenterDeployment plan = new DataCenterDeployment(0, null, null, null, null, null);

                if (reservedPlan.getAvoids() != null) {
                    plan.setAvoids(reservedPlan.getAvoids());
                }

                _itMgr.start(vm.getUuid(), params, plan, null);
            }
        } else {
            // no reservation found. Let VirtualMachineManager retry
            _itMgr.start(vm.getUuid(), params, null, null);
        }
    }

    @Override
    public boolean stopvirtualmachine(final VMEntityVO vmEntityVO, final String caller) throws ResourceUnavailableException {
        _itMgr.stop(vmEntityVO.getUuid());
        return true;
    }

    @Override
    public boolean destroyVirtualMachine(final VMEntityVO vmEntityVO, final String caller) throws AgentUnavailableException, OperationTimedoutException,
            ConcurrentOperationException {

        final VMInstanceVO vm = _vmDao.findByUuid(vmEntityVO.getUuid());
        _itMgr.destroy(vm.getUuid());
        return true;
    }

    public List<DeploymentPlanner> getPlanners() {
        return _planners;
    }

    @Inject
    public void setPlanners(final List<DeploymentPlanner> planners) {
        this._planners = planners;
    }
}
