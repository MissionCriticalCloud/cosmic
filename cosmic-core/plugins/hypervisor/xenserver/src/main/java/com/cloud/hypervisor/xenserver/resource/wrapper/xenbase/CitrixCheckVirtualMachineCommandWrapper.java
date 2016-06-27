//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CheckVirtualMachineAnswer;
import com.cloud.agent.api.CheckVirtualMachineCommand;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;
import com.cloud.vm.VirtualMachine.PowerState;

import com.xensource.xenapi.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = CheckVirtualMachineCommand.class)
public final class CitrixCheckVirtualMachineCommandWrapper extends CommandWrapper<CheckVirtualMachineCommand, Answer, CitrixResourceBase> {

    private static final Logger s_logger = LoggerFactory.getLogger(CitrixCheckVirtualMachineCommandWrapper.class);

    @Override
    public Answer execute(final CheckVirtualMachineCommand command, final CitrixResourceBase citrixResourceBase) {
        final Connection conn = citrixResourceBase.getConnection();
        final String vmName = command.getVmName();
        final PowerState powerState = citrixResourceBase.getVmState(conn, vmName);
        final Integer vncPort = null;
        if (powerState == PowerState.PowerOn) {
            s_logger.debug("3. The VM " + vmName + " is in Running state");
        }

        return new CheckVirtualMachineAnswer(command, powerState, vncPort);
    }
}
