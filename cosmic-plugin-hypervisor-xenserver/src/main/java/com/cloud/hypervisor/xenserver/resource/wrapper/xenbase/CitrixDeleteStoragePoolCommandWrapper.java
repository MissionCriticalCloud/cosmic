//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.DeleteStoragePoolCommand;
import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.SR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = DeleteStoragePoolCommand.class)
public final class CitrixDeleteStoragePoolCommandWrapper extends CommandWrapper<DeleteStoragePoolCommand, Answer, CitrixResourceBase> {

    private static final Logger s_logger = LoggerFactory.getLogger(CitrixDeleteStoragePoolCommandWrapper.class);

    @Override
    public Answer execute(final DeleteStoragePoolCommand command, final CitrixResourceBase citrixResourceBase) {
        final Connection conn = citrixResourceBase.getConnection();
        final StorageFilerTO poolTO = command.getPool();
        try {
            final SR sr = citrixResourceBase.getStorageRepository(conn, poolTO.getUuid());
            citrixResourceBase.removeSR(conn, sr);
            final Answer answer = new Answer(command, true, "success");
            return answer;
        } catch (final Exception e) {
            final String msg = "DeleteStoragePoolCommand XenAPIException:" + e.getMessage() + " host:" + citrixResourceBase.getHost().getUuid() + " pool: " + poolTO.getHost()
                    + poolTO.getPath();
            s_logger.warn(msg, e);
            return new Answer(command, false, msg);
        }
    }
}
