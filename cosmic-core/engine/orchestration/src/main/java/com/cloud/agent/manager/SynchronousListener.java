package com.cloud.agent.manager;

import com.cloud.agent.Listener;
import com.cloud.agent.api.AgentControlAnswer;
import com.cloud.agent.api.AgentControlCommand;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.StartupCommand;
import com.cloud.host.Host;
import com.cloud.host.Status;
import com.cloud.utils.Profiler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SynchronousListener implements Listener {
    private static final Logger s_logger = LoggerFactory.getLogger(SynchronousListener.class);

    protected Answer[] _answers;
    protected boolean _disconnected;
    protected String _peer;

    public SynchronousListener(final Listener listener) {
        _answers = null;
        _peer = null;
    }

    public String getPeer() {
        return _peer;
    }

    public void setPeer(final String peer) {
        _peer = peer;
    }

    public synchronized Answer[] getAnswers() {
        return _answers;
    }

    public synchronized boolean isDisconnected() {
        return _disconnected;
    }

    @Override
    public synchronized boolean processAnswers(final long agentId, final long seq, final Answer[] resp) {
        _answers = resp;
        notifyAll();
        return true;
    }

    @Override
    public boolean processCommands(final long agentId, final long seq, final Command[] req) {
        return false;
    }

    @Override
    public AgentControlAnswer processControlCommand(final long agentId, final AgentControlCommand cmd) {
        return null;
    }

    @Override
    public void processConnect(final Host agent, final StartupCommand cmd, final boolean forRebalance) {
    }

    @Override
    public synchronized boolean processDisconnect(final long agentId, final Status state) {
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Agent disconnected, agent id: " + agentId + ", state: " + state + ". Will notify waiters");
        }

        _disconnected = true;
        notifyAll();
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

    public Answer[] waitFor() throws InterruptedException {
        return waitFor(-1);
    }

    public synchronized Answer[] waitFor(final int s) throws InterruptedException {
        if (_disconnected) {
            return null;
        }

        if (_answers != null) {
            return _answers;
        }

        final Profiler profiler = new Profiler();
        profiler.start();
        if (s <= 0) {
            wait();
        } else {
            final int ms = s * 1000;
            wait(ms);
        }
        profiler.stop();

        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Synchronized command - sending completed, time: " + profiler.getDurationInMillis() + ", answer: " +
                    (_answers != null ? _answers[0].toString() : "null"));
        }
        return _answers;
    }
}
