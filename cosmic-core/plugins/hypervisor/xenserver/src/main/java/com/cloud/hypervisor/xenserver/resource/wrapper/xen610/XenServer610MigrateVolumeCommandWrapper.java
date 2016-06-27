//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xen610;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.storage.MigrateVolumeAnswer;
import com.cloud.agent.api.storage.MigrateVolumeCommand;
import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.hypervisor.xenserver.resource.XenServer610Resource;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import java.util.HashMap;
import java.util.Map;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.SR;
import com.xensource.xenapi.Task;
import com.xensource.xenapi.Types;
import com.xensource.xenapi.VDI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = MigrateVolumeCommand.class)
public final class XenServer610MigrateVolumeCommandWrapper extends CommandWrapper<MigrateVolumeCommand, Answer, XenServer610Resource> {

    private static final Logger s_logger = LoggerFactory.getLogger(XenServer610MigrateVolumeCommandWrapper.class);

    @Override
    public Answer execute(final MigrateVolumeCommand command, final XenServer610Resource xenServer610Resource) {
        final Connection connection = xenServer610Resource.getConnection();
        final String volumeUUID = command.getVolumePath();
        final StorageFilerTO poolTO = command.getPool();

        try {
            final String uuid = poolTO.getUuid();
            final SR destinationPool = xenServer610Resource.getStorageRepository(connection, uuid);
            final VDI srcVolume = xenServer610Resource.getVDIbyUuid(connection, volumeUUID);
            final Map<String, String> other = new HashMap<>();
            other.put("live", "true");

            // Live migrate the vdi across pool.
            final Task task = srcVolume.poolMigrateAsync(connection, destinationPool, other);
            final long timeout = xenServer610Resource.getMigrateWait() * 1000L;
            xenServer610Resource.waitForTask(connection, task, 1000, timeout);
            xenServer610Resource.checkForSuccess(connection, task);

            final VDI dvdi = Types.toVDI(task, connection);

            return new MigrateVolumeAnswer(command, true, null, dvdi.getUuid(connection));
        } catch (final Exception e) {
            final String msg = "Catch Exception " + e.getClass().getName() + " due to " + e.toString();
            s_logger.error(msg, e);
            return new MigrateVolumeAnswer(command, false, msg, null);
        }
    }
}
