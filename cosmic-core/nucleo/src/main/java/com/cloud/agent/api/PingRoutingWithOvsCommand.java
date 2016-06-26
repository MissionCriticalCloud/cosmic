//

//

package com.cloud.agent.api;

import com.cloud.host.Host;
import com.cloud.utils.Pair;

import java.util.List;
import java.util.Map;

public class PingRoutingWithOvsCommand extends PingRoutingCommand {
    List<Pair<String, Long>> states;

    protected PingRoutingWithOvsCommand() {
        super();
    }

    public PingRoutingWithOvsCommand(final Host.Type type, final long id, final Map<String, HostVmStateReportEntry> hostVmStateReport,
                                     final List<Pair<String, Long>> ovsStates) {
        super(type, id, hostVmStateReport);

        this.states = ovsStates;
    }

    public List<Pair<String, Long>> getStates() {
        return states;
    }
}
