package org.apache.cloudstack.storage.vmsnapshot;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CreateVMSnapshotAnswer;
import com.cloud.agent.api.CreateVMSnapshotCommand;
import com.cloud.agent.api.DeleteVMSnapshotAnswer;
import com.cloud.agent.api.DeleteVMSnapshotCommand;
import com.cloud.agent.api.RevertToVMSnapshotAnswer;
import com.cloud.agent.api.RevertToVMSnapshotCommand;
import com.cloud.agent.api.VMSnapshotTO;
import com.cloud.event.EventTypes;
import com.cloud.event.UsageEventUtils;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.storage.DiskOfferingVO;
import com.cloud.storage.GuestOSHypervisorVO;
import com.cloud.storage.GuestOSVO;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.DiskOfferingDao;
import com.cloud.storage.dao.GuestOSDao;
import com.cloud.storage.dao.GuestOSHypervisorDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.uservm.UserVm;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallbackWithExceptionNoReturn;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.fsm.NoTransitionException;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.snapshot.VMSnapshot;
import com.cloud.vm.snapshot.VMSnapshotVO;
import com.cloud.vm.snapshot.dao.VMSnapshotDao;
import org.apache.cloudstack.engine.subsystem.api.storage.StrategyPriority;
import org.apache.cloudstack.engine.subsystem.api.storage.VMSnapshotOptions;
import org.apache.cloudstack.engine.subsystem.api.storage.VMSnapshotStrategy;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.storage.to.VolumeObjectTO;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultVMSnapshotStrategy extends ManagerBase implements VMSnapshotStrategy {
    private static final Logger s_logger = LoggerFactory.getLogger(DefaultVMSnapshotStrategy.class);
    @Inject
    VMSnapshotHelper vmSnapshotHelper;
    @Inject
    GuestOSDao guestOSDao;
    @Inject
    GuestOSHypervisorDao guestOsHypervisorDao;
    @Inject
    UserVmDao userVmDao;
    @Inject
    VMSnapshotDao vmSnapshotDao;
    int _wait;
    @Inject
    ConfigurationDao configurationDao;
    @Inject
    AgentManager agentMgr;
    @Inject
    VolumeDao volumeDao;
    @Inject
    DiskOfferingDao diskOfferingDao;
    @Inject
    HostDao hostDao;

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        final String value = configurationDao.getValue("vmsnapshot.create.wait");
        _wait = NumbersUtil.parseInt(value, 1800);
        return true;
    }

    @Override
    public VMSnapshot takeVMSnapshot(final VMSnapshot vmSnapshot) {
        final Long hostId = vmSnapshotHelper.pickRunningHost(vmSnapshot.getVmId());
        final UserVm userVm = userVmDao.findById(vmSnapshot.getVmId());
        final VMSnapshotVO vmSnapshotVO = (VMSnapshotVO) vmSnapshot;
        try {
            vmSnapshotHelper.vmSnapshotStateTransitTo(vmSnapshotVO, VMSnapshot.Event.CreateRequested);
        } catch (final NoTransitionException e) {
            throw new CloudRuntimeException(e.getMessage());
        }

        CreateVMSnapshotAnswer answer = null;
        boolean result = false;
        try {
            final GuestOSVO guestOS = guestOSDao.findById(userVm.getGuestOSId());

            final List<VolumeObjectTO> volumeTOs = vmSnapshotHelper.getVolumeTOList(userVm.getId());

            VMSnapshotTO current = null;
            final VMSnapshotVO currentSnapshot = vmSnapshotDao.findCurrentSnapshotByVmId(userVm.getId());
            if (currentSnapshot != null) {
                current = vmSnapshotHelper.getSnapshotWithParents(currentSnapshot);
            }
            final VMSnapshotOptions options = ((VMSnapshotVO) vmSnapshot).getOptions();
            boolean quiescevm = true;
            if (options != null) {
                quiescevm = options.needQuiesceVM();
            }
            final VMSnapshotTO target =
                    new VMSnapshotTO(vmSnapshot.getId(), vmSnapshot.getName(), vmSnapshot.getType(), null, vmSnapshot.getDescription(), false, current, quiescevm);
            if (current == null) {
                vmSnapshotVO.setParent(null);
            } else {
                vmSnapshotVO.setParent(current.getId());
            }

            final HostVO host = hostDao.findById(hostId);
            final GuestOSHypervisorVO guestOsMapping = guestOsHypervisorDao.findByOsIdAndHypervisor(guestOS.getId(), host.getHypervisorType().toString(), host
                    .getHypervisorVersion());

            final CreateVMSnapshotCommand ccmd = new CreateVMSnapshotCommand(userVm.getInstanceName(), userVm.getUuid(), target, volumeTOs, guestOS.getDisplayName());
            if (guestOsMapping == null) {
                ccmd.setPlatformEmulator(null);
            } else {
                ccmd.setPlatformEmulator(guestOsMapping.getGuestOsName());
            }
            ccmd.setWait(_wait);

            answer = (CreateVMSnapshotAnswer) agentMgr.send(hostId, ccmd);
            if (answer != null && answer.getResult()) {
                processAnswer(vmSnapshotVO, userVm, answer, hostId);
                s_logger.debug("Create vm snapshot " + vmSnapshot.getName() + " succeeded for vm: " + userVm.getInstanceName());
                result = true;

                for (final VolumeObjectTO volumeTo : answer.getVolumeTOs()) {
                    publishUsageEvent(EventTypes.EVENT_VM_SNAPSHOT_CREATE, vmSnapshot, userVm, volumeTo);
                }
                return vmSnapshot;
            } else {
                String errMsg = "Creating VM snapshot: " + vmSnapshot.getName() + " failed";
                if (answer != null && answer.getDetails() != null) {
                    errMsg = errMsg + " due to " + answer.getDetails();
                }
                s_logger.error(errMsg);
                throw new CloudRuntimeException(errMsg);
            }
        } catch (final OperationTimedoutException e) {
            s_logger.debug("Creating VM snapshot: " + vmSnapshot.getName() + " failed: " + e.toString());
            throw new CloudRuntimeException("Creating VM snapshot: " + vmSnapshot.getName() + " failed: " + e.toString());
        } catch (final AgentUnavailableException e) {
            s_logger.debug("Creating VM snapshot: " + vmSnapshot.getName() + " failed", e);
            throw new CloudRuntimeException("Creating VM snapshot: " + vmSnapshot.getName() + " failed: " + e.toString());
        } finally {
            if (!result) {
                try {
                    vmSnapshotHelper.vmSnapshotStateTransitTo(vmSnapshot, VMSnapshot.Event.OperationFailed);
                } catch (final NoTransitionException e1) {
                    s_logger.error("Cannot set vm snapshot state due to: " + e1.getMessage());
                }
            }
        }
    }

    @Override
    public boolean deleteVMSnapshot(final VMSnapshot vmSnapshot) {
        final UserVmVO userVm = userVmDao.findById(vmSnapshot.getVmId());
        final VMSnapshotVO vmSnapshotVO = (VMSnapshotVO) vmSnapshot;
        try {
            vmSnapshotHelper.vmSnapshotStateTransitTo(vmSnapshot, VMSnapshot.Event.ExpungeRequested);
        } catch (final NoTransitionException e) {
            s_logger.debug("Failed to change vm snapshot state with event ExpungeRequested");
            throw new CloudRuntimeException("Failed to change vm snapshot state with event ExpungeRequested: " + e.getMessage());
        }

        try {
            final Long hostId = vmSnapshotHelper.pickRunningHost(vmSnapshot.getVmId());

            final List<VolumeObjectTO> volumeTOs = vmSnapshotHelper.getVolumeTOList(vmSnapshot.getVmId());

            final String vmInstanceName = userVm.getInstanceName();
            final VMSnapshotTO parent = vmSnapshotHelper.getSnapshotWithParents(vmSnapshotVO).getParent();
            final VMSnapshotTO vmSnapshotTO =
                    new VMSnapshotTO(vmSnapshot.getId(), vmSnapshot.getName(), vmSnapshot.getType(), vmSnapshot.getCreated().getTime(), vmSnapshot.getDescription(),
                            vmSnapshot.getCurrent(), parent, true);
            final GuestOSVO guestOS = guestOSDao.findById(userVm.getGuestOSId());
            final DeleteVMSnapshotCommand deleteSnapshotCommand = new DeleteVMSnapshotCommand(vmInstanceName, vmSnapshotTO, volumeTOs, guestOS.getDisplayName());

            final Answer answer = agentMgr.send(hostId, deleteSnapshotCommand);

            if (answer != null && answer.getResult()) {
                final DeleteVMSnapshotAnswer deleteVMSnapshotAnswer = (DeleteVMSnapshotAnswer) answer;
                processAnswer(vmSnapshotVO, userVm, answer, hostId);
                for (final VolumeObjectTO volumeTo : deleteVMSnapshotAnswer.getVolumeTOs()) {
                    publishUsageEvent(EventTypes.EVENT_VM_SNAPSHOT_DELETE, vmSnapshot, userVm, volumeTo);
                }
                return true;
            } else {
                final String errMsg = (answer == null) ? null : answer.getDetails();
                s_logger.error("Delete vm snapshot " + vmSnapshot.getName() + " of vm " + userVm.getInstanceName() + " failed due to " + errMsg);
                throw new CloudRuntimeException("Delete vm snapshot " + vmSnapshot.getName() + " of vm " + userVm.getInstanceName() + " failed due to " + errMsg);
            }
        } catch (final OperationTimedoutException e) {
            throw new CloudRuntimeException("Delete vm snapshot " + vmSnapshot.getName() + " of vm " + userVm.getInstanceName() + " failed due to " + e.getMessage());
        } catch (final AgentUnavailableException e) {
            throw new CloudRuntimeException("Delete vm snapshot " + vmSnapshot.getName() + " of vm " + userVm.getInstanceName() + " failed due to " + e.getMessage());
        }
    }

    @Override
    public boolean revertVMSnapshot(final VMSnapshot vmSnapshot) {
        final VMSnapshotVO vmSnapshotVO = (VMSnapshotVO) vmSnapshot;
        final UserVmVO userVm = userVmDao.findById(vmSnapshot.getVmId());
        try {
            vmSnapshotHelper.vmSnapshotStateTransitTo(vmSnapshotVO, VMSnapshot.Event.RevertRequested);
        } catch (final NoTransitionException e) {
            throw new CloudRuntimeException(e.getMessage());
        }

        boolean result = false;
        try {
            final VMSnapshotVO snapshot = vmSnapshotDao.findById(vmSnapshotVO.getId());
            final List<VolumeObjectTO> volumeTOs = vmSnapshotHelper.getVolumeTOList(userVm.getId());
            final String vmInstanceName = userVm.getInstanceName();
            final VMSnapshotTO parent = vmSnapshotHelper.getSnapshotWithParents(snapshot).getParent();

            final VMSnapshotTO vmSnapshotTO =
                    new VMSnapshotTO(snapshot.getId(), snapshot.getName(), snapshot.getType(), snapshot.getCreated().getTime(), snapshot.getDescription(),
                            snapshot.getCurrent(), parent, true);
            final Long hostId = vmSnapshotHelper.pickRunningHost(vmSnapshot.getVmId());
            final GuestOSVO guestOS = guestOSDao.findById(userVm.getGuestOSId());
            final RevertToVMSnapshotCommand revertToSnapshotCommand = new RevertToVMSnapshotCommand(vmInstanceName, userVm.getUuid(), vmSnapshotTO, volumeTOs, guestOS
                    .getDisplayName());
            final HostVO host = hostDao.findById(hostId);
            final GuestOSHypervisorVO guestOsMapping = guestOsHypervisorDao.findByOsIdAndHypervisor(guestOS.getId(), host.getHypervisorType().toString(), host
                    .getHypervisorVersion());
            if (guestOsMapping == null) {
                revertToSnapshotCommand.setPlatformEmulator(null);
            } else {
                revertToSnapshotCommand.setPlatformEmulator(guestOsMapping.getGuestOsName());
            }

            final RevertToVMSnapshotAnswer answer = (RevertToVMSnapshotAnswer) agentMgr.send(hostId, revertToSnapshotCommand);
            if (answer != null && answer.getResult()) {
                processAnswer(vmSnapshotVO, userVm, answer, hostId);
                result = true;
            } else {
                String errMsg = "Revert VM: " + userVm.getInstanceName() + " to snapshot: " + vmSnapshotVO.getName() + " failed";
                if (answer != null && answer.getDetails() != null) {
                    errMsg = errMsg + " due to " + answer.getDetails();
                }
                s_logger.error(errMsg);
                throw new CloudRuntimeException(errMsg);
            }
        } catch (final OperationTimedoutException e) {
            s_logger.debug("Failed to revert vm snapshot", e);
            throw new CloudRuntimeException(e.getMessage());
        } catch (final AgentUnavailableException e) {
            s_logger.debug("Failed to revert vm snapshot", e);
            throw new CloudRuntimeException(e.getMessage());
        } finally {
            if (!result) {
                try {
                    vmSnapshotHelper.vmSnapshotStateTransitTo(vmSnapshot, VMSnapshot.Event.OperationFailed);
                } catch (final NoTransitionException e1) {
                    s_logger.error("Cannot set vm snapshot state due to: " + e1.getMessage());
                }
            }
        }
        return result;
    }

    @Override
    public StrategyPriority canHandle(final VMSnapshot vmSnapshot) {
        return StrategyPriority.DEFAULT;
    }

    @DB
    protected void processAnswer(final VMSnapshotVO vmSnapshot, final UserVm userVm, final Answer as, final Long hostId) {
        try {
            Transaction.execute(new TransactionCallbackWithExceptionNoReturn<NoTransitionException>() {
                @Override
                public void doInTransactionWithoutResult(final TransactionStatus status) throws NoTransitionException {
                    if (as instanceof CreateVMSnapshotAnswer) {
                        final CreateVMSnapshotAnswer answer = (CreateVMSnapshotAnswer) as;
                        finalizeCreate(vmSnapshot, answer.getVolumeTOs());
                        vmSnapshotHelper.vmSnapshotStateTransitTo(vmSnapshot, VMSnapshot.Event.OperationSucceeded);
                    } else if (as instanceof RevertToVMSnapshotAnswer) {
                        final RevertToVMSnapshotAnswer answer = (RevertToVMSnapshotAnswer) as;
                        finalizeRevert(vmSnapshot, answer.getVolumeTOs());
                        vmSnapshotHelper.vmSnapshotStateTransitTo(vmSnapshot, VMSnapshot.Event.OperationSucceeded);
                    } else if (as instanceof DeleteVMSnapshotAnswer) {
                        final DeleteVMSnapshotAnswer answer = (DeleteVMSnapshotAnswer) as;
                        finalizeDelete(vmSnapshot, answer.getVolumeTOs());
                        vmSnapshotDao.remove(vmSnapshot.getId());
                    }
                }
            });
        } catch (final Exception e) {
            final String errMsg = "Error while process answer: " + as.getClass() + " due to " + e.getMessage();
            s_logger.error(errMsg, e);
            throw new CloudRuntimeException(errMsg);
        }
    }

    private void publishUsageEvent(final String type, final VMSnapshot vmSnapshot, final UserVm userVm, final VolumeObjectTO volumeTo) {
        final VolumeVO volume = volumeDao.findById(volumeTo.getId());
        final Long diskOfferingId = volume.getDiskOfferingId();
        Long offeringId = null;
        if (diskOfferingId != null) {
            final DiskOfferingVO offering = diskOfferingDao.findById(diskOfferingId);
            if (offering != null && (offering.getType() == DiskOfferingVO.Type.Disk)) {
                offeringId = offering.getId();
            }
        }
        UsageEventUtils.publishUsageEvent(type, vmSnapshot.getAccountId(), userVm.getDataCenterId(), userVm.getId(), vmSnapshot.getName(), offeringId, volume.getId(), // save
                // volume's id into templateId field
                volumeTo.getSize(), VMSnapshot.class.getName(), vmSnapshot.getUuid());
    }

    protected void finalizeCreate(final VMSnapshotVO vmSnapshot, final List<VolumeObjectTO> volumeTOs) {
        // update volumes path
        updateVolumePath(volumeTOs);

        vmSnapshot.setCurrent(true);

        // change current snapshot
        if (vmSnapshot.getParent() != null) {
            final VMSnapshotVO previousCurrent = vmSnapshotDao.findById(vmSnapshot.getParent());
            previousCurrent.setCurrent(false);
            vmSnapshotDao.persist(previousCurrent);
        }
        vmSnapshotDao.persist(vmSnapshot);
    }

    protected void finalizeRevert(final VMSnapshotVO vmSnapshot, final List<VolumeObjectTO> volumeToList) {
        // update volumes path
        updateVolumePath(volumeToList);

        // update current snapshot, current snapshot is the one reverted to
        final VMSnapshotVO previousCurrent = vmSnapshotDao.findCurrentSnapshotByVmId(vmSnapshot.getVmId());
        if (previousCurrent != null) {
            previousCurrent.setCurrent(false);
            vmSnapshotDao.persist(previousCurrent);
        }
        vmSnapshot.setCurrent(true);
        vmSnapshotDao.persist(vmSnapshot);
    }

    protected void finalizeDelete(final VMSnapshotVO vmSnapshot, final List<VolumeObjectTO> volumeTOs) {
        // update volumes path
        updateVolumePath(volumeTOs);

        // update children's parent snapshots
        final List<VMSnapshotVO> children = vmSnapshotDao.listByParent(vmSnapshot.getId());
        for (final VMSnapshotVO child : children) {
            child.setParent(vmSnapshot.getParent());
            vmSnapshotDao.persist(child);
        }

        // update current snapshot
        final VMSnapshotVO current = vmSnapshotDao.findCurrentSnapshotByVmId(vmSnapshot.getVmId());
        if (current != null && current.getId() == vmSnapshot.getId() && vmSnapshot.getParent() != null) {
            final VMSnapshotVO parent = vmSnapshotDao.findById(vmSnapshot.getParent());
            parent.setCurrent(true);
            vmSnapshotDao.persist(parent);
        }
        vmSnapshot.setCurrent(false);
        vmSnapshotDao.persist(vmSnapshot);
    }

    private void updateVolumePath(final List<VolumeObjectTO> volumeTOs) {
        for (final VolumeObjectTO volume : volumeTOs) {
            if (volume.getPath() != null) {
                final VolumeVO volumeVO = volumeDao.findById(volume.getId());
                volumeVO.setPath(volume.getPath());
                volumeVO.setVmSnapshotChainSize(volume.getSize());
                volumeDao.persist(volumeVO);
            }
        }
    }
}
