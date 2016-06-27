//

//

package com.cloud.agent.api;

import com.cloud.vm.VirtualMachine.PowerState;

public class CheckVirtualMachineAnswer extends Answer {

    Integer vncPort;
    PowerState state;

    protected CheckVirtualMachineAnswer() {
    }

    public CheckVirtualMachineAnswer(final CheckVirtualMachineCommand cmd, final PowerState state, final Integer vncPort) {
        this(cmd, state, vncPort, null);
    }

    public CheckVirtualMachineAnswer(final CheckVirtualMachineCommand cmd, final PowerState state, final Integer vncPort, final String detail) {
        super(cmd, true, detail);
        this.state = state;
        this.vncPort = vncPort;
    }

    public CheckVirtualMachineAnswer(final CheckVirtualMachineCommand cmd, final String detail) {
        super(cmd, false, detail);
    }

    public Integer getVncPort() {
        return vncPort;
    }

    public PowerState getState() {
        return state;
    }
}
