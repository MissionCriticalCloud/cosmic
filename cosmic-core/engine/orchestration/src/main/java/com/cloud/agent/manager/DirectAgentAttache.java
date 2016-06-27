package com.cloud.agent.manager;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.CronCommand;
import com.cloud.agent.api.PingCommand;
import com.cloud.agent.api.StartupAnswer;
import com.cloud.agent.transport.Request;
import com.cloud.agent.transport.Response;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.host.Status;
import com.cloud.resource.ServerResource;
import org.apache.cloudstack.framework.config.ConfigKey;
import org.apache.cloudstack.managed.context.ManagedContextRunnable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class DirectAgentAttache extends AgentAttache {
    private final static Logger s_logger = LoggerFactory.getLogger(DirectAgentAttache.class);

    protected final ConfigKey<Integer> _HostPingRetryCount = new ConfigKey<>("Advanced", Integer.class, "host.ping.retry.count", "0",
            "Number of times retrying a host ping while waiting for check results", true);
    protected final ConfigKey<Integer> _HostPingRetryTimer = new ConfigKey<>("Advanced", Integer.class, "host.ping.retry.timer", "5",
            "Interval to wait before retrying a host ping while waiting for check results", true);
    ServerResource _resource;
    List<ScheduledFuture<?>> _futures = new ArrayList<>();
    long _seq = 0;
    LinkedList<Task> tasks = new LinkedList<>();
    AtomicInteger _outstandingTaskCount;
    AtomicInteger _outstandingCronTaskCount;

    public DirectAgentAttache(final AgentManagerImpl agentMgr, final long id, final String name, final ServerResource resource, final boolean maintenance) {
        super(agentMgr, id, name, maintenance);
        _resource = resource;
        _outstandingTaskCount = new AtomicInteger(0);
        _outstandingCronTaskCount = new AtomicInteger(0);
    }

    @Override
    public void send(final Request req) throws AgentUnavailableException {
        req.logD("Executing: ", true);
        if (req instanceof Response) {
            final Response resp = (Response) req;
            final Answer[] answers = resp.getAnswers();
            if (answers != null && answers[0] instanceof StartupAnswer) {
                final StartupAnswer startup = (StartupAnswer) answers[0];
                final int interval = startup.getPingInterval();
                _futures.add(_agentMgr.getCronJobPool().scheduleAtFixedRate(new PingTask(), interval, interval, TimeUnit.SECONDS));
            }
        } else {
            final Command[] cmds = req.getCommands();
            if (cmds.length > 0 && !(cmds[0] instanceof CronCommand)) {
                queueTask(new Task(req));
                scheduleFromQueue();
            } else {
                final CronCommand cmd = (CronCommand) cmds[0];
                _futures.add(_agentMgr.getCronJobPool().scheduleAtFixedRate(new CronTask(req), cmd.getInterval(), cmd.getInterval(), TimeUnit.SECONDS));
            }
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof DirectAgentAttache)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public void process(final Answer[] answers) {
        if (answers != null && answers[0] instanceof StartupAnswer) {
            final StartupAnswer startup = (StartupAnswer) answers[0];
            final int interval = startup.getPingInterval();
            s_logger.info("StartupAnswer received " + startup.getHostId() + " Interval = " + interval);
            _futures.add(_agentMgr.getCronJobPool().scheduleAtFixedRate(new PingTask(), interval, interval, TimeUnit.SECONDS));
        }
    }

    @Override
    public void disconnect(final Status state) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Processing disconnect " + _id + "(" + _name + ")");
        }

        for (final ScheduledFuture<?> future : _futures) {
            future.cancel(false);
        }

        synchronized (this) {
            if (_resource != null) {
                _resource.disconnected();
                _resource = null;
            }
        }
    }

    @Override
    public synchronized boolean isClosed() {
        return _resource == null;
    }

    private synchronized void queueTask(final Task task) {
        tasks.add(task);
    }

    private synchronized void scheduleFromQueue() {
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Agent attache=" + _id + ", task queue size=" + tasks.size() + ", outstanding tasks=" + _outstandingTaskCount.get());
        }
        while (!tasks.isEmpty() && _outstandingTaskCount.get() < _agentMgr.getDirectAgentThreadCap()) {
            _outstandingTaskCount.incrementAndGet();
            _agentMgr.getDirectAgentPool().execute(tasks.remove());
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            assert _resource == null : "Come on now....If you're going to dabble in agent code, you better know how to close out our resources. Ever considered why there's a " +
                    "method called disconnect()?";
            synchronized (this) {
                if (_resource != null) {
                    s_logger.warn("Lost attache for " + _id + "(" + _name + ")");
                    disconnect(Status.Alert);
                }
            }
        } finally {
            super.finalize();
        }
    }

    protected class PingTask extends ManagedContextRunnable {
        @Override
        protected synchronized void runInContext() {
            try {
                if (_outstandingCronTaskCount.incrementAndGet() >= _agentMgr.getDirectAgentThreadCap()) {
                    s_logger.warn("PingTask execution for direct attache(" + _id + ") has reached maximum outstanding limit(" + _agentMgr.getDirectAgentThreadCap() + "), bailing" +
                            " out");
                    return;
                }

                final ServerResource resource = _resource;

                if (resource != null) {
                    PingCommand cmd = resource.getCurrentStatus(_id);
                    int retried = 0;
                    while (cmd == null && ++retried <= _HostPingRetryCount.value()) {
                        Thread.sleep(1000 * _HostPingRetryTimer.value());
                        cmd = resource.getCurrentStatus(_id);
                    }

                    if (cmd == null) {
                        s_logger.warn("Unable to get current status on " + _id + "(" + _name + ")");
                        return;
                    }

                    if (cmd.getContextParam("logid") != null) {
                        MDC.put("logcontextid", cmd.getContextParam("logid"));
                    }
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Ping from " + _id + "(" + _name + ")");
                    }
                    final long seq = _seq++;

                    if (s_logger.isTraceEnabled()) {
                        s_logger.trace("SeqA " + _id + "-" + seq + ": " + new Request(_id, -1, cmd, false).toString());
                    }

                    _agentMgr.handleCommands(DirectAgentAttache.this, seq, new Command[]{cmd});
                } else {
                    s_logger.debug("Unable to send ping because agent is disconnected " + _id + "(" + _name + ")");
                }
            } catch (final Exception e) {
                s_logger.warn("Unable to complete the ping task", e);
            } finally {
                _outstandingCronTaskCount.decrementAndGet();
            }
        }
    }

    protected class CronTask extends ManagedContextRunnable {
        Request _req;

        public CronTask(final Request req) {
            _req = req;
        }

        private void bailout() {
            final long seq = _req.getSequence();
            try {
                final Command[] cmds = _req.getCommands();
                final ArrayList<Answer> answers = new ArrayList<>(cmds.length);
                for (final Command cmd : cmds) {
                    final Answer answer = new Answer(cmd, false, "Bailed out as maximum outstanding task limit reached");
                    answers.add(answer);
                }
                final Response resp = new Response(_req, answers.toArray(new Answer[answers.size()]));
                processAnswers(seq, resp);
            } catch (final Exception e) {
                s_logger.warn(log(seq, "Exception caught in bailout "), e);
            }
        }

        @Override
        protected void runInContext() {
            final long seq = _req.getSequence();
            try {
                if (_outstandingCronTaskCount.incrementAndGet() >= _agentMgr.getDirectAgentThreadCap()) {
                    s_logger.warn("CronTask execution for direct attache(" + _id + ") has reached maximum outstanding limit(" + _agentMgr.getDirectAgentThreadCap() + "), bailing" +
                            " out");
                    bailout();
                    return;
                }

                final ServerResource resource = _resource;
                final Command[] cmds = _req.getCommands();
                final boolean stopOnError = _req.stopOnError();

                if (s_logger.isDebugEnabled()) {
                    s_logger.debug(log(seq, "Executing request"));
                }
                final ArrayList<Answer> answers = new ArrayList<>(cmds.length);
                for (int i = 0; i < cmds.length; i++) {
                    Answer answer = null;
                    final Command currentCmd = cmds[i];
                    if (currentCmd.getContextParam("logid") != null) {
                        MDC.put("logcontextid", currentCmd.getContextParam("logid"));
                    }
                    try {
                        if (resource != null) {
                            answer = resource.executeRequest(cmds[i]);
                            if (answer == null) {
                                s_logger.warn("Resource returned null answer!");
                                answer = new Answer(cmds[i], false, "Resource returned null answer");
                            }
                        } else {
                            answer = new Answer(cmds[i], false, "Agent is disconnected");
                        }
                    } catch (final Exception e) {
                        s_logger.warn(log(seq, "Exception Caught while executing command"), e);
                        answer = new Answer(cmds[i], false, e.toString());
                    }
                    answers.add(answer);
                    if (!answer.getResult() && stopOnError) {
                        if (i < cmds.length - 1 && s_logger.isDebugEnabled()) {
                            s_logger.debug(log(seq, "Cancelling because one of the answers is false and it is stop on error."));
                        }
                        break;
                    }
                }

                final Response resp = new Response(_req, answers.toArray(new Answer[answers.size()]));
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug(log(seq, "Response Received: "));
                }

                processAnswers(seq, resp);
            } catch (final Exception e) {
                s_logger.warn(log(seq, "Exception caught "), e);
            } finally {
                _outstandingCronTaskCount.decrementAndGet();
            }
        }
    }

    protected class Task extends ManagedContextRunnable {
        Request _req;

        public Task(final Request req) {
            _req = req;
        }

        @Override
        protected void runInContext() {
            final long seq = _req.getSequence();
            try {
                final ServerResource resource = _resource;
                final Command[] cmds = _req.getCommands();
                final boolean stopOnError = _req.stopOnError();

                if (s_logger.isDebugEnabled()) {
                    s_logger.debug(log(seq, "Executing request"));
                }
                final ArrayList<Answer> answers = new ArrayList<>(cmds.length);
                for (int i = 0; i < cmds.length; i++) {
                    Answer answer = null;
                    final Command currentCmd = cmds[i];
                    if (currentCmd.getContextParam("logid") != null) {
                        MDC.put("logcontextid", currentCmd.getContextParam("logid"));
                    }
                    try {
                        if (resource != null) {
                            answer = resource.executeRequest(cmds[i]);
                            if (answer == null) {
                                s_logger.warn("Resource returned null answer!");
                                answer = new Answer(cmds[i], false, "Resource returned null answer");
                            }
                        } else {
                            answer = new Answer(cmds[i], false, "Agent is disconnected");
                        }
                    } catch (final Throwable t) {
                        // Catch Throwable as all exceptions will otherwise be eaten by the executor framework
                        s_logger.warn(log(seq, "Throwable caught while executing command"), t);
                        answer = new Answer(cmds[i], false, t.toString());
                    }
                    answers.add(answer);
                    if (!answer.getResult() && stopOnError) {
                        if (i < cmds.length - 1 && s_logger.isDebugEnabled()) {
                            s_logger.debug(log(seq, "Cancelling because one of the answers is false and it is stop on error."));
                        }
                        break;
                    }
                }

                final Response resp = new Response(_req, answers.toArray(new Answer[answers.size()]));
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug(log(seq, "Response Received: "));
                }

                processAnswers(seq, resp);
            } catch (final Throwable t) {
                // This is pretty serious as processAnswers might not be called and the calling process is stuck waiting for the full timeout
                s_logger.error(log(seq, "Throwable caught in runInContext, this will cause the management to become unpredictable"), t);
            } finally {
                _outstandingTaskCount.decrementAndGet();
                scheduleFromQueue();
            }
        }
    }
}
