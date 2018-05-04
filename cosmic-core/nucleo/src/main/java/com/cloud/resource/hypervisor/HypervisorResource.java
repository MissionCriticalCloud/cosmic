package com.cloud.resource.hypervisor;

import com.cloud.legacymodel.communication.answer.RebootAnswer;
import com.cloud.legacymodel.communication.answer.StartAnswer;
import com.cloud.legacymodel.communication.answer.StopAnswer;
import com.cloud.legacymodel.communication.command.RebootCommand;
import com.cloud.legacymodel.communication.command.StartCommand;
import com.cloud.legacymodel.communication.command.StopCommand;
import com.cloud.resource.ServerResource;

/**
 * HypervisorResource specifies all of the commands a hypervisor agent needs
 */
public interface HypervisorResource extends ServerResource {
    /**
     * Starts a VM.  All information regarding the VM
     * are carried within the command.
     *
     * @param cmd carries all the information necessary to start a VM
     * @return Start2Answer answer.
     */
    StartAnswer execute(StartCommand cmd);

    /**
     * Stops a VM.  Must return true as long as the VM does not exist.
     *
     * @param cmd information necessary to identify the VM to stop.
     * @return StopAnswer
     */
    StopAnswer execute(StopCommand cmd);

    /**
     * Reboots a VM.
     *
     * @param cmd information necessary to identify the VM to reboot.
     * @return RebootAnswer
     */
    RebootAnswer execute(RebootCommand cmd);
}
