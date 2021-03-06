package com.cloud.storage.listener;

import com.cloud.agent.Listener;
import com.cloud.legacymodel.communication.answer.AgentControlAnswer;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.communication.command.agentcontrol.AgentControlCommand;
import com.cloud.legacymodel.communication.command.startup.StartupCommand;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.dc.HostStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageSyncListener implements Listener {
    private static final Logger s_logger = LoggerFactory.getLogger(StorageSyncListener.class);

    public StorageSyncListener() {
    }

    @Override
    public boolean processAnswers(final long agentId, final long seq, final Answer[] answers) {
        for (final Answer answer : answers) {
            if (answer.getResult() == false) {
                s_logger.warn("Unable to execute sync command: " + answer.toString());
            } else {
                s_logger.debug("Sync command executed: " + answer.toString());
            }
        }
        return true;
    }

    @Override
    public boolean processCommands(final long agentId, final long seq, final Command[] request) {
        return false;
    }

    @Override
    public AgentControlAnswer processControlCommand(final long agentId, final AgentControlCommand cmd) {
        return null;
    }

    @Override
    public void processConnect(final Host agent, final StartupCommand[] cmd, final boolean forRebalance) {
    }

    @Override
    public boolean processDisconnect(final long agentId, final HostStatus state) {
        s_logger.debug("Disconnecting");
        return true;
    }

    @Override
    public boolean isRecurring() {
        return false;
    }

    @Override
    public int getTimeout() {
        return -1;
    }

    @Override
    public boolean processTimeout(final long agentId, final long seq) {
        return true;
    }
}
