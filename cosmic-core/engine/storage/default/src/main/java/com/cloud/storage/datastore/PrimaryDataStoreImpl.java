package com.cloud.storage.datastore;

import com.cloud.engine.subsystem.api.storage.ClusterScope;
import com.cloud.engine.subsystem.api.storage.DataObject;
import com.cloud.engine.subsystem.api.storage.DataStoreDriver;
import com.cloud.engine.subsystem.api.storage.DataStoreProvider;
import com.cloud.engine.subsystem.api.storage.HostScope;
import com.cloud.engine.subsystem.api.storage.PrimaryDataStore;
import com.cloud.engine.subsystem.api.storage.PrimaryDataStoreDriver;
import com.cloud.engine.subsystem.api.storage.PrimaryDataStoreLifeCycle;
import com.cloud.engine.subsystem.api.storage.Scope;
import com.cloud.engine.subsystem.api.storage.SnapshotDataFactory;
import com.cloud.engine.subsystem.api.storage.SnapshotInfo;
import com.cloud.engine.subsystem.api.storage.TemplateDataFactory;
import com.cloud.engine.subsystem.api.storage.TemplateInfo;
import com.cloud.engine.subsystem.api.storage.VolumeInfo;
import com.cloud.engine.subsystem.api.storage.ZoneScope;
import com.cloud.legacymodel.storage.ObjectInDataStoreStateMachine;
import com.cloud.legacymodel.to.DataStoreTO;
import com.cloud.legacymodel.to.PrimaryDataStoreTO;
import com.cloud.model.enumeration.DataObjectType;
import com.cloud.model.enumeration.DataStoreRole;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.model.enumeration.StoragePoolStatus;
import com.cloud.model.enumeration.StoragePoolType;
import com.cloud.storage.ScopeType;
import com.cloud.storage.StoragePoolHostVO;
import com.cloud.storage.VMTemplateStoragePoolVO;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.StoragePoolHostDao;
import com.cloud.storage.dao.VMTemplatePoolDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.storage.datastore.db.PrimaryDataStoreDao;
import com.cloud.storage.datastore.db.StoragePoolVO;
import com.cloud.storage.volume.VolumeObject;
import com.cloud.utils.component.ComponentContext;
import com.cloud.utils.db.GlobalLock;
import com.cloud.utils.storage.encoding.EncodingType;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrimaryDataStoreImpl implements PrimaryDataStore {
    private static final Logger s_logger = LoggerFactory.getLogger(PrimaryDataStoreImpl.class);

    protected PrimaryDataStoreDriver driver;
    protected StoragePoolVO pdsv;
    @Inject
    protected PrimaryDataStoreDao dataStoreDao;
    protected PrimaryDataStoreLifeCycle lifeCycle;
    protected DataStoreProvider provider;
    @Inject
    TemplateDataFactory imageDataFactory;
    @Inject
    SnapshotDataFactory snapshotFactory;
    @Inject
    VMTemplatePoolDao templatePoolDao;
    @Inject
    StoragePoolHostDao poolHostDao;
    @Inject
    private ObjectInDataStoreManager objectInStoreMgr;
    @Inject
    private VolumeDao volumeDao;
    private Map<String, String> _details;

    public PrimaryDataStoreImpl() {

    }

    public static PrimaryDataStoreImpl createDataStore(final StoragePoolVO pdsv, final PrimaryDataStoreDriver driver, final DataStoreProvider provider) {
        final PrimaryDataStoreImpl dataStore = ComponentContext.inject(PrimaryDataStoreImpl.class);
        dataStore.configure(pdsv, driver, provider);
        return dataStore;
    }

    public void configure(final StoragePoolVO pdsv, final PrimaryDataStoreDriver driver, final DataStoreProvider provider) {
        this.pdsv = pdsv;
        this.driver = driver;
        this.provider = provider;
    }

    @Override
    public VolumeInfo getVolume(final long id) {
        final VolumeVO volumeVO = volumeDao.findById(id);
        final VolumeObject vol = VolumeObject.getVolumeObject(this, volumeVO);
        return vol;
    }

    @Override
    public List<VolumeInfo> getVolumes() {
        final List<VolumeVO> volumes = volumeDao.findByPoolId(getId());
        final List<VolumeInfo> volumeInfos = new ArrayList<>();
        for (final VolumeVO volume : volumes) {
            volumeInfos.add(VolumeObject.getVolumeObject(this, volume));
        }
        return volumeInfos;
    }

    @Override
    public boolean exists(final DataObject data) {
        return (objectInStoreMgr.findObject(data, data.getDataStore()) != null) ? true : false;
    }

    @Override
    public TemplateInfo getTemplate(final long templateId) {
        final VMTemplateStoragePoolVO template = templatePoolDao.findByPoolTemplate(getId(), templateId);
        if (template == null || template.getState() != ObjectInDataStoreStateMachine.State.Ready) {
            return null;
        }
        return imageDataFactory.getTemplate(templateId, this);
    }

    @Override
    public DataStoreDriver getDriver() {
        return driver;
    }

    @Override
    public SnapshotInfo getSnapshot(final long snapshotId) {
        return null;
    }

    @Override
    public DataStoreRole getRole() {
        return DataStoreRole.Primary;
    }

    @Override
    public long getId() {
        return pdsv.getId();
    }

    @Override
    public StoragePoolType getPoolType() {
        return pdsv.getPoolType();
    }

    @Override
    public boolean isManaged() {
        return pdsv.isManaged();
    }

    @Override
    public Map<String, String> getDetails() {
        return _details;
    }

    @Override
    public void setDetails(final Map<String, String> details) {
        _details = details;
    }

    @Override
    public String getUri() {
        final String path = pdsv.getPath().replaceFirst("/*", "");
        final StringBuilder builder = new StringBuilder();
        builder.append(pdsv.getPoolType());
        builder.append("://");
        builder.append(pdsv.getHostAddress());
        builder.append(File.separator);
        builder.append(path);
        builder.append(File.separator);
        builder.append("?" + EncodingType.ROLE + "=" + getRole());
        builder.append("&" + EncodingType.STOREUUID + "=" + pdsv.getUuid());
        return builder.toString();
    }

    @Override
    public Date getCreated() {
        return pdsv.getCreated();
    }

    @Override
    public Date getUpdateTime() {
        return pdsv.getUpdateTime();
    }

    @Override
    public Scope getScope() {
        final StoragePoolVO vo = dataStoreDao.findById(pdsv.getId());
        if (vo.getScope() == ScopeType.CLUSTER) {
            return new ClusterScope(vo.getClusterId(), vo.getPodId(), vo.getDataCenterId());
        } else if (vo.getScope() == ScopeType.ZONE) {
            return new ZoneScope(vo.getDataCenterId());
        } else if (vo.getScope() == ScopeType.HOST) {
            final List<StoragePoolHostVO> poolHosts = poolHostDao.listByPoolId(vo.getId());
            if (poolHosts.size() > 0) {
                return new HostScope(poolHosts.get(0).getHostId(), vo.getClusterId(), vo.getDataCenterId());
            }
            s_logger.debug("can't find a local storage in pool host table: " + vo.getId());
        }
        return null;
    }

    @Override
    public long getDataCenterId() {
        return pdsv.getDataCenterId();
    }

    @Override
    public long getCapacityBytes() {
        return pdsv.getCapacityBytes();
    }

    @Override
    public long getUsedBytes() {
        return pdsv.getUsedBytes();
    }

    @Override
    public Long getCapacityIops() {
        return pdsv.getCapacityIops();
    }

    @Override
    public Long getClusterId() {
        return pdsv.getClusterId();
    }

    @Override
    public String getUuid() {
        return pdsv.getUuid();
    }

    @Override
    public String getHostAddress() {
        return pdsv.getHostAddress();
    }

    @Override
    public String getPath() {
        return pdsv.getPath();
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getUserInfo() {
        return pdsv.getUserInfo();
    }

    @Override
    public boolean isShared() {
        return pdsv.getScope() != ScopeType.HOST ;
    }

    @Override
    public boolean isZoneWide() {
        return pdsv.getScope() == ScopeType.ZONE;
    }

    @Override
    public boolean isClusterWide() {
        return pdsv.getScope() == ScopeType.CLUSTER;
    }

    @Override
    public boolean isLocal() {
        return !isShared();
    }

    @Override
    public StoragePoolStatus getStatus() {
        return pdsv.getStatus();
    }

    @Override
    public int getPort() {
        return pdsv.getPort();
    }

    @Override
    public Long getPodId() {
        return pdsv.getPodId();
    }

    @Override
    public String getStorageProviderName() {
        return pdsv.getStorageProviderName();
    }

    @Override
    public boolean isInMaintenance() {
        return getStatus() == StoragePoolStatus.PrepareForMaintenance || getStatus() == StoragePoolStatus.Maintenance ||
                getStatus() == StoragePoolStatus.ErrorInMaintenance || getRemoved() != null;
    }

    public Date getRemoved() {
        return pdsv.getRemoved();
    }

    @Override
    public DataObject create(final DataObject obj) {
        // create template on primary storage
        if (obj.getType() == DataObjectType.TEMPLATE && !isManaged()) {
            try {
                final String templateIdPoolIdString = "templateId:" + obj.getId() + "poolId:" + getId();
                VMTemplateStoragePoolVO templateStoragePoolRef;
                final GlobalLock lock = GlobalLock.getInternLock(templateIdPoolIdString);
                if (!lock.lock(5)) {
                    s_logger.debug("Couldn't lock the db on the string " + templateIdPoolIdString);
                    return null;
                }
                try {
                    templateStoragePoolRef = templatePoolDao.findByPoolTemplate(getId(), obj.getId());
                    if (templateStoragePoolRef == null) {

                        if (s_logger.isDebugEnabled()) {
                            s_logger.debug("Not found (" + templateIdPoolIdString + ") in template_spool_ref, persisting it");
                        }
                        templateStoragePoolRef = new VMTemplateStoragePoolVO(getId(), obj.getId());
                        templateStoragePoolRef = templatePoolDao.persist(templateStoragePoolRef);
                    }
                } finally {
                    lock.unlock();
                    lock.releaseRef();
                }
            } catch (final Exception e) {
                s_logger.debug("Caught exception ", e);
            }
        } else if (obj.getType() == DataObjectType.SNAPSHOT) {
            return objectInStoreMgr.create(obj, this);
        } else if (obj.getType() == DataObjectType.VOLUME) {
            final VolumeVO vol = volumeDao.findById(obj.getId());
            if (vol != null) {
                vol.setPoolId(getId());
                volumeDao.update(vol.getId(), vol);
            }
        }

        return objectInStoreMgr.get(obj, this);
    }

    @Override
    public HypervisorType getHypervisor() {
        return pdsv.getHypervisor();
    }

    @Override
    public boolean delete(final DataObject obj) {
        //TODO: clean up through driver
        objectInStoreMgr.delete(obj);
        return true;
    }

    @Override
    public DataStoreTO getTO() {
        final DataStoreTO to = getDriver().getStoreTO(this);
        if (to == null) {
            return new PrimaryDataStoreTO(this.getUuid(), this.getName(), this.getId(), this.getHostAddress(), this.getPoolType(), this.getHostAddress(), this.getPath
                    (), this.getPort(), this.getDetails());
        }
        return to;
    }
}
