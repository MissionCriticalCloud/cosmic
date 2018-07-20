package com.cloud.model.enumeration;

public enum Event {
    AgentConnected(false, "Agent connected"),
    PingTimeout(false, "Agent is behind on ping"),
    ShutdownRequested(false, "Shutdown requested by the agent"),
    AgentDisconnected(false, "Agent disconnected"),
    AgentUnreachable(false, "Host is found to be disconnected by the investigator"),
    HostDown(false, "Host is found to be down by the investigator"),
    Ping(false, "Ping is received from the host"),
    ManagementServerDown(false, "Management Server that the agent is connected is going down"),
    WaitedTooLong(false, "Waited too long from the agent to reconnect on its own."),
    Remove(true, "Host is removed"),
    Ready(false, "Host is ready for commands"),
    RequestAgentRebalance(false, "Request rebalance for the certain host"),
    StartAgentRebalance(false, "Start rebalance for the certain host"),
    RebalanceCompleted(false, "Host is rebalanced successfully"),
    RebalanceFailed(false, "Failed to rebalance the host"),
    Error(false, "An internal error happened");

    private final boolean isUserRequest;
    private final String comment;

    private Event(final boolean isUserRequest, final String comment) {
        this.isUserRequest = isUserRequest;
        this.comment = comment;
    }

    public String getDescription() {
        return comment;
    }

    public boolean isUserRequest() {
        return isUserRequest;
    }
}
