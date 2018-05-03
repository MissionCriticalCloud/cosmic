package com.cloud.agent.api;

import com.cloud.legacymodel.utils.Pair;
import com.cloud.model.enumeration.HostType;

import java.util.List;
import java.util.Map;

public class PingRoutingWithOvsCommand extends PingRoutingCommand {
    List<Pair<String, Long>> states;

    protected PingRoutingWithOvsCommand() {
        super();
    }

    public PingRoutingWithOvsCommand(final HostType type, final long id, final Map<String, HostVmStateReportEntry> hostVmStateReport,
                                     final List<Pair<String, Long>> ovsStates) {
        super(type, id, hostVmStateReport);

        this.states = ovsStates;
    }

    public List<Pair<String, Long>> getStates() {
        return states;
    }
}
