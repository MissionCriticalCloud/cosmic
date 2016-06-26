package com.cloud.storage.download;

import com.cloud.agent.Listener;
import com.cloud.agent.api.AgentControlAnswer;
import com.cloud.agent.api.AgentControlCommand;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupRoutingCommand;
import com.cloud.agent.api.StartupSecondaryStorageCommand;
import com.cloud.agent.api.storage.DownloadAnswer;
import com.cloud.agent.api.to.DataObjectType;
import com.cloud.exception.ConnectionException;
import com.cloud.host.Host;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.resource.ResourceManager;
import com.cloud.storage.VMTemplateHostVO;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import com.cloud.storage.download.DownloadState.DownloadEvent;
import com.cloud.storage.upload.UploadListener;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObject;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPoint;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateService;
import org.apache.cloudstack.engine.subsystem.api.storage.VolumeService;
import org.apache.cloudstack.engine.subsystem.api.storage.ZoneScope;
import org.apache.cloudstack.framework.async.AsyncCompletionCallback;
import org.apache.cloudstack.managed.context.ManagedContextTimerTask;
import org.apache.cloudstack.storage.command.DownloadCommand;
import org.apache.cloudstack.storage.command.DownloadCommand.ResourceType;
import org.apache.cloudstack.storage.command.DownloadProgressCommand;
import org.apache.cloudstack.storage.command.DownloadProgressCommand.RequestType;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Monitor progress of template download to a single storage server
 */
public class DownloadListener implements Listener {

    public static final Logger s_logger = Logger.getLogger(DownloadListener.class.getName());
    public static final int SMALL_DELAY = 100;
    public static final long STATUS_POLL_INTERVAL = 10000L;
    public static final String DOWNLOADED = Status.DOWNLOADED.toString();
    public static final String NOT_DOWNLOADED = Status.NOT_DOWNLOADED.toString();
    public static final String DOWNLOAD_ERROR = Status.DOWNLOAD_ERROR.toString();
    public static final String DOWNLOAD_IN_PROGRESS = Status.DOWNLOAD_IN_PROGRESS.toString();
    public static final String DOWNLOAD_ABANDONED = Status.ABANDONED.toString();
    private final DownloadMonitorImpl _downloadMonitor;
    private final Map<String, DownloadState> _stateMap = new HashMap<>();
    private EndPoint _ssAgent;

    private DataObject object;

    private boolean _downloadActive = true;
    private DownloadState _currState;
    private DownloadCommand _cmd;
    private Timer _timer;
    private StatusTask _statusTask;
    private TimeoutTask _timeoutTask;
    private Date _lastUpdated = new Date();
    private String jobId;
    private AsyncCompletionCallback<DownloadAnswer> _callback;
    @Inject
    private ResourceManager _resourceMgr;
    @Inject
    private TemplateService _imageSrv;
    @Inject
    private DataStoreManager _storeMgr;
    @Inject
    private VolumeService _volumeSrv;

    // TODO: this constructor should be the one used for template only, remove other template constructor later
    public DownloadListener(final EndPoint ssAgent, final DataStore store, final DataObject object, final Timer timer, final DownloadMonitorImpl downloadMonitor, final
    DownloadCommand cmd,
                            final AsyncCompletionCallback<DownloadAnswer> callback) {
        _ssAgent = ssAgent;
        this.object = object;
        _downloadMonitor = downloadMonitor;
        _cmd = cmd;
        initStateMachine();
        _currState = getState(Status.NOT_DOWNLOADED.toString());
        this._timer = timer;
        _timeoutTask = new TimeoutTask(this);
        this._timer.schedule(_timeoutTask, 3 * STATUS_POLL_INTERVAL);
        _callback = callback;
        final DownloadAnswer answer = new DownloadAnswer("", Status.NOT_DOWNLOADED);
        callback(answer);
    }

    private void initStateMachine() {
        _stateMap.put(Status.NOT_DOWNLOADED.toString(), new NotDownloadedState(this));
        _stateMap.put(Status.DOWNLOADED.toString(), new DownloadCompleteState(this));
        _stateMap.put(Status.DOWNLOAD_ERROR.toString(), new DownloadErrorState(this));
        _stateMap.put(Status.DOWNLOAD_IN_PROGRESS.toString(), new DownloadInProgressState(this));
        _stateMap.put(Status.ABANDONED.toString(), new DownloadAbandonedState(this));
    }

    private DownloadState getState(final String stateName) {
        return _stateMap.get(stateName);
    }

    public void callback(final DownloadAnswer answer) {
        if (_callback != null) {
            _callback.complete(answer);
        }
    }

    public DownloadListener(final DownloadMonitorImpl monitor) {
        _downloadMonitor = monitor;
    }

    public AsyncCompletionCallback<DownloadAnswer> getCallback() {
        return _callback;
    }

    public void setCurrState(final VMTemplateHostVO.Status currState) {
        _currState = getState(currState.toString());
    }

    public void sendCommand(final RequestType reqType) {
        if (getJobId() != null) {
            if (s_logger.isTraceEnabled()) {
                log("Sending progress command ", Level.TRACE);
            }
            try {
                final DownloadProgressCommand dcmd = new DownloadProgressCommand(getCommand(), getJobId(), reqType);
                if (object.getType() == DataObjectType.VOLUME) {
                    dcmd.setResourceType(ResourceType.VOLUME);
                }
                _ssAgent.sendMessageAsync(dcmd, new UploadListener.Callback(_ssAgent.getId(), this));
            } catch (final Exception e) {
                s_logger.debug("Send command failed", e);
                setDisconnected();
            }
        }
    }

    public String getJobId() {
        return jobId;
    }

    public void log(final String message, final Level level) {
        s_logger.log(level, message + ", " + object.getType() + ": " + object.getId() + " at host " + _ssAgent.getId());
    }

    public DownloadCommand getCommand() {
        return _cmd;
    }

    public void setDisconnected() {
        transition(DownloadEvent.DISCONNECT, null);
    }

    private synchronized void transition(final DownloadEvent event, final Object evtObj) {
        if (_currState == null) {
            return;
        }
        final String prevName = _currState.getName();
        final String nextState = _currState.handleEvent(event, evtObj);
        if (nextState != null) {
            _currState = getState(nextState);
            if (_currState != null) {
                _currState.onEntry(prevName, event, evtObj);
            } else {
                throw new CloudRuntimeException("Invalid next state: currState=" + prevName + ", evt=" + event + ", next=" + nextState);
            }
        } else {
            throw new CloudRuntimeException("Unhandled event transition: currState=" + prevName + ", evt=" + event);
        }
    }

    public void setCommand(final DownloadCommand cmd) {
        this._cmd = cmd;
    }

    public void setJobId(final String jobId) {
        this.jobId = jobId;
    }

    public void checkProgress() {
        transition(DownloadEvent.TIMEOUT_CHECK, null);
    }

    public void logDisconnect() {
        s_logger.warn("Unable to monitor download progress of " + object.getType() + ": " + object.getId() + " at host " + _ssAgent.getId());
    }

    @Override
    public boolean processAnswers(final long agentId, final long seq, final Answer[] answers) {
        boolean processed = false;
        if (answers != null & answers.length > 0) {
            if (answers[0] instanceof DownloadAnswer) {
                final DownloadAnswer answer = (DownloadAnswer) answers[0];
                if (getJobId() == null) {
                    setJobId(answer.getJobId());
                } else if (!getJobId().equalsIgnoreCase(answer.getJobId())) {
                    return false;//TODO
                }
                transition(DownloadEvent.DOWNLOAD_ANSWER, answer);
                processed = true;
            }
        }
        return processed;
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
    public void processConnect(final Host agent, final StartupCommand cmd, final boolean forRebalance) throws ConnectionException {
        if (cmd instanceof StartupRoutingCommand) {
            final List<HypervisorType> hypers = _resourceMgr.listAvailHypervisorInZone(agent.getId(), agent.getDataCenterId());
            final HypervisorType hostHyper = agent.getHypervisorType();
            if (hypers.contains(hostHyper)) {
                return;
            }
            _imageSrv.handleSysTemplateDownload(hostHyper, agent.getDataCenterId());
            // update template_zone_ref for cross-zone templates
            _imageSrv.associateCrosszoneTemplatesToZone(agent.getDataCenterId());
        }
        /* This can be removed
        else if ( cmd instanceof StartupStorageCommand) {
            StartupStorageCommand storage = (StartupStorageCommand)cmd;
            if( storage.getResourceType() == Storage.StorageResourceType.SECONDARY_STORAGE ||
                    storage.getResourceType() == Storage.StorageResourceType.LOCAL_SECONDARY_STORAGE  ) {
                downloadMonitor.addSystemVMTemplatesToHost(agent, storage.getTemplateInfo());
                downloadMonitor.handleTemplateSync(agent);
                downloadMonitor.handleVolumeSync(agent);
            }
        }*/
        else if (cmd instanceof StartupSecondaryStorageCommand) {
            try {
                final List<DataStore> imageStores = _storeMgr.getImageStoresByScope(new ZoneScope(agent.getDataCenterId()));
                for (final DataStore store : imageStores) {
                    _volumeSrv.handleVolumeSync(store);
                    _imageSrv.handleTemplateSync(store);
                }
            } catch (final Exception e) {
                s_logger.error("Caught exception while doing template/volume sync ", e);
            }
        }
    }

    @Override
    public boolean processDisconnect(final long agentId, final com.cloud.host.Status state) {
        setDisconnected();
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

    public void abandon() {
        transition(DownloadEvent.ABANDON_DOWNLOAD, null);
    }

    public void scheduleStatusCheck(final RequestType request) {
        if (_statusTask != null) {
            _statusTask.cancel();
        }

        _statusTask = new StatusTask(this, request);
        _timer.schedule(_statusTask, STATUS_POLL_INTERVAL);
    }

    public void scheduleTimeoutTask(final long delay) {
        if (_timeoutTask != null) {
            _timeoutTask.cancel();
        }

        _timeoutTask = new TimeoutTask(this);
        _timer.schedule(_timeoutTask, delay);
        if (s_logger.isDebugEnabled()) {
            log("Scheduling timeout at " + delay + " ms", Level.DEBUG);
        }
    }

    public void scheduleImmediateStatusCheck(final RequestType request) {
        if (_statusTask != null) {
            _statusTask.cancel();
        }
        _statusTask = new StatusTask(this, request);
        _timer.schedule(_statusTask, SMALL_DELAY);
    }

    public boolean isDownloadActive() {
        return _downloadActive;
    }

    public void cancelStatusTask() {
        if (_statusTask != null) {
            _statusTask.cancel();
        }
    }

    public Date getLastUpdated() {
        return _lastUpdated;
    }

    public void setLastUpdated() {
        _lastUpdated = new Date();
    }

    public void setDownloadInactive(final Status reason) {
        _downloadActive = false;
    }

    public void cancelTimeoutTask() {
        if (_timeoutTask != null) {
            _timeoutTask.cancel();
        }
    }

    public void logDownloadStart() {
    }

    private static final class StatusTask extends ManagedContextTimerTask {
        private final DownloadListener dl;
        private final RequestType reqType;

        public StatusTask(final DownloadListener dl, final RequestType req) {
            reqType = req;
            this.dl = dl;
        }

        @Override
        protected void runInContext() {
            dl.sendCommand(reqType);
        }
    }

    private static final class TimeoutTask extends ManagedContextTimerTask {
        private final DownloadListener dl;

        public TimeoutTask(final DownloadListener dl) {
            this.dl = dl;
        }

        @Override
        protected void runInContext() {
            dl.checkProgress();
        }
    }
}
