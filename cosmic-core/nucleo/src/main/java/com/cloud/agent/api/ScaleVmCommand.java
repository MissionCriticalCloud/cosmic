//

//

package com.cloud.agent.api;

import com.cloud.agent.api.to.VirtualMachineTO;

public class ScaleVmCommand extends Command {

    VirtualMachineTO vm;
    String vmName;
    int cpus;
    Integer minSpeed;
    Integer maxSpeed;
    long minRam;
    long maxRam;

    public ScaleVmCommand(final String vmName, final int cpus, final Integer minSpeed, final Integer maxSpeed, final long minRam, final long maxRam, final boolean limitCpuUse) {
        super();
        this.vmName = vmName;
        this.cpus = cpus;
        this.minSpeed = minSpeed;
        this.maxSpeed = maxSpeed;
        this.minRam = minRam;
        this.maxRam = maxRam;
        this.vm = new VirtualMachineTO(1L, vmName, null, cpus, minSpeed, maxSpeed, minRam, maxRam, null, null, false, limitCpuUse, null);
        /*vm.setName(vmName);
        vm.setCpus(cpus);
        vm.setRam(minRam, maxRam);*/
    }

    protected ScaleVmCommand() {
    }

    public ScaleVmCommand(final VirtualMachineTO vm) {
        this.vm = vm;
    }

    public VirtualMachineTO getVm() {
        return vm;
    }

    public void setVm(final VirtualMachineTO vm) {
        this.vm = vm;
    }

    public int getCpus() {
        return cpus;
    }

    public void setCpus(final int cpus) {
        this.cpus = cpus;
    }

    public Integer getMinSpeed() {
        return minSpeed;
    }

    public void setMinSpeed(final Integer minSpeed) {
        this.minSpeed = minSpeed;
    }

    public Integer getMaxSpeed() {
        return minSpeed;
    }

    public void setMaxSpeed(final Integer maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public long getMinRam() {
        return minRam;
    }

    public void setMinRam(final long minRam) {
        this.minRam = minRam;
    }

    public long getMaxRam() {
        return maxRam;
    }

    public void setMaxRam(final long maxRam) {
        this.maxRam = maxRam;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(final String vmName) {
        this.vmName = vmName;
    }

    public VirtualMachineTO getVirtualMachine() {
        return vm;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }

    public boolean getLimitCpuUse() {
        // TODO Auto-generated method stub
        return false;
    }
}
