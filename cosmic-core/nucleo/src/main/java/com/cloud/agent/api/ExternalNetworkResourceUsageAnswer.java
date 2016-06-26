//

//

package com.cloud.agent.api;

import java.util.HashMap;
import java.util.Map;

public class ExternalNetworkResourceUsageAnswer extends Answer {
    public Map<String, long[]> ipBytes;
    public Map<String, long[]> guestVlanBytes;

    protected ExternalNetworkResourceUsageAnswer() {
    }

    public ExternalNetworkResourceUsageAnswer(final Command command) {
        super(command);
        this.ipBytes = new HashMap<>();
        this.guestVlanBytes = new HashMap<>();
    }

    public ExternalNetworkResourceUsageAnswer(final Command command, final Exception e) {
        super(command, e);
        this.ipBytes = null;
        this.guestVlanBytes = null;
    }
}
