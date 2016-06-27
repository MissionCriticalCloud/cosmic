//

//

package com.cloud.agent.api;

import com.cloud.host.Host;

import java.util.Map;

public class PingStorageCommand extends PingCommand {
    Map<String, Boolean> changes;

    protected PingStorageCommand() {
    }

    public PingStorageCommand(final Host.Type type, final long id, final Map<String, Boolean> changes) {
        super(type, id);
        this.changes = changes;
    }

    public Map<String, Boolean> getChanges() {
        return changes;
    }
}
