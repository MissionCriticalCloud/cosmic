//

//

package org.apache.cloudstack.storage.command;

import com.cloud.agent.api.to.DataTO;

public final class DeleteCommand extends StorageSubSystemCommand {
    private DataTO data;

    public DeleteCommand(final DataTO data) {
        super();
        this.data = data;
    }

    protected DeleteCommand() {
        super();
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    public DataTO getData() {
        return data;
    }

    @Override
    public void setExecuteInSequence(final boolean inSeq) {

    }
}
