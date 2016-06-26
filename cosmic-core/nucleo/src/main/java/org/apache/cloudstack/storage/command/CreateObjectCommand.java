//

//

package org.apache.cloudstack.storage.command;

import com.cloud.agent.api.to.DataTO;

public final class CreateObjectCommand extends StorageSubSystemCommand {
    private DataTO data;

    public CreateObjectCommand(final DataTO obj) {
        super();
        data = obj;
    }

    protected CreateObjectCommand() {
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
