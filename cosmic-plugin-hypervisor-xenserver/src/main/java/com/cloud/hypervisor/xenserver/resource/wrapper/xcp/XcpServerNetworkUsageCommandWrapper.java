//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xcp;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.NetworkUsageAnswer;
import com.cloud.agent.api.NetworkUsageCommand;
import com.cloud.hypervisor.xenserver.resource.XcpServerResource;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import com.xensource.xenapi.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = NetworkUsageCommand.class)
public final class XcpServerNetworkUsageCommandWrapper extends CommandWrapper<NetworkUsageCommand, Answer, XcpServerResource> {

    private static final Logger s_logger = LoggerFactory.getLogger(XcpServerNetworkUsageCommandWrapper.class);

    @Override
    public Answer execute(final NetworkUsageCommand command, final XcpServerResource xcpServerResource) {
        try {
            final Connection conn = xcpServerResource.getConnection();
            if (command.getOption() != null && command.getOption().equals("create")) {
                final String result = xcpServerResource.networkUsage(conn, command.getPrivateIP(), "create", null);
                final NetworkUsageAnswer answer = new NetworkUsageAnswer(command, result, 0L, 0L);
                return answer;
            }
            final long[] stats = xcpServerResource.getNetworkStats(conn, command.getPrivateIP());
            final NetworkUsageAnswer answer = new NetworkUsageAnswer(command, "", stats[0], stats[1]);
            return answer;
        } catch (final Exception ex) {
            s_logger.warn("Failed to get network usage stats due to ", ex);
            return new NetworkUsageAnswer(command, ex);
        }
    }
}
