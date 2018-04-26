package com.cloud.agent.api;

import com.cloud.storage.to.VolumeObjectTO;

import java.util.List;

public class VMSnapshotBaseCommand extends Command {
    protected List<VolumeObjectTO> volumeTOs;
    protected VMSnapshotTO target;
    protected String vmName;
    protected String platformEmulator;

    public VMSnapshotBaseCommand(final String vmName, final VMSnapshotTO snapshot, final List<VolumeObjectTO> volumeTOs) {
        this.vmName = vmName;
        target = snapshot;
        this.volumeTOs = volumeTOs;
    }

    public List<VolumeObjectTO> getVolumeTOs() {
        return volumeTOs;
    }

    public void setVolumeTOs(final List<VolumeObjectTO> volumeTOs) {
        this.volumeTOs = volumeTOs;
    }

    public VMSnapshotTO getTarget() {
        return target;
    }

    public void setTarget(final VMSnapshotTO target) {
        this.target = target;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(final String vmName) {
        this.vmName = vmName;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    public String getPlatformEmulator() {
        return platformEmulator;
    }

    public void setPlatformEmulator(final String platformEmulator) {
        this.platformEmulator = platformEmulator;
    }
}
