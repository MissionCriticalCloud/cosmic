package com.cloud.agent.service;

import com.cloud.agent.IAgentControl;
import com.cloud.agent.IAgentControlListener;
import com.cloud.agent.api.AgentControlAnswer;
import com.cloud.agent.api.AgentControlCommand;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.CronCommand;
import com.cloud.agent.api.MaintainAnswer;
import com.cloud.agent.api.MaintainCommand;
import com.cloud.agent.api.PingCommand;
import com.cloud.agent.api.ReadyCommand;
import com.cloud.agent.api.ShutdownCommand;
import com.cloud.agent.api.StartupAnswer;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.transport.Request;
import com.cloud.agent.transport.Response;
import com.cloud.exception.AgentControlChannelException;
import com.cloud.managed.context.ManagedContextTimerTask;
import com.cloud.resource.ServerResource;
import com.cloud.utils.backoff.BackoffAlgorithm;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.exception.NioConnectionException;
import com.cloud.utils.exception.TaskExecutionException;
import com.cloud.utils.nio.HandlerFactory;
import com.cloud.utils.nio.Link;
import com.cloud.utils.nio.NioClient;
import com.cloud.utils.nio.NioConnection;
import com.cloud.utils.nio.Task;
import com.cloud.utils.script.OutputInterpreter;
import com.cloud.utils.script.Script;

import javax.naming.ConfigurationException;
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

    private final AgentProperties agentProperties;
    private final BackoffAlgorithm backOffAlgorithm;

    private final HostRotator hostRotator = new HostRotator();

    private NioConnection _connection;
    private final ServerResource resource;
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
    private long _startupWait = _startupWaitDefault;
    private boolean _reconnectAllowed = true;
    private final ExecutorService _executor;

    public Agent(final AgentProperties agentProperties, final BackoffAlgorithm backOffAlgorithm, final ServerResource resource) throws ConfigurationException {
        this.agentProperties = agentProperties;
        this.backOffAlgorithm = backOffAlgorithm;
        this.resource = resource;
        resource.setAgentControl(this);

        hostRotator.addAll(agentProperties.getHosts());

        createNioClient(agentProperties);

        logger.debug("Adding shutdown hook");
        Runtime.getRuntime().addShutdownHook(new ShutdownThread(this));

        _urgentTaskPool = new ThreadPoolExecutor(agentProperties.getPingRetries(), 2 * agentProperties.getPingRetries(), 10, TimeUnit.MINUTES, new SynchronousQueue<>(),
                new NamedThreadFactory("UrgentTask"));

        _executor =
                new ThreadPoolExecutor(agentProperties.getWorkers(), 5 * agentProperties.getWorkers(), 1, TimeUnit.DAYS, new LinkedBlockingQueue<>(),
                        new NamedThreadFactory("agentRequest-Handler"));

        logger.info("Agent [id = " + (_id != null ? _id : "new") + " : type = " + getResourceName() + " : zone = " + agentProperties.getZone() + " : pod = "
                + agentProperties.getPod() + " : workers = " + agentProperties.getWorkers() + " : host = " + agentProperties.getHosts() + " : port = " + agentProperties.getPort());
    }

    private void createNioClient(final AgentProperties agentProperties) {
        final String host = rotateHost();
        logger.debug("Creating new NIO Client");
        _connection = new NioClient("Agent", host, agentProperties.getPort(), agentProperties.getWorkers(), this);
    }

    private String rotateHost() {
        final String host = hostRotator.nextHost();
        logger.debug("Rotating management server host to {}", host);
        return host;
    }

    public String getResourceName() {
        return resource.getClass().getSimpleName();
    }

    public ServerResource getResource() {
        return resource;
    }

    public void start() {
        logger.info("Starting Agent resource");
        if (!resource.start()) {
            logger.error("Unable to start the resource: " + resource.getName());
            throw new CloudRuntimeException("Unable to start the resource: " + resource.getName());
        }

        try {
            connectToManagementServer();
        } catch (final NioConnectionException e) {
            logger.warn("Attempted to connect to the  server, but received an unexpected exception, trying again...", e);
        }
        while (!_connection.isStartup()) {
            logger.info("Backing off for a while before attempting to reconnect to management server");
            backOffAlgorithm.waitBeforeRetry();
            createNioClient(agentProperties);
            try {
                connectToManagementServer();
            } catch (final NioConnectionException e) {
                logger.warn("Attempted to connect to the server, but received an unexpected exception, trying again...", e);
            }
        }
    }

    private void connectToManagementServer() throws NioConnectionException {
        logger.info("Opening connection to management server");
        _connection.start();
    }

    public void stop(final String reason) {
        logger.info("Stopping the agent due to: {}", reason);
        if (_connection != null) {
            final ShutdownCommand cmd = new ShutdownCommand(reason);
            try {
                if (_link != null) {
                    final Request req = new Request(_id != null ? _id : -1, -1, cmd, false);
                    _link.send(req.toBytes());
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
            _connection.stop();
            _connection = null;
        }

        if (resource != null) {
            resource.stop();
        }

        _urgentTaskPool.shutdownNow();
        synchronized (this) {
            this.notifyAll();
        }
    }

    public void sendStartup(final Link link) {
        final StartupCommand[] startup = resource.initialize();
        if (startup != null) {
            final Command[] commands = new Command[startup.length];
            for (int i = 0; i < startup.length; i++) {
                setupStartupCommand(startup[i]);
                commands[i] = startup[i];
            }
            final Request request = new Request(_id != null ? _id : -1, -1, commands, false, false);
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
        startup.setDataCenter(agentProperties.getZone());
        startup.setPod(agentProperties.getPod());
        startup.setGuid(getResourceGuid());
        startup.setResourceName(getResourceName());
        if (startup.getVersion() == null) {
            startup.setVersion(agentProperties.getVersion());
        }
    }

    protected synchronized long getNextSequence() {
        return _sequence++;
    }

    public synchronized void lockStartupTask(final Link link) {
        _startup = new StartupTask(link);
        _timer.schedule(_startup, _startupWait);
    }

    public Long getId() {
        return _id;
    }

    public String getResourceGuid() {
        final String guid = agentProperties.getGuid();
        return guid + "-" + getResourceName();
    }

    public void setId(final Long id) {
        logger.info("Set agent id " + id);
        _id = id;
    }

    @Override
    public Task create(final Task.Type type, final Link link, final byte[] data) {
        return new ServerHandler(type, link, data);
    }

    protected void reconnect(final Link link) {
        if (!_reconnectAllowed) {
            return;
        }
        synchronized (this) {
            if (_startup != null) {
                _startup.cancel();
                _startup = null;
            }
        }

        link.close();
        link.terminated();

        setLink(null);
        cancelTasks();

        resource.disconnected();

        int inProgress = 0;
        do {
            backOffAlgorithm.waitBeforeRetry();

            logger.info("Lost connection to the server. Dealing with the remaining commands...");

            inProgress = _inProgress.get();
            if (inProgress > 0) {
                logger.info("Cannot connect because we still have " + inProgress + " commands in progress.");
            }
        } while (inProgress > 0);

        _connection.stop();

        try {
            _connection.cleanUp();
        } catch (final IOException e) {
            logger.warn("Fail to clean up old connection. " + e);
        }

        while (_connection.isStartup()) {
            backOffAlgorithm.waitBeforeRetry();
        }

        do {
            createNioClient(agentProperties);
            logger.info("Reconnecting...");
            try {
                _connection.start();
            } catch (final NioConnectionException e) {
                logger.warn("NIO Connection Exception  " + e);
                logger.info("Attempted to connect to the server, but received an unexpected exception, trying again...");
            }
            backOffAlgorithm.waitBeforeRetry();
        } while (!_connection.isStartup());
        logger.info("Connected to the server");
    }

    protected void setLink(final Link link) {
        _link = link;
    }

    protected void cancelTasks() {
        synchronized (_watchList) {
            for (final WatchTask task : _watchList) {
                task.cancel();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Clearing watch list: " + _watchList.size());
            }
            _watchList.clear();
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
                    _reconnectAllowed = false;
                    answer = new Answer(cmd, true, null);
                } else if (cmd instanceof ReadyCommand && ((ReadyCommand) cmd).getDetails() != null) {
                    logger.debug("Not ready to connect to mgt server: " + ((ReadyCommand) cmd).getDetails());
                    System.exit(1);
                    return;
                } else if (cmd instanceof MaintainCommand) {
                    logger.debug("Received maintainCommand");
                    cancelTasks();
                    _reconnectAllowed = false;
                    answer = new MaintainAnswer((MaintainCommand) cmd);
                } else if (cmd instanceof AgentControlCommand) {
                    answer = null;
                    synchronized (_controlListeners) {
                        for (final IAgentControlListener listener : _controlListeners) {
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
                    _inProgress.incrementAndGet();
                    try {
                        answer = resource.executeRequest(cmd);
                    } finally {
                        _inProgress.decrementAndGet();
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
        } catch (RuntimeException e) {
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
        synchronized (_watchList) {
            if (logger.isDebugEnabled()) {
                logger.debug("Adding a watch list");
            }
            final WatchTask task = new WatchTask(link, request, this);
            _timer.schedule(task, 0, period);
            _watchList.add(task);
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
            synchronized (_controlListeners) {
                for (final IAgentControlListener listener : _controlListeners) {
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
            if (_startup != null) {
                _startup.cancel();
                _startup = null;
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
        _pingInterval = (long) startup.getPingInterval() * 1000; // change to ms.

        setLastPingResponseTime();
        scheduleWatch(link, response, _pingInterval, _pingInterval);

        _urgentTaskPool.setKeepAliveTime(2 * _pingInterval, TimeUnit.MILLISECONDS);

        logger.info("Startup Response Received: agent id = " + getId());
    }

    public synchronized void setLastPingResponseTime() {
        _lastPingResponseTime = System.currentTimeMillis();
    }

    public void processOtherTask(final Task task) {
        final Object obj = task.get();
        if (obj instanceof Response) {
            if (System.currentTimeMillis() - _lastPingResponseTime > _pingInterval * agentProperties.getPingRetries()) {
                logger.error("Ping Interval has gone past " + _pingInterval * agentProperties.getPingRetries() + ". Won't reconnect to mgt server, as connection is still alive");
                return;
            }

            final PingCommand ping = resource.getCurrentStatus(getId());
            final Request request = new Request(_id, -1, ping, false);
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
            _inProgress.incrementAndGet();
            try {
                answer = resource.executeRequest(command);
            } finally {
                _inProgress.decrementAndGet();
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

    public enum ExitStatus {
        Normal(0), // Normal status = 0.
        Upgrade(65), // Exiting for upgrade.
        Configuration(66), // Exiting due to configuration problems.
        Error(67); // Exiting because of error.

        int value;

        ExitStatus(final int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }

    public class AgentControlListener implements IAgentControlListener {
        private final Request _request;
        private AgentControlAnswer _answer;

        public AgentControlListener(final Request request) {
            _request = request;
        }

        public AgentControlAnswer getAnswer() {
            return _answer;
        }

        @Override
        public Answer processControlRequest(final Request request, final AgentControlCommand cmd) {
            return null;
        }

        @Override
        public void processControlResponse(final Response response, final AgentControlAnswer answer) {
            if (_request.getSequence() == response.getSequence()) {
                _answer = answer;
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
            _agent = agent;
        }

        @Override
        public void run() {
            _agent.stop(ShutdownCommand.Requested);
        }
    }

    @Override
    public void registerControlListener(final IAgentControlListener listener) {
        synchronized (_controlListeners) {
            _controlListeners.add(listener);
        }
    }

    public class WatchTask extends ManagedContextTimerTask {
        protected Request _request;
        protected Agent _agent;
        protected Link _link;

        public WatchTask(final Link link, final Request request, final Agent agent) {
            super();
            _request = request;
            _link = link;
            _agent = agent;
        }

        @Override
        protected void runInContext() {
            if (logger.isTraceEnabled()) {
                logger.trace("Scheduling " + (_request instanceof Response ? "Ping" : "Watch Task"));
            }
            try {
                if (_request instanceof Response) {
                    _urgentTaskPool.submit(new ServerHandler(Task.Type.OTHER, _link, _request));
                } else {
                    _link.schedule(new ServerHandler(Task.Type.OTHER, _link, _request));
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
            _link = link;
        }

        @Override
        public synchronized boolean cancel() {
            // TimerTask.cancel may fail depends on the calling context
            if (!cancelled) {
                cancelled = true;
                _startupWait = _startupWaitDefault;
                logger.debug("Startup task cancelled");
                return super.cancel();
            }
            return true;
        }

        @Override
        protected synchronized void runInContext() {
            if (!cancelled) {
                if (logger.isInfoEnabled()) {
                    logger.info("The startup command is now cancelled");
                }
                cancelled = true;
                _startup = null;
                _startupWait = _startupWaitDefault * 2;
                reconnect(_link);
            }
        }
    }

    @Override
    public void unregisterControlListener(final IAgentControlListener listener) {
        synchronized (_controlListeners) {
            _controlListeners.remove(listener);
        }
    }

    public class AgentRequestHandler extends Task {
        public AgentRequestHandler(final Task.Type type, final Link link, final Request req) {
            super(type, link, req);
        }

        @Override
        protected void doTask(final Task task) throws TaskExecutionException {
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
        public void doTask(final Task task) throws TaskExecutionException {
            if (task.getType() == Task.Type.CONNECT) {
                backOffAlgorithm.reset();
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
                        _executor.submit(new AgentRequestHandler(getType(), getLink(), request));
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
        if (_link != null) {
            try {
                _link.send(request.toBytes());
            } catch (final ClosedChannelException e) {
                logger.warn("Unable to post agent control request: " + request.toString());
                throw new AgentControlChannelException("Unable to post agent control request due to " + e.getMessage());
            }
        } else {
            throw new AgentControlChannelException("Unable to post agent control request as link is not available");
        }
    }
}
