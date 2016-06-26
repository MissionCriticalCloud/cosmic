//

//

package com.cloud.agent.api;

import com.cloud.host.Host;

public class StartupSecondaryStorageCommand extends StartupCommand {

    public StartupSecondaryStorageCommand() {
        super(Host.Type.SecondaryStorage);
        setIqn("NoIqn");
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }
}
