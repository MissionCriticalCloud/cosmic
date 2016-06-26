package org.apache.cloudstack.storage;

import com.cloud.agent.AgentManager;
import com.cloud.agent.Listener;
import com.cloud.agent.api.AgentControlAnswer;
import com.cloud.agent.api.AgentControlCommand;
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
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.HypervisorGuruManager;
import com.cloud.utils.component.ComponentContext;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.SecondaryStorageVmVO;
import com.cloud.vm.dao.SecondaryStorageVmDao;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPoint;
import org.apache.cloudstack.framework.async.AsyncCompletionCallback;
import org.apache.cloudstack.managed.context.ManagedContextRunnable;

import javax.inject.Inject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteHostEndPoint implements EndPoint {
    private static final Logger s_logger = LoggerFactory.getLogger(RemoteHostEndPoint.class);
    @Inject
    protected HypervisorGuruManager _hvGuruMgr;
    @Inject
    protected SecondaryStorageVmDao vmDao;
    @Inject
    protected HostDao _hostDao;
    @Inject
    AgentManager agentMgr;
    private long hostId;
    private String hostAddress;
    private String publicAddress;
    private final ScheduledExecutorService executor;

    public RemoteHostEndPoint() {
        executor = Executors.newScheduledThreadPool(10, new NamedThreadFactory("RemoteHostEndPoint"));
    }

    public static RemoteHostEndPoint getHypervisorHostEndPoint(final Host host) {
        final RemoteHostEndPoint ep = ComponentContext.inject(RemoteHostEndPoint.class);
        ep.configure(host);
        return ep;
    }

    private void configure(final Host host) {
        hostId = host.getId();
        hostAddress = host.getPrivateIpAddress();
        publicAddress = host.getPublicIpAddress();
        if (Host.Type.SecondaryStorageVM == host.getType()) {
            final String vmName = host.getName();
            final SecondaryStorageVmVO ssvm = vmDao.findByInstanceName(vmName);
            if (ssvm != null) {
                publicAddress = ssvm.getPublicIpAddress();
            }
        }
    }

    private class CmdRunner extends ManagedContextRunnable implements Listener {
        final AsyncCompletionCallback<Answer> callback;
        Answer answer;

        public CmdRunner(final AsyncCompletionCallback<Answer> callback) {
            this.callback = callback;
        }

        @Override
        public boolean processAnswers(final long agentId, final long seq, final Answer[] answers) {
            answer = answers[0];
            executor.schedule(this, 10, TimeUnit.SECONDS);
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
            // TODO Auto-generated method stub

        }

        @Override
        public boolean processDisconnect(final long agentId, final Status state) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isRecurring() {
            // TODO Auto-generated method stub
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

        @Override
        protected void runInContext() {
            callback.complete(answer);
        }
    }

    @Override
    public String getHostAddr() {
        return hostAddress;
    }

    @Override
    public String getPublicAddr() {
        return publicAddress;
    }

    @Override
    public long getId() {
        return hostId;
    }

    // used when HypervisorGuruManager choose a different host to send command
    private void setId(final long id) {
        final HostVO host = _hostDao.findById(id);
        if (host != null) {
            configure(host);
        }
    }

    @Override
    public Answer sendMessage(final Command cmd) {
        String errMsg = null;
        try {
            final long newHostId = _hvGuruMgr.getGuruProcessedCommandTargetHost(hostId, cmd);
            if (newHostId != hostId) {
                // update endpoint with new host if changed
                setId(newHostId);
            }
            return agentMgr.send(newHostId, cmd);
        } catch (final AgentUnavailableException e) {
            errMsg = e.toString();
            s_logger.debug("Failed to send command, due to Agent:" + getId() + ", " + e.toString());
        } catch (final OperationTimedoutException e) {
            errMsg = e.toString();
            s_logger.debug("Failed to send command, due to Agent:" + getId() + ", " + e.toString());
        }
        throw new CloudRuntimeException("Failed to send command, due to Agent:" + getId() + ", " + errMsg);
    }

    @Override
    public void sendMessageAsync(final Command cmd, final AsyncCompletionCallback<Answer> callback) {
        try {
            final long newHostId = _hvGuruMgr.getGuruProcessedCommandTargetHost(hostId, cmd);
            if (newHostId != hostId) {
                // update endpoint with new host if changed
                setId(newHostId);
            }
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Sending command " + cmd.toString() + " to host: " + newHostId);
            }
            agentMgr.send(newHostId, new Commands(cmd), new CmdRunner(callback));
        } catch (final AgentUnavailableException e) {
            throw new CloudRuntimeException("Unable to send message", e);
        }
    }
}
