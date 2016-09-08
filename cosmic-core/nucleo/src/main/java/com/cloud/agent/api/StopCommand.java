package com.cloud.agent.api;

import com.cloud.agent.api.to.GPUDeviceTO;
import com.cloud.vm.VirtualMachine;

public class StopCommand extends RebootCommand {
    boolean checkBeforeCleanup = false;
    private boolean isProxy = false;
    private String urlPort = null;
    private String publicConsoleProxyIpAddress = null;
    private GPUDeviceTO gpuDevice;

    protected StopCommand() {
    }

    public StopCommand(final VirtualMachine vm, final boolean isProxy, final String urlPort, final String publicConsoleProxyIpAddress, final boolean executeInSequence, final
    boolean checkBeforeCleanup) {
        super(vm.getInstanceName(), executeInSequence);
        this.isProxy = isProxy;
        this.urlPort = urlPort;
        this.publicConsoleProxyIpAddress = publicConsoleProxyIpAddress;
        this.checkBeforeCleanup = checkBeforeCleanup;
    }

    public StopCommand(final VirtualMachine vm, final boolean executeInSequence, final boolean checkBeforeCleanup) {
        super(vm.getInstanceName(), executeInSequence);
        this.checkBeforeCleanup = checkBeforeCleanup;
    }

    public StopCommand(final String vmName, final boolean executeInSequence, final boolean checkBeforeCleanup) {
        super(vmName, executeInSequence);
        this.checkBeforeCleanup = checkBeforeCleanup;
    }

    @Override
    public boolean executeInSequence() {
        //VR stop doesn't go through queue
        if (this.vmName != null && this.vmName.startsWith("r-")) {
            return false;
        }
        return this.executeInSequence;
    }

    public boolean isProxy() {
        return this.isProxy;
    }

    public String getURLPort() {
        return this.urlPort;
    }

    public String getPublicConsoleProxyIpAddress() {
        return this.publicConsoleProxyIpAddress;
    }

    public GPUDeviceTO getGpuDevice() {
        return this.gpuDevice;
    }

    public void setGpuDevice(final GPUDeviceTO gpuDevice) {
        this.gpuDevice = gpuDevice;
    }

    public boolean checkBeforeCleanup() {
        return this.checkBeforeCleanup;
    }
}
