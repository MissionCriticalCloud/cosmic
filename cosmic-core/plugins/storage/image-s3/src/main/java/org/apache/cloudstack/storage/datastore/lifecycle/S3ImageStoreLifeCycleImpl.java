package org.apache.cloudstack.storage.datastore.lifecycle;

import com.cloud.agent.api.StoragePoolInfo;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.resource.Discoverer;
import com.cloud.resource.ResourceManager;
import com.cloud.storage.DataStoreRole;
import com.cloud.storage.ScopeType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.engine.subsystem.api.storage.ClusterScope;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.HostScope;
import org.apache.cloudstack.engine.subsystem.api.storage.ZoneScope;
import org.apache.cloudstack.storage.datastore.db.ImageStoreDao;
import org.apache.cloudstack.storage.datastore.db.ImageStoreVO;
import org.apache.cloudstack.storage.image.datastore.ImageStoreHelper;
import org.apache.cloudstack.storage.image.datastore.ImageStoreProviderManager;
import org.apache.cloudstack.storage.image.store.lifecycle.ImageStoreLifeCycle;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3ImageStoreLifeCycleImpl implements ImageStoreLifeCycle {

    private static final Logger s_logger = LoggerFactory.getLogger(S3ImageStoreLifeCycleImpl.class);
    @Inject
    protected ResourceManager _resourceMgr;
    @Inject
    protected ImageStoreDao imageStoreDao;
    protected List<? extends Discoverer> _discoverers;
    @Inject
    ImageStoreHelper imageStoreHelper;
    @Inject
    ImageStoreProviderManager imageStoreMgr;

    public S3ImageStoreLifeCycleImpl() {
    }

    public List<? extends Discoverer> getDiscoverers() {
        return _discoverers;
    }

    public void setDiscoverers(final List<? extends Discoverer> discoverers) {
        this._discoverers = discoverers;
    }

    @Override
    public DataStore initialize(final Map<String, Object> dsInfos) {

        final String url = (String) dsInfos.get("url");
        final String name = (String) dsInfos.get("name");
        final String providerName = (String) dsInfos.get("providerName");
        final ScopeType scope = (ScopeType) dsInfos.get("scope");
        final DataStoreRole role = (DataStoreRole) dsInfos.get("role");
        final Map<String, String> details = (Map<String, String>) dsInfos.get("details");

        s_logger.info("Trying to add a S3 store with endpoint: " + details.get(ApiConstants.S3_END_POINT));

        final Map<String, Object> imageStoreParameters = new HashMap();
        imageStoreParameters.put("name", name);
        imageStoreParameters.put("url", url);
        String protocol = "http";
        final String useHttps = details.get(ApiConstants.S3_HTTPS_FLAG);
        if (useHttps != null && Boolean.parseBoolean(useHttps)) {
            protocol = "https";
        }
        imageStoreParameters.put("protocol", protocol);
        if (scope != null) {
            imageStoreParameters.put("scope", scope);
        } else {
            imageStoreParameters.put("scope", ScopeType.REGION);
        }
        imageStoreParameters.put("providerName", providerName);
        imageStoreParameters.put("role", role);

        final ImageStoreVO ids = imageStoreHelper.createImageStore(imageStoreParameters, details);
        return imageStoreMgr.getImageStore(ids.getId());
    }

    @Override
    public boolean attachCluster(final DataStore store, final ClusterScope scope) {
        return false;
    }

    @Override
    public boolean attachHost(final DataStore store, final HostScope scope, final StoragePoolInfo existingInfo) {
        return false;
    }

    @Override
    public boolean attachZone(final DataStore dataStore, final ZoneScope scope, final HypervisorType hypervisorType) {
        return false;
    }

    @Override
    public boolean maintain(final DataStore store) {
        return false;
    }

    @Override
    public boolean cancelMaintain(final DataStore store) {
        return false;
    }

    @Override
    public boolean deleteDataStore(final DataStore store) {
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.cloudstack.engine.subsystem.api.storage.DataStoreLifeCycle#migrateToObjectStore(org.apache.cloudstack.engine.subsystem.api.storage.DataStore)
     */
    @Override
    public boolean migrateToObjectStore(final DataStore store) {
        return false;
    }
}
