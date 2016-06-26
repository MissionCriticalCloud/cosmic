//

//

package com.cloud.resource.hypervisor;

import com.cloud.agent.api.RebootAnswer;
import com.cloud.agent.api.RebootCommand;
import com.cloud.agent.api.StartAnswer;
import com.cloud.agent.api.StartCommand;
import com.cloud.agent.api.StopAnswer;
import com.cloud.agent.api.StopCommand;
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
