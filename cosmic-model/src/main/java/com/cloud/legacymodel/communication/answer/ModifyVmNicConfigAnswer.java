package com.cloud.legacymodel.communication.answer;

public class ModifyVmNicConfigAnswer extends Answer {
    String vmName;

    protected ModifyVmNicConfigAnswer() {
    }

    public ModifyVmNicConfigAnswer(final String vmName) {
        this.vmName = vmName;
    }

    public String getVmName() {
        return vmName;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
