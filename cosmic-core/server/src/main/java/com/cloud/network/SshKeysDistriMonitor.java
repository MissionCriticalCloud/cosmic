package com.cloud.network;

import com.cloud.agent.AgentManager;
import com.cloud.agent.Listener;
import com.cloud.agent.manager.Commands;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.host.dao.HostDao;
import com.cloud.legacymodel.communication.answer.AgentControlAnswer;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.AgentControlCommand;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.communication.command.ModifySshKeysCommand;
import com.cloud.legacymodel.communication.command.StartupCommand;
import com.cloud.legacymodel.communication.command.StartupRoutingCommand;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.dc.HostStatus;
import com.cloud.legacymodel.exceptions.AgentUnavailableException;
import com.cloud.legacymodel.exceptions.ConnectionException;
import com.cloud.model.enumeration.HypervisorType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshKeysDistriMonitor implements Listener {
    private static final Logger s_logger = LoggerFactory.getLogger(SshKeysDistriMonitor.class);
    private final HostDao _hostDao;
    AgentManager _agentMgr;
    private final ConfigurationDao _configDao;

    public SshKeysDistriMonitor(final AgentManager mgr, final HostDao host, final ConfigurationDao config) {
        this._agentMgr = mgr;
        _hostDao = host;
        _configDao = config;
    }

    @Override
    public synchronized boolean processAnswers(final long agentId, final long seq, final Answer[] resp) {
        return true;
    }

    @Override
    public boolean processCommands(final long agentId, final long seq, final Command[] commands) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public AgentControlAnswer processControlCommand(final long agentId, final AgentControlCommand cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void processConnect(final Host host, final StartupCommand cmd, final boolean forRebalance) throws ConnectionException {
        if (cmd instanceof StartupRoutingCommand) {
            if (((StartupRoutingCommand) cmd).getHypervisorType() == HypervisorType.KVM || ((StartupRoutingCommand) cmd).getHypervisorType() == HypervisorType.XenServer) {
                /*TODO: Get the private/public keys here*/

                final String pubKey = _configDao.getValue("ssh.publickey");
                final String prvKey = _configDao.getValue("ssh.privatekey");

                try {
                    final ModifySshKeysCommand cmds = new ModifySshKeysCommand(pubKey, prvKey);
                    final Commands c = new Commands(cmds);
                    _agentMgr.send(host.getId(), c, this);
                } catch (final AgentUnavailableException e) {
                    s_logger.debug("Failed to send keys to agent: " + host.getId());
                }
            }
        }
    }

    @Override
    public synchronized boolean processDisconnect(final long agentId, final HostStatus state) {
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Agent disconnected, agent id: " + agentId + ", state: " + state + ". Will notify waiters");
        }

        return true;
    }

    @Override
    public boolean isRecurring() {
        return false;
    }

    @Override
    public int getTimeout() {
        // TODO Auto-generated method stub
        return -1;
    }

    @Override
    public boolean processTimeout(final long agentId, final long seq) {
        // TODO Auto-generated method stub
        return false;
    }
}
