package com.cloud.storage.motion;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.MigrateWithStorageCompleteAnswer;
import com.cloud.agent.api.MigrateWithStorageCompleteCommand;
import com.cloud.agent.api.MigrateWithStorageReceiveAnswer;
import com.cloud.agent.api.MigrateWithStorageReceiveCommand;
import com.cloud.agent.api.MigrateWithStorageSendAnswer;
import com.cloud.agent.api.MigrateWithStorageSendCommand;
import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.agent.api.to.VolumeTO;
import com.cloud.engine.subsystem.api.storage.DataStore;
import com.cloud.engine.subsystem.api.storage.StrategyPriority;
import com.cloud.engine.subsystem.api.storage.VolumeInfo;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.host.Host;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.utils.Pair;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.VMInstanceVO;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class XenServerStorageMotionStrategy extends AbstractHyperVisorStorageMotionStrategy {
    private static final Logger s_logger = LoggerFactory.getLogger(XenServerStorageMotionStrategy.class);

    @Override
    public StrategyPriority canHandle(final Map<VolumeInfo, DataStore> volumeMap, final Host srcHost, final Host destHost) {
        if (srcHost.getHypervisorType() == HypervisorType.XenServer && destHost.getHypervisorType() == HypervisorType.XenServer) {
            return StrategyPriority.HYPERVISOR;
        }

        return StrategyPriority.CANT_HANDLE;
    }

    @Override
    protected Answer migrateVmWithVolumesAcrossCluster(
            final VMInstanceVO vm,
            final VirtualMachineTO to,
            final Host srcHost,
            final Host destHost,
            final Map<VolumeInfo, DataStore> volumeToPool
    ) throws AgentUnavailableException {
        // Initiate migration of a virtual machine with it's volumes.
        try {
            final List<Pair<VolumeTO, StorageFilerTO>> volumeToFilerto = buildVolumeMapping(volumeToPool);

            // Migration across cluster needs to be done in three phases.
            // 1. Send a migrate receive command to the destination host so that it is ready to receive a vm.
            // 2. Send a migrate send command to the source host. This actually migrates the vm to the destination.
            // 3. Complete the process. Update the volume details.
            final MigrateWithStorageReceiveCommand receiveCmd = new MigrateWithStorageReceiveCommand(to, volumeToFilerto);
            final MigrateWithStorageReceiveAnswer receiveAnswer = (MigrateWithStorageReceiveAnswer) agentMgr.send(destHost.getId(), receiveCmd);
            if (receiveAnswer == null) {
                s_logger.error("Migration with storage of vm " + vm + " to host " + destHost + " failed.");
                throw new CloudRuntimeException("Error while migrating the vm " + vm + " to host " + destHost);
            } else if (!receiveAnswer.getResult()) {
                s_logger.error("Migration with storage of vm " + vm + " failed. Details: " + receiveAnswer.getDetails());
                throw new CloudRuntimeException("Error while migrating the vm " + vm + " to host " + destHost);
            }

            final MigrateWithStorageSendCommand sendCmd = new MigrateWithStorageSendCommand(
                    to,
                    receiveAnswer.getVolumeToSr(),
                    receiveAnswer.getNicToNetwork(),
                    receiveAnswer.getToken()
            );
            final MigrateWithStorageSendAnswer sendAnswer = (MigrateWithStorageSendAnswer) agentMgr.send(srcHost.getId(), sendCmd);
            if (sendAnswer == null) {
                s_logger.error("Migration with storage of vm " + vm + " to host " + destHost + " failed.");
                throw new CloudRuntimeException("Error while migrating the vm " + vm + " to host " + destHost);
            } else if (!sendAnswer.getResult()) {
                s_logger.error("Migration with storage of vm " + vm + " failed. Details: " + sendAnswer.getDetails());
                throw new CloudRuntimeException("Error while migrating the vm " + vm + " to host " + destHost);
            }

            final MigrateWithStorageCompleteCommand command = new MigrateWithStorageCompleteCommand(to);
            final MigrateWithStorageCompleteAnswer answer = (MigrateWithStorageCompleteAnswer) agentMgr.send(destHost.getId(), command);
            if (answer == null) {
                s_logger.error("Migration with storage of vm " + vm + " failed.");
                throw new CloudRuntimeException("Error while migrating the vm " + vm + " to host " + destHost);
            } else if (!answer.getResult()) {
                s_logger.error("Migration with storage of vm " + vm + " failed. Details: " + answer.getDetails());
                throw new CloudRuntimeException("Error while migrating the vm " + vm + " to host " + destHost);
            } else {
                // Update the volume details after migration.
                updateVolumePathsAfterMigration(volumeToPool, answer.getVolumeTos());
            }

            return answer;
        } catch (final OperationTimedoutException e) {
            s_logger.error("Error while migrating vm " + vm + " to host " + destHost, e);
            throw new AgentUnavailableException("Operation timed out on storage motion for " + vm, destHost.getId());
        }
    }
}
