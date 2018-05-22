package com.cloud.agent.service;

import com.cloud.agent.resource.AgentResource;
import com.cloud.common.agent.IAgentControl;
import com.cloud.common.agent.IAgentControlListener;
import com.cloud.common.managed.context.ManagedContextTimerTask;
import com.cloud.common.transport.Request;
import com.cloud.common.transport.Response;
import com.cloud.legacymodel.communication.answer.AgentControlAnswer;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.MaintainAnswer;
import com.cloud.legacymodel.communication.answer.StartupAnswer;
import com.cloud.legacymodel.communication.command.AgentControlCommand;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.communication.command.CronCommand;
import com.cloud.legacymodel.communication.command.MaintainCommand;
import com.cloud.legacymodel.communication.command.PingCommand;
import com.cloud.legacymodel.communication.command.ReadyCommand;
import com.cloud.legacymodel.communication.command.ShutdownCommand;
import com.cloud.legacymodel.communication.command.StartupCommand;
import com.cloud.legacymodel.exceptions.AgentControlChannelException;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.exceptions.NioConnectionException;
import com.cloud.utils.backoff.BackoffAlgorithm;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.nio.HandlerFactory;
import com.cloud.utils.nio.Link;
import com.cloud.utils.nio.NioClient;
import com.cloud.utils.nio.NioConnection;
import com.cloud.utils.nio.Task;
import com.cloud.utils.script.OutputInterpreter;
import com.cloud.utils.script.Script;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * @config {@table
 * || Param Name | Description | Values | Default ||
 * || type | Type of server | Storage / Computing / Routing | No Default ||
 * || workers | # of workers to process the requests | int | 1 ||
 * || host | host to connect to | ip address | localhost ||
 * || port | port to connect to | port number | 8250 ||
 * || instance | Used to allow multiple agents running on the same host | String | none || * }
 * <p>
 * For more configuration options, see the individual types.
 **/
public class Agent implements HandlerFactory, IAgentControl {
    private static final Logger logger = LoggerFactory.getLogger(Agent.class.getName());
    private final ThreadPoolExecutor _urgentTaskPool;
    private final List<IAgentControlListener> _controlListeners = new ArrayList<>();

    private final AgentConfiguration agentConfiguration;
    private final BackoffAlgorithm backOffAlgorithm;

    private final HostRotator hostRotator = new HostRotator();

    private NioConnection _connection;
    private final AgentResource resource;
    private Link _link;
    private Long _id;

    private final Timer _timer = new Timer("Agent Timer");

    private final List<WatchTask> _watchList = new ArrayList<>();
    private long _sequence = 0;
    private long _lastPingResponseTime = 0;
    private long _pingInterval = 0;
    private final AtomicInteger _inProgress = new AtomicInteger();

    private StartupTask _startup = null;
    private final long _startupWaitDefault = 180000;
    private long _startupWait = this._startupWaitDefault;
    private boolean _reconnectAllowed = true;
    private final ExecutorService _executor;

    public Agent(final AgentConfiguration agentConfiguration, final BackoffAlgorithm backOffAlgorithm, final AgentResource resource) {
        this.agentConfiguration = agentConfiguration;
        this.backOffAlgorithm = backOffAlgorithm;
        this.resource = resource;

        configDefaults();

        resource.setAgentControl(this);

        this.hostRotator.addAll(this.agentConfiguration.getHosts());

        createNioClient(agentConfiguration);

        logger.debug("Adding shutdown hook");
        Runtime.getRuntime().addShutdownHook(new ShutdownThread(this));

        this._urgentTaskPool = new ThreadPoolExecutor(agentConfiguration.getPingRetries(), 2 * agentConfiguration.getPingRetries(), 10, TimeUnit.MINUTES, new SynchronousQueue<>(),
                new NamedThreadFactory("UrgentTask"));

        this._executor = new ThreadPoolExecutor(agentConfiguration.getWorkers(), 5 * agentConfiguration.getWorkers(), 1, TimeUnit.DAYS, new LinkedBlockingQueue<>(),
                new NamedThreadFactory("agentRequest-Handler"));

        logger.info("Agent [id = " + (this._id != null ? this._id : "new") + " : type = " + getResourceName() + " : zone = " + agentConfiguration.getZone() + " : pod = "
                + agentConfiguration.getPod() + " : workers = " + agentConfiguration.getWorkers() + " : host = " + agentConfiguration.getHosts() + " : port = " + agentConfiguration.getPort());
    }

    private void configDefaults() {
        if (this.agentConfiguration.getPingRetries() == null) {
            this.agentConfiguration.setPingRetries(5);
        }
        if (this.agentConfiguration.getWorkers() == null) {
            this.agentConfiguration.setWorkers(5);
        }
        if (this.agentConfiguration.getPort() == null) {
            this.agentConfiguration.setPort(8250);
        }
    }

    private void createNioClient(final AgentConfiguration agentProperties) {
        final String host = rotateHost();
        logger.debug("Creating new NIO Client");
        this._connection = new NioClient("Agent", host, agentProperties.getPort(), agentProperties.getWorkers(), this);
    }

    private String rotateHost() {
        final String host = this.hostRotator.nextHost();
        logger.debug("Rotating management server host to {}", host);
        return host;
    }

    public String getResourceName() {
        return this.resource.getClass().getSimpleName();
    }

    public AgentResource getResource() {
        return this.resource;
    }

    public void start() {
        logger.info("Starting Agent resource");
        if (!this.resource.start()) {
            logger.error("Unable to start the resource: " + this.resource.getName());
            throw new CloudRuntimeException("Unable to start the resource: " + this.resource.getName());
        }

        try {
            connectToManagementServer();
        } catch (final NioConnectionException e) {
            logger.warn("Attempted to connect to the  server, but received an unexpected exception, trying again...", e);
        }
        while (!this._connection.isStartup()) {
            logger.info("Backing off for a while before attempting to reconnect to management server");
            this.backOffAlgorithm.waitBeforeRetry();
            createNioClient(this.agentConfiguration);
            try {
                connectToManagementServer();
            } catch (final NioConnectionException e) {
                logger.warn("Attempted to connect to the server, but received an unexpected exception, trying again...", e);
            }
        }
    }

    private void connectToManagementServer() throws NioConnectionException {
        logger.info("Opening connection to management server");
        this._connection.start();
    }

    public void stop(final String reason) {
        logger.info("Stopping the agent due to: {}", reason);
        if (this._connection != null) {
            final ShutdownCommand cmd = new ShutdownCommand(reason);
            try {
                if (this._link != null) {
                    final Request req = new Request(this._id != null ? this._id : -1, -1, cmd, false);
                    this._link.send(req.toBytes());
                }
            } catch (final ClosedChannelException e) {
                logger.warn("Unable to send: " + cmd.toString());
            } catch (final Exception e) {
                logger.warn("Unable to send: " + cmd.toString() + " due to exception: ", e);
            }
            logger.debug("Sending shutdown to management server");
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException e) {
                logger.debug("Who the heck interrupted me here?");
            }
            this._connection.stop();
            this._connection = null;
        }

        if (this.resource != null) {
            this.resource.stop();
        }

        this._urgentTaskPool.shutdownNow();
        synchronized (this) {
            this.notifyAll();
        }
    }

    public void sendStartup(final Link link) {
        final StartupCommand[] startup = this.resource.initialize();
        if (startup != null) {
            final Command[] commands = new Command[startup.length];
            for (int i = 0; i < startup.length; i++) {
                setupStartupCommand(startup[i]);
                commands[i] = startup[i];
            }
            final Request request = new Request(this._id != null ? this._id : -1, -1, commands, false, false);
            request.setSequence(getNextSequence());

            if (logger.isDebugEnabled()) {
                logger.debug("Sending Startup: " + request.toString());
            }
            lockStartupTask(link);
            try {
                link.send(request.toBytes());
            } catch (final ClosedChannelException e) {
                logger.warn("Unable to send reques: " + request.toString());
            }
        }
    }

    protected void setupStartupCommand(final StartupCommand startup) {
        final InetAddress addr;
        try {
            addr = InetAddress.getLocalHost();
        } catch (final UnknownHostException e) {
            logger.warn("Unknown host", e);
            throw new CloudRuntimeException("Cannot get local IP address", e);
        }

        final Script command = new Script("hostname", 500, logger);
        final OutputInterpreter.OneLineParser parser = new OutputInterpreter.OneLineParser();
        final String result = command.execute(parser);
        final String hostname = result == null ? parser.getLine() : addr.toString();

        startup.setId(getId());
        if (startup.getName() == null) {
            startup.setName(hostname);
        }
        startup.setDataCenter(this.agentConfiguration.getZone());
        startup.setPod(this.agentConfiguration.getPod());
        startup.setGuid(getResourceGuid());
        startup.setResourceName(getResourceName());
    }

    protected synchronized long getNextSequence() {
        return this._sequence++;
    }

    public synchronized void lockStartupTask(final Link link) {
        this._startup = new StartupTask(link);
        this._timer.schedule(this._startup, this._startupWait);
    }

    public Long getId() {
        return this._id;
    }

    public String getResourceGuid() {
        final String guid = this.agentConfiguration.getGuid();
        return guid + "-" + getResourceName();
    }

    public void setId(final Long id) {
        logger.info("Set agent id " + id);
        this._id = id;
    }

    @Override
    public Task create(final Task.Type type, final Link link, final byte[] data) {
        return new ServerHandler(type, link, data);
    }

    protected void reconnect(final Link link) {
        if (!this._reconnectAllowed) {
            return;
        }
        synchronized (this) {
            if (this._startup != null) {
                this._startup.cancel();
                this._startup = null;
            }
        }

        link.close();
        link.terminated();

        setLink(null);
        cancelTasks();

        this.resource.disconnected();

        int inProgress = 0;
        do {
            this.backOffAlgorithm.waitBeforeRetry();

            logger.info("Lost connection to the server. Dealing with the remaining commands...");

            inProgress = this._inProgress.get();
            if (inProgress > 0) {
                logger.info("Cannot connect because we still have " + inProgress + " commands in progress.");
            }
        } while (inProgress > 0);

        this._connection.stop();

        try {
            this._connection.cleanUp();
        } catch (final IOException e) {
            logger.warn("Fail to clean up old connection. " + e);
        }

        while (this._connection.isStartup()) {
            this.backOffAlgorithm.waitBeforeRetry();
        }

        do {
            createNioClient(this.agentConfiguration);
            logger.info("Reconnecting...");
            try {
                this._connection.start();
            } catch (final NioConnectionException e) {
                logger.warn("NIO Connection Exception  " + e);
                logger.info("Attempted to connect to the server, but received an unexpected exception, trying again...");
            }
            this.backOffAlgorithm.waitBeforeRetry();
        } while (!this._connection.isStartup());
        logger.info("Connected to the server");
    }

    protected void setLink(final Link link) {
        this._link = link;
    }

    protected void cancelTasks() {
        synchronized (this._watchList) {
            for (final WatchTask task : this._watchList) {
                task.cancel();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Clearing watch list: " + this._watchList.size());
            }
            this._watchList.clear();
        }
    }

    protected void processRequest(final Request request, final Link link) {
        boolean requestLogged = false;
        Response response = null;
        try {
            final Command[] cmds = request.getCommands();
            final Answer[] answers = new Answer[cmds.length];

            for (int i = 0; i < cmds.length; i++) {
                final Command cmd = cmds[i];
                Answer answer;
                if (cmd.getContextParam("logid") != null) {
                    MDC.put("logcontextid", cmd.getContextParam("logid"));
                }

                if (!requestLogged) // ensures request is logged only once per method call
                {
                    final String requestMsg = request.toString();
                    if (requestMsg != null) {
                        logger.debug("Request:" + requestMsg);
                    }
                    requestLogged = true;
                }
                logger.debug("Processing command: " + cmd.toString());

                if (cmd instanceof CronCommand) {
                    final CronCommand watch = (CronCommand) cmd;
                    scheduleWatch(link, request, (long) watch.getInterval() * 1000, watch.getInterval() * 1000);
                    answer = new Answer(cmd, true, null);
                } else if (cmd instanceof ShutdownCommand) {
                    final ShutdownCommand shutdown = (ShutdownCommand) cmd;
                    logger.debug("Received shutdownCommand, due to: " + shutdown.getReason());
                    cancelTasks();
                    this._reconnectAllowed = false;
                    answer = new Answer(cmd, true, null);
                } else if (cmd instanceof ReadyCommand && ((ReadyCommand) cmd).getDetails() != null) {
                    logger.debug("Not ready to connect to mgt server: " + ((ReadyCommand) cmd).getDetails());
                    System.exit(1);
                    return;
                } else if (cmd instanceof MaintainCommand) {
                    logger.debug("Received maintainCommand");
                    cancelTasks();
                    this._reconnectAllowed = false;
                    answer = new MaintainAnswer((MaintainCommand) cmd);
                } else if (cmd instanceof AgentControlCommand) {
                    answer = null;
                    synchronized (this._controlListeners) {
                        for (final IAgentControlListener listener : this._controlListeners) {
                            answer = listener.processControlRequest(request, (AgentControlCommand) cmd);
                            if (answer != null) {
                                break;
                            }
                        }
                    }

                    if (answer == null) {
                        logger.warn("No handler found to process cmd: " + cmd.toString());
                        answer = new AgentControlAnswer(cmd);
                    }
                } else {
                    if (cmd instanceof ReadyCommand) {
                        processReadyCommand(cmd);
                    }
                    this._inProgress.incrementAndGet();
                    try {
                        answer = this.resource.executeRequest(cmd);
                    } finally {
                        this._inProgress.decrementAndGet();
                    }
                    if (answer == null) {
                        logger.debug("Response: unsupported command" + cmd.toString());
                        answer = Answer.createUnsupportedCommandAnswer(cmd);
                    }
                }

                answers[i] = answer;
                if (!answer.getResult() && request.stopOnError()) {
                    for (i++; i < cmds.length; i++) {
                        answers[i] = new Answer(cmds[i], false, "Stopped by previous failure");
                    }
                    break;
                }
            }
            response = new Response(request, answers);
        } catch (final RuntimeException e) {
            logger.error("Error while handling request: " + e.getMessage());
            logger.error(ExceptionUtils.getRootCauseMessage(e));
        } finally {
            if (logger.isDebugEnabled()) {
                final String responseMsg = response.toString();
                if (responseMsg != null) {
                    logger.debug(response.toString());
                }
            }

            if (response != null) {
                try {
                    link.send(response.toBytes());
                } catch (final ClosedChannelException e) {
                    logger.warn("Unable to send response: " + response.toString());
                }
            }
        }
    }

    public void scheduleWatch(final Link link, final Request request, final long delay, final long period) {
        synchronized (this._watchList) {
            if (logger.isDebugEnabled()) {
                logger.debug("Adding a watch list");
            }
            final WatchTask task = new WatchTask(link, request, this);
            this._timer.schedule(task, 0, period);
            this._watchList.add(task);
        }
    }

    public void processReadyCommand(final Command cmd) {

        final ReadyCommand ready = (ReadyCommand) cmd;

        logger.info("Process agent ready command, agent id = " + ready.getHostId());
        if (ready.getHostId() != null) {
            setId(ready.getHostId());
        }
        logger.info("Ready command is processed: agent id = " + getId());
    }

    public void processResponse(final Response response, final Link link) {
        final Answer answer = response.getAnswer();
        if (logger.isDebugEnabled()) {
            logger.debug("Received response: " + response.toString());
        }
        if (answer instanceof StartupAnswer) {
            processStartupAnswer(answer, response, link);
        } else if (answer instanceof AgentControlAnswer) {
            // Notice, we are doing callback while holding a lock!
            synchronized (this._controlListeners) {
                for (final IAgentControlListener listener : this._controlListeners) {
                    listener.processControlResponse(response, (AgentControlAnswer) answer);
                }
            }
        } else {
            setLastPingResponseTime();
        }
    }

    public void processStartupAnswer(final Answer answer, final Response response, final Link link) {
        boolean cancelled = false;
        synchronized (this) {
            if (this._startup != null) {
                this._startup.cancel();
                this._startup = null;
            } else {
                cancelled = true;
            }
        }
        final StartupAnswer startup = (StartupAnswer) answer;
        if (!startup.getResult()) {
            logger.error("Not allowed to connect to the server: " + answer.getDetails());
            System.exit(1);
        }
        if (cancelled) {
            logger.warn("Threw away a startup answer because we're reconnecting.");
            return;
        }

        logger.info("Process agent startup answer, agent id = " + startup.getHostId());

        setId(startup.getHostId());
        this._pingInterval = (long) startup.getPingInterval() * 1000; // change to ms.

        setLastPingResponseTime();
        scheduleWatch(link, response, this._pingInterval, this._pingInterval);

        this._urgentTaskPool.setKeepAliveTime(2 * this._pingInterval, TimeUnit.MILLISECONDS);

        logger.info("Startup Response Received: agent id = " + getId());
    }

    public synchronized void setLastPingResponseTime() {
        this._lastPingResponseTime = System.currentTimeMillis();
    }

    public void processOtherTask(final Task task) {
        final Object obj = task.get();
        if (obj instanceof Response) {
            if (System.currentTimeMillis() - this._lastPingResponseTime > this._pingInterval * this.agentConfiguration.getPingRetries()) {
                logger.error("Ping Interval has gone past " + this._pingInterval * this.agentConfiguration.getPingRetries() + ". Won't reconnect to mgt server, as connection is still alive");
                return;
            }

            final PingCommand ping = this.resource.getCurrentStatus(getId());
            final Request request = new Request(this._id, -1, ping, false);
            request.setSequence(getNextSequence());
            if (logger.isDebugEnabled()) {
                logger.debug("Sending ping: " + request.toString());
            }

            try {
                task.getLink().send(request.toBytes());
                //if i can send pingcommand out, means the link is ok
                setLastPingResponseTime();
            } catch (final ClosedChannelException e) {
                logger.warn("Unable to send request: " + request.toString());
            }
        } else if (obj instanceof Request) {
            final Request req = (Request) obj;
            final Command command = req.getCommand();
            if (command.getContextParam("logid") != null) {
                MDC.put("logcontextid", command.getContextParam("logid"));
            }
            Answer answer = null;
            this._inProgress.incrementAndGet();
            try {
                answer = this.resource.executeRequest(command);
            } finally {
                this._inProgress.decrementAndGet();
            }
            if (answer != null) {
                final Response response = new Response(req, answer);

                if (logger.isDebugEnabled()) {
                    logger.debug("Watch Sent: " + response.toString());
                }
                try {
                    task.getLink().send(response.toBytes());
                } catch (final ClosedChannelException e) {
                    logger.warn("Unable to send response: " + response.toString());
                }
            }
        } else {
            logger.warn("Ignoring an unknown task");
        }
    }

    public class AgentControlListener implements IAgentControlListener {
        private final Request _request;
        private AgentControlAnswer _answer;

        public AgentControlListener(final Request request) {
            this._request = request;
        }

        public AgentControlAnswer getAnswer() {
            return this._answer;
        }

        @Override
        public Answer processControlRequest(final Request request, final AgentControlCommand cmd) {
            return null;
        }

        @Override
        public void processControlResponse(final Response response, final AgentControlAnswer answer) {
            if (this._request.getSequence() == response.getSequence()) {
                this._answer = answer;
                synchronized (this) {
                    notifyAll();
                }
            }
        }
    }

    protected class ShutdownThread extends Thread {
        Agent _agent;

        public ShutdownThread(final Agent agent) {
            super("AgentShutdownThread");
            this._agent = agent;
        }

        @Override
        public void run() {
            this._agent.stop(ShutdownCommand.Requested);
        }
    }

    @Override
    public void registerControlListener(final IAgentControlListener listener) {
        synchronized (this._controlListeners) {
            this._controlListeners.add(listener);
        }
    }

    public class WatchTask extends ManagedContextTimerTask {
        protected Request _request;
        protected Agent _agent;
        protected Link _link;

        public WatchTask(final Link link, final Request request, final Agent agent) {
            super();
            this._request = request;
            this._link = link;
            this._agent = agent;
        }

        @Override
        protected void runInContext() {
            if (logger.isTraceEnabled()) {
                logger.trace("Scheduling " + (this._request instanceof Response ? "Ping" : "Watch Task"));
            }
            try {
                if (this._request instanceof Response) {
                    Agent.this._urgentTaskPool.submit(new ServerHandler(Task.Type.OTHER, this._link, this._request));
                } else {
                    this._link.schedule(new ServerHandler(Task.Type.OTHER, this._link, this._request));
                }
            } catch (final ClosedChannelException e) {
                logger.warn("Unable to schedule task because channel is closed");
            }
        }
    }

    public class StartupTask extends ManagedContextTimerTask {
        protected Link _link;
        protected volatile boolean cancelled = false;

        public StartupTask(final Link link) {
            logger.debug("Startup task created");
            this._link = link;
        }

        @Override
        public synchronized boolean cancel() {
            // TimerTask.cancel may fail depends on the calling context
            if (!this.cancelled) {
                this.cancelled = true;
                Agent.this._startupWait = Agent.this._startupWaitDefault;
                logger.debug("Startup task cancelled");
                return super.cancel();
            }
            return true;
        }

        @Override
        protected synchronized void runInContext() {
            if (!this.cancelled) {
                if (logger.isInfoEnabled()) {
                    logger.info("The startup command is now cancelled");
                }
                this.cancelled = true;
                Agent.this._startup = null;
                Agent.this._startupWait = Agent.this._startupWaitDefault * 2;
                reconnect(this._link);
            }
        }
    }

    @Override
    public void unregisterControlListener(final IAgentControlListener listener) {
        synchronized (this._controlListeners) {
            this._controlListeners.remove(listener);
        }
    }

    public class AgentRequestHandler extends Task {
        public AgentRequestHandler(final Task.Type type, final Link link, final Request req) {
            super(type, link, req);
        }

        @Override
        protected void doTask(final Task task) {
            final Request req = (Request) get();
            if (!(req instanceof Response)) {
                processRequest(req, task.getLink());
            }
        }
    }

    public class ServerHandler extends Task {
        public ServerHandler(final Task.Type type, final Link link, final byte[] data) {
            super(type, link, data);
        }

        public ServerHandler(final Task.Type type, final Link link, final Request req) {
            super(type, link, req);
        }

        @Override
        public void doTask(final Task task) {
            if (task.getType() == Task.Type.CONNECT) {
                Agent.this.backOffAlgorithm.reset();
                setLink(task.getLink());
                sendStartup(task.getLink());
            } else if (task.getType() == Task.Type.DATA) {
                final Request request;
                try {
                    request = Request.parse(task.getData());
                    if (request instanceof Response) {
                        //It's for pinganswer etc, should be processed immediately.
                        processResponse((Response) request, task.getLink());
                    } else {
                        //put the requests from mgt server into another thread pool, as the request may take a longer time to finish. Don't block the NIO main thread pool
                        //processRequest(request, task.getLink());
                        Agent.this._executor.submit(new AgentRequestHandler(getType(), getLink(), request));
                    }
                } catch (final ClassNotFoundException e) {
                    logger.error("Unable to find this request ");
                } catch (final Exception e) {
                    logger.error("Error parsing task", e);
                }
            } else if (task.getType() == Task.Type.DISCONNECT) {
                reconnect(task.getLink());
                return;
            } else if (task.getType() == Task.Type.OTHER) {
                processOtherTask(task);
            }
        }
    }

    @Override
    public AgentControlAnswer sendRequest(final AgentControlCommand cmd, final int timeoutInMilliseconds) throws AgentControlChannelException {
        final Request request = new Request(getId(), -1, new Command[]{cmd}, true, false);
        request.setSequence(getNextSequence());

        final AgentControlListener listener = new AgentControlListener(request);

        registerControlListener(listener);
        try {
            postRequest(request);
            synchronized (listener) {
                try {
                    listener.wait(timeoutInMilliseconds);
                } catch (final InterruptedException e) {
                    logger.warn("sendRequest is interrupted, exit waiting");
                }
            }

            return listener.getAnswer();
        } finally {
            unregisterControlListener(listener);
        }
    }

    @Override
    public void postRequest(final AgentControlCommand cmd) throws AgentControlChannelException {
        final Request request = new Request(getId(), -1, new Command[]{cmd}, true, false);
        request.setSequence(getNextSequence());
        postRequest(request);
    }

    private void postRequest(final Request request) throws AgentControlChannelException {
        if (this._link != null) {
            try {
                this._link.send(request.toBytes());
            } catch (final ClosedChannelException e) {
                logger.warn("Unable to post agent control request: " + request.toString());
                throw new AgentControlChannelException("Unable to post agent control request due to " + e.getMessage());
            }
        } else {
            throw new AgentControlChannelException("Unable to post agent control request as link is not available");
        }
    }
}
