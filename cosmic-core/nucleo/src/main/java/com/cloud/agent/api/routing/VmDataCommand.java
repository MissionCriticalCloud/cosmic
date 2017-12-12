package com.cloud.agent.api.routing;

import com.cloud.agent.api.LogLevel;
import com.cloud.agent.api.LogLevel.Level;

import java.util.ArrayList;
import java.util.List;

public class VmDataCommand extends NetworkElementCommand {

    private String vmName;
    private String vmIpAddress;
    @LogLevel(Level.Trace)
    private List<String[]> vmData = new ArrayList<>();
    private boolean executeInSequence = false;

    protected VmDataCommand() {
    }

    public VmDataCommand(final String vmName, final String vmIpAddress, final boolean executeInSequence) {
        this.vmName = vmName;
        this.vmIpAddress = vmIpAddress;
        this.executeInSequence = executeInSequence;
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

    @Override
    public boolean executeInSequence() {
        return executeInSequence;
    }
}
