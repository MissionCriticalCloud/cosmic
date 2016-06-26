package org.apache.cloudstack.storage.volume;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.storage.DownloadAnswer;
import com.cloud.agent.api.to.DataObjectType;
import com.cloud.agent.api.to.DataTO;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.offering.DiskOffering.DiskCacheMode;
import com.cloud.storage.DataStoreRole;
import com.cloud.storage.DiskOfferingVO;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Storage.ProvisioningType;
import com.cloud.storage.Volume;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.DiskOfferingDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.utils.component.ComponentContext;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.fsm.NoTransitionException;
import com.cloud.utils.fsm.StateMachine2;
import com.cloud.utils.storage.encoding.EncodingType;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.dao.VMInstanceDao;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObjectInStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine;
import org.apache.cloudstack.engine.subsystem.api.storage.VolumeInfo;
import org.apache.cloudstack.storage.command.CopyCmdAnswer;
import org.apache.cloudstack.storage.command.CreateObjectAnswer;
import org.apache.cloudstack.storage.datastore.ObjectInDataStoreManager;
import org.apache.cloudstack.storage.datastore.db.VolumeDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.VolumeDataStoreVO;
import org.apache.cloudstack.storage.to.VolumeObjectTO;

import javax.inject.Inject;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VolumeObject implements VolumeInfo {
    private static final Logger s_logger = LoggerFactory.getLogger(VolumeObject.class);
    protected VolumeVO volumeVO;
    protected DataStore dataStore;
    @Inject
    VolumeDao volumeDao;
    @Inject
    VolumeDataStoreDao volumeStoreDao;
    @Inject
    ObjectInDataStoreManager objectInStoreMgr;
    @Inject
    VMInstanceDao vmInstanceDao;
    @Inject
    DiskOfferingDao diskOfferingDao;
    private final StateMachine2<Volume.State, Volume.Event, Volume> _volStateMachine;
    private Object payload;

    public VolumeObject() {
        _volStateMachine = Volume.State.getStateMachine();
    }

    public static VolumeObject getVolumeObject(final DataStore dataStore, final VolumeVO volumeVO) {
        final VolumeObject vo = ComponentContext.inject(VolumeObject.class);
        vo.configure(dataStore, volumeVO);
        return vo;
    }

    protected void configure(final DataStore dataStore, final VolumeVO volumeVO) {
        this.volumeVO = volumeVO;
        this.dataStore = dataStore;
    }

    public void update() {
        volumeDao.update(volumeVO.getId(), volumeVO);
        volumeVO = volumeDao.findById(volumeVO.getId());
    }

    @Override
    public boolean isAttachedVM() {
        return (volumeVO.getInstanceId() == null) ? false : true;
    }

    @Override
    public void addPayload(final Object data) {
        payload = data;
    }

    @Override
    public Object getpayload() {
        return payload;
    }

    @Override
    public HypervisorType getHypervisorType() {
        return volumeDao.getHypervisorType(volumeVO.getId());
    }

    @Override
    public Long getLastPoolId() {
        return volumeVO.getLastPoolId();
    }

    @Override
    public String getAttachedVmName() {
        final Long vmId = volumeVO.getInstanceId();
        if (vmId != null) {
            final VMInstanceVO vm = vmInstanceDao.findById(vmId);

            if (vm == null) {
                return null;
            }
            return vm.getInstanceName();
        }
        return null;
    }

    @Override
    public VirtualMachine getAttachedVM() {
        final Long vmId = volumeVO.getInstanceId();
        if (vmId != null) {
            final VMInstanceVO vm = vmInstanceDao.findById(vmId);
            return vm;
        }
        return null;
    }

    @Override
    public void processEventOnly(final ObjectInDataStoreStateMachine.Event event) {
        try {
            objectInStoreMgr.update(this, event);
        } catch (final Exception e) {
            s_logger.debug("Failed to update state", e);
            throw new CloudRuntimeException("Failed to update state:" + e.toString());
        } finally {
            // in case of OperationFailed, expunge the entry
            if (event == ObjectInDataStoreStateMachine.Event.OperationFailed) {
                objectInStoreMgr.deleteIfNotReady(this);
            }
        }
    }

    @Override
    public void processEventOnly(final ObjectInDataStoreStateMachine.Event event, final Answer answer) {
        try {
            if (dataStore.getRole() == DataStoreRole.Primary) {
                if (answer instanceof CopyCmdAnswer) {
                    final CopyCmdAnswer cpyAnswer = (CopyCmdAnswer) answer;
                    final VolumeVO vol = volumeDao.findById(getId());
                    final VolumeObjectTO newVol = (VolumeObjectTO) cpyAnswer.getNewData();
                    vol.setPath(newVol.getPath());
                    if (newVol.getSize() != null) {
                        vol.setSize(newVol.getSize());
                    }
                    vol.setPoolId(getDataStore().getId());
                    volumeDao.update(vol.getId(), vol);
                } else if (answer instanceof CreateObjectAnswer) {
                    final CreateObjectAnswer createAnswer = (CreateObjectAnswer) answer;
                    final VolumeObjectTO newVol = (VolumeObjectTO) createAnswer.getData();
                    final VolumeVO vol = volumeDao.findById(getId());
                    vol.setPath(newVol.getPath());
                    if (newVol.getSize() != null) {
                        vol.setSize(newVol.getSize());
                    }
                    vol.setPoolId(getDataStore().getId());
                    volumeDao.update(vol.getId(), vol);
                }
            } else {
                // image store or imageCache store
                if (answer instanceof DownloadAnswer) {
                    final DownloadAnswer dwdAnswer = (DownloadAnswer) answer;
                    final VolumeDataStoreVO volStore = volumeStoreDao.findByStoreVolume(dataStore.getId(), getId());
                    volStore.setInstallPath(dwdAnswer.getInstallPath());
                    volStore.setChecksum(dwdAnswer.getCheckSum());
                    volumeStoreDao.update(volStore.getId(), volStore);
                } else if (answer instanceof CopyCmdAnswer) {
                    final CopyCmdAnswer cpyAnswer = (CopyCmdAnswer) answer;
                    final VolumeDataStoreVO volStore = volumeStoreDao.findByStoreVolume(dataStore.getId(), getId());
                    final VolumeObjectTO newVol = (VolumeObjectTO) cpyAnswer.getNewData();
                    volStore.setInstallPath(newVol.getPath());
                    if (newVol.getSize() != null) {
                        volStore.setSize(newVol.getSize());
                    }
                    volumeStoreDao.update(volStore.getId(), volStore);
                }
            }
        } catch (final RuntimeException ex) {
            if (event == ObjectInDataStoreStateMachine.Event.OperationFailed) {
                objectInStoreMgr.deleteIfNotReady(this);
            }
            throw ex;
        }
        this.processEventOnly(event);
    }

    @Override
    public boolean stateTransit(final Volume.Event event) {
        boolean result = false;
        try {
            volumeVO = volumeDao.findById(volumeVO.getId());
            if (volumeVO != null) {
                result = _volStateMachine.transitTo(volumeVO, event, null, volumeDao);
                volumeVO = volumeDao.findById(volumeVO.getId());
            }
        } catch (final NoTransitionException e) {
            final String errorMessage = "Failed to transit volume: " + getVolumeId() + ", due to: " + e.toString();
            s_logger.debug(errorMessage);
            throw new CloudRuntimeException(errorMessage);
        }
        return result;
    }

    @Override
    public Long getBytesReadRate() {
        final DiskOfferingVO diskOfferingVO = getDiskOfferingVO();
        if (diskOfferingVO != null) {
            return diskOfferingVO.getBytesReadRate();
        }
        return null;
    }

    private DiskOfferingVO getDiskOfferingVO() {
        if (getDiskOfferingId() != null) {
            final DiskOfferingVO diskOfferingVO = diskOfferingDao.findById(getDiskOfferingId());
            return diskOfferingVO;
        }
        return null;
    }

    @Override
    public Long getBytesWriteRate() {
        final DiskOfferingVO diskOfferingVO = getDiskOfferingVO();
        if (diskOfferingVO != null) {
            return diskOfferingVO.getBytesWriteRate();
        }
        return null;
    }

    @Override
    public Long getIopsReadRate() {
        final DiskOfferingVO diskOfferingVO = getDiskOfferingVO();
        if (diskOfferingVO != null) {
            return diskOfferingVO.getIopsReadRate();
        }
        return null;
    }

    @Override
    public Long getIopsWriteRate() {
        final DiskOfferingVO diskOfferingVO = getDiskOfferingVO();
        if (diskOfferingVO != null) {
            return diskOfferingVO.getIopsWriteRate();
        }
        return null;
    }

    @Override
    public DiskCacheMode getCacheMode() {
        final DiskOfferingVO diskOfferingVO = getDiskOfferingVO();
        if (diskOfferingVO != null) {
            return diskOfferingVO.getCacheMode();
        }
        return null;
    }

    @Override
    public long getId() {
        return volumeVO.getId();
    }

    @Override
    public String getUri() {
        if (dataStore == null) {
            throw new CloudRuntimeException("datastore must be set before using this object");
        }
        final DataObjectInStore obj = objectInStoreMgr.findObject(volumeVO.getId(), DataObjectType.VOLUME, dataStore.getId(), dataStore.getRole());
        if (obj.getState() != ObjectInDataStoreStateMachine.State.Ready) {
            return dataStore.getUri() + "&" + EncodingType.OBJTYPE + "=" + DataObjectType.VOLUME + "&" + EncodingType.SIZE + "=" + volumeVO.getSize() + "&" +
                    EncodingType.NAME + "=" + volumeVO.getName();
        } else {
            return dataStore.getUri() + "&" + EncodingType.OBJTYPE + "=" + DataObjectType.VOLUME + "&" + EncodingType.PATH + "=" + obj.getInstallPath();
        }
    }

    @Override
    public DataTO getTO() {
        DataTO to = getDataStore().getDriver().getTO(this);
        if (to == null) {
            to = new VolumeObjectTO(this);
        }
        return to;
    }

    @Override
    public DataStore getDataStore() {
        return dataStore;
    }

    @Override
    public Long getSize() {
        return volumeVO.getSize();
    }

    public void setSize(final Long size) {
        volumeVO.setSize(size);
    }

    @Override
    public DataObjectType getType() {
        return DataObjectType.VOLUME;
    }

    @Override
    public String getUuid() {
        return volumeVO.getUuid();
    }

    public void setUuid(final String uuid) {
        volumeVO.setUuid(uuid);
    }

    @Override
    public boolean delete() {
        if (dataStore != null) {
            return dataStore.delete(this);
        }
        return true;
    }

    @Override
    public void processEvent(final ObjectInDataStoreStateMachine.Event event) {
        if (dataStore == null) {
            return;
        }
        try {
            Volume.Event volEvent = null;
            if (dataStore.getRole() == DataStoreRole.ImageCache) {
                objectInStoreMgr.update(this, event);
                return;
            }
            if (dataStore.getRole() == DataStoreRole.Image) {
                objectInStoreMgr.update(this, event);
                if (volumeVO.getState() == Volume.State.Migrating || volumeVO.getState() == Volume.State.Copying ||
                        volumeVO.getState() == Volume.State.Uploaded || volumeVO.getState() == Volume.State.Expunged) {
                    return;
                }
                if (event == ObjectInDataStoreStateMachine.Event.CreateOnlyRequested) {
                    volEvent = Volume.Event.UploadRequested;
                } else if (event == ObjectInDataStoreStateMachine.Event.MigrationRequested) {
                    volEvent = Volume.Event.CopyRequested;
                }
            } else {
                if (event == ObjectInDataStoreStateMachine.Event.CreateRequested || event == ObjectInDataStoreStateMachine.Event.CreateOnlyRequested) {
                    volEvent = Volume.Event.CreateRequested;
                } else if (event == ObjectInDataStoreStateMachine.Event.CopyingRequested) {
                    volEvent = Volume.Event.CopyRequested;
                } else if (event == ObjectInDataStoreStateMachine.Event.MigrationRequested) {
                    volEvent = Volume.Event.MigrationRequested;
                } else if (event == ObjectInDataStoreStateMachine.Event.MigrationCopyRequested) {
                    volEvent = Event.MigrationCopyRequested;
                }
            }

            if (event == ObjectInDataStoreStateMachine.Event.DestroyRequested) {
                volEvent = Volume.Event.DestroyRequested;
            } else if (event == ObjectInDataStoreStateMachine.Event.ExpungeRequested) {
                volEvent = Volume.Event.ExpungingRequested;
            } else if (event == ObjectInDataStoreStateMachine.Event.OperationSuccessed) {
                volEvent = Volume.Event.OperationSucceeded;
            } else if (event == ObjectInDataStoreStateMachine.Event.MigrationCopySucceeded) {
                volEvent = Event.MigrationCopySucceeded;
            } else if (event == ObjectInDataStoreStateMachine.Event.OperationFailed) {
                volEvent = Volume.Event.OperationFailed;
            } else if (event == ObjectInDataStoreStateMachine.Event.MigrationCopyFailed) {
                volEvent = Event.MigrationCopyFailed;
            } else if (event == ObjectInDataStoreStateMachine.Event.ResizeRequested) {
                volEvent = Volume.Event.ResizeRequested;
            }
            stateTransit(volEvent);
        } catch (final Exception e) {
            s_logger.debug("Failed to update state", e);
            throw new CloudRuntimeException("Failed to update state:" + e.toString());
        } finally {
            // in case of OperationFailed, expunge the entry
            if (event == ObjectInDataStoreStateMachine.Event.OperationFailed &&
                    (volumeVO.getState() != Volume.State.Copying && volumeVO.getState() != Volume.State.Uploaded && volumeVO.getState() != Volume.State.UploadError)) {
                objectInStoreMgr.deleteIfNotReady(this);
            }
        }
    }

    @Override
    public void processEvent(final ObjectInDataStoreStateMachine.Event event, final Answer answer) {
        try {
            if (dataStore.getRole() == DataStoreRole.Primary) {
                if (answer instanceof CopyCmdAnswer) {
                    final CopyCmdAnswer cpyAnswer = (CopyCmdAnswer) answer;
                    final VolumeVO vol = volumeDao.findById(getId());
                    final VolumeObjectTO newVol = (VolumeObjectTO) cpyAnswer.getNewData();
                    vol.setPath(newVol.getPath());
                    if (newVol.getSize() != null) {
                        vol.setSize(newVol.getSize());
                    }
                    if (newVol.getFormat() != null) {
                        vol.setFormat(newVol.getFormat());
                    }
                    vol.setPoolId(getDataStore().getId());
                    volumeDao.update(vol.getId(), vol);
                } else if (answer instanceof CreateObjectAnswer) {
                    final CreateObjectAnswer createAnswer = (CreateObjectAnswer) answer;
                    final VolumeObjectTO newVol = (VolumeObjectTO) createAnswer.getData();
                    final VolumeVO vol = volumeDao.findById(getId());
                    vol.setPath(newVol.getPath());
                    if (newVol.getSize() != null) {
                        vol.setSize(newVol.getSize());
                    }
                    vol.setPoolId(getDataStore().getId());
                    if (newVol.getFormat() != null) {
                        vol.setFormat(newVol.getFormat());
                    }
                    volumeDao.update(vol.getId(), vol);
                }
            } else {
                // image store or imageCache store
                if (answer instanceof DownloadAnswer) {
                    final DownloadAnswer dwdAnswer = (DownloadAnswer) answer;
                    final VolumeDataStoreVO volStore = volumeStoreDao.findByStoreVolume(dataStore.getId(), getId());
                    volStore.setInstallPath(dwdAnswer.getInstallPath());
                    volStore.setChecksum(dwdAnswer.getCheckSum());
                    volumeStoreDao.update(volStore.getId(), volStore);
                } else if (answer instanceof CopyCmdAnswer) {
                    final CopyCmdAnswer cpyAnswer = (CopyCmdAnswer) answer;
                    final VolumeDataStoreVO volStore = volumeStoreDao.findByStoreVolume(dataStore.getId(), getId());
                    final VolumeObjectTO newVol = (VolumeObjectTO) cpyAnswer.getNewData();
                    volStore.setInstallPath(newVol.getPath());
                    if (newVol.getSize() != null) {
                        volStore.setSize(newVol.getSize());
                    }
                    volumeStoreDao.update(volStore.getId(), volStore);
                }
            }
        } catch (final RuntimeException ex) {
            if (event == ObjectInDataStoreStateMachine.Event.OperationFailed) {
                objectInStoreMgr.deleteIfNotReady(this);
            }
            throw ex;
        }
        this.processEvent(event);
    }

    public long getVolumeId() {
        return volumeVO.getId();
    }

    @Override
    public void incRefCount() {
        if (dataStore == null) {
            return;
        }

        if (dataStore.getRole() == DataStoreRole.Image || dataStore.getRole() == DataStoreRole.ImageCache) {
            final VolumeDataStoreVO store = volumeStoreDao.findByStoreVolume(dataStore.getId(), getId());
            store.incrRefCnt();
            store.setLastUpdated(new Date());
            volumeStoreDao.update(store.getId(), store);
        }
    }

    @Override
    public void decRefCount() {
        if (dataStore == null) {
            return;
        }
        if (dataStore.getRole() == DataStoreRole.Image || dataStore.getRole() == DataStoreRole.ImageCache) {
            final VolumeDataStoreVO store = volumeStoreDao.findByStoreVolume(dataStore.getId(), getId());
            store.decrRefCnt();
            store.setLastUpdated(new Date());
            volumeStoreDao.update(store.getId(), store);
        }
    }

    @Override
    public Long getRefCount() {
        if (dataStore == null) {
            return null;
        }
        if (dataStore.getRole() == DataStoreRole.Image || dataStore.getRole() == DataStoreRole.ImageCache) {
            final VolumeDataStoreVO store = volumeStoreDao.findByStoreVolume(dataStore.getId(), getId());
            return store.getRefCnt();
        }
        return null;
    }

    @Override
    public String getName() {
        return volumeVO.getName();
    }

    @Override
    public Long getMinIops() {
        return volumeVO.getMinIops();
    }

    @Override
    public Long getMaxIops() {
        return volumeVO.getMaxIops();
    }

    @Override
    public String get_iScsiName() {
        return volumeVO.get_iScsiName();
    }

    @Override
    public Long getInstanceId() {
        return volumeVO.getInstanceId();
    }

    @Override
    public String getFolder() {
        return volumeVO.getFolder();
    }

    @Override
    public String getPath() {
        if (dataStore.getRole() == DataStoreRole.Primary) {
            return volumeVO.getPath();
        } else {
            final DataObjectInStore objInStore = objectInStoreMgr.findObject(this, dataStore);
            if (objInStore != null) {
                return objInStore.getInstallPath();
            } else {
                return null;
            }
        }
    }

    @Override
    public Long getPodId() {
        return volumeVO.getPodId();
    }

    @Override
    public long getDataCenterId() {
        return volumeVO.getDataCenterId();
    }

    @Override
    public Type getVolumeType() {
        return volumeVO.getVolumeType();
    }

    @Override
    public Long getPoolId() {
        return volumeVO.getPoolId();
    }

    @Override
    public Volume.State getState() {
        return volumeVO.getState();
    }

    @Override
    public Date getAttached() {
        return volumeVO.getAttached();
    }

    @Override
    public Long getDeviceId() {
        return volumeVO.getDeviceId();
    }

    @Override
    public Date getCreated() {
        return volumeVO.getCreated();
    }

    @Override
    public Long getDiskOfferingId() {
        return volumeVO.getDiskOfferingId();
    }

    @Override
    public String getChainInfo() {
        return volumeVO.getChainInfo();
    }

    @Override
    public boolean isRecreatable() {
        return volumeVO.isRecreatable();
    }

    @Override
    public long getUpdatedCount() {
        return volumeVO.getUpdatedCount();
    }

    @Override
    public void incrUpdatedCount() {
        volumeVO.incrUpdatedCount();
    }

    @Override
    public Date getUpdated() {
        return volumeVO.getUpdated();
    }

    @Override
    public String getReservationId() {
        return volumeVO.getReservationId();
    }

    @Override
    public void setReservationId(final String reserv) {
        volumeVO.setReservationId(reserv);
    }

    @Override
    public ImageFormat getFormat() {
        return volumeVO.getFormat();
    }

    @Override
    public ProvisioningType getProvisioningType() {
        return this.volumeVO.getProvisioningType();
    }

    @Override
    public Long getVmSnapshotChainSize() {
        return volumeVO.getVmSnapshotChainSize();
    }

    @Override
    public Integer getHypervisorSnapshotReserve() {
        return volumeVO.getHypervisorSnapshotReserve();
    }

    @Override
    public boolean isDisplayVolume() {
        return volumeVO.isDisplayVolume();
    }

    @Override
    public boolean isDisplay() {
        return volumeVO.isDisplay();
    }

    @Override
    public long getAccountId() {
        return volumeVO.getAccountId();
    }

    @Override
    public long getDomainId() {
        return volumeVO.getDomainId();
    }

    @Override
    public Long getTemplateId() {
        return volumeVO.getTemplateId();
    }

    public VolumeVO getVolume() {
        return volumeVO;
    }

    @Override
    public Class<?> getEntityType() {
        return Volume.class;
    }
}
