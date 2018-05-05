package com.cloud.agent.manager;

import com.cloud.agent.AgentManager;
import com.cloud.agent.Listener;
import com.cloud.agent.StartupCommandProcessor;
import com.cloud.alert.AlertManager;
import com.cloud.common.managed.context.ManagedContextRunnable;
import com.cloud.common.resource.ServerResource;
import com.cloud.common.transport.Request;
import com.cloud.common.transport.Response;
import com.cloud.dao.EntityManager;
import com.cloud.db.model.Zone;
import com.cloud.db.repository.ZoneRepository;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.framework.config.ConfigKey;
import com.cloud.framework.config.Configurable;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.framework.jobs.AsyncJob;
import com.cloud.framework.jobs.AsyncJobExecutionContext;
import com.cloud.ha.HighAvailabilityManager;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.HypervisorGuruManager;
import com.cloud.legacymodel.communication.answer.AgentControlAnswer;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.PingAnswer;
import com.cloud.legacymodel.communication.answer.ReadyAnswer;
import com.cloud.legacymodel.communication.answer.StartupAnswer;
import com.cloud.legacymodel.communication.answer.UnsupportedAnswer;
import com.cloud.legacymodel.communication.command.AgentControlCommand;
import com.cloud.legacymodel.communication.command.CheckHealthCommand;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.communication.command.PingCommand;
import com.cloud.legacymodel.communication.command.PingRoutingCommand;
import com.cloud.legacymodel.communication.command.ReadyCommand;
import com.cloud.legacymodel.communication.command.ShutdownCommand;
import com.cloud.legacymodel.communication.command.StartupCommand;
import com.cloud.legacymodel.communication.command.StartupProxyCommand;
import com.cloud.legacymodel.communication.command.StartupRoutingCommand;
import com.cloud.legacymodel.communication.command.StartupSecondaryStorageCommand;
import com.cloud.legacymodel.communication.command.StartupStorageCommand;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.dc.HostStatus;
import com.cloud.legacymodel.exceptions.AgentUnavailableException;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.exceptions.ConnectionException;
import com.cloud.legacymodel.exceptions.HypervisorVersionChangedException;
import com.cloud.legacymodel.exceptions.NioConnectionException;
import com.cloud.legacymodel.exceptions.NoTransitionException;
import com.cloud.legacymodel.exceptions.OperationTimedoutException;
import com.cloud.legacymodel.exceptions.TaskExecutionException;
import com.cloud.legacymodel.exceptions.UnsupportedVersionException;
import com.cloud.legacymodel.resource.ResourceState;
import com.cloud.legacymodel.statemachine.StateMachine2;
import com.cloud.legacymodel.utils.Pair;
import com.cloud.model.enumeration.Event;
import com.cloud.model.enumeration.HostType;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.resource.Discoverer;
import com.cloud.resource.ResourceManager;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.QueryBuilder;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.fsm.StateMachine2Transitions;
import com.cloud.utils.identity.ManagementServerNode;
import com.cloud.utils.nio.HandlerFactory;
import com.cloud.utils.nio.Link;
import com.cloud.utils.nio.NioServer;
import com.cloud.utils.nio.Task;
import com.cloud.utils.time.InaccurateClock;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Implementation of the Agent Manager. This class controls the connection to the agents.
 **/
public class AgentManagerImpl extends ManagerBase implements AgentManager, HandlerFactory, Configurable {
    protected static final Logger s_logger = LoggerFactory.getLogger(AgentManagerImpl.class);
    protected static final Logger status_logger = LoggerFactory.getLogger(HostStatus.class);
    protected final ConfigKey<Integer> Workers = new ConfigKey<>("Advanced", Integer.class, "workers", "5",
            "Number of worker threads handling remote agent connections.", false);
    protected final ConfigKey<Integer> Port = new ConfigKey<>("Advanced", Integer.class, "port", "8250", "Port to listen on for remote agent connections.", false);
    protected final ConfigKey<Integer> PingInterval = new ConfigKey<>("Advanced", Integer.class, "ping.interval", "60",
            "Interval to send application level pings to make sure the connection is still working", false);
    protected final ConfigKey<Float> PingTimeout = new ConfigKey<>("Advanced", Float.class, "ping.timeout", "2.5",
            "Multiplier to ping.interval before announcing an agent has timed out", true);
    protected final ConfigKey<Integer> AlertWait = new ConfigKey<>("Advanced", Integer.class, "alert.wait", "1800",
            "Seconds to wait before alerting on a disconnected agent", true);
    protected final ConfigKey<Integer> DirectAgentLoadSize = new ConfigKey<>("Advanced", Integer.class, "direct.agent.load.size", "16",
            "The number of direct agents to load each time", false);
    protected final ConfigKey<Integer> DirectAgentPoolSize = new ConfigKey<>("Advanced", Integer.class, "direct.agent.pool.size", "500",
            "Default size for DirectAgentPool", false);
    protected final ConfigKey<Float> DirectAgentThreadCap = new ConfigKey<>("Advanced", Float.class, "direct.agent.thread.cap", "1",
            "Percentage (as a value between 0 and 1) of direct.agent.pool.size to be used as upper thread cap for a single direct agent to process requests", false);
    protected final ConfigKey<Boolean> CheckTxnBeforeSending = new ConfigKey<>(
            "Developer",
            Boolean.class,
            "check.txn.before.sending.agent.commands",
            "false",
            "This parameter allows developers to enable a check to see if a transaction wraps commands that are sent to the resource.  This is not to be enabled on production " +
                    "systems.",
            true);
    private final Lock _agentStatusLock = new ReentrantLock();
    private final ConcurrentHashMap<Long, Long> _pingMap = new ConcurrentHashMap<>(10007);
    /**
     * _agents is a ConcurrentHashMap, but it is used from within a synchronized block. This will be reported by findbugs as JLM_JSR166_UTILCONCURRENT_MONITORENTER. Maybe a
     * ConcurrentHashMap is not the right thing to use here, but i'm not sure so i leave it alone.
     */
    protected ConcurrentHashMap<Long, AgentAttache> _agents = new ConcurrentHashMap<>(10007);
    protected List<Pair<Integer, Listener>> _hostMonitors = new ArrayList<>(17);
    protected List<Pair<Integer, Listener>> _cmdMonitors = new ArrayList<>(17);
    protected List<Pair<Integer, StartupCommandProcessor>> _creationMonitors = new ArrayList<>(17);
    protected List<Long> _loadingAgents = new ArrayList<>();
    protected int _monitorId = 0;
    @Inject
    protected EntityManager _entityMgr;
    protected NioServer _connection;
    @Inject
    protected HostDao _hostDao = null;
    @Inject
    protected HostPodDao _podDao = null;
    @Inject
    protected ConfigurationDao _configDao = null;
    @Inject
    protected ClusterDao _clusterDao = null;
    @Inject
    protected HighAvailabilityManager _haMgr = null;
    @Inject
    protected AlertManager _alertMgr = null;
    @Inject
    protected HypervisorGuruManager _hvGuruMgr;
    protected int _retry = 2;
    protected long _nodeId = -1;
    protected ExecutorService _executor;
    protected ThreadPoolExecutor _connectExecutor;
    protected ScheduledExecutorService _directAgentExecutor;
    protected ScheduledExecutorService _cronJobExecutor;
    protected ScheduledExecutorService _monitorExecutor;
    protected StateMachine2<HostStatus, Event, Host> _statusStateMachine = HostStatus.getStateMachine();
    private int _directAgentThreadCap;
    @Inject
    ResourceManager _resourceMgr;
    @Inject
    private ZoneRepository _zoneRepository;

    protected AgentManagerImpl() {
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {

        s_logger.info("Ping Timeout is " + this.PingTimeout.value());

        final int threads = this.DirectAgentLoadSize.value();

        this._nodeId = ManagementServerNode.getManagementServerId();
        s_logger.info("Configuring AgentManagerImpl. management server node id(msid): " + this._nodeId);

        final long lastPing = (System.currentTimeMillis() >> 10) - (long) (this.PingTimeout.value() * this.PingInterval.value());
        this._hostDao.markHostsAsDisconnected(this._nodeId, lastPing);

        registerForHostEvents(new BehindOnPingListener(), true, true, false);

        this._executor = new ThreadPoolExecutor(threads, threads, 60l, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new NamedThreadFactory("AgentTaskPool"));

        this._connectExecutor = new ThreadPoolExecutor(100, 500, 60l, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new NamedThreadFactory("AgentConnectTaskPool"));
        // allow core threads to time out even when there are no items in the queue
        this._connectExecutor.allowCoreThreadTimeOut(true);

        this._connection = new NioServer("AgentManager", this.Port.value(), this.Workers.value() + 10, this);
        s_logger.info("Listening on " + this.Port.value() + " with " + this.Workers.value() + " workers");

        // executes all agent commands other than cron and ping
        this._directAgentExecutor = new ScheduledThreadPoolExecutor(this.DirectAgentPoolSize.value(), new NamedThreadFactory("DirectAgent"));
        // executes cron and ping agent commands
        this._cronJobExecutor = new ScheduledThreadPoolExecutor(this.DirectAgentPoolSize.value(), new NamedThreadFactory("DirectAgentCronJob"));
        s_logger.debug("Created DirectAgentAttache pool with size: " + this.DirectAgentPoolSize.value());
        this._directAgentThreadCap = Math.round(this.DirectAgentPoolSize.value() * this.DirectAgentThreadCap.value()) + 1; // add 1 to always make the value > 0

        this._monitorExecutor = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("AgentMonitor"));

        return true;
    }

    @Override
    public boolean start() {
        startDirectlyConnectedHosts();

        if (this._connection != null) {
            try {
                this._connection.start();
            } catch (final NioConnectionException e) {
                s_logger.error("Error when connecting to the NioServer!", e);
            }
        }

        this._monitorExecutor.scheduleWithFixedDelay(new MonitorTask(), this.PingInterval.value(), this.PingInterval.value(), TimeUnit.SECONDS);

        return true;
    }

    public void startDirectlyConnectedHosts() {
        final List<HostVO> hosts = this._resourceMgr.findDirectlyConnectedHosts();
        for (final HostVO host : hosts) {
            loadDirectlyConnectedHost(host, false);
        }
    }

    protected boolean loadDirectlyConnectedHost(final HostVO host, final boolean forRebalance) {
        boolean initialized = false;
        ServerResource resource = null;
        try {
            // load the respective discoverer
            final Discoverer discoverer = this._resourceMgr.getMatchingDiscover(host.getHypervisorType());
            if (discoverer == null) {
                s_logger.info("Could not to find a Discoverer to load the resource: " + host.getId() + " for hypervisor type: " + host.getHypervisorType());
                resource = loadResourcesWithoutHypervisor(host);
            } else {
                resource = discoverer.reloadResource(host);
            }

            if (resource == null) {
                s_logger.warn("Unable to load the resource: " + host.getId());
                return false;
            }

            initialized = true;
        } finally {
            if (!initialized) {
                if (host != null) {
                    agentStatusTransitTo(host, Event.AgentDisconnected, this._nodeId);
                }
            }
        }

        if (forRebalance) {
            tapLoadingAgents(host.getId(), TapAgentsAction.Add);
            final Host h = this._resourceMgr.createHostAndAgent(host.getId(), resource, host.getDetails(), false, null, true);
            tapLoadingAgents(host.getId(), TapAgentsAction.Del);

            return h == null ? false : true;
        } else {
            this._executor.execute(new SimulateStartTask(host.getId(), resource, host.getDetails()));
            return true;
        }
    }

    private ServerResource loadResourcesWithoutHypervisor(final HostVO host) {
        final String resourceName = host.getResource();
        ServerResource resource = null;
        try {
            final Class<?> clazz = Class.forName(resourceName);
            final Constructor<?> constructor = clazz.getConstructor();
            resource = (ServerResource) constructor.newInstance();
        } catch (final ClassNotFoundException e) {
            s_logger.warn("Unable to find class " + host.getResource(), e);
        } catch (final InstantiationException e) {
            s_logger.warn("Unablet to instantiate class " + host.getResource(), e);
        } catch (final IllegalAccessException e) {
            s_logger.warn("Illegal access " + host.getResource(), e);
        } catch (final SecurityException e) {
            s_logger.warn("Security error on " + host.getResource(), e);
        } catch (final NoSuchMethodException e) {
            s_logger.warn("NoSuchMethodException error on " + host.getResource(), e);
        } catch (final IllegalArgumentException e) {
            s_logger.warn("IllegalArgumentException error on " + host.getResource(), e);
        } catch (final InvocationTargetException e) {
            s_logger.warn("InvocationTargetException error on " + host.getResource(), e);
        }

        if (resource != null) {
            this._hostDao.loadDetails(host);

            final HashMap<String, Object> params = new HashMap<>(host.getDetails().size() + 5);
            params.putAll(host.getDetails());

            params.put("guid", host.getGuid());
            params.put("zone", Long.toString(host.getDataCenterId()));
            if (host.getPodId() != null) {
                params.put("pod", Long.toString(host.getPodId()));
            }
            if (host.getClusterId() != null) {
                params.put("cluster", Long.toString(host.getClusterId()));
                String guid = null;
                final ClusterVO cluster = this._clusterDao.findById(host.getClusterId());
                if (cluster.getGuid() == null) {
                    guid = host.getDetail("pool");
                } else {
                    guid = cluster.getGuid();
                }
                if (guid != null && !guid.isEmpty()) {
                    params.put("pool", guid);
                }
            }

            params.put("ipaddress", host.getPrivateIpAddress());
            params.put("secondary.storage.vm", "false");

            try {
                resource.configure(host.getName(), params);
            } catch (final ConfigurationException e) {
                s_logger.warn("Unable to configure resource due to " + e.getMessage());
                return null;
            }

            if (!resource.start()) {
                s_logger.warn("Unable to start the resource");
                return null;
            }
        }
        return resource;
    }

    public boolean tapLoadingAgents(final Long hostId, final TapAgentsAction action) {
        synchronized (this._loadingAgents) {
            if (action == TapAgentsAction.Add) {
                if (this._loadingAgents.contains(hostId)) {
                    return false;
                } else {
                    this._loadingAgents.add(hostId);
                }
            } else if (action == TapAgentsAction.Del) {
                this._loadingAgents.remove(hostId);
            } else if (action == TapAgentsAction.Contains) {
                return this._loadingAgents.contains(hostId);
            } else {
                throw new CloudRuntimeException("Unkonwn TapAgentsAction " + action);
            }
        }
        return true;
    }

    @Override
    public boolean stop() {

        if (this._connection != null) {
            this._connection.stop();
        }

        s_logger.info("Disconnecting agents: " + this._agents.size());
        synchronized (this._agents) {
            for (final AgentAttache agent : this._agents.values()) {
                final HostVO host = this._hostDao.findById(agent.getId());
                if (host == null) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Cant not find host " + agent.getId());
                    }
                } else {
                    if (!agent.forForward()) {
                        agentStatusTransitTo(host, Event.ManagementServerDown, this._nodeId);
                    }
                }
            }
        }

        this._connectExecutor.shutdownNow();
        this._monitorExecutor.shutdownNow();
        return true;
    }

    protected long getTimeout() {
        return (long) (this.PingTimeout.value() * this.PingInterval.value());
    }

    @Override
    public Task create(final Task.Type type, final Link link, final byte[] data) {
        return new AgentHandler(type, link, data);
    }

    private AgentControlAnswer handleControlCommand(final AgentAttache attache, final AgentControlCommand cmd) {
        AgentControlAnswer answer = null;

        for (final Pair<Integer, Listener> listener : this._cmdMonitors) {
            answer = listener.second().processControlCommand(attache.getId(), cmd);

            if (answer != null) {
                return answer;
            }
        }

        s_logger.warn("No handling of agent control command: " + cmd + " sent from " + attache.getId());
        return new AgentControlAnswer(cmd);
    }

    public void handleCommands(final AgentAttache attache, final long sequence, final Command[] cmds) {
        for (final Pair<Integer, Listener> listener : this._cmdMonitors) {
            final boolean processed = listener.second().processCommands(attache.getId(), sequence, cmds);
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("SeqA " + attache.getId() + "-" + sequence + ": " + (processed ? "processed" : "not processed") + " by " + listener.getClass());
            }
        }
    }

    protected boolean handleDisconnectWithInvestigation(final AgentAttache attache, Event event) {
        final long hostId = attache.getId();
        HostVO host = this._hostDao.findById(hostId);

        if (host != null) {
            HostStatus nextStatus = null;
            try {
                nextStatus = host.getStatus().getNextStatus(event);
            } catch (final NoTransitionException ne) {
                /*
                 * Agent may be currently in status of Down, Alert, Removed, namely there is no next status for some events. Why this can happen? Ask God not me. I hate there was
                 * no piece of comment for code handling race condition. God knew what race condition the code dealt with!
                 */
                s_logger.debug("Caught exception while getting agent's next status", ne);
            }

            if (nextStatus == HostStatus.Alert) {
                /* OK, we are going to the bad status, let's see what happened */
                s_logger.info("Investigating why host " + hostId + " has disconnected with event " + event);

                HostStatus determinedState = investigate(attache);
                // if state cannot be determined do nothing and bail out
                if (determinedState == null) {
                    if ((System.currentTimeMillis() >> 10) - host.getLastPinged() > this.AlertWait.value()) {
                        s_logger.warn("Agent " + hostId + " state cannot be determined for more than " + this.AlertWait + "(" + this.AlertWait.value() + ") seconds, will go to Alert state");
                        determinedState = HostStatus.Alert;
                    } else {
                        s_logger.warn("Agent " + hostId + " state cannot be determined, do nothing");
                        return false;
                    }
                }

                final HostStatus currentStatus = host.getStatus();
                s_logger.info("The agent from host " + hostId + " state determined is " + determinedState);

                if (determinedState == HostStatus.Down) {
                    final String message = "Host is down: " + host.getId() + "-" + host.getName() + ". Starting HA on the VMs";
                    s_logger.error(message);
                    if (host.getType() != HostType.SecondaryStorage && host.getType() != HostType.ConsoleProxy) {
                        this._alertMgr.sendAlert(AlertManager.AlertType.ALERT_TYPE_HOST, host.getDataCenterId(), host.getPodId(), "Host down, " + host.getId(), message);
                    }
                    event = Event.HostDown;
                } else if (determinedState == HostStatus.Up) {
                    /* Got ping response from host, bring it back */
                    s_logger.info("Agent is determined to be up and running");
                    agentStatusTransitTo(host, Event.Ping, this._nodeId);
                    return false;
                } else if (determinedState == HostStatus.Disconnected) {
                    s_logger.warn("Agent is disconnected but the host is still up: " + host.getId() + "-" + host.getName());
                    if (currentStatus == HostStatus.Disconnected) {
                        if ((System.currentTimeMillis() >> 10) - host.getLastPinged() > this.AlertWait.value()) {
                            s_logger.warn("Host " + host.getId() + " has been disconnected past the wait time it should be disconnected.");
                            event = Event.WaitedTooLong;
                        } else {
                            s_logger.debug("Host " + host.getId() + " has been determined to be disconnected but it hasn't passed the wait time yet.");
                            return false;
                        }
                    } else if (currentStatus == HostStatus.Up) {
                        final Zone zone = this._zoneRepository.findById(host.getDataCenterId()).orElse(null);
                        final HostPodVO podVO = this._podDao.findById(host.getPodId());
                        final String hostDesc = "name: " + host.getName() + " (id:" + host.getId() + "), availability zone: " + zone.getName() + ", pod: " + podVO.getName();
                        if (host.getType() != HostType.SecondaryStorage && host.getType() != HostType.ConsoleProxy) {
                            this._alertMgr.sendAlert(AlertManager.AlertType.ALERT_TYPE_HOST, host.getDataCenterId(), host.getPodId(), "Host disconnected, " + hostDesc,
                                    "If the agent for host [" + hostDesc + "] is not restarted within " + this.AlertWait + " seconds, host will go to Alert state");
                        }
                        event = Event.AgentDisconnected;
                    }
                } else {
                    // if we end up here we are in alert state, send an alert
                    final Zone zone = this._zoneRepository.findById(host.getDataCenterId()).orElse(null);
                    final HostPodVO podVO = this._podDao.findById(host.getPodId());
                    final String podName = podVO != null ? podVO.getName() : "NO POD";
                    final String hostDesc = "name: " + host.getName() + " (id:" + host.getId() + "), availability zone: " + zone.getName() + ", pod: " + podName;
                    this._alertMgr.sendAlert(AlertManager.AlertType.ALERT_TYPE_HOST, host.getDataCenterId(), host.getPodId(), "Host in ALERT state, " + hostDesc,
                            "In availability zone " + host.getDataCenterId() + ", host is in alert state: " + host.getId() + "-" + host.getName());
                }
            } else {
                s_logger.debug("The next status of agent " + host.getId() + " is not Alert, no need to investigate what happened");
            }
        }
        handleDisconnectWithoutInvestigation(attache, event, true, true);
        host = this._hostDao.findById(hostId); // Maybe the host magically reappeared?
        if (host != null && host.getStatus() == HostStatus.Down) {
            this._haMgr.scheduleRestartForVmsOnHost(host, true);
        }
        return true;
    }

    protected HostStatus investigate(final AgentAttache agent) {
        final Long hostId = agent.getId();
        final HostVO host = this._hostDao.findById(hostId);
        if (host != null && host.getType() != null && !host.getType().isVirtual()) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("checking if agent (" + hostId + ") is alive");
            }
            final Answer answer = easySend(hostId, new CheckHealthCommand());
            if (answer != null && answer.getResult()) {
                final HostStatus status = HostStatus.Up;
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("agent (" + hostId + ") responded to checkHeathCommand, reporting that agent is " + status);
                }
                return status;
            }
            return this._haMgr.investigate(hostId);
        }
        return HostStatus.Alert;
    }

    protected boolean handleDisconnectWithoutInvestigation(final AgentAttache attache, final Event event, final boolean transitState, final boolean removeAgent) {
        final long hostId = attache.getId();

        s_logger.info("Host " + hostId + " is disconnecting with event " + event);
        HostStatus nextStatus = null;
        final HostVO host = this._hostDao.findById(hostId);
        if (host == null) {
            s_logger.warn("Can't find host with " + hostId);
            nextStatus = HostStatus.Removed;
        } else {
            final HostStatus currentStatus = host.getStatus();
            if (currentStatus == HostStatus.Down || currentStatus == HostStatus.Alert || currentStatus == HostStatus.Removed) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Host " + hostId + " is already " + currentStatus);
                }
                nextStatus = currentStatus;
            } else {
                try {
                    nextStatus = currentStatus.getNextStatus(event);
                } catch (final NoTransitionException e) {
                    final String err = "Cannot find next status for " + event + " as current status is " + currentStatus + " for agent " + hostId;
                    s_logger.debug(err);
                    throw new CloudRuntimeException(err);
                }

                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("The next status of agent " + hostId + "is " + nextStatus + ", current status is " + currentStatus);
                }
            }
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Deregistering link for " + hostId + " with state " + nextStatus);
        }

        removeAgent(attache, nextStatus);
        // update the DB
        if (host != null && transitState) {
            disconnectAgent(host, event, this._nodeId);
        }

        return true;
    }

    public void removeAgent(final AgentAttache attache, final HostStatus nextState) {
        if (attache == null) {
            return;
        }
        final long hostId = attache.getId();
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Remove Agent : " + hostId);
        }
        AgentAttache removed = null;
        boolean conflict = false;
        synchronized (this._agents) {
            removed = this._agents.remove(hostId);
            if (removed != null && removed != attache) {
                conflict = true;
                this._agents.put(hostId, removed);
                removed = attache;
            }
        }
        if (conflict) {
            s_logger.debug("Agent for host " + hostId + " is created when it is being disconnected");
        }
        if (removed != null) {
            removed.disconnect(nextState);
        }

        for (final Pair<Integer, Listener> monitor : this._hostMonitors) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Sending Disconnect to listener: " + monitor.second().getClass().getName());
            }
            monitor.second().processDisconnect(hostId, nextState);
        }
    }

    public boolean disconnectAgent(final HostVO host, final Event e, final long msId) {
        host.setDisconnectedOn(new Date());
        if (e.equals(Event.Remove)) {
            host.setGuid(null);
            host.setClusterId(null);
        }

        return agentStatusTransitTo(host, e, msId);
    }

    @DB
    protected boolean noDbTxn() {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        return !txn.dbTxnStarted();
    }

    /**
     * @param commands
     * @return
     */
    private Command[] checkForCommandsAndTag(final Commands commands) {
        final Command[] cmds = commands.toCommands();

        assert cmds.length > 0 : "Ask yourself this about a hundred times.  Why am I  sending zero length commands?";

        setEmptyAnswers(commands, cmds);

        for (final Command cmd : cmds) {
            tagCommand(cmd);
        }
        return cmds;
    }

    protected AgentAttache getAttache(final Long hostId) throws AgentUnavailableException {
        if (hostId == null) {
            return null;
        }
        final AgentAttache agent = findAttache(hostId);
        if (agent == null) {
            s_logger.debug("Unable to find agent for " + hostId);
            throw new AgentUnavailableException("Unable to find agent ", hostId);
        }

        return agent;
    }

    public void notifyAnswersToMonitors(final long agentId, final long seq, final Answer[] answers) {
        for (final Pair<Integer, Listener> listener : this._cmdMonitors) {
            listener.second().processAnswers(agentId, seq, answers);
        }
    }

    /**
     * @param commands
     * @param cmds
     */
    private void setEmptyAnswers(final Commands commands, final Command[] cmds) {
        if (cmds.length == 0) {
            commands.setAnswers(new Answer[0]);
        }
    }

    private static void tagCommand(final Command cmd) {
        final AsyncJobExecutionContext context = AsyncJobExecutionContext.getCurrent();
        if (context != null && context.getJob() != null) {
            final AsyncJob job = context.getJob();

            if (job.getRelated() != null && !job.getRelated().isEmpty()) {
                cmd.setContextParam("job", "job-" + job.getRelated() + "/" + "job-" + job.getId());
            } else {
                cmd.setContextParam("job", "job-" + job.getId());
            }
        }
        if (MDC.get("logcontextid") != null && !MDC.get("logcontextid").isEmpty()) {
            cmd.setContextParam("logid", MDC.get("logcontextid"));
        }
    }

    public AgentAttache findAttache(final long hostId) {
        AgentAttache attache = null;
        synchronized (this._agents) {
            attache = this._agents.get(hostId);
        }
        return attache;
    }

    public boolean executeUserRequest(final long hostId, final Event event) throws AgentUnavailableException {
        if (event == Event.AgentDisconnected) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Received agent disconnect event for host " + hostId);
            }
            AgentAttache attache = null;
            attache = findAttache(hostId);
            if (attache != null) {
                handleDisconnectWithoutInvestigation(attache, Event.AgentDisconnected, true, true);
            }
            return true;
        } else if (event == Event.ShutdownRequested) {
            return reconnect(hostId);
        }
        return false;
    }

    protected void disconnectWithoutInvestigation(final AgentAttache attache, final Event event) {
        this._executor.submit(new DisconnectTask(attache, event, false));
    }

    protected AgentAttache createAttacheForConnect(final HostVO host, final Link link) throws ConnectionException {
        s_logger.debug("create ConnectedAgentAttache for " + host.getId());
        final AgentAttache attache = new ConnectedAgentAttache(this, host.getId(), host.getName(), link, host.isInMaintenanceStates());
        link.attach(attache);

        AgentAttache old = null;
        synchronized (this._agents) {
            old = this._agents.put(host.getId(), attache);
        }
        if (old != null) {
            old.disconnect(HostStatus.Removed);
        }

        return attache;
    }

    private AgentAttache handleConnectedAgent(final Link link, final StartupCommand[] startup, final Request request) {
        AgentAttache attache = null;
        ReadyCommand ready = null;
        try {
            final HostVO host = this._resourceMgr.createHostVOForConnectedAgent(startup);
            if (host != null) {
                ready = new ReadyCommand(host.getDataCenterId(), host.getId());
                attache = createAttacheForConnect(host, link);
                attache = notifyMonitorsOfConnection(attache, startup, false);
            }
        } catch (final Exception e) {
            s_logger.debug("Failed to handle host connection: " + e.toString());
            ready = new ReadyCommand(null);
            ready.setDetails(e.toString());
        } finally {
            if (ready == null) {
                ready = new ReadyCommand(null);
            }
        }

        try {
            if (attache == null) {
                final Request readyRequest = new Request(-1, -1, ready, false);
                link.send(readyRequest.getBytes());
            } else {
                easySend(attache.getId(), ready);
            }
        } catch (final Exception e) {
            s_logger.debug("Failed to send ready command:" + e.toString());
        }
        return attache;
    }

    protected void connectAgent(final Link link, final Command[] cmds, final Request request) {
        // send startupanswer to agent in the very beginning, so agent can move on without waiting for the answer for an undetermined time, if we put this logic into another
        // thread pool.
        final StartupAnswer[] answers = new StartupAnswer[cmds.length];
        Command cmd;
        for (int i = 0; i < cmds.length; i++) {
            cmd = cmds[i];
            if (cmd instanceof StartupRoutingCommand || cmd instanceof StartupProxyCommand || cmd instanceof StartupSecondaryStorageCommand || cmd instanceof StartupStorageCommand) {
                answers[i] = new StartupAnswer((StartupCommand) cmds[i], 0, getPingInterval());
                break;
            }
        }
        Response response = null;
        response = new Response(request, answers[0], this._nodeId, -1);
        try {
            link.send(response.toBytes());
        } catch (final ClosedChannelException e) {
            s_logger.debug("Failed to send startupanswer: " + e.toString());
        }
        this._connectExecutor.execute(new HandleAgentConnectTask(link, cmds, request));
    }

    protected int getPingInterval() {
        return this.PingInterval.value();
    }

    protected boolean isHostOwnerSwitched(final long hostId) {
        final HostVO host = this._hostDao.findById(hostId);
        if (host == null) {
            s_logger.warn("Can't find the host " + hostId);
            return false;
        }
        return isHostOwnerSwitched(host);
    }

    protected boolean isHostOwnerSwitched(final HostVO host) {
        if (host.getStatus() == HostStatus.Up && host.getManagementServerId() != null && host.getManagementServerId() != this._nodeId) {
            return true;
        }
        return false;
    }

    public void disconnectWithInvestigation(final long hostId, final Event event) {
        disconnectInternal(hostId, event, true);
    }

    private void disconnectInternal(final long hostId, final Event event, final boolean invstigate) {
        final AgentAttache attache = findAttache(hostId);

        if (attache != null) {
            if (!invstigate) {
                disconnectWithoutInvestigation(attache, event);
            } else {
                disconnectWithInvestigation(attache, event);
            }
        } else {
            /* Agent is still in connecting process, don't allow to disconnect right away */
            if (tapLoadingAgents(hostId, TapAgentsAction.Contains)) {
                s_logger.info("Host " + hostId + " is being loaded so no disconnects needed.");
                return;
            }

            final HostVO host = this._hostDao.findById(hostId);
            if (host != null && host.getRemoved() == null) {
                disconnectAgent(host, event, this._nodeId);
            }
        }
    }

    public void disconnectWithInvestigation(final AgentAttache attache, final Event event) {
        this._executor.submit(new DisconnectTask(attache, event, true));
    }

    @Override
    public boolean handleDirectConnectAgent(final Host host, final StartupCommand[] cmds, final ServerResource resource, final boolean forRebalance) throws ConnectionException {
        AgentAttache attache;

        attache = createAttacheForDirectConnect(host, resource);
        final StartupAnswer[] answers = new StartupAnswer[cmds.length];
        for (int i = 0; i < answers.length; i++) {
            answers[i] = new StartupAnswer(cmds[i], attache.getId(), this.PingInterval.value());
        }
        attache.process(answers);
        attache = notifyMonitorsOfConnection(attache, cmds, forRebalance);

        return attache != null;
    }

    protected AgentAttache createAttacheForDirectConnect(final Host host, final ServerResource resource) throws ConnectionException {
        s_logger.debug("create DirectAgentAttache for " + host.getId());
        final DirectAgentAttache attache = new DirectAgentAttache(this, host.getId(), host.getName(), resource, host.isInMaintenanceStates());

        AgentAttache old = null;
        synchronized (this._agents) {
            old = this._agents.put(host.getId(), attache);
        }
        if (old != null) {
            old.disconnect(HostStatus.Removed);
        }

        return attache;
    }

    protected AgentAttache notifyMonitorsOfConnection(final AgentAttache attache, final StartupCommand[] cmd, final boolean forRebalance) throws ConnectionException {
        final long hostId = attache.getId();
        final HostVO host = this._hostDao.findById(hostId);
        for (final Pair<Integer, Listener> monitor : this._hostMonitors) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Sending Connect to listener: " + monitor.second().getClass().getSimpleName());
            }
            for (int i = 0; i < cmd.length; i++) {
                try {
                    monitor.second().processConnect(host, cmd[i], forRebalance);
                } catch (final Exception e) {
                    if (e instanceof ConnectionException) {
                        final ConnectionException ce = (ConnectionException) e;
                        if (ce.isSetupError()) {
                            s_logger.warn("Monitor " + monitor.second().getClass().getSimpleName() + " says there is an error in the connect process for " + hostId +
                                    " due to " + e.getMessage());
                            handleDisconnectWithoutInvestigation(attache, Event.AgentDisconnected, true, true);
                            throw ce;
                        } else {
                            s_logger.info("Monitor " + monitor.second().getClass().getSimpleName() + " says not to continue the connect process for " + hostId +
                                    " due to " + e.getMessage());
                            handleDisconnectWithoutInvestigation(attache, Event.ShutdownRequested, true, true);
                            return attache;
                        }
                    } else if (e instanceof HypervisorVersionChangedException) {
                        handleDisconnectWithoutInvestigation(attache, Event.ShutdownRequested, true, true);
                        throw new CloudRuntimeException("Unable to connect " + attache.getId(), e);
                    } else {
                        s_logger.error("Monitor " + monitor.second().getClass().getSimpleName() + " says there is an error in the connect process for " + hostId +
                                " due to " + e.getMessage(), e);
                        handleDisconnectWithoutInvestigation(attache, Event.AgentDisconnected, true, true);
                        throw new CloudRuntimeException("Unable to connect " + attache.getId(), e);
                    }
                }
            }
        }

        final Long dcId = host.getDataCenterId();
        final ReadyCommand ready = new ReadyCommand(dcId, host.getId());
        final Answer answer = easySend(hostId, ready);
        if (answer == null || !answer.getResult()) {
            // this is tricky part for secondary storage
            // make it as disconnected, wait for secondary storage VM to be up
            // return the attache instead of null, even it is disconnectede
            handleDisconnectWithoutInvestigation(attache, Event.AgentDisconnected, true, true);
        }

        agentStatusTransitTo(host, Event.Ready, this._nodeId);
        attache.ready();
        return attache;
    }

    @Override
    public Answer easySend(final Long hostId, final Command cmd) {
        try {
            final Host h = this._hostDao.findById(hostId);
            if (h == null || h.getRemoved() != null) {
                s_logger.debug("Host with id " + hostId + " doesn't exist");
                return null;
            }
            final HostStatus status = h.getStatus();
            if (!status.equals(HostStatus.Up) && !status.equals(HostStatus.Connecting)) {
                s_logger.debug("Can not send command " + cmd + " due to Host " + hostId + " is not up");
                return null;
            }
            final Answer answer = send(hostId, cmd);
            if (answer == null) {
                s_logger.warn("send returns null answer");
                return null;
            }

            if (s_logger.isDebugEnabled() && answer.getDetails() != null) {
                s_logger.debug("Details from executing " + cmd.getClass() + ": " + answer.getDetails());
            }

            return answer;
        } catch (final AgentUnavailableException e) {
            s_logger.warn(e.getMessage());
            return null;
        } catch (final OperationTimedoutException e) {
            s_logger.warn("Operation timed out: " + e.getMessage());
            return null;
        } catch (final Exception e) {
            s_logger.warn("Exception while sending", e);
            return null;
        }
    }

    @Override
    public Answer send(final Long hostId, final Command cmd) throws AgentUnavailableException, OperationTimedoutException {
        final Commands cmds = new Commands(Command.OnError.Stop);
        cmds.addCommand(cmd);
        send(hostId, cmds, cmd.getWait());
        final Answer[] answers = cmds.getAnswers();
        if (answers != null && !(answers[0] instanceof UnsupportedAnswer)) {
            return answers[0];
        }

        if (answers != null && answers[0] instanceof UnsupportedAnswer) {
            s_logger.warn("Unsupported Command: " + answers[0].getDetails());
            return answers[0];
        }

        return null;
    }

    @Override
    public Answer[] send(final Long hostId, final Commands cmds) throws AgentUnavailableException, OperationTimedoutException {
        int wait = 0;
        for (final Command cmd : cmds) {
            if (cmd.getWait() > wait) {
                wait = cmd.getWait();
            }
        }
        return send(hostId, cmds, wait);
    }

    @Override
    public Answer[] send(final Long hostId, final Commands commands, int timeout) throws AgentUnavailableException, OperationTimedoutException {
        assert hostId != null : "Who's not checking the agent id before sending?  ... (finger wagging)";
        if (hostId == null) {
            throw new AgentUnavailableException(-1);
        }

        if (timeout <= 0) {
            timeout = Wait.value();
        }

        if (this.CheckTxnBeforeSending.value()) {
            if (!noDbTxn()) {
                throw new CloudRuntimeException("We do not allow transactions to be wrapped around commands sent to be executed on remote agents.  "
                        + "We cannot predict how long it takes a command to complete.  "
                        + "The transaction may be rolled back because the connection took too long.");
            }
        } else {
            assert noDbTxn() : "I know, I know.  Why are we so strict as to not allow txn across an agent call?  ...  Why are we so cruel ... Why are we such a dictator .... Too" +
                    " bad... Sorry...but NO AGENT COMMANDS WRAPPED WITHIN DB TRANSACTIONS!";
        }

        final Command[] cmds = checkForCommandsAndTag(commands);

        final AgentAttache agent = getAttache(hostId);
        if (agent == null || agent.isClosed()) {
            throw new AgentUnavailableException("agent not logged into this management server", hostId);
        }

        final Request req = new Request(hostId, agent.getName(), this._nodeId, cmds, commands.stopOnError(), true);
        req.setSequence(agent.getNextSequence());
        final Answer[] answers = agent.send(req, timeout);
        notifyAnswersToMonitors(hostId, req.getSequence(), answers);
        commands.setAnswers(answers);
        return answers;
    }

    @Override
    public long send(final Long hostId, final Commands commands, final Listener listener) throws AgentUnavailableException {
        final AgentAttache agent = getAttache(hostId);
        if (agent.isClosed()) {
            throw new AgentUnavailableException("Agent " + agent.getId() + " is closed", agent.getId());
        }

        final Command[] cmds = checkForCommandsAndTag(commands);

        final Request req = new Request(hostId, agent.getName(), this._nodeId, cmds, commands.stopOnError(), true);
        req.setSequence(agent.getNextSequence());

        agent.send(req, listener);
        return req.getSequence();
    }

    @Override
    public int registerForHostEvents(final Listener listener, final boolean connections, final boolean commands, final boolean priority) {
        synchronized (this._hostMonitors) {
            this._monitorId++;
            if (connections) {
                if (priority) {
                    this._hostMonitors.add(0, new Pair<>(this._monitorId, listener));
                } else {
                    this._hostMonitors.add(new Pair<>(this._monitorId, listener));
                }
            }
            if (commands) {
                if (priority) {
                    this._cmdMonitors.add(0, new Pair<>(this._monitorId, listener));
                } else {
                    this._cmdMonitors.add(new Pair<>(this._monitorId, listener));
                }
            }
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Registering listener " + listener.getClass().getSimpleName() + " with id " + this._monitorId);
            }
            return this._monitorId;
        }
    }

    @Override
    public int registerForInitialConnects(final StartupCommandProcessor creator, final boolean priority) {
        synchronized (this._hostMonitors) {
            this._monitorId++;
            if (priority) {
                this._creationMonitors.add(0, new Pair<>(this._monitorId, creator));
            } else {
                this._creationMonitors.add(new Pair<>(this._monitorId, creator));
            }
            return this._monitorId;
        }
    }

    @Override
    public void unregisterForHostEvents(final int id) {
        s_logger.debug("Deregistering " + id);
        this._hostMonitors.remove(id);
    }

    @Override
    public Answer sendTo(final Long dcId, final HypervisorType type, final Command cmd) {
        final List<ClusterVO> clusters = this._clusterDao.listByDcHyType(dcId, type.toString());
        int retry = 0;
        for (final ClusterVO cluster : clusters) {
            final List<HostVO> hosts = this._resourceMgr.listAllUpAndEnabledHosts(HostType.Routing, cluster.getId(), null, dcId);
            for (final HostVO host : hosts) {
                retry++;
                if (retry > this._retry) {
                    return null;
                }
                Answer answer = null;
                try {

                    final long targetHostId = this._hvGuruMgr.getGuruProcessedCommandTargetHost(host.getId(), cmd);
                    answer = easySend(targetHostId, cmd);
                } catch (final Exception e) {
                }
                if (answer != null) {
                    return answer;
                }
            }
        }
        return null;
    }

    @Override
    public boolean agentStatusTransitTo(final HostVO host, final Event e, final long msId) {
        try {
            this._agentStatusLock.lock();
            if (status_logger.isDebugEnabled()) {
                final ResourceState state = host.getResourceState();
                final StringBuilder msg = new StringBuilder("Transition:");
                msg.append("[Resource state = ").append(state);
                msg.append(", Agent event = ").append(e.toString());
                msg.append(", Host id = ").append(host.getId()).append(", name = " + host.getName()).append("]");
                status_logger.debug(msg.toString());
            }

            host.setManagementServerId(msId);
            try {

                return new StateMachine2Transitions(this._statusStateMachine).transitTo(host, e, host.getId(), this._hostDao);
            } catch (final NoTransitionException e1) {
                status_logger.debug("Cannot transit agent status with event " + e + " for host " + host.getId() + ", name=" + host.getName() +
                        ", mangement server id is " + msId);
                throw new CloudRuntimeException("Cannot transit agent status with event " + e + " for host " + host.getId() + ", mangement server id is " + msId + "," +
                        e1.getMessage());
            }
        } finally {
            this._agentStatusLock.unlock();
        }
    }

    @Override
    public boolean isAgentAttached(final long hostId) {
        final AgentAttache agentAttache = findAttache(hostId);
        return agentAttache != null;
    }

    @Override
    public void disconnectWithoutInvestigation(final long hostId, final Event event) {
        disconnectInternal(hostId, event, false);
    }

    @Override
    public void pullAgentToMaintenance(final long hostId) {
        final AgentAttache attache = findAttache(hostId);
        if (attache != null) {
            attache.setMaintenanceMode(true);
            // Now cancel all of the commands except for the active one.
            attache.cancelAllCommands(HostStatus.Disconnected, false);
        }
    }

    @Override
    public void pullAgentOutMaintenance(final long hostId) {
        final AgentAttache attache = findAttache(hostId);
        if (attache != null) {
            attache.setMaintenanceMode(false);
        }
    }

    @Override
    public boolean reconnect(final long hostId) {
        final HostVO host;

        host = this._hostDao.findById(hostId);
        if (host == null || host.getRemoved() != null) {
            s_logger.warn("Unable to find host " + hostId);
            return false;
        }

        if (host.getStatus() == HostStatus.Disconnected) {
            s_logger.info("Host is already disconnected, no work to be done");
            return true;
        }

        if (host.getStatus() != HostStatus.Up && host.getStatus() != HostStatus.Alert && host.getStatus() != HostStatus.Rebalancing) {
            s_logger.info("Unable to disconnect host because it is not in the correct state: host=" + hostId + "; HostStatus=" + host.getStatus());
            return false;
        }

        final AgentAttache attache = findAttache(hostId);
        if (attache == null) {
            s_logger.info("Unable to disconnect host because it is not connected to this server: " + hostId);
            return false;
        }

        disconnectWithoutInvestigation(attache, Event.ShutdownRequested);
        return true;
    }

    @Override
    public void rescan() {
    }

    public ScheduledExecutorService getDirectAgentPool() {
        return this._directAgentExecutor;
    }

    public ScheduledExecutorService getCronJobPool() {
        return this._cronJobExecutor;
    }

    public int getDirectAgentThreadCap() {
        return this._directAgentThreadCap;
    }

    public Long getAgentPingTime(final long agentId) {
        return this._pingMap.get(agentId);
    }

    public void pingBy(final long agentId) {
        // Update PingMap with the latest time if agent entry exists in the PingMap
        if (this._pingMap.replace(agentId, InaccurateClock.getTimeInSeconds()) == null) {
            s_logger.info("PingMap for agent: " + agentId + " will not be updated because agent is no longer in the PingMap");
        }
    }

    @Override
    public String getConfigComponentName() {
        return AgentManager.class.getSimpleName();
    }

    @Override
    public ConfigKey<?>[] getConfigKeys() {
        return new ConfigKey<?>[]{this.CheckTxnBeforeSending, this.Workers, this.Port, this.PingInterval, this.PingTimeout, Wait, this.AlertWait, this.DirectAgentLoadSize, this.DirectAgentPoolSize,
                this.DirectAgentThreadCap};
    }

    protected class DisconnectTask extends ManagedContextRunnable {
        AgentAttache _attache;
        Event _event;
        boolean _investigate;

        DisconnectTask(final AgentAttache attache, final Event event, final boolean investigate) {
            this._attache = attache;
            this._event = event;
            this._investigate = investigate;
        }

        @Override
        protected void runInContext() {
            try {
                if (this._investigate == true) {
                    handleDisconnectWithInvestigation(this._attache, this._event);
                } else {
                    handleDisconnectWithoutInvestigation(this._attache, this._event, true, false);
                }
            } catch (final Exception e) {
                s_logger.error("Exception caught while handling disconnect: ", e);
            }
        }
    }

    protected class SimulateStartTask extends ManagedContextRunnable {
        ServerResource resource;
        Map<String, String> details;
        long id;

        public SimulateStartTask(final long id, final ServerResource resource, final Map<String, String> details) {
            this.id = id;
            this.resource = resource;
            this.details = details;
        }

        @Override
        protected void runInContext() {
            try {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Simulating start for resource " + this.resource.getName() + " id " + this.id);
                }

                if (tapLoadingAgents(this.id, TapAgentsAction.Add)) {
                    try {
                        final AgentAttache agentattache = findAttache(this.id);
                        if (agentattache == null) {
                            s_logger.debug("Creating agent for host " + this.id);
                            AgentManagerImpl.this._resourceMgr.createHostAndAgent(this.id, this.resource, this.details, false, null, false);
                            s_logger.debug("Completed creating agent for host " + this.id);
                        } else {
                            s_logger.debug("Agent already created in another thread for host " + this.id + ", ignore this");
                        }
                    } finally {
                        tapLoadingAgents(this.id, TapAgentsAction.Del);
                    }
                } else {
                    s_logger.debug("Agent creation already getting processed in another thread for host " + this.id + ", ignore this");
                }
            } catch (final Exception e) {
                s_logger.warn("Unable to simulate start on resource " + this.id + " name " + this.resource.getName(), e);
            }
        }
    }

    protected class HandleAgentConnectTask extends ManagedContextRunnable {
        Link _link;
        Command[] _cmds;
        Request _request;

        HandleAgentConnectTask(final Link link, final Command[] cmds, final Request request) {
            this._link = link;
            this._cmds = cmds;
            this._request = request;
        }

        @Override
        protected void runInContext() {
            this._request.logD("Processing the first command ");
            final StartupCommand[] startups = new StartupCommand[this._cmds.length];
            for (int i = 0; i < this._cmds.length; i++) {
                startups[i] = (StartupCommand) this._cmds[i];
            }

            final AgentAttache attache = handleConnectedAgent(this._link, startups, this._request);
            if (attache == null) {
                s_logger.warn("Unable to create attache for agent: " + this._request);
            }
        }
    }

    public class AgentHandler extends Task {
        public AgentHandler(final Task.Type type, final Link link, final byte[] data) {
            super(type, link, data);
        }

        @Override
        protected void doTask(final Task task) throws TaskExecutionException {
            final TransactionLegacy txn = TransactionLegacy.open(TransactionLegacy.CLOUD_DB);
            try {
                final Type type = task.getType();
                if (type == Task.Type.DATA) {
                    final byte[] data = task.getData();
                    try {
                        final Request event = Request.parse(data);
                        if (event instanceof Response) {
                            processResponse(task.getLink(), (Response) event);
                        } else {
                            processRequest(task.getLink(), event);
                        }
                    } catch (final UnsupportedVersionException e) {
                        s_logger.warn(e.getMessage());
                        // upgradeAgent(task.getLink(), data, e.getReason());
                    } catch (final ClassNotFoundException e) {
                        final String message = String.format("Exception occured when executing taks! Error '%s'", e.getMessage());
                        s_logger.error(message);
                        throw new TaskExecutionException(message, e);
                    }
                } else if (type == Task.Type.CONNECT) {
                } else if (type == Task.Type.DISCONNECT) {
                    final Link link = task.getLink();
                    final AgentAttache attache = (AgentAttache) link.attachment();
                    if (attache != null) {
                        disconnectWithInvestigation(attache, Event.AgentDisconnected);
                    } else {
                        s_logger.info("Connection from " + link.getIpAddress() + " closed but no cleanup was done.");
                        link.close();
                        link.terminated();
                    }
                }
            } finally {
                txn.close();
            }
        }

        protected void processResponse(final Link link, final Response response) {
            final AgentAttache attache = (AgentAttache) link.attachment();
            if (attache == null) {
                s_logger.warn("Unable to process: " + response);
            } else if (!attache.processAnswers(response.getSequence(), response)) {
                s_logger.info("Host " + attache.getId() + " - Seq " + response.getSequence() + ": Response is not processed: " + response);
            }
        }

        protected void processRequest(final Link link, final Request request) {
            final AgentAttache attache = (AgentAttache) link.attachment();
            final Command[] cmds = request.getCommands();
            Command cmd = cmds[0];
            boolean logD = true;

            if (attache == null) {
                if (!(cmd instanceof StartupCommand)) {
                    s_logger.warn("Throwing away a request because it came through as the first command on a connect: " + request);
                } else {
                    // submit the task for execution
                    request.logD("Scheduling the first command ");
                    connectAgent(link, cmds, request);
                }
                return;
            }

            final long hostId = attache.getId();
            final String hostName = attache.getName();

            if (s_logger.isDebugEnabled()) {
                if (cmd instanceof PingRoutingCommand) {
                    logD = false;
                    s_logger.debug("Ping from " + hostId + "(" + hostName + ")");
                    s_logger.trace("SeqA " + hostId + "-" + request.getSequence() + ": Processing " + request);
                } else if (cmd instanceof PingCommand) {
                    logD = false;
                    s_logger.debug("Ping from " + hostId + "(" + hostName + ")");
                    s_logger.trace("SeqA " + attache.getId() + "-" + request.getSequence() + ": Processing " + request);
                } else {
                    s_logger.debug("SeqA " + attache.getId() + "-" + request.getSequence() + ": Processing " + request);
                }
            }

            final Answer[] answers = new Answer[cmds.length];
            for (int i = 0; i < cmds.length; i++) {
                cmd = cmds[i];
                Answer answer = null;
                if (cmd instanceof StartupRoutingCommand) {
                    final StartupRoutingCommand startup = (StartupRoutingCommand) cmd;
                    answer = new StartupAnswer(startup, attache.getId(), getPingInterval());
                } else if (cmd instanceof StartupProxyCommand) {
                    final StartupProxyCommand startup = (StartupProxyCommand) cmd;
                    answer = new StartupAnswer(startup, attache.getId(), getPingInterval());
                } else if (cmd instanceof StartupSecondaryStorageCommand) {
                    final StartupSecondaryStorageCommand startup = (StartupSecondaryStorageCommand) cmd;
                    answer = new StartupAnswer(startup, attache.getId(), getPingInterval());
                } else if (cmd instanceof StartupStorageCommand) {
                    final StartupStorageCommand startup = (StartupStorageCommand) cmd;
                    answer = new StartupAnswer(startup, attache.getId(), getPingInterval());
                } else if (cmd instanceof ShutdownCommand) {
                    final ShutdownCommand shutdown = (ShutdownCommand) cmd;
                    final String reason = shutdown.getReason();
                    s_logger.info("Host " + attache.getId() + " has informed us that it is shutting down with reason " + reason + " and detail " +
                            shutdown.getDetail());
                    if (reason.equals(ShutdownCommand.Update)) {
                        // disconnectWithoutInvestigation(attache, Event.UpdateNeeded);
                        throw new CloudRuntimeException("Agent update not implemented");
                    } else if (reason.equals(ShutdownCommand.Requested)) {
                        disconnectWithoutInvestigation(attache, Event.ShutdownRequested);
                    }
                    return;
                } else if (cmd instanceof AgentControlCommand) {
                    answer = handleControlCommand(attache, (AgentControlCommand) cmd);
                } else {
                    handleCommands(attache, request.getSequence(), new Command[]{cmd});
                    if (cmd instanceof PingCommand) {
                        final long cmdHostId = ((PingCommand) cmd).getHostId();

                        // if the router is sending a ping, verify the
                        // gateway was pingable
                        if (cmd instanceof PingRoutingCommand) {
                            final boolean gatewayAccessible = ((PingRoutingCommand) cmd).isGatewayAccessible();
                            final HostVO host = AgentManagerImpl.this._hostDao.findById(Long.valueOf(cmdHostId));

                            if (host != null) {
                                if (!gatewayAccessible) {
                                    // alert that host lost connection to
                                    // gateway (cannot ping the default route)
                                    final Zone zone = AgentManagerImpl.this._zoneRepository.findById(host.getDataCenterId()).orElse(null);
                                    final HostPodVO podVO = AgentManagerImpl.this._podDao.findById(host.getPodId());
                                    final String hostDesc =
                                            "name: " + host.getName() + " (id:" + host.getId() + "), availability zone: " + zone.getName() + ", pod: "
                                                    + podVO.getName();

                                    AgentManagerImpl.this._alertMgr.sendAlert(AlertManager.AlertType.ALERT_TYPE_ROUTING, host.getDataCenterId(), host.getPodId(),
                                            "Host lost connection to gateway, " + hostDesc, "Host [" + hostDesc +
                                                    "] lost connection to gateway (default route) and is possibly having network connection issues.");
                                } else {
                                    AgentManagerImpl.this._alertMgr.clearAlert(AlertManager.AlertType.ALERT_TYPE_ROUTING, host.getDataCenterId(), host.getPodId());
                                }
                            } else {
                                s_logger.debug("Not processing " + PingRoutingCommand.class.getSimpleName() + " for agent id=" + cmdHostId +
                                        "; can't find the host in the DB");
                            }
                        }
                        answer = new PingAnswer((PingCommand) cmd);
                    } else if (cmd instanceof ReadyAnswer) {
                        final HostVO host = AgentManagerImpl.this._hostDao.findById(attache.getId());
                        if (host == null) {
                            if (s_logger.isDebugEnabled()) {
                                s_logger.debug("Cant not find host " + attache.getId());
                            }
                        }
                        answer = new Answer(cmd);
                    } else {
                        answer = new Answer(cmd);
                    }
                }
                answers[i] = answer;
            }

            final Response response = new Response(request, answers, AgentManagerImpl.this._nodeId, attache.getId());
            if (s_logger.isDebugEnabled()) {
                if (logD) {
                    s_logger.debug("SeqA " + attache.getId() + "-" + response.getSequence() + ": Sending " + response);
                } else {
                    s_logger.trace("SeqA " + attache.getId() + "-" + response.getSequence() + ": Sending " + response);
                }
            }
            try {
                link.send(response.toBytes());
            } catch (final ClosedChannelException e) {
                s_logger.warn("Unable to send response because connection is closed: " + response);
            }
        }
    }

    protected class MonitorTask extends ManagedContextRunnable {
        @Override
        protected void runInContext() {
            s_logger.trace("Agent Monitor is started.");

            final List<Long> behindAgents = findAgentsBehindOnPing();
            for (final Long agentId : behindAgents) {
                final QueryBuilder<HostVO> sc = QueryBuilder.create(HostVO.class);
                sc.and(sc.entity().getId(), Op.EQ, agentId);
                final HostVO h = sc.find();
                if (h != null) {
                    final ResourceState resourceState = h.getResourceState();
                    if (resourceState == ResourceState.Disabled || resourceState == ResourceState.Maintenance || resourceState == ResourceState.ErrorInMaintenance) {
                        /*
                         * Host is in non-operation state, so no investigation and direct put agent to Disconnected
                         */
                        status_logger.debug("Ping timeout but agent " + agentId + " is in resource state of " + resourceState + ", so no investigation");
                        disconnectWithoutInvestigation(agentId, Event.ShutdownRequested);
                    } else {
                        final HostVO host = AgentManagerImpl.this._hostDao.findById(agentId);
                        if (host != null && (host.getType() == HostType.ConsoleProxy || host.getType() == HostType.SecondaryStorageVM
                                || host.getType() == HostType.SecondaryStorageCmdExecutor)) {

                            s_logger.warn("Disconnect agent for CPVM/SSVM due to physical connection close. host: " + host.getId());
                            disconnectWithoutInvestigation(agentId, Event.ShutdownRequested);
                        } else {
                            status_logger.debug("Ping timeout for agent " + agentId + ", do invstigation");
                            disconnectWithInvestigation(agentId, Event.PingTimeout);
                        }
                    }
                }
            }

            final QueryBuilder<HostVO> sc = QueryBuilder.create(HostVO.class);
            sc.and(sc.entity().getResourceState(), Op.IN, ResourceState.PrepareForMaintenance, ResourceState.ErrorInMaintenance);
            final List<HostVO> hosts = sc.list();

            for (final HostVO host : hosts) {
                if (AgentManagerImpl.this._resourceMgr.checkAndMaintain(host.getId())) {
                    final Zone zone = AgentManagerImpl.this._zoneRepository.findById(host.getDataCenterId()).orElse(null);
                    final HostPodVO podVO = AgentManagerImpl.this._podDao.findById(host.getPodId());
                    final String hostDesc = "name: " + host.getName() + " (id:" + host.getId() + "), availability zone: " + zone.getName() + ", pod: " + podVO.getName();
                    AgentManagerImpl.this._alertMgr.sendAlert(AlertManager.AlertType.ALERT_TYPE_HOST, host.getDataCenterId(), host.getPodId(), "Migration Complete for host " + hostDesc, "Host ["
                            + hostDesc + "] is ready for maintenance");
                }
            }

            s_logger.trace("Agent Monitor is leaving the building!");
        }

        protected List<Long> findAgentsBehindOnPing() {
            final List<Long> agentsBehind = new ArrayList<>();
            final long cutoffTime = InaccurateClock.getTimeInSeconds() - getTimeout();
            for (final Map.Entry<Long, Long> entry : AgentManagerImpl.this._pingMap.entrySet()) {
                if (entry.getValue() < cutoffTime) {
                    agentsBehind.add(entry.getKey());
                }
            }

            if (agentsBehind.size() > 0) {
                s_logger.info("Found the following agents behind on ping: " + agentsBehind);
            }

            return agentsBehind;
        }
    }

    protected class BehindOnPingListener implements Listener {
        @Override
        public boolean processAnswers(final long agentId, final long seq, final Answer[] answers) {
            return false;
        }

        @Override
        public boolean processCommands(final long agentId, final long seq, final Command[] commands) {
            final boolean processed = false;
            for (final Command cmd : commands) {
                if (cmd instanceof PingCommand) {
                    pingBy(agentId);
                }
            }
            return processed;
        }

        @Override
        public AgentControlAnswer processControlCommand(final long agentId, final AgentControlCommand cmd) {
            return null;
        }

        @Override
        public void processConnect(final Host host, final StartupCommand cmd, final boolean forRebalance) {
            if (host.getType().equals(HostType.TrafficMonitor) || host.getType().equals(HostType.SecondaryStorage)) {
                return;
            }

            // NOTE: We don't use pingBy here because we're initiating.
            AgentManagerImpl.this._pingMap.put(host.getId(), InaccurateClock.getTimeInSeconds());
        }

        @Override
        public boolean processDisconnect(final long agentId, final HostStatus state) {
            AgentManagerImpl.this._pingMap.remove(agentId);
            return true;
        }

        @Override
        public boolean isRecurring() {
            return true;
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
}
