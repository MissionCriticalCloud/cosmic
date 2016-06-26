package com.cloud.ha;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.CheckVirtualMachineAnswer;
import com.cloud.agent.api.CheckVirtualMachineCommand;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.host.Host;
import com.cloud.host.Status;
import com.cloud.utils.component.AdapterBase;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.PowerState;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckOnAgentInvestigator extends AdapterBase implements Investigator {
    private final static Logger s_logger = LoggerFactory.getLogger(CheckOnAgentInvestigator.class);
    @Inject
    AgentManager _agentMgr;

    protected CheckOnAgentInvestigator() {
    }

    @Override
    public boolean isVmAlive(final VirtualMachine vm, final Host host) throws UnknownVM {
        final CheckVirtualMachineCommand cmd = new CheckVirtualMachineCommand(vm.getInstanceName());
        try {
            final CheckVirtualMachineAnswer answer = (CheckVirtualMachineAnswer) _agentMgr.send(vm.getHostId(), cmd);
            if (!answer.getResult()) {
                s_logger.debug("Unable to get vm state on " + vm.toString());
                throw new UnknownVM();
            }

            s_logger.debug("Agent responded with state " + answer.getState().toString());
            return answer.getState() == PowerState.PowerOn;
        } catch (final AgentUnavailableException e) {
            s_logger.debug("Unable to reach the agent for " + vm.toString() + ": " + e.getMessage());
            throw new UnknownVM();
        } catch (final OperationTimedoutException e) {
            s_logger.debug("Operation timed out for " + vm.toString() + ": " + e.getMessage());
            throw new UnknownVM();
        }
    }

    @Override
    public Status isAgentAlive(final Host agent) {
        return null;
    }
}
