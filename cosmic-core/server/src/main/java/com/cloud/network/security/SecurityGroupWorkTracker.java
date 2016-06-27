package com.cloud.network.security;

import com.cloud.agent.AgentManager;
import com.cloud.agent.Listener;
import com.cloud.agent.api.Answer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityGroupWorkTracker {
    protected static final Logger s_logger = LoggerFactory.getLogger(SecurityGroupWorkTracker.class);
    protected AtomicLong _discardCount = new AtomicLong(0);
    AgentManager _agentMgr;
    Listener _answerListener;
    int _bufferLength;

    Map<Long, Integer> _unackedMessages = new ConcurrentHashMap<>();

    public SecurityGroupWorkTracker(final AgentManager agentMgr, final Listener answerListener, final int bufferLength) {
        super();
        assert (bufferLength >= 1) : "SecurityGroupWorkTracker: Cannot have a zero length buffer";
        this._agentMgr = agentMgr;
        this._answerListener = answerListener;
        this._bufferLength = bufferLength;
    }

    public boolean canSend(final long agentId) {
        int currLength = 0;
        synchronized (this) {
            Integer outstanding = _unackedMessages.get(agentId);
            if (outstanding == null) {
                outstanding = 0;
                _unackedMessages.put(agentId, outstanding);
            }
            currLength = outstanding.intValue();
            if (currLength + 1 > _bufferLength) {
                final long discarded = _discardCount.incrementAndGet();
                //drop it on the floor
                s_logger.debug("SecurityGroupManager: dropping a message because there are more than " + currLength + " outstanding messages, total dropped=" + discarded);
                return false;
            }
            _unackedMessages.put(agentId, ++currLength);
        }
        return true;
    }

    public void handleException(final long agentId) {
        synchronized (this) {
            Integer outstanding = _unackedMessages.get(agentId);
            if (outstanding != null && outstanding != 0) {
                _unackedMessages.put(agentId, --outstanding);
            }
        }
    }

    public void processAnswers(final long agentId, final long seq, final Answer[] answers) {
        synchronized (this) {
            Integer outstanding = _unackedMessages.get(agentId);
            if (outstanding != null && outstanding != 0) {
                _unackedMessages.put(agentId, --outstanding);
            }
        }
    }

    public void processTimeout(final long agentId, final long seq) {
        synchronized (this) {
            Integer outstanding = _unackedMessages.get(agentId);
            if (outstanding != null && outstanding != 0) {
                _unackedMessages.put(agentId, --outstanding);
            }
        }
    }

    public void processDisconnect(final long agentId) {
        synchronized (this) {
            _unackedMessages.put(agentId, 0);
        }
    }

    public void processConnect(final long agentId) {
        synchronized (this) {
            _unackedMessages.put(agentId, 0);
        }
    }

    public long getDiscardCount() {
        return _discardCount.get();
    }

    public int getUnackedCount(final long agentId) {
        final Integer outstanding = _unackedMessages.get(agentId);
        if (outstanding == null) {
            return 0;
        }
        return outstanding.intValue();
    }
}
