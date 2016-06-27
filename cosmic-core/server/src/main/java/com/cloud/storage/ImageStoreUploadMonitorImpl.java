package com.cloud.storage;

import com.cloud.agent.Listener;
import com.cloud.agent.api.AgentControlAnswer;
import com.cloud.agent.api.AgentControlCommand;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.StartupCommand;
import com.cloud.alert.AlertManager;
import com.cloud.configuration.Resource;
import com.cloud.exception.ConnectionException;
import com.cloud.host.Host;
import com.cloud.host.Status;
import com.cloud.host.dao.HostDao;
import com.cloud.storage.Volume.Event;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.dao.VMTemplateZoneDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.user.ResourceLimitService;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallbackNoReturn;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.fsm.NoTransitionException;
import com.cloud.utils.fsm.StateMachine2;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPoint;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPointSelector;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.State;
import org.apache.cloudstack.framework.config.ConfigKey;
import org.apache.cloudstack.framework.config.Configurable;
import org.apache.cloudstack.managed.context.ManagedContextRunnable;
import org.apache.cloudstack.storage.command.UploadStatusAnswer;
import org.apache.cloudstack.storage.command.UploadStatusAnswer.UploadStatus;
import org.apache.cloudstack.storage.command.UploadStatusCommand;
import org.apache.cloudstack.storage.command.UploadStatusCommand.EntityType;
import org.apache.cloudstack.storage.datastore.db.TemplateDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.TemplateDataStoreVO;
import org.apache.cloudstack.storage.datastore.db.VolumeDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.VolumeDataStoreVO;
import org.apache.cloudstack.utils.identity.ManagementServerNode;

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
        _executor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("Upload-Monitor"));
        _monitoringInterval = UploadMonitoringInterval.value();
        _uploadOperationTimeout = UploadOperationTimeout.value() * 60 * 1000L;
        _nodeId = ManagementServerNode.getManagementServerId();
        return true;
    }

    @Override
    public boolean start() {
        _executor.scheduleWithFixedDelay(new UploadStatusCheck(), _monitoringInterval, _monitoringInterval, TimeUnit.SECONDS);
        return true;
    }

    @Override
    public boolean stop() {
        _executor.shutdownNow();
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
    public boolean processDisconnect(final long agentId, final Status state) {
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
            final List<VolumeDataStoreVO> volumeDataStores = _volumeDataStoreDao.listByVolumeState(Volume.State.NotUploaded, Volume.State.UploadInProgress);
            for (final VolumeDataStoreVO volumeDataStore : volumeDataStores) {
                try {
                    final DataStore dataStore = storeMgr.getDataStore(volumeDataStore.getDataStoreId(), DataStoreRole.Image);
                    final EndPoint ep = _epSelector.select(dataStore, volumeDataStore.getExtractUrl());
                    if (ep == null) {
                        s_logger.warn("There is no secondary storage VM for image store " + dataStore.getName());
                        continue;
                    }
                    final VolumeVO volume = _volumeDao.findById(volumeDataStore.getVolumeId());
                    if (volume == null) {
                        s_logger.warn("Volume with id " + volumeDataStore.getVolumeId() + " not found");
                        continue;
                    }
                    final Host host = _hostDao.findById(ep.getId());
                    final UploadStatusCommand cmd = new UploadStatusCommand(volume.getUuid(), EntityType.Volume);
                    if (host != null && host.getManagementServerId() != null) {
                        if (_nodeId == host.getManagementServerId().longValue()) {
                            Answer answer = null;
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
                } catch (final Throwable th) {
                    s_logger.warn("Exception while checking status for uploaded volume " + volumeDataStore.getExtractUrl() + ". Error details: " + th.getMessage());
                    if (s_logger.isTraceEnabled()) {
                        s_logger.trace("Exception details: ", th);
                    }
                }
            }

            // Handle for template upload as well
            final List<TemplateDataStoreVO> templateDataStores = _templateDataStoreDao.listByTemplateState(VirtualMachineTemplate.State.NotUploaded, VirtualMachineTemplate.State
                    .UploadInProgress);
            for (final TemplateDataStoreVO templateDataStore : templateDataStores) {
                try {
                    final DataStore dataStore = storeMgr.getDataStore(templateDataStore.getDataStoreId(), DataStoreRole.Image);
                    final EndPoint ep = _epSelector.select(dataStore, templateDataStore.getExtractUrl());
                    if (ep == null) {
                        s_logger.warn("There is no secondary storage VM for image store " + dataStore.getName());
                        continue;
                    }
                    final VMTemplateVO template = _templateDao.findById(templateDataStore.getTemplateId());
                    if (template == null) {
                        s_logger.warn("Template with id " + templateDataStore.getTemplateId() + " not found");
                        continue;
                    }
                    final Host host = _hostDao.findById(ep.getId());
                    final UploadStatusCommand cmd = new UploadStatusCommand(template.getUuid(), EntityType.Template);
                    if (host != null && host.getManagementServerId() != null) {
                        if (_nodeId == host.getManagementServerId().longValue()) {
                            Answer answer = null;
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
                } catch (final Throwable th) {
                    s_logger.warn("Exception while checking status for uploaded template " + templateDataStore.getExtractUrl() + ". Error details: " + th.getMessage());
                    if (s_logger.isTraceEnabled()) {
                        s_logger.trace("Exception details: ", th);
                    }
                }
            }
        }

        private void handleVolumeStatusResponse(final UploadStatusAnswer answer, final VolumeVO volume, final VolumeDataStoreVO volumeDataStore) {
            final StateMachine2<Volume.State, Event, Volume> stateMachine = Volume.State.getStateMachine();
            Transaction.execute(new TransactionCallbackNoReturn() {
                @Override
                public void doInTransactionWithoutResult(final TransactionStatus status) {
                    final VolumeVO tmpVolume = _volumeDao.findById(volume.getId());
                    final VolumeDataStoreVO tmpVolumeDataStore = _volumeDataStoreDao.findById(volumeDataStore.getId());
                    boolean sendAlert = false;
                    String msg = null;
                    try {
                        switch (answer.getStatus()) {
                            case COMPLETED:
                                tmpVolumeDataStore.setDownloadState(VMTemplateStorageResourceAssoc.Status.DOWNLOADED);
                                tmpVolumeDataStore.setState(State.Ready);
                                tmpVolumeDataStore.setInstallPath(answer.getInstallPath());
                                tmpVolumeDataStore.setPhysicalSize(answer.getPhysicalSize());
                                tmpVolumeDataStore.setSize(answer.getVirtualSize());
                                tmpVolumeDataStore.setDownloadPercent(100);

                                final VolumeVO volumeUpdate = _volumeDao.createForUpdate();
                                volumeUpdate.setSize(answer.getVirtualSize());
                                _volumeDao.update(tmpVolume.getId(), volumeUpdate);
                                stateMachine.transitTo(tmpVolume, Event.OperationSucceeded, null, _volumeDao);
                                _resourceLimitMgr.incrementResourceCount(volume.getAccountId(), Resource.ResourceType.secondary_storage, answer.getVirtualSize());

                                if (s_logger.isDebugEnabled()) {
                                    s_logger.debug("Volume " + tmpVolume.getUuid() + " uploaded successfully");
                                }
                                break;
                            case IN_PROGRESS:
                                if (tmpVolume.getState() == Volume.State.NotUploaded) {
                                    tmpVolumeDataStore.setDownloadState(VMTemplateStorageResourceAssoc.Status.DOWNLOAD_IN_PROGRESS);
                                    tmpVolumeDataStore.setDownloadPercent(answer.getDownloadPercent());
                                    stateMachine.transitTo(tmpVolume, Event.UploadRequested, null, _volumeDao);
                                } else if (tmpVolume.getState() == Volume.State.UploadInProgress) { // check for timeout
                                    if (System.currentTimeMillis() - tmpVolumeDataStore.getCreated().getTime() > _uploadOperationTimeout) {
                                        tmpVolumeDataStore.setDownloadState(VMTemplateStorageResourceAssoc.Status.DOWNLOAD_ERROR);
                                        tmpVolumeDataStore.setState(State.Failed);
                                        stateMachine.transitTo(tmpVolume, Event.OperationFailed, null, _volumeDao);
                                        msg = "Volume " + tmpVolume.getUuid() + " failed to upload due to operation timed out";
                                        s_logger.error(msg);
                                        sendAlert = true;
                                    } else {
                                        tmpVolumeDataStore.setDownloadPercent(answer.getDownloadPercent());
                                    }
                                }
                                break;
                            case ERROR:
                                tmpVolumeDataStore.setDownloadState(VMTemplateStorageResourceAssoc.Status.DOWNLOAD_ERROR);
                                tmpVolumeDataStore.setState(State.Failed);
                                stateMachine.transitTo(tmpVolume, Event.OperationFailed, null, _volumeDao);
                                msg = "Volume " + tmpVolume.getUuid() + " failed to upload. Error details: " + answer.getDetails();
                                s_logger.error(msg);
                                sendAlert = true;
                                break;
                            case UNKNOWN:
                                if (tmpVolume.getState() == Volume.State.NotUploaded) { // check for timeout
                                    if (System.currentTimeMillis() - tmpVolumeDataStore.getCreated().getTime() > _uploadOperationTimeout) {
                                        tmpVolumeDataStore.setDownloadState(VMTemplateStorageResourceAssoc.Status.ABANDONED);
                                        tmpVolumeDataStore.setState(State.Failed);
                                        stateMachine.transitTo(tmpVolume, Event.OperationTimeout, null, _volumeDao);
                                        msg = "Volume " + tmpVolume.getUuid() + " failed to upload due to operation timed out";
                                        s_logger.error(msg);
                                        sendAlert = true;
                                    }
                                }
                                break;
                        }
                        _volumeDataStoreDao.update(tmpVolumeDataStore.getId(), tmpVolumeDataStore);
                    } catch (final NoTransitionException e) {
                        s_logger.error("Unexpected error " + e.getMessage());
                    } finally {
                        if (sendAlert) {
                            _alertMgr.sendAlert(AlertManager.AlertType.ALERT_TYPE_UPLOAD_FAILED, tmpVolume.getDataCenterId(), null, msg, msg);
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
                    final VMTemplateVO tmpTemplate = _templateDao.findById(template.getId());
                    final TemplateDataStoreVO tmpTemplateDataStore = _templateDataStoreDao.findById(templateDataStore.getId());
                    boolean sendAlert = false;
                    String msg = null;
                    try {
                        switch (answer.getStatus()) {
                            case COMPLETED:
                                tmpTemplateDataStore.setDownloadState(VMTemplateStorageResourceAssoc.Status.DOWNLOADED);
                                tmpTemplateDataStore.setState(State.Ready);
                                tmpTemplateDataStore.setInstallPath(answer.getInstallPath());
                                tmpTemplateDataStore.setPhysicalSize(answer.getPhysicalSize());
                                tmpTemplateDataStore.setSize(answer.getVirtualSize());
                                tmpTemplateDataStore.setDownloadPercent(100);
                                tmpTemplateDataStore.setExtractUrl(null);

                                final VMTemplateVO templateUpdate = _templateDao.createForUpdate();
                                templateUpdate.setSize(answer.getVirtualSize());
                                _templateDao.update(tmpTemplate.getId(), templateUpdate);
                                stateMachine.transitTo(tmpTemplate, VirtualMachineTemplate.Event.OperationSucceeded, null, _templateDao);
                                _resourceLimitMgr.incrementResourceCount(template.getAccountId(), Resource.ResourceType.secondary_storage, answer.getVirtualSize());

                                if (s_logger.isDebugEnabled()) {
                                    s_logger.debug("Template " + tmpTemplate.getUuid() + " uploaded successfully");
                                }
                                break;
                            case IN_PROGRESS:
                                if (tmpTemplate.getState() == VirtualMachineTemplate.State.NotUploaded) {
                                    tmpTemplateDataStore.setDownloadState(VMTemplateStorageResourceAssoc.Status.DOWNLOAD_IN_PROGRESS);
                                    stateMachine.transitTo(tmpTemplate, VirtualMachineTemplate.Event.UploadRequested, null, _templateDao);
                                    tmpTemplateDataStore.setDownloadPercent(answer.getDownloadPercent());
                                } else if (tmpTemplate.getState() == VirtualMachineTemplate.State.UploadInProgress) { // check for timeout
                                    if (System.currentTimeMillis() - tmpTemplateDataStore.getCreated().getTime() > _uploadOperationTimeout) {
                                        tmpTemplateDataStore.setDownloadState(VMTemplateStorageResourceAssoc.Status.DOWNLOAD_ERROR);
                                        tmpTemplateDataStore.setState(State.Failed);
                                        stateMachine.transitTo(tmpTemplate, VirtualMachineTemplate.Event.OperationFailed, null, _templateDao);
                                        msg = "Template " + tmpTemplate.getUuid() + " failed to upload due to operation timed out";
                                        s_logger.error(msg);
                                        sendAlert = true;
                                    } else {
                                        tmpTemplateDataStore.setDownloadPercent(answer.getDownloadPercent());
                                    }
                                }
                                break;
                            case ERROR:
                                tmpTemplateDataStore.setDownloadState(VMTemplateStorageResourceAssoc.Status.DOWNLOAD_ERROR);
                                tmpTemplateDataStore.setState(State.Failed);
                                stateMachine.transitTo(tmpTemplate, VirtualMachineTemplate.Event.OperationFailed, null, _templateDao);
                                msg = "Template " + tmpTemplate.getUuid() + " failed to upload. Error details: " + answer.getDetails();
                                s_logger.error(msg);
                                sendAlert = true;
                                break;
                            case UNKNOWN:
                                if (tmpTemplate.getState() == VirtualMachineTemplate.State.NotUploaded) { // check for timeout
                                    if (System.currentTimeMillis() - tmpTemplateDataStore.getCreated().getTime() > _uploadOperationTimeout) {
                                        tmpTemplateDataStore.setDownloadState(VMTemplateStorageResourceAssoc.Status.ABANDONED);
                                        tmpTemplateDataStore.setState(State.Failed);
                                        stateMachine.transitTo(tmpTemplate, VirtualMachineTemplate.Event.OperationTimeout, null, _templateDao);
                                        msg = "Template " + tmpTemplate.getUuid() + " failed to upload due to operation timed out";
                                        s_logger.error(msg);
                                        sendAlert = true;
                                    }
                                }
                                break;
                        }
                        _templateDataStoreDao.update(tmpTemplateDataStore.getId(), tmpTemplateDataStore);
                    } catch (final NoTransitionException e) {
                        s_logger.error("Unexpected error " + e.getMessage());
                    } finally {
                        if (sendAlert) {
                            _alertMgr.sendAlert(AlertManager.AlertType.ALERT_TYPE_UPLOAD_FAILED,
                                    _vmTemplateZoneDao.listByTemplateId(tmpTemplate.getId()).get(0).getZoneId(), null, msg, msg);
                        }
                    }
                }
            });
        }
    }
}
