package org.apache.cloudstack.storage;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.configuration.Config;
import com.cloud.resource.ServerResource;
import com.cloud.utils.component.ComponentContext;
import com.cloud.utils.net.NetUtils;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPoint;
import org.apache.cloudstack.framework.async.AsyncCompletionCallback;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.managed.context.ManagedContextRunnable;
import org.apache.cloudstack.storage.command.CopyCommand;
import org.apache.cloudstack.storage.command.DownloadCommand;
import org.apache.cloudstack.storage.resource.LocalNfsSecondaryStorageResource;

import javax.inject.Inject;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LocalHostEndpoint implements EndPoint {
    protected ServerResource resource;
    @Inject
    ConfigurationDao configDao;
    private ScheduledExecutorService executor;

    public LocalHostEndpoint() {

    }

    public static EndPoint getEndpoint() {
        final LocalHostEndpoint endpoint = ComponentContext.inject(LocalHostEndpoint.class);
        endpoint.configure();
        return endpoint;
    }

    private void configure() {
        // get mount parent folder configured in global setting, if set, this will overwrite _parent in NfsSecondaryStorageResource to work
        // around permission issue for default /mnt folder
        final String mountParent = configDao.getValue(Config.MountParent.key());

        final String path = mountParent + File.separator + "secStorage";

        final LocalNfsSecondaryStorageResource localResource = new LocalNfsSecondaryStorageResource();
        localResource.setParentPath(path);
        resource = localResource;
        executor = Executors.newScheduledThreadPool(10);
    }

    @Override
    public long getId() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getHostAddr() {
        return "127.0.0.0";
    }

    @Override
    public String getPublicAddr() {
        final String hostIp = NetUtils.getDefaultHostIp();
        if (hostIp != null) {
            return hostIp;
        } else {
            return "127.0.0.0";
        }
    }

    @Override
    public Answer sendMessage(final Command cmd) {
        if ((cmd instanceof CopyCommand) || (cmd instanceof DownloadCommand)) {
            return resource.executeRequest(cmd);
        }
        // TODO Auto-generated method stub
        return new Answer(cmd, false, "unsupported command:" + cmd.toString());
    }

    @Override
    public void sendMessageAsync(final Command cmd, final AsyncCompletionCallback<Answer> callback) {
        executor.schedule(new CmdRunner(cmd, callback), 10, TimeUnit.SECONDS);
    }

    public ServerResource getResource() {
        return resource;
    }

    public void setResource(final ServerResource resource) {
        this.resource = resource;
    }

    private class CmdRunner extends ManagedContextRunnable {
        final Command cmd;
        final AsyncCompletionCallback<Answer> callback;

        public CmdRunner(final Command cmd, final AsyncCompletionCallback<Answer> callback) {
            this.cmd = cmd;
            this.callback = callback;
        }

        @Override
        protected void runInContext() {
            final Answer answer = sendMessage(cmd);
            callback.complete(answer);
        }
    }
}
