//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.GetStorageStatsAnswer;
import com.cloud.agent.api.GetStorageStatsCommand;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import java.util.Set;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.SR;
import com.xensource.xenapi.Types.XenAPIException;
import org.apache.xmlrpc.XmlRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = GetStorageStatsCommand.class)
public final class CitrixGetStorageStatsCommandWrapper extends CommandWrapper<GetStorageStatsCommand, Answer, CitrixResourceBase> {

    private static final Logger s_logger = LoggerFactory.getLogger(CitrixGetStorageStatsCommandWrapper.class);

    @Override
    public Answer execute(final GetStorageStatsCommand command, final CitrixResourceBase citrixResourceBase) {
        final Connection conn = citrixResourceBase.getConnection();
        try {
            final Set<SR> srs = SR.getByNameLabel(conn, command.getStorageId());
            if (srs.size() != 1) {
                final String msg = "There are " + srs.size() + " storageid: " + command.getStorageId();
                s_logger.warn(msg);
                return new GetStorageStatsAnswer(command, msg);
            }
            final SR sr = srs.iterator().next();
            sr.scan(conn);
            final long capacity = sr.getPhysicalSize(conn);
            final long used = sr.getPhysicalUtilisation(conn);
            return new GetStorageStatsAnswer(command, capacity, used);
        } catch (final XenAPIException e) {
            final String msg = "GetStorageStats Exception:" + e.toString() + "host:" + citrixResourceBase.getHost().getUuid() + "storageid: " + command.getStorageId();
            s_logger.warn(msg);
            return new GetStorageStatsAnswer(command, msg);
        } catch (final XmlRpcException e) {
            final String msg = "GetStorageStats Exception:" + e.getMessage() + "host:" + citrixResourceBase.getHost().getUuid() + "storageid: " + command.getStorageId();
            s_logger.warn(msg);
            return new GetStorageStatsAnswer(command, msg);
        } catch (final Exception e) {
            final String msg = "GetStorageStats Exception:" + e.getMessage() + "host:" + citrixResourceBase.getHost().getUuid() + "storageid: " + command.getStorageId();
            s_logger.warn(msg);
            return new GetStorageStatsAnswer(command, msg);
        }
    }
}
