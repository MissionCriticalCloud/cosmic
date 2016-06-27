//

//

package com.cloud.agent.api;

import com.cloud.host.HostEnvironment;

public class SetupCommand extends Command {

    HostEnvironment env;
    boolean multipath;
    boolean needSetup;

    public SetupCommand(final HostEnvironment env) {
        this.env = env;
        this.multipath = false;
        this.needSetup = false;
    }

    protected SetupCommand() {
    }

    public boolean needSetup() {
        return needSetup;
    }

    public void setNeedSetup(final boolean setup) {
        this.needSetup = setup;
    }

    public HostEnvironment getEnvironment() {
        return env;
    }

    public void setMultipathOn() {
        this.multipath = true;
    }

    public boolean useMultipath() {
        return multipath;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }
}
