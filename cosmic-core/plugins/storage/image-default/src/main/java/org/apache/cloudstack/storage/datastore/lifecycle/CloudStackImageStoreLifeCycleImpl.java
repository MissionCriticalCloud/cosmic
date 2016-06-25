package org.apache.cloudstack.storage.datastore.lifecycle;

import com.cloud.agent.api.StoragePoolInfo;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.resource.Discoverer;
import com.cloud.resource.ResourceManager;
import com.cloud.storage.DataStoreRole;
import com.cloud.storage.ScopeType;
import com.cloud.utils.StringUtils;
import com.cloud.utils.UriUtils;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudStackImageStoreLifeCycleImpl implements ImageStoreLifeCycle {

    private static final Logger s_logger = LoggerFactory.getLogger(CloudStackImageStoreLifeCycleImpl.class);
    @Inject
    protected ResourceManager _resourceMgr;
    @Inject
    protected ImageStoreDao imageStoreDao;
    protected List<? extends Discoverer> _discoverers;
    @Inject
    ImageStoreHelper imageStoreHelper;
    @Inject
    ImageStoreProviderManager imageStoreMgr;

    public CloudStackImageStoreLifeCycleImpl() {
    }

    public List<? extends Discoverer> getDiscoverers() {
        return _discoverers;
    }

    public void setDiscoverers(final List<? extends Discoverer> discoverers) {
        this._discoverers = discoverers;
    }

    @Override
    public DataStore initialize(final Map<String, Object> dsInfos) {

        final Long dcId = (Long) dsInfos.get("zoneId");
        final String url = (String) dsInfos.get("url");
        String name = (String) dsInfos.get("name");
        if (name == null) {
            name = url;
        }
        final String providerName = (String) dsInfos.get("providerName");
        final DataStoreRole role = (DataStoreRole) dsInfos.get("role");
        final Map<String, String> details = (Map<String, String>) dsInfos.get("details");

        String logString = "";
        if (url.contains("cifs")) {
            logString = cleanPassword(url);
        } else {
            logString = StringUtils.cleanString(url);
        }
        s_logger.info("Trying to add a new data store at " + logString + " to data center " + dcId);

        URI uri = null;
        try {
            uri = new URI(UriUtils.encodeURIComponent(url));
            if (uri.getScheme() == null) {
                throw new InvalidParameterValueException("uri.scheme is null " + StringUtils.cleanString(url) + ", add nfs:// (or cifs://) as a prefix");
            } else if (uri.getScheme().equalsIgnoreCase("nfs")) {
                if (uri.getHost() == null || uri.getHost().equalsIgnoreCase("") || uri.getPath() == null || uri.getPath().equalsIgnoreCase("")) {
                    throw new InvalidParameterValueException("Your host and/or path is wrong.  Make sure it's of the format nfs://hostname/path");
                }
            } else if (uri.getScheme().equalsIgnoreCase("cifs")) {
                // Don't validate against a URI encoded URI.
                final URI cifsUri = new URI(url);
                final String warnMsg = UriUtils.getCifsUriParametersProblems(cifsUri);
                if (warnMsg != null) {
                    throw new InvalidParameterValueException(warnMsg);
                }
            }
        } catch (final URISyntaxException e) {
            throw new InvalidParameterValueException(url + " is not a valid uri");
        }

        if (dcId == null) {
            throw new InvalidParameterValueException("DataCenter id is null, and cloudstack default image store has to be associated with a data center");
        }

        final Map<String, Object> imageStoreParameters = new HashMap<>();
        imageStoreParameters.put("name", name);
        imageStoreParameters.put("zoneId", dcId);
        imageStoreParameters.put("url", url);
        imageStoreParameters.put("protocol", uri.getScheme().toLowerCase());
        imageStoreParameters.put("scope", ScopeType.ZONE); // default cloudstack provider only supports zone-wide image store
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
        return imageStoreHelper.convertToStagingStore(store);
    }

    public static String cleanPassword(final String logString) {
        String cleanLogString = null;
        if (logString != null) {
            cleanLogString = logString;
            final String[] temp = logString.split(",");
            int i = 0;
            if (temp != null) {
                while (i < temp.length) {
                    temp[i] = StringUtils.cleanString(temp[i]);
                    i++;
                }
                final List<String> stringList = new ArrayList<>();
                Collections.addAll(stringList, temp);
                cleanLogString = StringUtils.join(stringList, ",");
            }
        }
        return cleanLogString;
    }
}
