package org.apache.cloudstack.affinity;

import com.cloud.configuration.Config;
import com.cloud.deploy.DeployDestination;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.exception.AffinityConflictException;
import com.cloud.utils.DateUtil;
import com.cloud.utils.NumbersUtil;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.dao.VMInstanceDao;
import org.apache.cloudstack.affinity.dao.AffinityGroupDao;
import org.apache.cloudstack.affinity.dao.AffinityGroupVMMapDao;
import org.apache.cloudstack.engine.cloud.entity.api.db.VMReservationVO;
import org.apache.cloudstack.engine.cloud.entity.api.db.dao.VMReservationDao;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostAntiAffinityProcessor extends AffinityProcessorBase implements AffinityGroupProcessor {

    private static final Logger s_logger = LoggerFactory.getLogger(HostAntiAffinityProcessor.class);
    @Inject
    protected UserVmDao _vmDao;
    @Inject
    protected VMInstanceDao _vmInstanceDao;
    @Inject
    protected AffinityGroupDao _affinityGroupDao;
    @Inject
    protected AffinityGroupVMMapDao _affinityGroupVMMapDao;
    @Inject
    protected ConfigurationDao _configDao;
    @Inject
    protected VMReservationDao _reservationDao;
    private int _vmCapacityReleaseInterval;

    @Override
    public void process(final VirtualMachineProfile vmProfile, final DeploymentPlan plan, final ExcludeList avoid) throws AffinityConflictException {
        final VirtualMachine vm = vmProfile.getVirtualMachine();
        final List<AffinityGroupVMMapVO> vmGroupMappings = _affinityGroupVMMapDao.findByVmIdType(vm.getId(), getType());

        for (final AffinityGroupVMMapVO vmGroupMapping : vmGroupMappings) {
            if (vmGroupMapping != null) {
                final AffinityGroupVO group = _affinityGroupDao.findById(vmGroupMapping.getAffinityGroupId());

                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Processing affinity group " + group.getName() + " for VM Id: " + vm.getId());
                }

                final List<Long> groupVMIds = _affinityGroupVMMapDao.listVmIdsByAffinityGroup(group.getId());
                groupVMIds.remove(vm.getId());

                for (final Long groupVMId : groupVMIds) {
                    final VMInstanceVO groupVM = _vmInstanceDao.findById(groupVMId);
                    if (groupVM != null && !groupVM.isRemoved()) {
                        if (groupVM.getHostId() != null) {
                            avoid.addHost(groupVM.getHostId());
                            if (s_logger.isDebugEnabled()) {
                                s_logger.debug("Added host " + groupVM.getHostId() + " to avoid set, since VM " + groupVM.getId() + " is present on the host");
                            }
                        } else if (VirtualMachine.State.Stopped.equals(groupVM.getState()) && groupVM.getLastHostId() != null) {
                            final long secondsSinceLastUpdate = (DateUtil.currentGMTTime().getTime() - groupVM.getUpdateTime().getTime()) / 1000;
                            if (secondsSinceLastUpdate < _vmCapacityReleaseInterval) {
                                avoid.addHost(groupVM.getLastHostId());
                                if (s_logger.isDebugEnabled()) {
                                    s_logger.debug("Added host " + groupVM.getLastHostId() + " to avoid set, since VM " + groupVM.getId() +
                                            " is present on the host, in Stopped state but has reserved capacity");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean check(final VirtualMachineProfile vmProfile, final DeployDestination plannedDestination) throws AffinityConflictException {

        if (plannedDestination.getHost() == null) {
            return true;
        }
        final long plannedHostId = plannedDestination.getHost().getId();

        final VirtualMachine vm = vmProfile.getVirtualMachine();

        final List<AffinityGroupVMMapVO> vmGroupMappings = _affinityGroupVMMapDao.findByVmIdType(vm.getId(), getType());

        for (final AffinityGroupVMMapVO vmGroupMapping : vmGroupMappings) {
            // if more than 1 VM's are present in the group then check for
            // conflict due to parallel deployment
            final List<Long> groupVMIds = _affinityGroupVMMapDao.listVmIdsByAffinityGroup(vmGroupMapping.getAffinityGroupId());
            groupVMIds.remove(vm.getId());

            for (final Long groupVMId : groupVMIds) {
                final VMReservationVO vmReservation = _reservationDao.findByVmId(groupVMId);
                if (vmReservation != null && vmReservation.getHostId() != null && vmReservation.getHostId().equals(plannedHostId)) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Planned destination for VM " + vm.getId() + " conflicts with an existing VM " + vmReservation.getVmId() +
                                " reserved on the same host " + plannedHostId);
                    }
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);
        _vmCapacityReleaseInterval = NumbersUtil.parseInt(_configDao.getValue(Config.CapacitySkipcountingHours.key()), 3600);
        return true;
    }
}
