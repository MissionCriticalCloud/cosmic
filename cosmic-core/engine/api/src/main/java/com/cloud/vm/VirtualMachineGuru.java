package com.cloud.vm;

import com.cloud.agent.manager.Commands;
import com.cloud.deploy.DeployDestination;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.vm.VirtualMachine;

/**
 * A VirtualMachineGuru knows how to process a certain type of virtual machine.
 */
public interface VirtualMachineGuru {

    boolean finalizeVirtualMachineProfile(VirtualMachineProfile profile, DeployDestination dest, ReservationContext context);

    /**
     * finalize the virtual machine deployment.
     *
     * @param cmds    commands that were created.
     * @param profile virtual machine profile.
     * @param dest    destination to send the command.
     * @return true if everything checks out.  false if not and we should try again.
     */
    boolean finalizeDeployment(Commands cmds, VirtualMachineProfile profile, DeployDestination dest, ReservationContext context) throws ResourceUnavailableException;

    /**
     * Check the deployment results.
     *
     * @param cmds    commands and answers that were sent.
     * @param profile virtual machine profile.
     * @param dest    destination it was sent to.
     * @return true if deployment was fine; false if it didn't go well.
     */
    boolean finalizeStart(VirtualMachineProfile profile, long hostId, Commands cmds, ReservationContext context);

    boolean finalizeCommandsOnStart(Commands cmds, VirtualMachineProfile profile);

    void finalizeStop(VirtualMachineProfile profile, Answer answer);

    void finalizeExpunge(VirtualMachine vm);

    /**
     * Prepare Vm for Stop
     *
     * @param profile
     * @return
     */
    void prepareStop(VirtualMachineProfile profile);
}
