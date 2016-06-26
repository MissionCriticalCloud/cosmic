//

//

package org.apache.cloudstack.storage.command;

import com.cloud.agent.api.to.DataTO;

public class ForgetObjectCmd extends StorageSubSystemCommand {
    private final DataTO dataTO;

    public ForgetObjectCmd(final DataTO data) {
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
