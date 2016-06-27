package com.cloud.agent.api;

import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.PowerState;

//
// TODO vmsync
// We should also have a HostVmStateReport class instead of using raw Map<> data structure,
// for now, we store host-specific info at each VM entry and host fields are fixed
//
// This needs to be refactor-ed afterwards
//
public class HostVmStateReportEntry {
    VirtualMachine.PowerState state;

    // host name or host uuid
    String host;

    public HostVmStateReportEntry() {
    }

    public HostVmStateReportEntry(final PowerState state, final String host) {
        this.state = state;
        this.host = host;
    }

    public PowerState getState() {
        return state;
    }

    public String getHost() {
        return host;
    }
}
