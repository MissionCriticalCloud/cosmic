//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CleanupNetworkRulesCmd;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import com.xensource.xenapi.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = CleanupNetworkRulesCmd.class)
public final class CitrixCleanupNetworkRulesCmdWrapper extends CommandWrapper<CleanupNetworkRulesCmd, Answer, CitrixResourceBase> {

    private static final Logger s_logger = LoggerFactory.getLogger(CitrixCleanupNetworkRulesCmdWrapper.class);

    @Override
    public Answer execute(final CleanupNetworkRulesCmd command, final CitrixResourceBase citrixResourceBase) {
        if (!citrixResourceBase.canBridgeFirewall()) {
            return new Answer(command, true, null);
        }
        final Connection conn = citrixResourceBase.getConnection();

        final String result = citrixResourceBase.callHostPlugin(conn, "vmops", "cleanup_rules", "instance", citrixResourceBase.getVMInstanceName());
        final int numCleaned = Integer.parseInt(result);

        if (result == null || result.isEmpty() || numCleaned < 0) {
            s_logger.warn("Failed to cleanup rules for host " + citrixResourceBase.getHost().getIp());
            return new Answer(command, false, result);
        }

        if (numCleaned > 0) {
            s_logger.info("Cleaned up rules for " + result + " vms on host " + citrixResourceBase.getHost().getIp());
        }
        return new Answer(command, true, result);
    }
}
