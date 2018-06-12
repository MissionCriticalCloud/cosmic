package com.cloud.agent.resource.kvm.storage.utils;

import com.cloud.utils.script.Script;

public class LVM {
    /* The binaries. We expect this to be in $PATH */
    private final String lvresizePath = "lvresize";

    private final int timeout;

    public LVM(final int timeout) {
        this.timeout = timeout;
    }

    public void resize(final long newSize, final String lvPath) throws LVMException {
        final Script script = new Script(lvresizePath, timeout);

        // Always force
        script.add("-f");

        // Size in bytes
        script.add("-L" + newSize + "B");

        // LV path
        script.add(lvPath);

        final String result = script.execute();
        if (result != null) {
            throw new LVMException(result);
        }
    }
}
