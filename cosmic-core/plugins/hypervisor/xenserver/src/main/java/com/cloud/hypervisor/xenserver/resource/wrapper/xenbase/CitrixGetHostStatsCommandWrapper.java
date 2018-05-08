package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.GetHostStatsAnswer;
import com.cloud.legacymodel.communication.command.GetHostStatsCommand;
import com.cloud.legacymodel.dc.HostStatsEntry;

import com.xensource.xenapi.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = GetHostStatsCommand.class)
public final class CitrixGetHostStatsCommandWrapper extends CommandWrapper<GetHostStatsCommand, Answer, CitrixResourceBase> {

    private static final Logger s_logger = LoggerFactory.getLogger(CitrixGetHostStatsCommandWrapper.class);

    @Override
    public Answer execute(final GetHostStatsCommand command, final CitrixResourceBase citrixResourceBase) {
        final Connection conn = citrixResourceBase.getConnection();
        try {
            final HostStatsEntry hostStats = citrixResourceBase.getHostStats(conn, command, command.getHostGuid(), command.getHostId());
            return new GetHostStatsAnswer(command, hostStats);
        } catch (final Exception e) {
            final String msg = "Unable to get Host stats" + e.toString();
            s_logger.warn(msg, e);
            return new GetHostStatsAnswer(command, null);
        }
    }
}
