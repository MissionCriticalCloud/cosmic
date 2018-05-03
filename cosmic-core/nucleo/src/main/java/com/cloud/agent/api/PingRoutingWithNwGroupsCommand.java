package com.cloud.agent.api;

import com.cloud.legacymodel.utils.Pair;
import com.cloud.model.enumeration.HostType;

import java.util.HashMap;
import java.util.Map;

public class PingRoutingWithNwGroupsCommand extends PingRoutingCommand {
    HashMap<String, Pair<Long, Long>> newGroupStates;

    protected PingRoutingWithNwGroupsCommand() {
        super();
    }

    public PingRoutingWithNwGroupsCommand(final HostType type, final long id, final Map<String, HostVmStateReportEntry> hostVmStateReport,
                                          final HashMap<String, Pair<Long, Long>> nwGrpStates) {
        super(type, id, hostVmStateReport);
        newGroupStates = nwGrpStates;
    }

    public HashMap<String, Pair<Long, Long>> getNewGroupStates() {
        return newGroupStates;
    }

    public void setNewGroupStates(final HashMap<String, Pair<Long, Long>> newGroupStates) {
        this.newGroupStates = newGroupStates;
    }
}
