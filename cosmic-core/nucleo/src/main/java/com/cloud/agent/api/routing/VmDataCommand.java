//

//

package com.cloud.agent.api.routing;

import com.cloud.agent.api.LogLevel;
import com.cloud.agent.api.LogLevel.Log4jLevel;

import java.util.ArrayList;
import java.util.List;

public class VmDataCommand extends NetworkElementCommand {

    String vmIpAddress;
    String vmName;
    @LogLevel(Log4jLevel.Trace)
    List<String[]> vmData;
    boolean executeInSequence = false;

    protected VmDataCommand() {
    }

    public VmDataCommand(final String vmIpAddress, final boolean executeInSequence) {
        this(vmIpAddress, null, executeInSequence);
    }

    public VmDataCommand(final String vmIpAddress, final String vmName, final boolean executeInSequence) {
        this.vmName = vmName;
        this.vmIpAddress = vmIpAddress;
        this.vmData = new ArrayList<>();
        this.executeInSequence = executeInSequence;
    }

    public VmDataCommand(final String vmName) {
        this.vmName = vmName;
        this.vmData = new ArrayList<>();
    }

    @Override
    public boolean executeInSequence() {
        return executeInSequence;
    }

    public String getVmName() {
        return vmName;
    }

    public String getVmIpAddress() {
        return vmIpAddress;
    }

    public List<String[]> getVmData() {
        return vmData;
    }

    public void addVmData(final String folder, final String file, final String contents) {
        vmData.add(new String[]{folder, file, contents});
    }
}
