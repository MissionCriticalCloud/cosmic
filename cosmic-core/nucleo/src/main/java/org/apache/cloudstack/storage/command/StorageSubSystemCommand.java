//

//

package org.apache.cloudstack.storage.command;

import com.cloud.agent.api.Command;

public abstract class StorageSubSystemCommand extends Command {
    public abstract void setExecuteInSequence(boolean inSeq);
}
