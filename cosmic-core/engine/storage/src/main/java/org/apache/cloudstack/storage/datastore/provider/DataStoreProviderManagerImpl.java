package org.apache.cloudstack.storage.datastore.provider;

import com.cloud.exception.InvalidParameterValueException;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.component.Registry;
import org.apache.cloudstack.api.response.StorageProviderResponse;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreProvider;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreProvider.DataStoreProviderType;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreProviderManager;
import org.apache.cloudstack.engine.subsystem.api.storage.PrimaryDataStoreDriver;
import org.apache.cloudstack.storage.datastore.PrimaryDataStoreProviderManager;
import org.apache.cloudstack.storage.image.ImageStoreDriver;
import org.apache.cloudstack.storage.image.datastore.ImageStoreProviderManager;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DataStoreProviderManagerImpl extends ManagerBase implements DataStoreProviderManager, Registry<DataStoreProvider> {
    private static final Logger s_logger = LoggerFactory.getLogger(DataStoreProviderManagerImpl.class);
    protected Map<String, DataStoreProvider> providerMap = new ConcurrentHashMap<>();
    List<DataStoreProvider> providers;
    @Inject
    PrimaryDataStoreProviderManager primaryDataStoreProviderMgr;
    @Inject
    ImageStoreProviderManager imageStoreProviderMgr;

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {

        if (providers != null) {
            for (final DataStoreProvider provider : providers) {
                registerProvider(provider);
            }
        }

        providers = new CopyOnWriteArrayList<>(providers);

        return true;
    }

    protected boolean registerProvider(final DataStoreProvider provider) {
        final Map<String, Object> copyParams = new HashMap<>();

        final String providerName = provider.getName();
        if (providerMap.get(providerName) != null) {
            s_logger.debug("Did not register data store provider, provider name: " + providerName + " is not unique");
            return false;
        }

        s_logger.debug("registering data store provider:" + provider.getName());

        providerMap.put(providerName, provider);
        try {
            final boolean registrationResult = provider.configure(copyParams);
            if (!registrationResult) {
                providerMap.remove(providerName);
                s_logger.debug("Failed to register data store provider: " + providerName);
                return false;
            }

            final Set<DataStoreProviderType> types = provider.getTypes();
            if (types.contains(DataStoreProviderType.PRIMARY)) {
                primaryDataStoreProviderMgr.registerDriver(provider.getName(), (PrimaryDataStoreDriver) provider.getDataStoreDriver());
                primaryDataStoreProviderMgr.registerHostListener(provider.getName(), provider.getHostListener());
            } else if (types.contains(DataStoreProviderType.IMAGE)) {
                imageStoreProviderMgr.registerDriver(provider.getName(), (ImageStoreDriver) provider.getDataStoreDriver());
            }
        } catch (final Exception e) {
            s_logger.debug("configure provider failed", e);
            providerMap.remove(providerName);
            return false;
        }

        return true;
    }

    @Override
    public DataStoreProvider getDataStoreProvider(final String name) {
        if (name == null) {
            return null;
        }

        return providerMap.get(name);
    }

    @Override
    public List<StorageProviderResponse> getDataStoreProviders(final String type) {
        if (type == null) {
            throw new InvalidParameterValueException("Invalid parameter, need to specify type: either primary or image");
        }
        if (type.equalsIgnoreCase(DataStoreProvider.DataStoreProviderType.PRIMARY.toString())) {
            return this.getPrimaryDataStoreProviders();
        } else if (type.equalsIgnoreCase(DataStoreProvider.DataStoreProviderType.IMAGE.toString())) {
            return this.getImageDataStoreProviders();
        } else if (type.equalsIgnoreCase(DataStoreProvider.DataStoreProviderType.ImageCache.toString())) {
            return this.getCacheDataStoreProviders();
        } else {
            throw new InvalidParameterValueException("Invalid parameter: " + type);
        }
    }

    public List<StorageProviderResponse> getPrimaryDataStoreProviders() {
        final List<StorageProviderResponse> providers = new ArrayList<>();
        for (final DataStoreProvider provider : providerMap.values()) {
            if (provider.getTypes().contains(DataStoreProviderType.PRIMARY)) {
                final StorageProviderResponse response = new StorageProviderResponse();
                response.setName(provider.getName());
                response.setType(DataStoreProvider.DataStoreProviderType.PRIMARY.toString());
                providers.add(response);
            }
        }
        return providers;
    }

    public List<StorageProviderResponse> getImageDataStoreProviders() {
        final List<StorageProviderResponse> providers = new ArrayList<>();
        for (final DataStoreProvider provider : providerMap.values()) {
            if (provider.getTypes().contains(DataStoreProviderType.IMAGE)) {
                final StorageProviderResponse response = new StorageProviderResponse();
                response.setName(provider.getName());
                response.setType(DataStoreProvider.DataStoreProviderType.IMAGE.toString());
                providers.add(response);
            }
        }
        return providers;
    }

    public List<StorageProviderResponse> getCacheDataStoreProviders() {
        final List<StorageProviderResponse> providers = new ArrayList<>();
        for (final DataStoreProvider provider : providerMap.values()) {
            if (provider.getTypes().contains(DataStoreProviderType.ImageCache)) {
                final StorageProviderResponse response = new StorageProviderResponse();
                response.setName(provider.getName());
                response.setType(DataStoreProviderType.ImageCache.toString());
                providers.add(response);
            }
        }
        return providers;
    }

    @Override
    public boolean register(final DataStoreProvider type) {
        if (registerProvider(type)) {
            providers.add(type);
            return true;
        }

        return false;
    }

    @Override
    public void unregister(final DataStoreProvider type) {
        /* Sorry, no unregister supported... */
    }

    @Override
    public List<DataStoreProvider> getRegistered() {
        return Collections.unmodifiableList(providers);
    }

    @Override
    public DataStoreProvider getDefaultPrimaryDataStoreProvider() {
        return this.getDataStoreProvider(DataStoreProvider.DEFAULT_PRIMARY);
    }

    public void setPrimaryDataStoreProviderMgr(final PrimaryDataStoreProviderManager primaryDataStoreProviderMgr) {
        this.primaryDataStoreProviderMgr = primaryDataStoreProviderMgr;
    }

    public void setImageStoreProviderMgr(final ImageStoreProviderManager imageDataStoreProviderMgr) {
        this.imageStoreProviderMgr = imageDataStoreProviderMgr;
    }

    @Override
    public DataStoreProvider getDefaultImageDataStoreProvider() {
        return this.getDataStoreProvider(DataStoreProvider.NFS_IMAGE);
    }

    public List<DataStoreProvider> getProviders() {
        return providers;
    }

    @Inject
    public void setProviders(final List<DataStoreProvider> providers) {
        this.providers = providers;
    }

    @Override
    public DataStoreProvider getDefaultCacheDataStoreProvider() {
        return this.getDataStoreProvider(DataStoreProvider.NFS_IMAGE);
    }
}
