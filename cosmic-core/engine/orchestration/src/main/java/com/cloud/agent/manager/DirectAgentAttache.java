package com.cloud.agent.manager;

import com.cloud.common.managed.context.ManagedContextRunnable;
import com.cloud.common.resource.ServerResource;
import com.cloud.common.transport.Request;
import com.cloud.common.transport.Response;
import com.cloud.framework.config.ConfigKey;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.StartupAnswer;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.communication.command.CronCommand;
import com.cloud.legacymodel.communication.command.PingCommand;
import com.cloud.legacymodel.dc.HostStatus;
import com.cloud.legacymodel.exceptions.AgentUnavailableException;

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
        this._resource = resource;
        this._outstandingTaskCount = new AtomicInteger(0);
        this._outstandingCronTaskCount = new AtomicInteger(0);
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
                this._futures.add(this._agentMgr.getCronJobPool().scheduleAtFixedRate(new PingTask(), interval, interval, TimeUnit.SECONDS));
            }
        } else {
            final Command[] cmds = req.getCommands();
            if (cmds.length > 0 && !(cmds[0] instanceof CronCommand)) {
                queueTask(new Task(req));
                scheduleFromQueue();
            } else {
                final CronCommand cmd = (CronCommand) cmds[0];
                this._futures.add(this._agentMgr.getCronJobPool().scheduleAtFixedRate(new CronTask(req), cmd.getInterval(), cmd.getInterval(), TimeUnit.SECONDS));
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
            this._futures.add(this._agentMgr.getCronJobPool().scheduleAtFixedRate(new PingTask(), interval, interval, TimeUnit.SECONDS));
        }
    }

    @Override
    public void disconnect(final HostStatus state) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Processing disconnect " + this._id + "(" + this._name + ")");
        }

        for (final ScheduledFuture<?> future : this._futures) {
            future.cancel(false);
        }

        synchronized (this) {
            if (this._resource != null) {
                this._resource.disconnected();
                this._resource = null;
            }
        }
    }

    @Override
    public synchronized boolean isClosed() {
        return this._resource == null;
    }

    private synchronized void queueTask(final Task task) {
        this.tasks.add(task);
    }

    private synchronized void scheduleFromQueue() {
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Agent attache=" + this._id + ", task queue size=" + this.tasks.size() + ", outstanding tasks=" + this._outstandingTaskCount.get());
        }
        while (!this.tasks.isEmpty() && this._outstandingTaskCount.get() < this._agentMgr.getDirectAgentThreadCap()) {
            this._outstandingTaskCount.incrementAndGet();
            this._agentMgr.getDirectAgentPool().execute(this.tasks.remove());
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            assert this._resource == null : "Come on now....If you're going to dabble in agent code, you better know how to close out our resources. Ever considered why there's a " +
                    "method called disconnect()?";
            synchronized (this) {
                if (this._resource != null) {
                    s_logger.warn("Lost attache for " + this._id + "(" + this._name + ")");
                    disconnect(HostStatus.Alert);
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
                if (DirectAgentAttache.this._outstandingCronTaskCount.incrementAndGet() >= DirectAgentAttache.this._agentMgr.getDirectAgentThreadCap()) {
                    s_logger.warn("PingTask execution for direct attache(" + DirectAgentAttache.this._id + ") has reached maximum outstanding limit(" + DirectAgentAttache.this._agentMgr
                            .getDirectAgentThreadCap() + "), bailing" +
                            " out");
                    return;
                }

                final ServerResource resource = DirectAgentAttache.this._resource;

                if (resource != null) {
                    PingCommand cmd = resource.getCurrentStatus(DirectAgentAttache.this._id);
                    int retried = 0;
                    while (cmd == null && ++retried <= DirectAgentAttache.this._HostPingRetryCount.value()) {
                        Thread.sleep(1000 * DirectAgentAttache.this._HostPingRetryTimer.value());
                        cmd = resource.getCurrentStatus(DirectAgentAttache.this._id);
                    }

                    if (cmd == null) {
                        s_logger.warn("Unable to get current status on " + DirectAgentAttache.this._id + "(" + DirectAgentAttache.this._name + ")");
                        return;
                    }

                    if (cmd.getContextParam("logid") != null) {
                        MDC.put("logcontextid", cmd.getContextParam("logid"));
                    }
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Ping from " + DirectAgentAttache.this._id + "(" + DirectAgentAttache.this._name + ")");
                    }
                    final long seq = DirectAgentAttache.this._seq++;

                    if (s_logger.isTraceEnabled()) {
                        s_logger.trace("SeqA " + DirectAgentAttache.this._id + "-" + seq + ": " + new Request(DirectAgentAttache.this._id, -1, cmd, false).toString());
                    }

                    DirectAgentAttache.this._agentMgr.handleCommands(DirectAgentAttache.this, seq, new Command[]{cmd});
                } else {
                    s_logger.debug("Unable to send ping because agent is disconnected " + DirectAgentAttache.this._id + "(" + DirectAgentAttache.this._name + ")");
                }
            } catch (final Exception e) {
                s_logger.warn("Unable to complete the ping task", e);
            } finally {
                DirectAgentAttache.this._outstandingCronTaskCount.decrementAndGet();
            }
        }
    }

    protected class CronTask extends ManagedContextRunnable {
        Request _req;

        public CronTask(final Request req) {
            this._req = req;
        }

        private void bailout() {
            final long seq = this._req.getSequence();
            try {
                final Command[] cmds = this._req.getCommands();
                final ArrayList<Answer> answers = new ArrayList<>(cmds.length);
                for (final Command cmd : cmds) {
                    final Answer answer = new Answer(cmd, false, "Bailed out as maximum outstanding task limit reached");
                    answers.add(answer);
                }
                final Response resp = new Response(this._req, answers.toArray(new Answer[answers.size()]));
                processAnswers(seq, resp);
            } catch (final Exception e) {
                s_logger.warn(log(seq, "Exception caught in bailout "), e);
            }
        }

        @Override
        protected void runInContext() {
            final long seq = this._req.getSequence();
            try {
                if (DirectAgentAttache.this._outstandingCronTaskCount.incrementAndGet() >= DirectAgentAttache.this._agentMgr.getDirectAgentThreadCap()) {
                    s_logger.warn("CronTask execution for direct attache(" + DirectAgentAttache.this._id + ") has reached maximum outstanding limit(" + DirectAgentAttache.this._agentMgr
                            .getDirectAgentThreadCap() + "), bailing" +
                            " out");
                    bailout();
                    return;
                }

                final ServerResource resource = DirectAgentAttache.this._resource;
                final Command[] cmds = this._req.getCommands();
                final boolean stopOnError = this._req.stopOnError();

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

                final Response resp = new Response(this._req, answers.toArray(new Answer[answers.size()]));
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug(log(seq, "Response Received: "));
                }

                processAnswers(seq, resp);
            } catch (final Exception e) {
                s_logger.warn(log(seq, "Exception caught "), e);
            } finally {
                DirectAgentAttache.this._outstandingCronTaskCount.decrementAndGet();
            }
        }
    }

    protected class Task extends ManagedContextRunnable {
        Request _req;

        public Task(final Request req) {
            this._req = req;
        }

        @Override
        protected void runInContext() {
            final long seq = this._req.getSequence();
            try {
                final ServerResource resource = DirectAgentAttache.this._resource;
                final Command[] cmds = this._req.getCommands();
                final boolean stopOnError = this._req.stopOnError();

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
                    if (resource != null) {
                        answer = resource.executeRequest(cmds[i]);
                        if (answer == null) {
                            s_logger.warn("Resource returned null answer!");
                            answer = new Answer(cmds[i], false, "Resource returned null answer");
                        }
                    } else {
                        answer = new Answer(cmds[i], false, "Agent is disconnected");
                    }
                    answers.add(answer);
                    if (!answer.getResult() && stopOnError) {
                        if (i < cmds.length - 1 && s_logger.isDebugEnabled()) {
                            s_logger.debug(log(seq, "Cancelling because one of the answers is false and it is stop on error."));
                        }
                        break;
                    }
                }

                final Response resp = new Response(this._req, answers.toArray(new Answer[answers.size()]));
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug(log(seq, "Response Received: "));
                }

                processAnswers(seq, resp);
            } finally {
                DirectAgentAttache.this._outstandingTaskCount.decrementAndGet();
                scheduleFromQueue();
            }
        }
    }
}
