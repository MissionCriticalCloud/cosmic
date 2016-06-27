package com.cloud.agent;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.manager.Commands;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.ConnectionException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.resource.ServerResource;
import org.apache.cloudstack.framework.config.ConfigKey;

/**
 * AgentManager manages hosts. It directly coordinates between the DAOs and the connections it manages.
 */
public interface AgentManager {
    static final ConfigKey<Integer> Wait = new ConfigKey<>("Advanced", Integer.class, "wait", "1800", "Time in seconds to wait for control commands to return",
            true);

    boolean handleDirectConnectAgent(Host host, StartupCommand[] cmds, ServerResource resource, boolean forRebalance) throws ConnectionException;

    /**
     * easy send method that returns null if there's any errors. It handles all exceptions.
     *
     * @param hostId host id
     * @param cmd    command to send.
     * @return Answer if successful; null if not.
     */
    Answer easySend(Long hostId, Command cmd);

    /**
     * Synchronous sending a command to the agent.
     *
     * @param hostId id of the agent on host
     * @param cmd    command
     * @return an Answer
     */

    Answer send(Long hostId, Command cmd) throws AgentUnavailableException, OperationTimedoutException;

    /**
     * Synchronous sending a list of commands to the agent.
     *
     * @param hostId      id of the agent on host
     * @param cmds        array of commands
     * @param isControl   Commands sent contains control commands
     * @param stopOnError should the agent stop execution on the first error.
     * @return an array of Answer
     */
    Answer[] send(Long hostId, Commands cmds) throws AgentUnavailableException, OperationTimedoutException;

    Answer[] send(Long hostId, Commands cmds, int timeout) throws AgentUnavailableException, OperationTimedoutException;

    /**
     * Asynchronous sending of a command to the agent.
     *
     * @param hostId      id of the agent on the host.
     * @param cmds        Commands to send.
     * @param stopOnError should the agent stop execution on the first error.
     * @param listener    the listener to process the answer.
     * @return sequence number.
     */
    long send(Long hostId, Commands cmds, Listener listener) throws AgentUnavailableException;

    /**
     * Register to listen for host events. These are mostly connection and disconnection events.
     *
     * @param listener
     * @param connections listen for connections
     * @param commands    listen for connections
     * @param priority    in listening for events.
     * @return id to unregister if needed.
     */
    int registerForHostEvents(Listener listener, boolean connections, boolean commands, boolean priority);

    /**
     * Register to listen for initial agent connections.
     *
     * @param creator
     * @param priority in listening for events.
     * @return id to unregister if needed.
     */
    int registerForInitialConnects(StartupCommandProcessor creator, boolean priority);

    /**
     * Unregister for listening to host events.
     *
     * @param id returned from registerForHostEvents
     */
    void unregisterForHostEvents(int id);

    Answer sendTo(Long dcId, HypervisorType type, Command cmd);

    public boolean agentStatusTransitTo(HostVO host, Status.Event e, long msId);

    //    public AgentAttache handleDirectConnectAgent(HostVO host, StartupCommand[] cmds, ServerResource resource, boolean forRebalance) throws ConnectionException;

    boolean isAgentAttached(long hostId);

    void disconnectWithoutInvestigation(long hostId, Status.Event event);

    public void pullAgentToMaintenance(long hostId);

    public void pullAgentOutMaintenance(long hostId);

    boolean reconnect(long hostId);

    void rescan();

    public enum TapAgentsAction {
        Add, Del, Contains,
    }
}
