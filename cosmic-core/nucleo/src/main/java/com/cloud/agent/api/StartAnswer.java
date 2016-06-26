//

//

package com.cloud.agent.api;

import com.cloud.agent.api.to.VirtualMachineTO;

import java.util.Map;

public class StartAnswer extends Answer {
    VirtualMachineTO vm;
    String hostGuid;
    Map<String, String> _iqnToPath;

    protected StartAnswer() {
    }

    public StartAnswer(final StartCommand cmd, final String msg) {
        super(cmd, false, msg);
        this.vm = cmd.getVirtualMachine();
    }

    public StartAnswer(final StartCommand cmd, final Exception e) {
        super(cmd, false, e.getMessage());
        this.vm = cmd.getVirtualMachine();
    }

    public StartAnswer(final StartCommand cmd) {
        super(cmd, true, null);
        this.vm = cmd.getVirtualMachine();
        this.hostGuid = null;
    }

    public StartAnswer(final StartCommand cmd, final String msg, final String guid) {
        super(cmd, true, msg);
        this.vm = cmd.getVirtualMachine();
        this.hostGuid = guid;
    }

    public VirtualMachineTO getVirtualMachine() {
        return vm;
    }

    public String getHost_guid() {
        return hostGuid;
    }

    public Map<String, String> getIqnToPath() {
        return _iqnToPath;
    }

    public void setIqnToPath(final Map<String, String> iqnToPath) {
        _iqnToPath = iqnToPath;
    }
}
