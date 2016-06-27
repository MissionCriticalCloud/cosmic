//

//

package com.cloud.agent.api;

public class GetVncPortCommand extends Command {
    long id;
    String name;

    public GetVncPortCommand() {
    }

    public GetVncPortCommand(final long id, final String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }
}
