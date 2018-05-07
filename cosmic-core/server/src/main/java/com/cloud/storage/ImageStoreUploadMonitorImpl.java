package com.cloud.storage;

import com.cloud.agent.Listener;
import com.cloud.alert.AlertManager;
import com.cloud.common.managed.context.ManagedContextRunnable;
import com.cloud.engine.subsystem.api.storage.DataStore;
import com.cloud.engine.subsystem.api.storage.DataStoreManager;
import com.cloud.engine.subsystem.api.storage.EndPoint;
import com.cloud.engine.subsystem.api.storage.EndPointSelector;
import com.cloud.framework.config.ConfigKey;
import com.cloud.framework.config.Configurable;
import com.cloud.host.dao.HostDao;
import com.cloud.legacymodel.communication.answer.AgentControlAnswer;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.UploadStatusAnswer;
import com.cloud.legacymodel.communication.answer.UploadStatusAnswer.UploadStatus;
import com.cloud.legacymodel.communication.command.AgentControlCommand;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.communication.command.StartupCommand;
import com.cloud.legacymodel.communication.command.UploadStatusCommand;
import com.cloud.legacymodel.communication.command.UploadStatusCommand.EntityType;
import com.cloud.legacymodel.configuration.Resource;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.dc.HostStatus;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.exceptions.ConnectionException;
import com.cloud.legacymodel.exceptions.NoTransitionException;
import com.cloud.legacymodel.statemachine.StateMachine2;
import com.cloud.legacymodel.storage.ObjectInDataStoreStateMachine.State;
import com.cloud.legacymodel.storage.VMTemplateStatus;
import com.cloud.legacymodel.storage.VirtualMachineTemplate;
import com.cloud.legacymodel.storage.Volume;
import com.cloud.legacymodel.storage.Volume.Event;
import com.cloud.model.enumeration.DataStoreRole;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.dao.VMTemplateZoneDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.storage.datastore.db.TemplateDataStoreDao;
import com.cloud.storage.datastore.db.TemplateDataStoreVO;
import com.cloud.storage.datastore.db.VolumeDataStoreDao;
import com.cloud.storage.datastore.db.VolumeDataStoreVO;
import com.cloud.user.ResourceLimitService;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallbackNoReturn;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.utils.fsm.StateMachine2Transitions;
import com.cloud.utils.identity.ManagementServerNode;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Monitors the progress of upload.
 */
@Component
public class ImageStoreUploadMonitorImpl extends ManagerBase implements ImageStoreUploadMonitor, Listener, Configurable {

    static final Logger s_logger = LoggerFactory.getLogger(ImageStoreUploadMonitorImpl.class);
    static final ConfigKey<Integer> UploadMonitoringInterval = new ConfigKey<>("Advanced", Integer.class, "upload.monitoring.interval", "60",
            "Interval (in seconds) to check the status of volumes that are uploaded using HTTP POST request", true);
    static final ConfigKey<Integer> UploadOperationTimeout = new ConfigKey<>("Advanced", Integer.class, "upload.operation.timeout", "10",
            "Time (in minutes) to wait before abandoning volume upload using HTTP POST request", true);
    @Inject
    ResourceLimitService _resourceLimitMgr;
    @Inject
    private VolumeDao _volumeDao;
    @Inject
    private VolumeDataStoreDao _volumeDataStoreDao;
    @Inject
    private VMTemplateDao _templateDao;
    @Inject
    private TemplateDataStoreDao _templateDataStoreDao;
    @Inject
    private HostDao _hostDao;
    @Inject
    private EndPointSelector _epSelector;
    @Inject
    private DataStoreManager storeMgr;
    @Inject
    private AlertManager _alertMgr;
    @Inject
    private VMTemplateZoneDao _vmTemplateZoneDao;
    private long _nodeId;
    private ScheduledExecutorService _executor = null;
    private int _monitoringInterval;
    private long _uploadOperationTimeout;

    public static int getUploadOperationTimeout() {
        return UploadOperationTimeout.value();
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        this._executor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("Upload-Monitor"));
        this._monitoringInterval = UploadMonitoringInterval.value();
        this._uploadOperationTimeout = UploadOperationTimeout.value() * 60 * 1000L;
        this._nodeId = ManagementServerNode.getManagementServerId();
        return true;
    }

    @Override
    public boolean start() {
        this._executor.scheduleWithFixedDelay(new UploadStatusCheck(), this._monitoringInterval, this._monitoringInterval, TimeUnit.SECONDS);
        return true;
    }

    @Override
    public boolean stop() {
        this._executor.shutdownNow();
        return true;
    }

    @Override
    public boolean processAnswers(final long agentId, final long seq, final Answer[] answers) {
        return false;
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
    public void processConnect(final Host host, final StartupCommand cmd, final boolean forRebalance) throws ConnectionException {
    }

    @Override
    public boolean processDisconnect(final long agentId, final HostStatus state) {
        return false;
    }

    @Override
    public boolean isRecurring() {
        return false;
    }

    @Override
    public int getTimeout() {
        return 0;
    }

    @Override
    public boolean processTimeout(final long agentId, final long seq) {
        return false;
    }

    @Override
    public String getConfigComponentName() {
        return ImageStoreUploadMonitor.class.getSimpleName();
    }

    @Override
    public ConfigKey<?>[] getConfigKeys() {
        return new ConfigKey<?>[]{UploadMonitoringInterval, UploadOperationTimeout};
    }

    protected class UploadStatusCheck extends ManagedContextRunnable {

        public UploadStatusCheck() {
        }

        @Override
        protected void runInContext() {
            // 1. Select all entries with download_state = Not_Downloaded or Download_In_Progress
            // 2. Get corresponding volume
            // 3. Get EP using _epSelector
            // 4. Check if SSVM is owned by this MS
            // 5. If owned by MS then send command to appropriate SSVM
            // 6. In listener check for the answer and update DB accordingly
            final List<VolumeDataStoreVO> volumeDataStores = ImageStoreUploadMonitorImpl.this._volumeDataStoreDao.listByVolumeState(Volume.State.NotUploaded, Volume.State.UploadInProgress);
            for (final VolumeDataStoreVO volumeDataStore : volumeDataStores) {
                final DataStore dataStore = ImageStoreUploadMonitorImpl.this.storeMgr.getDataStore(volumeDataStore.getDataStoreId(), DataStoreRole.Image);
                final EndPoint ep = ImageStoreUploadMonitorImpl.this._epSelector.select(dataStore, volumeDataStore.getExtractUrl());
                if (ep == null) {
                    s_logger.warn("There is no secondary storage VM for image store " + dataStore.getName());
                    continue;
                }
                final VolumeVO volume = ImageStoreUploadMonitorImpl.this._volumeDao.findById(volumeDataStore.getVolumeId());
                if (volume == null) {
                    s_logger.warn("Volume with id " + volumeDataStore.getVolumeId() + " not found");
                    continue;
                }
                final Host host = ImageStoreUploadMonitorImpl.this._hostDao.findById(ep.getId());
                final UploadStatusCommand cmd = new UploadStatusCommand(volume.getUuid(), EntityType.Volume);
                if (host != null && host.getManagementServerId() != null) {
                    if (ImageStoreUploadMonitorImpl.this._nodeId == host.getManagementServerId().longValue()) {
                        Answer answer;
                        try {
                            answer = ep.sendMessage(cmd);
                        } catch (final CloudRuntimeException e) {
                            s_logger.warn("Unable to get upload status for volume " + volume.getUuid() + ". Error details: " + e.getMessage());
                            answer = new UploadStatusAnswer(cmd, UploadStatus.UNKNOWN, e.getMessage());
                        }
                        if (answer == null || !(answer instanceof UploadStatusAnswer)) {
                            s_logger.warn("No or invalid answer corresponding to UploadStatusCommand for volume " + volumeDataStore.getVolumeId());
                            continue;
                        }
                        handleVolumeStatusResponse((UploadStatusAnswer) answer, volume, volumeDataStore);
                    }
                } else {
                    final String error = "Volume " + volume.getUuid() + " failed to upload as SSVM is either destroyed or SSVM agent not in 'Up' state";
                    handleVolumeStatusResponse(new UploadStatusAnswer(cmd, UploadStatus.ERROR, error), volume, volumeDataStore);
                }
            }

            // Handle for template upload as well
            final List<TemplateDataStoreVO> templateDataStores = ImageStoreUploadMonitorImpl.this._templateDataStoreDao.listByTemplateState(VirtualMachineTemplate.State.NotUploaded,
                    VirtualMachineTemplate.State
                            .UploadInProgress);
            for (final TemplateDataStoreVO templateDataStore : templateDataStores) {
                final DataStore dataStore = ImageStoreUploadMonitorImpl.this.storeMgr.getDataStore(templateDataStore.getDataStoreId(), DataStoreRole.Image);
                final EndPoint ep = ImageStoreUploadMonitorImpl.this._epSelector.select(dataStore, templateDataStore.getExtractUrl());
                if (ep == null) {
                    s_logger.warn("There is no secondary storage VM for image store " + dataStore.getName());
                    continue;
                }
                final VMTemplateVO template = ImageStoreUploadMonitorImpl.this._templateDao.findById(templateDataStore.getTemplateId());
                if (template == null) {
                    s_logger.warn("Template with id " + templateDataStore.getTemplateId() + " not found");
                    continue;
                }
                final Host host = ImageStoreUploadMonitorImpl.this._hostDao.findById(ep.getId());
                final UploadStatusCommand cmd = new UploadStatusCommand(template.getUuid(), EntityType.Template);
                if (host != null && host.getManagementServerId() != null) {
                    if (ImageStoreUploadMonitorImpl.this._nodeId == host.getManagementServerId().longValue()) {
                        Answer answer;
                        try {
                            answer = ep.sendMessage(cmd);
                        } catch (final CloudRuntimeException e) {
                            s_logger.warn("Unable to get upload status for template " + template.getUuid() + ". Error details: " + e.getMessage());
                            answer = new UploadStatusAnswer(cmd, UploadStatus.UNKNOWN, e.getMessage());
                        }
                        if (answer == null || !(answer instanceof UploadStatusAnswer)) {
                            s_logger.warn("No or invalid answer corresponding to UploadStatusCommand for template " + templateDataStore.getTemplateId());
                            continue;
                        }
                        handleTemplateStatusResponse((UploadStatusAnswer) answer, template, templateDataStore);
                    }
                } else {
                    final String error = "Template " + template.getUuid() + " failed to upload as SSVM is either destroyed or SSVM agent not in 'Up' state";
                    handleTemplateStatusResponse(new UploadStatusAnswer(cmd, UploadStatus.ERROR, error), template, templateDataStore);
                }
            }
        }

        private void handleVolumeStatusResponse(final UploadStatusAnswer answer, final VolumeVO volume, final VolumeDataStoreVO volumeDataStore) {
            final StateMachine2<Volume.State, Event, Volume> stateMachine = Volume.State.getStateMachine();
            Transaction.execute(new TransactionCallbackNoReturn() {
                @Override
                public void doInTransactionWithoutResult(final TransactionStatus status) {
                    final VolumeVO tmpVolume = ImageStoreUploadMonitorImpl.this._volumeDao.findById(volume.getId());
                    final VolumeDataStoreVO tmpVolumeDataStore = ImageStoreUploadMonitorImpl.this._volumeDataStoreDao.findById(volumeDataStore.getId());
                    boolean sendAlert = false;
                    String msg = null;
                    try {
                        switch (answer.getStatus()) {
                            case COMPLETED:
                                tmpVolumeDataStore.setDownloadState(VMTemplateStatus.DOWNLOADED);
                                tmpVolumeDataStore.setState(State.Ready);
                                tmpVolumeDataStore.setInstallPath(answer.getInstallPath());
                                tmpVolumeDataStore.setPhysicalSize(answer.getPhysicalSize());
                                tmpVolumeDataStore.setSize(answer.getVirtualSize());
                                tmpVolumeDataStore.setDownloadPercent(100);

                                final VolumeVO volumeUpdate = ImageStoreUploadMonitorImpl.this._volumeDao.createForUpdate();
                                volumeUpdate.setSize(answer.getVirtualSize());
                                ImageStoreUploadMonitorImpl.this._volumeDao.update(tmpVolume.getId(), volumeUpdate);
                                new StateMachine2Transitions(stateMachine).transitTo(tmpVolume, Event.OperationSucceeded, null, ImageStoreUploadMonitorImpl.this._volumeDao);
                                ImageStoreUploadMonitorImpl.this._resourceLimitMgr.incrementResourceCount(volume.getAccountId(), Resource.ResourceType.secondary_storage, answer.getVirtualSize());

                                if (s_logger.isDebugEnabled()) {
                                    s_logger.debug("Volume " + tmpVolume.getUuid() + " uploaded successfully");
                                }
                                break;
                            case IN_PROGRESS:
                                if (tmpVolume.getState() == Volume.State.NotUploaded) {
                                    tmpVolumeDataStore.setDownloadState(VMTemplateStatus.DOWNLOAD_IN_PROGRESS);
                                    tmpVolumeDataStore.setDownloadPercent(answer.getDownloadPercent());
                                    new StateMachine2Transitions(stateMachine).transitTo(tmpVolume, Event.UploadRequested, null, ImageStoreUploadMonitorImpl.this._volumeDao);
                                } else if (tmpVolume.getState() == Volume.State.UploadInProgress) { // check for timeout
                                    if (System.currentTimeMillis() - tmpVolumeDataStore.getCreated().getTime() > ImageStoreUploadMonitorImpl.this._uploadOperationTimeout) {
                                        tmpVolumeDataStore.setDownloadState(VMTemplateStatus.DOWNLOAD_ERROR);
                                        tmpVolumeDataStore.setState(State.Failed);
                                        new StateMachine2Transitions(stateMachine).transitTo(tmpVolume, Event.OperationFailed, null, ImageStoreUploadMonitorImpl.this._volumeDao);
                                        msg = "Volume " + tmpVolume.getUuid() + " failed to upload due to operation timed out";
                                        s_logger.error(msg);
                                        sendAlert = true;
                                    } else {
                                        tmpVolumeDataStore.setDownloadPercent(answer.getDownloadPercent());
                                    }
                                }
                                break;
                            case ERROR:
                                tmpVolumeDataStore.setDownloadState(VMTemplateStatus.DOWNLOAD_ERROR);
                                tmpVolumeDataStore.setState(State.Failed);
                                new StateMachine2Transitions(stateMachine).transitTo(tmpVolume, Event.OperationFailed, null, ImageStoreUploadMonitorImpl.this._volumeDao);
                                msg = "Volume " + tmpVolume.getUuid() + " failed to upload. Error details: " + answer.getDetails();
                                s_logger.error(msg);
                                sendAlert = true;
                                break;
                            case UNKNOWN:
                                if (tmpVolume.getState() == Volume.State.NotUploaded) { // check for timeout
                                    if (System.currentTimeMillis() - tmpVolumeDataStore.getCreated().getTime() > ImageStoreUploadMonitorImpl.this._uploadOperationTimeout) {
                                        tmpVolumeDataStore.setDownloadState(VMTemplateStatus.ABANDONED);
                                        tmpVolumeDataStore.setState(State.Failed);
                                        new StateMachine2Transitions(stateMachine).transitTo(tmpVolume, Event.OperationTimeout, null, ImageStoreUploadMonitorImpl.this._volumeDao);
                                        msg = "Volume " + tmpVolume.getUuid() + " failed to upload due to operation timed out";
                                        s_logger.error(msg);
                                        sendAlert = true;
                                    }
                                }
                                break;
                        }
                        ImageStoreUploadMonitorImpl.this._volumeDataStoreDao.update(tmpVolumeDataStore.getId(), tmpVolumeDataStore);
                    } catch (final NoTransitionException e) {
                        s_logger.error("Unexpected error " + e.getMessage());
                    } finally {
                        if (sendAlert) {
                            ImageStoreUploadMonitorImpl.this._alertMgr.sendAlert(AlertManager.AlertType.ALERT_TYPE_UPLOAD_FAILED, tmpVolume.getDataCenterId(), null, msg, msg);
                        }
                    }
                }
            });
        }

        private void handleTemplateStatusResponse(final UploadStatusAnswer answer, final VMTemplateVO template, final TemplateDataStoreVO templateDataStore) {
            final StateMachine2<VirtualMachineTemplate.State, VirtualMachineTemplate.Event, VirtualMachineTemplate> stateMachine = VirtualMachineTemplate.State.getStateMachine();
            Transaction.execute(new TransactionCallbackNoReturn() {
                @Override
                public void doInTransactionWithoutResult(final TransactionStatus status) {
                    final VMTemplateVO tmpTemplate = ImageStoreUploadMonitorImpl.this._templateDao.findById(template.getId());
                    final TemplateDataStoreVO tmpTemplateDataStore = ImageStoreUploadMonitorImpl.this._templateDataStoreDao.findById(templateDataStore.getId());
                    boolean sendAlert = false;
                    String msg = null;
                    try {
                        switch (answer.getStatus()) {
                            case COMPLETED:
                                tmpTemplateDataStore.setDownloadState(VMTemplateStatus.DOWNLOADED);
                                tmpTemplateDataStore.setState(State.Ready);
                                tmpTemplateDataStore.setInstallPath(answer.getInstallPath());
                                tmpTemplateDataStore.setPhysicalSize(answer.getPhysicalSize());
                                tmpTemplateDataStore.setSize(answer.getVirtualSize());
                                tmpTemplateDataStore.setDownloadPercent(100);
                                tmpTemplateDataStore.setExtractUrl(null);

                                final VMTemplateVO templateUpdate = ImageStoreUploadMonitorImpl.this._templateDao.createForUpdate();
                                templateUpdate.setSize(answer.getVirtualSize());
                                ImageStoreUploadMonitorImpl.this._templateDao.update(tmpTemplate.getId(), templateUpdate);
                                new StateMachine2Transitions(stateMachine).transitTo(tmpTemplate, VirtualMachineTemplate.Event.OperationSucceeded, null, ImageStoreUploadMonitorImpl.this._templateDao);
                                ImageStoreUploadMonitorImpl.this._resourceLimitMgr.incrementResourceCount(template.getAccountId(), Resource.ResourceType.secondary_storage, answer.getVirtualSize());

                                if (s_logger.isDebugEnabled()) {
                                    s_logger.debug("Template " + tmpTemplate.getUuid() + " uploaded successfully");
                                }
                                break;
                            case IN_PROGRESS:
                                if (tmpTemplate.getState() == VirtualMachineTemplate.State.NotUploaded) {
                                    tmpTemplateDataStore.setDownloadState(VMTemplateStatus.DOWNLOAD_IN_PROGRESS);
                                    new StateMachine2Transitions(stateMachine).transitTo(tmpTemplate, VirtualMachineTemplate.Event.UploadRequested, null, ImageStoreUploadMonitorImpl.this
                                            ._templateDao);
                                    tmpTemplateDataStore.setDownloadPercent(answer.getDownloadPercent());
                                } else if (tmpTemplate.getState() == VirtualMachineTemplate.State.UploadInProgress) { // check for timeout
                                    if (System.currentTimeMillis() - tmpTemplateDataStore.getCreated().getTime() > ImageStoreUploadMonitorImpl.this._uploadOperationTimeout) {
                                        tmpTemplateDataStore.setDownloadState(VMTemplateStatus.DOWNLOAD_ERROR);
                                        tmpTemplateDataStore.setState(State.Failed);
                                        new StateMachine2Transitions(stateMachine).transitTo(tmpTemplate, VirtualMachineTemplate.Event.OperationFailed, null, ImageStoreUploadMonitorImpl.this
                                                ._templateDao);
                                        msg = "Template " + tmpTemplate.getUuid() + " failed to upload due to operation timed out";
                                        s_logger.error(msg);
                                        sendAlert = true;
                                    } else {
                                        tmpTemplateDataStore.setDownloadPercent(answer.getDownloadPercent());
                                    }
                                }
                                break;
                            case ERROR:
                                tmpTemplateDataStore.setDownloadState(VMTemplateStatus.DOWNLOAD_ERROR);
                                tmpTemplateDataStore.setState(State.Failed);
                                new StateMachine2Transitions(stateMachine).transitTo(tmpTemplate, VirtualMachineTemplate.Event.OperationFailed, null, ImageStoreUploadMonitorImpl.this._templateDao);
                                msg = "Template " + tmpTemplate.getUuid() + " failed to upload. Error details: " + answer.getDetails();
                                s_logger.error(msg);
                                sendAlert = true;
                                break;
                            case UNKNOWN:
                                if (tmpTemplate.getState() == VirtualMachineTemplate.State.NotUploaded) { // check for timeout
                                    if (System.currentTimeMillis() - tmpTemplateDataStore.getCreated().getTime() > ImageStoreUploadMonitorImpl.this._uploadOperationTimeout) {
                                        tmpTemplateDataStore.setDownloadState(VMTemplateStatus.ABANDONED);
                                        tmpTemplateDataStore.setState(State.Failed);
                                        new StateMachine2Transitions(stateMachine).transitTo(tmpTemplate, VirtualMachineTemplate.Event.OperationTimeout, null, ImageStoreUploadMonitorImpl.this
                                                ._templateDao);
                                        msg = "Template " + tmpTemplate.getUuid() + " failed to upload due to operation timed out";
                                        s_logger.error(msg);
                                        sendAlert = true;
                                    }
                                }
                                break;
                        }
                        ImageStoreUploadMonitorImpl.this._templateDataStoreDao.update(tmpTemplateDataStore.getId(), tmpTemplateDataStore);
                    } catch (final NoTransitionException e) {
                        s_logger.error("Unexpected error " + e.getMessage());
                    } finally {
                        if (sendAlert) {
                            ImageStoreUploadMonitorImpl.this._alertMgr.sendAlert(AlertManager.AlertType.ALERT_TYPE_UPLOAD_FAILED,
                                    ImageStoreUploadMonitorImpl.this._vmTemplateZoneDao.listByTemplateId(tmpTemplate.getId()).get(0).getZoneId(), null, msg, msg);
                        }
                    }
                }
            });
        }
    }
}
