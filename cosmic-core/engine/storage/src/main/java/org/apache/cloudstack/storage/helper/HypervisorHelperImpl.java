package org.apache.cloudstack.storage.helper;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CreateVMSnapshotAnswer;
import com.cloud.agent.api.CreateVMSnapshotCommand;
import com.cloud.agent.api.DeleteVMSnapshotCommand;
import com.cloud.agent.api.VMSnapshotTO;
import com.cloud.agent.api.to.DataTO;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.storage.GuestOSHypervisorVO;
import com.cloud.storage.GuestOSVO;
import com.cloud.storage.dao.GuestOSDao;
import com.cloud.storage.dao.GuestOSHypervisorDao;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.snapshot.VMSnapshot;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPoint;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPointSelector;
import org.apache.cloudstack.engine.subsystem.api.storage.Scope;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.storage.command.ForgetObjectCmd;
import org.apache.cloudstack.storage.command.IntroduceObjectAnswer;
import org.apache.cloudstack.storage.command.IntroduceObjectCmd;
import org.apache.cloudstack.storage.to.VolumeObjectTO;
import org.apache.cloudstack.storage.vmsnapshot.VMSnapshotHelper;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HypervisorHelperImpl implements HypervisorHelper {
    private static final Logger s_logger = LoggerFactory.getLogger(HypervisorHelperImpl.class);
    @Inject
    EndPointSelector selector;
    @Inject
    VMSnapshotHelper vmSnapshotHelper;
    @Inject
    GuestOSDao guestOSDao;
    @Inject
    GuestOSHypervisorDao guestOsHypervisorDao;
    @Inject
    ConfigurationDao configurationDao;
    @Inject
    AgentManager agentMgr;
    @Inject
    HostDao hostDao;

    @Override
    public DataTO introduceObject(final DataTO object, final Scope scope, final Long storeId) {
        final EndPoint ep = selector.select(scope, storeId);
        final IntroduceObjectCmd cmd = new IntroduceObjectCmd(object);
        Answer answer = null;
        if (ep == null) {
            final String errMsg = "No remote endpoint to send command, check if host or ssvm is down?";
            s_logger.error(errMsg);
            answer = new Answer(cmd, false, errMsg);
        } else {
            answer = ep.sendMessage(cmd);
        }
        if (answer == null || !answer.getResult()) {
            final String errMsg = answer == null ? null : answer.getDetails();
            throw new CloudRuntimeException("Failed to introduce object, due to " + errMsg);
        }
        final IntroduceObjectAnswer introduceObjectAnswer = (IntroduceObjectAnswer) answer;
        return introduceObjectAnswer.getDataTO();
    }

    @Override
    public boolean forgetObject(final DataTO object, final Scope scope, final Long storeId) {
        final EndPoint ep = selector.select(scope, storeId);
        final ForgetObjectCmd cmd = new ForgetObjectCmd(object);
        Answer answer = null;
        if (ep == null) {
            final String errMsg = "No remote endpoint to send command, check if host or ssvm is down?";
            s_logger.error(errMsg);
            answer = new Answer(cmd, false, errMsg);
        } else {
            answer = ep.sendMessage(cmd);
        }
        if (answer == null || !answer.getResult()) {
            final String errMsg = answer == null ? null : answer.getDetails();
            if (errMsg != null) {
                s_logger.debug("Failed to forget object: " + errMsg);
            }
            return false;
        }
        return true;
    }

    @Override
    public VMSnapshotTO quiesceVm(final VirtualMachine virtualMachine) {
        final String value = configurationDao.getValue("vmsnapshot.create.wait");
        final int wait = NumbersUtil.parseInt(value, 1800);
        final Long hostId = vmSnapshotHelper.pickRunningHost(virtualMachine.getId());
        final VMSnapshotTO vmSnapshotTO = new VMSnapshotTO(1L, UUID.randomUUID().toString(), VMSnapshot.Type.Disk, null, null, false,
                null, true);
        final GuestOSVO guestOS = guestOSDao.findById(virtualMachine.getGuestOSId());
        final List<VolumeObjectTO> volumeTOs = vmSnapshotHelper.getVolumeTOList(virtualMachine.getId());
        final CreateVMSnapshotCommand ccmd =
                new CreateVMSnapshotCommand(virtualMachine.getInstanceName(), virtualMachine.getUuid(), vmSnapshotTO, volumeTOs, guestOS.getDisplayName());
        final HostVO host = hostDao.findById(hostId);
        final GuestOSHypervisorVO guestOsMapping = guestOsHypervisorDao.findByOsIdAndHypervisor(guestOS.getId(), host.getHypervisorType().toString(), host.getHypervisorVersion());
        ccmd.setPlatformEmulator(guestOsMapping.getGuestOsName());
        ccmd.setWait(wait);
        try {
            final Answer answer = agentMgr.send(hostId, ccmd);
            if (answer != null && answer.getResult()) {
                final CreateVMSnapshotAnswer snapshotAnswer = (CreateVMSnapshotAnswer) answer;
                vmSnapshotTO.setVolumes(snapshotAnswer.getVolumeTOs());
            } else {
                final String errMsg = (answer != null) ? answer.getDetails() : null;
                throw new CloudRuntimeException("Failed to quiesce vm, due to " + errMsg);
            }
        } catch (final AgentUnavailableException e) {
            throw new CloudRuntimeException("Failed to quiesce vm", e);
        } catch (final OperationTimedoutException e) {
            throw new CloudRuntimeException("Failed to quiesce vm", e);
        }
        return vmSnapshotTO;
    }

    @Override
    public boolean unquiesceVM(final VirtualMachine virtualMachine, final VMSnapshotTO vmSnapshotTO) {
        final Long hostId = vmSnapshotHelper.pickRunningHost(virtualMachine.getId());
        final List<VolumeObjectTO> volumeTOs = vmSnapshotHelper.getVolumeTOList(virtualMachine.getId());
        final GuestOSVO guestOS = guestOSDao.findById(virtualMachine.getGuestOSId());

        final DeleteVMSnapshotCommand deleteSnapshotCommand = new DeleteVMSnapshotCommand(virtualMachine.getInstanceName(), vmSnapshotTO, volumeTOs, guestOS.getDisplayName());
        try {
            final Answer answer = agentMgr.send(hostId, deleteSnapshotCommand);
            if (answer != null && answer.getResult()) {
                return true;
            } else {
                final String errMsg = (answer != null) ? answer.getDetails() : null;
                throw new CloudRuntimeException("Failed to unquiesce vm, due to " + errMsg);
            }
        } catch (final AgentUnavailableException e) {
            throw new CloudRuntimeException("Failed to unquiesce vm", e);
        } catch (final OperationTimedoutException e) {
            throw new CloudRuntimeException("Failed to unquiesce vm", e);
        }
    }
}
