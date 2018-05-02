package com.cloud.legacymodel.communication.command;

import com.cloud.legacymodel.to.DataTO;

public class ForgetObjectCommand extends StorageSubSystemCommand {
    private final DataTO dataTO;

    public ForgetObjectCommand(final DataTO data) {
        dataTO = data;
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
