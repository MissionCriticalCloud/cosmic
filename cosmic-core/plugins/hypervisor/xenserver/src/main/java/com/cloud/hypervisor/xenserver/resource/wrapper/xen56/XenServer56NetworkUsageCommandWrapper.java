//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xen56;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.NetworkUsageAnswer;
import com.cloud.agent.api.NetworkUsageCommand;
import com.cloud.hypervisor.xenserver.resource.XenServer56Resource;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;
import com.cloud.utils.ExecutionResult;

import com.xensource.xenapi.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = NetworkUsageCommand.class)
public final class XenServer56NetworkUsageCommandWrapper extends CommandWrapper<NetworkUsageCommand, Answer, XenServer56Resource> {

    private static final Logger s_logger = LoggerFactory.getLogger(XenServer56NetworkUsageCommandWrapper.class);

    @Override
    public Answer execute(final NetworkUsageCommand command, final XenServer56Resource xenServer56) {
        if (command.isForVpc()) {
            return executeNetworkUsage(command, xenServer56);
        }
        try {
            final Connection conn = xenServer56.getConnection();
            if (command.getOption() != null && command.getOption().equals("create")) {
                final String result = xenServer56.networkUsage(conn, command.getPrivateIP(), "create", null);
                final NetworkUsageAnswer answer = new NetworkUsageAnswer(command, result, 0L, 0L);
                return answer;
            }
            final long[] stats = xenServer56.getNetworkStats(conn, command.getPrivateIP());
            final NetworkUsageAnswer answer = new NetworkUsageAnswer(command, "", stats[0], stats[1]);
            return answer;
        } catch (final Exception ex) {
            s_logger.warn("Failed to get network usage stats due to ", ex);
            return new NetworkUsageAnswer(command, ex);
        }
    }

    protected NetworkUsageAnswer executeNetworkUsage(final NetworkUsageCommand command, final XenServer56Resource xenServer56) {
        try {
            final String option = command.getOption();
            final String publicIp = command.getGatewayIP();

            String args = " -l " + publicIp + " ";
            if (option.equals("get")) {
                args += "-g";
            } else if (option.equals("create")) {
                args += "-c";
                final String vpcCIDR = command.getVpcCIDR();
                args += " -v " + vpcCIDR;
            } else if (option.equals("reset")) {
                args += "-r";
            } else if (option.equals("vpn")) {
                args += "-n";
            } else if (option.equals("remove")) {
                args += "-d";
            } else {
                return new NetworkUsageAnswer(command, "success", 0L, 0L);
            }

            final ExecutionResult result = xenServer56.executeInVR(command.getPrivateIP(), "vpc_netusage.sh", args);
            final String detail = result.getDetails();
            if (!result.isSuccess()) {
                throw new Exception(" vpc network usage plugin call failed ");
            }
            if (option.equals("get") || option.equals("vpn")) {
                final long[] stats = new long[2];
                if (detail != null) {
                    final String[] splitResult = detail.split(":");
                    int i = 0;
                    while (i < splitResult.length - 1) {
                        stats[0] += Long.parseLong(splitResult[i++]);
                        stats[1] += Long.parseLong(splitResult[i++]);
                    }
                    return new NetworkUsageAnswer(command, "success", stats[0], stats[1]);
                }
            }
            return new NetworkUsageAnswer(command, "success", 0L, 0L);
        } catch (final Exception ex) {
            s_logger.warn("Failed to get network usage stats due to ", ex);
            return new NetworkUsageAnswer(command, ex);
        }
    }
}
