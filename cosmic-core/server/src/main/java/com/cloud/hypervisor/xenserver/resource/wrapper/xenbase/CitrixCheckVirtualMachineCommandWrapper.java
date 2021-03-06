package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.CheckVirtualMachineAnswer;
import com.cloud.legacymodel.communication.command.CheckVirtualMachineCommand;
import com.cloud.legacymodel.vm.VirtualMachine.PowerState;

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
