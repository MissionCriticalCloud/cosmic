package com.cloud.engine.subsystem.api.storage;

public class VMSnapshotOptions {
    private final boolean quiesceVM;

    public VMSnapshotOptions(final boolean quiesceVM) {
        this.quiesceVM = quiesceVM;
    }

    public boolean needQuiesceVM() {
        return quiesceVM;
    }
}
