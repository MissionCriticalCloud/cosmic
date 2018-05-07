package com.cloud.storage.download;

import com.cloud.agent.Listener;
import com.cloud.common.managed.context.ManagedContextTimerTask;
import com.cloud.engine.subsystem.api.storage.DataObject;
import com.cloud.engine.subsystem.api.storage.DataStore;
import com.cloud.engine.subsystem.api.storage.DataStoreManager;
import com.cloud.engine.subsystem.api.storage.EndPoint;
import com.cloud.engine.subsystem.api.storage.TemplateService;
import com.cloud.engine.subsystem.api.storage.VolumeService;
import com.cloud.engine.subsystem.api.storage.ZoneScope;
import com.cloud.framework.async.AsyncCompletionCallback;
import com.cloud.legacymodel.communication.answer.AgentControlAnswer;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.DownloadAnswer;
import com.cloud.legacymodel.communication.command.AgentControlCommand;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.communication.command.DownloadCommand;
import com.cloud.legacymodel.communication.command.DownloadCommand.ResourceType;
import com.cloud.legacymodel.communication.command.DownloadProgressCommand;
import com.cloud.legacymodel.communication.command.DownloadProgressCommand.RequestType;
import com.cloud.legacymodel.communication.command.StartupCommand;
import com.cloud.legacymodel.communication.command.StartupRoutingCommand;
import com.cloud.legacymodel.communication.command.StartupSecondaryStorageCommand;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.dc.HostStatus;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.exceptions.ConnectionException;
import com.cloud.legacymodel.storage.VMTemplateStatus;
import com.cloud.model.enumeration.DataObjectType;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.resource.ResourceManager;
import com.cloud.storage.download.DownloadState.DownloadEvent;
import com.cloud.storage.upload.UploadListener;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitor progress of template download to a single storage server
 */
public class DownloadListener implements Listener {

    public static final Logger s_logger = LoggerFactory.getLogger(DownloadListener.class.getName());
    public static final int SMALL_DELAY = 100;
    public static final long STATUS_POLL_INTERVAL = 10000L;
    public static final String DOWNLOADED = VMTemplateStatus.DOWNLOADED.toString();
    public static final String NOT_DOWNLOADED = VMTemplateStatus.NOT_DOWNLOADED.toString();
    public static final String DOWNLOAD_ERROR = VMTemplateStatus.DOWNLOAD_ERROR.toString();
    public static final String DOWNLOAD_IN_PROGRESS = VMTemplateStatus.DOWNLOAD_IN_PROGRESS.toString();
    public static final String DOWNLOAD_ABANDONED = VMTemplateStatus.ABANDONED.toString();
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
        this._ssAgent = ssAgent;
        this.object = object;
        this._downloadMonitor = downloadMonitor;
        this._cmd = cmd;
        initStateMachine();
        this._currState = getState(VMTemplateStatus.NOT_DOWNLOADED.toString());
        this._timer = timer;
        this._timeoutTask = new TimeoutTask(this);
        this._timer.schedule(this._timeoutTask, 3 * STATUS_POLL_INTERVAL);
        this._callback = callback;
        final DownloadAnswer answer = new DownloadAnswer("", VMTemplateStatus.NOT_DOWNLOADED);
        callback(answer);
    }

    private void initStateMachine() {
        this._stateMap.put(VMTemplateStatus.NOT_DOWNLOADED.toString(), new NotDownloadedState(this));
        this._stateMap.put(VMTemplateStatus.DOWNLOADED.toString(), new DownloadCompleteState(this));
        this._stateMap.put(VMTemplateStatus.DOWNLOAD_ERROR.toString(), new DownloadErrorState(this));
        this._stateMap.put(VMTemplateStatus.DOWNLOAD_IN_PROGRESS.toString(), new DownloadInProgressState(this));
        this._stateMap.put(VMTemplateStatus.ABANDONED.toString(), new DownloadAbandonedState(this));
    }

    private DownloadState getState(final String stateName) {
        return this._stateMap.get(stateName);
    }

    public void callback(final DownloadAnswer answer) {
        if (this._callback != null) {
            this._callback.complete(answer);
        }
    }

    public DownloadListener(final DownloadMonitorImpl monitor) {
        this._downloadMonitor = monitor;
    }

    public AsyncCompletionCallback<DownloadAnswer> getCallback() {
        return this._callback;
    }

    public void setCurrState(final VMTemplateStatus currState) {
        this._currState = getState(currState.toString());
    }

    public void sendCommand(final RequestType reqType) {
        if (getJobId() != null) {
            if (s_logger.isTraceEnabled()) {
                logTrace("Sending progress command ");
            }
            try {
                final DownloadProgressCommand dcmd = new DownloadProgressCommand(getCommand(), getJobId(), reqType);
                if (this.object.getType() == DataObjectType.VOLUME) {
                    dcmd.setResourceType(ResourceType.VOLUME);
                }
                this._ssAgent.sendMessageAsync(dcmd, new UploadListener.Callback(this._ssAgent.getId(), this));
            } catch (final Exception e) {
                s_logger.debug("Send command failed", e);
                setDisconnected();
            }
        }
    }

    public String getJobId() {
        return this.jobId;
    }

    public void logWarn(final String message) {
        s_logger.warn(message + ", " + this.object.getType() + ": " + this.object.getId() + " at host " + this._ssAgent.getId());
    }

    public void logDebug(final String message) {
        s_logger.debug(message + ", " + this.object.getType() + ": " + this.object.getId() + " at host " + this._ssAgent.getId());
    }

    public void logTrace(final String message) {
        s_logger.trace(message + ", " + this.object.getType() + ": " + this.object.getId() + " at host " + this._ssAgent.getId());
    }

    public DownloadCommand getCommand() {
        return this._cmd;
    }

    public void setDisconnected() {
        transition(DownloadEvent.DISCONNECT, null);
    }

    private synchronized void transition(final DownloadEvent event, final Object evtObj) {
        if (this._currState == null) {
            return;
        }
        final String prevName = this._currState.getName();
        final String nextState = this._currState.handleEvent(event, evtObj);
        if (nextState != null) {
            this._currState = getState(nextState);
            if (this._currState != null) {
                this._currState.onEntry(prevName, event, evtObj);
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
        s_logger.warn("Unable to monitor download progress of " + this.object.getType() + ": " + this.object.getId() + " at host " + this._ssAgent.getId());
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
            final List<HypervisorType> hypers = this._resourceMgr.listAvailHypervisorInZone(agent.getId(), agent.getDataCenterId());
            final HypervisorType hostHyper = agent.getHypervisorType();
            if (hypers.contains(hostHyper)) {
                return;
            }
            this._imageSrv.handleSysTemplateDownload(hostHyper, agent.getDataCenterId());
            // update template_zone_ref for cross-zone templates
            this._imageSrv.associateCrosszoneTemplatesToZone(agent.getDataCenterId());
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
                final List<DataStore> imageStores = this._storeMgr.getImageStoresByScope(new ZoneScope(agent.getDataCenterId()));
                for (final DataStore store : imageStores) {
                    this._volumeSrv.handleVolumeSync(store);
                    this._imageSrv.handleTemplateSync(store);
                }
            } catch (final Exception e) {
                s_logger.error("Caught exception while doing template/volume sync ", e);
            }
        }
    }

    @Override
    public boolean processDisconnect(final long agentId, final HostStatus state) {
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
        if (this._statusTask != null) {
            this._statusTask.cancel();
        }

        this._statusTask = new StatusTask(this, request);
        this._timer.schedule(this._statusTask, STATUS_POLL_INTERVAL);
    }

    public void scheduleTimeoutTask(final long delay) {
        if (this._timeoutTask != null) {
            this._timeoutTask.cancel();
        }

        this._timeoutTask = new TimeoutTask(this);
        this._timer.schedule(this._timeoutTask, delay);
        if (s_logger.isDebugEnabled()) {
            logDebug("Scheduling timeout at " + delay + " ms");
        }
    }

    public void scheduleImmediateStatusCheck(final RequestType request) {
        if (this._statusTask != null) {
            this._statusTask.cancel();
        }
        this._statusTask = new StatusTask(this, request);
        this._timer.schedule(this._statusTask, SMALL_DELAY);
    }

    public boolean isDownloadActive() {
        return this._downloadActive;
    }

    public void cancelStatusTask() {
        if (this._statusTask != null) {
            this._statusTask.cancel();
        }
    }

    public Date getLastUpdated() {
        return this._lastUpdated;
    }

    public void setLastUpdated() {
        this._lastUpdated = new Date();
    }

    public void setDownloadInactive(final VMTemplateStatus reason) {
        this._downloadActive = false;
    }

    public void cancelTimeoutTask() {
        if (this._timeoutTask != null) {
            this._timeoutTask.cancel();
        }
    }

    public void logDownloadStart() {
    }

    private static final class StatusTask extends ManagedContextTimerTask {
        private final DownloadListener dl;
        private final RequestType reqType;

        public StatusTask(final DownloadListener dl, final RequestType req) {
            this.reqType = req;
            this.dl = dl;
        }

        @Override
        protected void runInContext() {
            this.dl.sendCommand(this.reqType);
        }
    }

    private static final class TimeoutTask extends ManagedContextTimerTask {
        private final DownloadListener dl;

        public TimeoutTask(final DownloadListener dl) {
            this.dl = dl;
        }

        @Override
        protected void runInContext() {
            this.dl.checkProgress();
        }
    }
}
