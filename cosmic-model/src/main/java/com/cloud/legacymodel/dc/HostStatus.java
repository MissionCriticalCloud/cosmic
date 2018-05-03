package com.cloud.legacymodel.dc;

import com.cloud.legacymodel.exceptions.NoTransitionException;
import com.cloud.legacymodel.statemachine.StateMachine2;
import com.cloud.model.enumeration.Event;

import java.util.Set;

public enum HostStatus {
    Creating(true, false, false),
    Connecting(true, false, false),
    Up(true, false, false),
    Down(true, true, true),
    Disconnected(true, true, true),
    Alert(true, true, true),
    Removed(true, false, true),
    Error(true, false, true),
    Rebalancing(true, false, true),
    Unknown(false, false, false); // null

    protected static final StateMachine2<HostStatus, Event, Host> s_fsm = new StateMachine2<>();

    static {
        s_fsm.addTransition(null, Event.AgentConnected, HostStatus.Connecting);
        s_fsm.addTransition(HostStatus.Creating, Event.AgentConnected, HostStatus.Connecting);
        s_fsm.addTransition(HostStatus.Creating, Event.Error, HostStatus.Error);
        s_fsm.addTransition(HostStatus.Connecting, Event.AgentConnected, HostStatus.Connecting);
        s_fsm.addTransition(HostStatus.Connecting, Event.Ready, HostStatus.Up);
        s_fsm.addTransition(HostStatus.Connecting, Event.PingTimeout, HostStatus.Alert);
        s_fsm.addTransition(HostStatus.Connecting, Event.ShutdownRequested, HostStatus.Disconnected);
        s_fsm.addTransition(HostStatus.Connecting, Event.HostDown, HostStatus.Down);
        s_fsm.addTransition(HostStatus.Connecting, Event.Ping, HostStatus.Connecting);
        s_fsm.addTransition(HostStatus.Connecting, Event.ManagementServerDown, HostStatus.Disconnected);
        s_fsm.addTransition(HostStatus.Connecting, Event.AgentDisconnected, HostStatus.Alert);
        s_fsm.addTransition(HostStatus.Up, Event.PingTimeout, HostStatus.Alert);
        s_fsm.addTransition(HostStatus.Up, Event.AgentDisconnected, HostStatus.Alert);
        s_fsm.addTransition(HostStatus.Up, Event.ShutdownRequested, HostStatus.Disconnected);
        s_fsm.addTransition(HostStatus.Up, Event.HostDown, HostStatus.Down);
        s_fsm.addTransition(HostStatus.Up, Event.Ping, HostStatus.Up);
        s_fsm.addTransition(HostStatus.Up, Event.AgentConnected, HostStatus.Connecting);
        s_fsm.addTransition(HostStatus.Up, Event.ManagementServerDown, HostStatus.Disconnected);
        s_fsm.addTransition(HostStatus.Up, Event.StartAgentRebalance, HostStatus.Rebalancing);
        s_fsm.addTransition(HostStatus.Up, Event.Remove, HostStatus.Removed);
        s_fsm.addTransition(HostStatus.Disconnected, Event.PingTimeout, HostStatus.Alert);
        s_fsm.addTransition(HostStatus.Disconnected, Event.AgentConnected, HostStatus.Connecting);
        s_fsm.addTransition(HostStatus.Disconnected, Event.Ping, HostStatus.Up);
        s_fsm.addTransition(HostStatus.Disconnected, Event.HostDown, HostStatus.Down);
        s_fsm.addTransition(HostStatus.Disconnected, Event.ManagementServerDown, HostStatus.Disconnected);
        s_fsm.addTransition(HostStatus.Disconnected, Event.WaitedTooLong, HostStatus.Alert);
        s_fsm.addTransition(HostStatus.Disconnected, Event.Remove, HostStatus.Removed);
        s_fsm.addTransition(HostStatus.Disconnected, Event.AgentDisconnected, HostStatus.Disconnected);
        s_fsm.addTransition(HostStatus.Down, Event.AgentConnected, HostStatus.Connecting);
        s_fsm.addTransition(HostStatus.Down, Event.Remove, HostStatus.Removed);
        s_fsm.addTransition(HostStatus.Down, Event.ManagementServerDown, HostStatus.Down);
        s_fsm.addTransition(HostStatus.Down, Event.AgentDisconnected, HostStatus.Down);
        s_fsm.addTransition(HostStatus.Down, Event.PingTimeout, HostStatus.Down);
        s_fsm.addTransition(HostStatus.Alert, Event.AgentConnected, HostStatus.Connecting);
        s_fsm.addTransition(HostStatus.Alert, Event.Ping, HostStatus.Up);
        s_fsm.addTransition(HostStatus.Alert, Event.Remove, HostStatus.Removed);
        s_fsm.addTransition(HostStatus.Alert, Event.ManagementServerDown, HostStatus.Alert);
        s_fsm.addTransition(HostStatus.Alert, Event.AgentDisconnected, HostStatus.Alert);
        s_fsm.addTransition(HostStatus.Alert, Event.ShutdownRequested, HostStatus.Disconnected);
        s_fsm.addTransition(HostStatus.Rebalancing, Event.RebalanceFailed, HostStatus.Disconnected);
        s_fsm.addTransition(HostStatus.Rebalancing, Event.RebalanceCompleted, HostStatus.Connecting);
        s_fsm.addTransition(HostStatus.Rebalancing, Event.ManagementServerDown, HostStatus.Disconnected);
        s_fsm.addTransition(HostStatus.Rebalancing, Event.AgentConnected, HostStatus.Connecting);
        s_fsm.addTransition(HostStatus.Rebalancing, Event.AgentDisconnected, HostStatus.Rebalancing);
        s_fsm.addTransition(HostStatus.Error, Event.AgentConnected, HostStatus.Connecting);
    }

    private final boolean updateManagementServer;
    private final boolean checkManagementServer;
    private final boolean lostConnection;

    HostStatus(final boolean updateConnection, final boolean checkManagementServer, final boolean lostConnection) {
        this.updateManagementServer = updateConnection;
        this.checkManagementServer = checkManagementServer;
        this.lostConnection = lostConnection;
    }

    public static StateMachine2<HostStatus, Event, Host> getStateMachine() {
        return s_fsm;
    }

    public boolean updateManagementServer() {
        return updateManagementServer;
    }

    public boolean checkManagementServer() {
        return checkManagementServer;
    }

    public boolean lostConnection() {
        return lostConnection;
    }

    public HostStatus getNextStatus(final Event e) throws NoTransitionException {
        return s_fsm.getNextState(this, e);
    }

    public Set<Event> getPossibleEvents() {
        return s_fsm.getPossibleEvents(this);
    }

}
