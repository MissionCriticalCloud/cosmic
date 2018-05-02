package com.cloud.agent.api;

import com.cloud.legacymodel.communication.command.Command;

public class ExternalNetworkResourceUsageCommand extends Command {

    public ExternalNetworkResourceUsageCommand() {
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
