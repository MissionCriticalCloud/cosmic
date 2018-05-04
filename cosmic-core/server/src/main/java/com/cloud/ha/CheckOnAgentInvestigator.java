package com.cloud.ha;

import com.cloud.agent.AgentManager;
import com.cloud.legacymodel.communication.answer.CheckVirtualMachineAnswer;
import com.cloud.legacymodel.communication.command.CheckVirtualMachineCommand;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.dc.HostStatus;
import com.cloud.legacymodel.exceptions.AgentUnavailableException;
import com.cloud.legacymodel.exceptions.OperationTimedoutException;
import com.cloud.legacymodel.vm.VirtualMachine;
import com.cloud.legacymodel.vm.VirtualMachine.PowerState;
import com.cloud.utils.component.AdapterBase;

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
    public HostStatus isAgentAlive(final Host agent) {
        return null;
    }
}
