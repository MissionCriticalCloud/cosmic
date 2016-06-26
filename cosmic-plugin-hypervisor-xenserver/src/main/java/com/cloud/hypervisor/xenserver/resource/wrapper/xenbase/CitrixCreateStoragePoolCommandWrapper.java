//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CreateStoragePoolCommand;
import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;
import com.cloud.storage.Storage.StoragePoolType;

import com.xensource.xenapi.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = CreateStoragePoolCommand.class)
public final class CitrixCreateStoragePoolCommandWrapper extends CommandWrapper<CreateStoragePoolCommand, Answer, CitrixResourceBase> {

    private static final Logger s_logger = LoggerFactory.getLogger(CitrixCreateStoragePoolCommandWrapper.class);

    @Override
    public Answer execute(final CreateStoragePoolCommand command, final CitrixResourceBase citrixResourceBase) {
        final Connection conn = citrixResourceBase.getConnection();
        final StorageFilerTO pool = command.getPool();
        try {
            if (pool.getType() == StoragePoolType.NetworkFilesystem) {
                citrixResourceBase.getNfsSR(conn, Long.toString(pool.getId()), pool.getUuid(), pool.getHost(), pool.getPath(), pool.toString());
            } else if (pool.getType() == StoragePoolType.IscsiLUN) {
                citrixResourceBase.getIscsiSR(conn, pool.getUuid(), pool.getHost(), pool.getPath(), null, null, false);
            } else if (pool.getType() == StoragePoolType.PreSetup) {
            } else {
                return new Answer(command, false, "The pool type: " + pool.getType().name() + " is not supported.");
            }
            return new Answer(command, true, "success");
        } catch (final Exception e) {
            final String msg = "Catch Exception " + e.getClass().getName() + ", create StoragePool failed due to " + e.toString() + " on host:"
                    + citrixResourceBase.getHost().getUuid() + " pool: " + pool.getHost() + pool.getPath();
            s_logger.warn(msg, e);
            return new Answer(command, false, msg);
        }
    }
}
