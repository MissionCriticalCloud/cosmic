package com.cloud.legacymodel.communication.command;

import com.cloud.legacymodel.to.DataTO;

public class IntroduceObjectCommand extends StorageSubSystemCommand {
    private final DataTO dataTO;

    public IntroduceObjectCommand(final DataTO dataTO) {
        this.dataTO = dataTO;
    }

    public DataTO getDataTO() {
        return dataTO;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    @Override
    public void setExecuteInSequence(final boolean inSeq) {

    }
}
