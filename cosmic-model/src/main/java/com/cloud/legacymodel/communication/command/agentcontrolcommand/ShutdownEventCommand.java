package com.cloud.legacymodel.communication.command.agentcontrolcommand;

public class ShutdownEventCommand extends AgentControlCommand {
    final String instanceName;

    public ShutdownEventCommand(final String instanceName) {
        this.instanceName = instanceName;
    }

    public String getInstanceName() {
        return this.instanceName;
    }
}
