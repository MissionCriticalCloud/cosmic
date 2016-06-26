//

//

package com.cloud.agent.api.check;

import com.cloud.agent.api.Command;

public class CheckSshCommand extends Command {
    String ip;
    int port;
    int interval;
    int retries;
    String name;

    protected CheckSshCommand() {
        super();
    }

    public CheckSshCommand(final String instanceName, final String ip, final int port) {
        super();
        this.ip = ip;
        this.port = port;
        this.interval = 6;
        this.retries = 100;
        this.name = instanceName;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public int getInterval() {
        return interval;
    }

    public int getRetries() {
        return retries;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
