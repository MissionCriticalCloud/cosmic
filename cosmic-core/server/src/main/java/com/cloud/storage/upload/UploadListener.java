package com.cloud.storage.upload;

import com.cloud.agent.Listener;
import com.cloud.api.ApiDBUtils;
import com.cloud.api.ApiSerializerHelper;
import com.cloud.api.command.user.iso.ExtractIsoCmd;
import com.cloud.api.command.user.template.ExtractTemplateCmd;
import com.cloud.api.command.user.volume.ExtractVolumeCmd;
import com.cloud.api.response.ExtractResponse;
import com.cloud.common.managed.context.ManagedContextTimerTask;
import com.cloud.engine.subsystem.api.storage.DataStore;
import com.cloud.engine.subsystem.api.storage.EndPoint;
import com.cloud.engine.subsystem.api.storage.EndPointSelector;
import com.cloud.framework.async.AsyncCompletionCallback;
import com.cloud.framework.jobs.AsyncJobManager;
import com.cloud.jobs.JobInfo;
import com.cloud.legacymodel.communication.answer.AgentControlAnswer;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.UploadAnswer;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.communication.command.UploadCommand;
import com.cloud.legacymodel.communication.command.UploadProgressCommand;
import com.cloud.legacymodel.communication.command.UploadProgressCommand.RequestType;
import com.cloud.legacymodel.communication.command.agentcontrol.AgentControlCommand;
import com.cloud.legacymodel.communication.command.startup.StartupCommand;
import com.cloud.legacymodel.communication.command.startup.StartupStorageCommand;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.dc.HostStatus;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.storage.Upload.Type;
import com.cloud.legacymodel.storage.UploadStatus;
import com.cloud.model.enumeration.StorageResourceType;
import com.cloud.storage.UploadVO;
import com.cloud.storage.dao.UploadDao;
import com.cloud.storage.upload.UploadState.UploadEvent;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UploadListener implements Listener {

    public static final Logger s_logger = LoggerFactory.getLogger(UploadListener.class.getName());
    public static final int SMALL_DELAY = 100;
    public static final long STATUS_POLL_INTERVAL = 10000L;
    public static final String UPLOADED = UploadStatus.UPLOADED.toString();
    public static final String NOT_UPLOADED = UploadStatus.NOT_UPLOADED.toString();
    public static final String UPLOAD_ERROR = UploadStatus.UPLOAD_ERROR.toString();
    public static final String UPLOAD_IN_PROGRESS = UploadStatus.UPLOAD_IN_PROGRESS.toString();
    public static final String UPLOAD_ABANDONED = UploadStatus.ABANDONED.toString();
    public static final Map<String, String> responseNameMap;

    static {
        final Map<String, String> tempMap = new HashMap<>();
        tempMap.put(Type.ISO.toString(), ExtractIsoCmd.getStaticName());
        tempMap.put(Type.TEMPLATE.toString(), ExtractTemplateCmd.getStaticName());
        tempMap.put(Type.VOLUME.toString(), ExtractVolumeCmd.getStaticName());
        tempMap.put("DEFAULT", "extractresponse");
        responseNameMap = Collections.unmodifiableMap(tempMap);
    }

    private final UploadMonitorImpl uploadMonitor;
    private final Map<String, UploadState> stateMap = new HashMap<>();
    @Inject
    EndPointSelector _epSelector;
    private DataStore sserver;
    private boolean uploadActive = true;
    private UploadDao uploadDao;
    private UploadState currState;

    private UploadCommand cmd;

    private Timer timer;

    private StatusTask statusTask;
    private TimeoutTask timeoutTask;
    private Date lastUpdated = new Date();
    private String jobId;
    private Long accountId;
    private String typeName;
    private Type type;
    private long asyncJobId;
    private long eventId;
    private AsyncJobManager asyncMgr;
    private ExtractResponse resultObj;
    private Long uploadId;

    public UploadListener(final DataStore host, final Timer timerInput, final UploadDao uploadDao, final UploadVO uploadObj, final UploadMonitorImpl uploadMonitor, final
    UploadCommand cmd, final Long accountId,
                          final String typeName, final Type type, final long eventId, final long asyncJobId, final AsyncJobManager asyncMgr) {
        this.sserver = host;
        this.uploadDao = uploadDao;
        this.uploadMonitor = uploadMonitor;
        this.cmd = cmd;
        this.uploadId = uploadObj.getId();
        this.accountId = accountId;
        this.typeName = typeName;
        this.type = type;
        initStateMachine();
        this.currState = getState(UploadStatus.NOT_UPLOADED.toString());
        this.timer = timerInput;
        this.timeoutTask = new TimeoutTask(this);
        this.timer.schedule(this.timeoutTask, 3 * STATUS_POLL_INTERVAL);
        this.eventId = eventId;
        this.asyncJobId = asyncJobId;
        this.asyncMgr = asyncMgr;
        final String extractId;
        if (type == Type.VOLUME) {
            extractId = ApiDBUtils.findVolumeById(uploadObj.getTypeId()).getUuid();
        } else {
            extractId = ApiDBUtils.findTemplateById(uploadObj.getTypeId()).getUuid();
        }
        this.resultObj =
                new ExtractResponse(extractId, typeName, ApiDBUtils.findAccountById(accountId).getUuid(), UploadStatus.NOT_UPLOADED.toString(), ApiDBUtils.findUploadById(this.uploadId)
                                                                                                                                                          .getUuid());
        this.resultObj.setResponseName(responseNameMap.get(type.toString()));
        updateDatabase(UploadStatus.NOT_UPLOADED, cmd.getUrl(), "");
    }

    private void initStateMachine() {
        this.stateMap.put(UploadStatus.NOT_UPLOADED.toString(), new NotUploadedState(this));
        this.stateMap.put(UploadStatus.UPLOADED.toString(), new UploadCompleteState(this));
        this.stateMap.put(UploadStatus.UPLOAD_ERROR.toString(), new UploadErrorState(this));
        this.stateMap.put(UploadStatus.UPLOAD_IN_PROGRESS.toString(), new UploadInProgressState(this));
        this.stateMap.put(UploadStatus.ABANDONED.toString(), new UploadAbandonedState(this));
    }

    private UploadState getState(final String stateName) {
        return this.stateMap.get(stateName);
    }

    public void updateDatabase(final UploadStatus state, final String uploadUrl, final String uploadErrorString) {
        this.resultObj.setResultString(uploadErrorString);
        this.resultObj.setState(state.toString());
        this.asyncMgr.updateAsyncJobAttachment(this.asyncJobId, this.type.toString(), 1L);
        this.asyncMgr.updateAsyncJobStatus(this.asyncJobId, JobInfo.Status.IN_PROGRESS.ordinal(), ApiSerializerHelper.toSerializedString(this.resultObj));

        final UploadVO vo = this.uploadDao.createForUpdate();
        vo.setUploadState(state);
        vo.setLastUpdated(new Date());
        vo.setUploadUrl(uploadUrl);
        vo.setJobId(null);
        vo.setUploadPercent(0);
        vo.setErrorString(uploadErrorString);

        this.uploadDao.update(getUploadId(), vo);
    }

    private Long getUploadId() {
        return this.uploadId;
    }

    public UploadListener(final UploadMonitorImpl monitor) {
        this.uploadMonitor = monitor;
    }

    public AsyncJobManager getAsyncMgr() {
        return this.asyncMgr;
    }

    public void setAsyncMgr(final AsyncJobManager asyncMgr) {
        this.asyncMgr = asyncMgr;
    }

    public long getAsyncJobId() {
        return this.asyncJobId;
    }

    public void setAsyncJobId(final long asyncJobId) {
        this.asyncJobId = asyncJobId;
    }

    public long getEventId() {
        return this.eventId;
    }

    public void setEventId(final long eventId) {
        this.eventId = eventId;
    }

    public void checkProgress() {
        transition(UploadEvent.TIMEOUT_CHECK, null);
    }

    private synchronized void transition(final UploadEvent event, final Object evtObj) {
        if (this.currState == null) {
            return;
        }
        final String prevName = this.currState.getName();
        final String nextState = this.currState.handleEvent(event, evtObj);
        if (nextState != null) {
            this.currState = getState(nextState);
            if (this.currState != null) {
                this.currState.onEntry(prevName, event, evtObj);
            } else {
                throw new CloudRuntimeException("Invalid next state: currState=" + prevName + ", evt=" + event + ", next=" + nextState);
            }
        } else {
            throw new CloudRuntimeException("Unhandled event transition: currState=" + prevName + ", evt=" + event);
        }
    }

    @Override
    public boolean processAnswers(final long agentId, final long seq, final Answer[] answers) {
        boolean processed = false;
        if (answers != null & answers.length > 0) {
            if (answers[0] instanceof UploadAnswer) {
                final UploadAnswer answer = (UploadAnswer) answers[0];
                if (getJobId() == null) {
                    setJobId(answer.getJobId());
                } else if (!getJobId().equalsIgnoreCase(answer.getJobId())) {
                    return false;//TODO
                }
                transition(UploadEvent.UPLOAD_ANSWER, answer);
                processed = true;
            }
        }
        return processed;
    }

    public String getJobId() {
        return this.jobId;
    }

    public void setJobId(final String jobId) {
        this.jobId = jobId;
    }

    @Override
    public boolean processCommands(final long agentId, final long seq, final Command[] commands) {
        return false;
    }

    @Override
    public AgentControlAnswer processControlCommand(final long agentId, final AgentControlCommand cmd) {
        return null;
    }

    @Override
    public void processConnect(final Host agent, final StartupCommand cmd, final boolean forRebalance) {
        if (!(cmd instanceof StartupStorageCommand)) {
            return;
        }

        final long agentId = agent.getId();

        final StartupStorageCommand storage = (StartupStorageCommand) cmd;
        if (storage.getResourceType() == StorageResourceType.STORAGE_HOST || storage.getResourceType() == StorageResourceType.SECONDARY_STORAGE) {
            this.uploadMonitor.handleUploadSync(agentId);
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

    public void setDisconnected() {
        transition(UploadEvent.DISCONNECT, null);
    }

    public void setUploadInactive(final UploadStatus reason) {
        this.uploadActive = false;
        this.uploadMonitor.handleUploadEvent(this.accountId, this.typeName, this.type, this.uploadId, reason, this.eventId);
    }

    public void logUploadStart() {
        //uploadMonitor.logEvent(accountId, event, "Storage server " + sserver.getName() + " started upload of " +type.toString() + " " + typeName, EventVO.LEVEL_INFO, eventId);
    }

    public void cancelTimeoutTask() {
        if (this.timeoutTask != null) {
            this.timeoutTask.cancel();
        }
    }

    public void cancelStatusTask() {
        if (this.statusTask != null) {
            this.statusTask.cancel();
        }
    }

    public Date getLastUpdated() {
        return this.lastUpdated;
    }

    public void setLastUpdated() {
        this.lastUpdated = new Date();
    }

    public void scheduleStatusCheck(final UploadProgressCommand.RequestType getStatus) {
        if (this.statusTask != null) {
            this.statusTask.cancel();
        }

        this.statusTask = new StatusTask(this, getStatus);
        this.timer.schedule(this.statusTask, STATUS_POLL_INTERVAL);
    }

    public void scheduleTimeoutTask(final long delay) {
        if (this.timeoutTask != null) {
            this.timeoutTask.cancel();
        }

        this.timeoutTask = new TimeoutTask(this);
        this.timer.schedule(this.timeoutTask, delay);
        if (s_logger.isDebugEnabled()) {
            logDebug("Scheduling timeout at " + delay + " ms");
        }
    }

    public void logWarn(final String message) {
        s_logger.warn(message + ", " + this.type.toString() + " = " + this.typeName + " at host " + this.sserver.getName());
    }

    public void logDebug(final String message) {
        s_logger.debug(message + ", " + this.type.toString() + " = " + this.typeName + " at host " + this.sserver.getName());
    }

    public void logTrace(final String message) {
        s_logger.trace(message + ", " + this.type.toString() + " = " + this.typeName + " at host " + this.sserver.getName());
    }

    public void updateDatabase(final UploadStatus state, final String uploadErrorString) {
        this.resultObj.setResultString(uploadErrorString);
        this.resultObj.setState(state.toString());
        this.asyncMgr.updateAsyncJobAttachment(this.asyncJobId, this.type.toString(), 1L);
        this.asyncMgr.updateAsyncJobStatus(this.asyncJobId, JobInfo.Status.IN_PROGRESS.ordinal(), ApiSerializerHelper.toSerializedString(this.resultObj));

        final UploadVO vo = this.uploadDao.createForUpdate();
        vo.setUploadState(state);
        vo.setLastUpdated(new Date());
        vo.setErrorString(uploadErrorString);
        this.uploadDao.update(getUploadId(), vo);
    }

    public synchronized void updateDatabase(final UploadAnswer answer) {

        if (answer.getErrorString().startsWith("553")) {
            answer.setErrorString(answer.getErrorString().concat("Please check if the file name already exists."));
        }
        this.resultObj.setResultString(answer.getErrorString());
        this.resultObj.setState(answer.getUploadStatus().toString());
        this.resultObj.setUploadPercent(answer.getUploadPct());

        if (answer.getUploadStatus() == UploadStatus.UPLOAD_IN_PROGRESS) {
            this.asyncMgr.updateAsyncJobAttachment(this.asyncJobId, this.type.toString(), 1L);
            this.asyncMgr.updateAsyncJobStatus(this.asyncJobId, JobInfo.Status.IN_PROGRESS.ordinal(), ApiSerializerHelper.toSerializedString(this.resultObj));
        } else if (answer.getUploadStatus() == UploadStatus.UPLOADED) {
            this.resultObj.setResultString("Success");
            this.asyncMgr.completeAsyncJob(this.asyncJobId, JobInfo.Status.SUCCEEDED, 1, ApiSerializerHelper.toSerializedString(this.resultObj));
        } else {
            this.asyncMgr.completeAsyncJob(this.asyncJobId, JobInfo.Status.FAILED, 2, ApiSerializerHelper.toSerializedString(this.resultObj));
        }
        final UploadVO updateBuilder = this.uploadDao.createForUpdate();
        updateBuilder.setUploadPercent(answer.getUploadPct());
        updateBuilder.setUploadState(answer.getUploadStatus());
        updateBuilder.setLastUpdated(new Date());
        updateBuilder.setErrorString(answer.getErrorString());
        updateBuilder.setJobId(answer.getJobId());

        this.uploadDao.update(getUploadId(), updateBuilder);
    }

    public void sendCommand(final RequestType reqType) {
        if (getJobId() != null) {
            if (s_logger.isTraceEnabled()) {
                logTrace("Sending progress command ");
            }
            try {
                final EndPoint ep = this._epSelector.select(this.sserver);
                if (ep == null) {
                    final String errMsg = "No remote endpoint to send command, check if host or ssvm is down?";
                    s_logger.error(errMsg);
                    return;
                }
                ep.sendMessageAsync(new UploadProgressCommand(getCommand(), getJobId(), reqType), new Callback(ep.getId(), this));
            } catch (final Exception e) {
                s_logger.debug("Send command failed", e);
                setDisconnected();
            }
        }
    }

    private UploadCommand getCommand() {
        return this.cmd;
    }

    public void setCommand(final UploadCommand cmd) {
        this.cmd = cmd;
    }

    public void logDisconnect() {
        s_logger.warn("Unable to monitor upload progress of " + this.typeName + " at host " + this.sserver.getName());
    }

    public void scheduleImmediateStatusCheck(final RequestType request) {
        if (this.statusTask != null) {
            this.statusTask.cancel();
        }
        this.statusTask = new StatusTask(this, request);
        this.timer.schedule(this.statusTask, SMALL_DELAY);
    }

    public void setCurrState(final UploadStatus uploadState) {
        this.currState = getState(this.currState.toString());
    }

    private static final class StatusTask extends ManagedContextTimerTask {
        private final UploadListener ul;
        private final RequestType reqType;

        public StatusTask(final UploadListener ul, final RequestType req) {
            this.reqType = req;
            this.ul = ul;
        }

        @Override
        protected void runInContext() {
            this.ul.sendCommand(this.reqType);
        }
    }

    private static final class TimeoutTask extends ManagedContextTimerTask {
        private final UploadListener ul;

        public TimeoutTask(final UploadListener ul) {
            this.ul = ul;
        }

        @Override
        protected void runInContext() {
            this.ul.checkProgress();
        }
    }

    public static class Callback implements AsyncCompletionCallback<Answer> {
        long id;
        Listener listener;

        public Callback(final long id, final Listener listener) {
            this.id = id;
            this.listener = listener;
        }

        @Override
        public void complete(final Answer answer) {
            this.listener.processAnswers(this.id, -1, new Answer[]{answer});
        }
    }
}
