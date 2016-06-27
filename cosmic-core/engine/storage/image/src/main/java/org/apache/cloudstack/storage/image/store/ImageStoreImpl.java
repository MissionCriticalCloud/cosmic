package org.apache.cloudstack.storage.image.store;

import com.cloud.agent.api.to.DataStoreTO;
import com.cloud.capacity.dao.CapacityDao;
import com.cloud.storage.DataStoreRole;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Upload;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.utils.component.ComponentContext;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObject;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreDriver;
import org.apache.cloudstack.engine.subsystem.api.storage.ImageStoreProvider;
import org.apache.cloudstack.engine.subsystem.api.storage.Scope;
import org.apache.cloudstack.engine.subsystem.api.storage.SnapshotInfo;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateInfo;
import org.apache.cloudstack.engine.subsystem.api.storage.VolumeInfo;
import org.apache.cloudstack.engine.subsystem.api.storage.ZoneScope;
import org.apache.cloudstack.framework.async.AsyncCallFuture;
import org.apache.cloudstack.storage.command.CommandResult;
import org.apache.cloudstack.storage.datastore.ObjectInDataStoreManager;
import org.apache.cloudstack.storage.datastore.db.ImageStoreVO;
import org.apache.cloudstack.storage.image.ImageStoreDriver;
import org.apache.cloudstack.storage.image.datastore.ImageStoreEntity;
import org.apache.cloudstack.storage.to.ImageStoreTO;

import javax.inject.Inject;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageStoreImpl implements ImageStoreEntity {
    private static final Logger s_logger = LoggerFactory.getLogger(ImageStoreImpl.class);
    protected ImageStoreDriver driver;
    protected ImageStoreVO imageDataStoreVO;
    protected ImageStoreProvider provider;
    @Inject
    VMTemplateDao imageDao;
    @Inject
    private ObjectInDataStoreManager objectInStoreMgr;
    @Inject
    private CapacityDao capacityDao;

    public ImageStoreImpl() {
        super();
    }

    public static ImageStoreEntity getDataStore(final ImageStoreVO dataStoreVO, final ImageStoreDriver imageDataStoreDriver, final ImageStoreProvider provider) {
        final ImageStoreImpl instance = ComponentContext.inject(ImageStoreImpl.class);
        instance.configure(dataStoreVO, imageDataStoreDriver, provider);
        return instance;
    }

    protected void configure(final ImageStoreVO dataStoreVO, final ImageStoreDriver imageDataStoreDriver, final ImageStoreProvider provider) {
        this.driver = imageDataStoreDriver;
        this.imageDataStoreVO = dataStoreVO;
        this.provider = provider;
    }

    @Override
    public TemplateInfo getTemplate(final long templateId) {
        return null;
    }

    @Override
    public VolumeInfo getVolume(final long volumeId) {
        return null;
    }

    @Override
    public SnapshotInfo getSnapshot(final long snapshotId) {
        return null;
    }

    @Override
    public DataStoreDriver getDriver() {
        return this.driver;
    }

    @Override
    public boolean exists(final DataObject object) {
        return (objectInStoreMgr.findObject(object, this) != null) ? true : false;
    }

    @Override
    public Set<TemplateInfo> listTemplates() {
        return null;
    }

    @Override
    public DataStoreRole getRole() {
        return this.imageDataStoreVO.getRole();
    }

    @Override
    public String getMountPoint() {
        return imageDataStoreVO.getParent();
    }

    @Override
    public String createEntityExtractUrl(final String installPath, final ImageFormat format, final DataObject dataObject) {
        return driver.createEntityExtractUrl(this, installPath, format, dataObject);
    }

    @Override
    public long getId() {
        return this.imageDataStoreVO.getId();
    }

    @Override
    public void deleteExtractUrl(final String installPath, final String url, final Upload.Type entityType) {
        driver.deleteEntityExtractUrl(this, installPath, url, entityType);
    }

    public Date getCreated() {
        return this.imageDataStoreVO.getCreated();
    }

    @Override
    public String getUri() {
        return this.imageDataStoreVO.getUrl();
    }

    @Override
    public Long getDataCenterId() {
        return imageDataStoreVO.getDataCenterId();
    }

    @Override
    public String getProviderName() {
        return imageDataStoreVO.getProviderName();
    }

    @Override
    public Scope getScope() {
        return new ZoneScope(imageDataStoreVO.getDataCenterId());
    }

    @Override
    public String getProtocol() {
        return imageDataStoreVO.getProtocol();
    }

    @Override
    public String getUuid() {
        return this.imageDataStoreVO.getUuid();
    }

    @Override
    public DataObject create(final DataObject obj) {
        final DataObject object = objectInStoreMgr.create(obj, this);
        return object;
    }

    @Override
    public boolean delete(final DataObject obj) {
        final AsyncCallFuture<CommandResult> future = new AsyncCallFuture<>();
        this.driver.deleteAsync(obj.getDataStore(), obj, future);
        try {
            future.get();
        } catch (final InterruptedException e) {
            s_logger.debug("failed delete obj", e);
            return false;
        } catch (final ExecutionException e) {
            s_logger.debug("failed delete obj", e);
            return false;
        }
        objectInStoreMgr.delete(obj);
        return true;
    }

    @Override
    public String getName() {
        return imageDataStoreVO.getName();
    }

    @Override
    public DataStoreTO getTO() {
        final DataStoreTO to = getDriver().getStoreTO(this);
        if (to == null) {
            final ImageStoreTO primaryTO = new ImageStoreTO();
            primaryTO.setProviderName(getProviderName());
            primaryTO.setRole(getRole());
            primaryTO.setType(getProtocol());
            primaryTO.setUri(getUri());
            return primaryTO;
        }
        return to;
    }
}
