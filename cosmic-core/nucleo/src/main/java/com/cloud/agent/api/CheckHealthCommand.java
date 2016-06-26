//

//

package com.cloud.agent.api;

public class CheckHealthCommand extends Command {

    public CheckHealthCommand() {
        setWait(50);
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
