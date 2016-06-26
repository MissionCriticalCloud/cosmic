//

//

package org.apache.cloudstack.storage.command;

import com.cloud.agent.api.to.DataTO;

public class IntroduceObjectCmd extends StorageSubSystemCommand {
    private final DataTO dataTO;

    public IntroduceObjectCmd(final DataTO dataTO) {
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
