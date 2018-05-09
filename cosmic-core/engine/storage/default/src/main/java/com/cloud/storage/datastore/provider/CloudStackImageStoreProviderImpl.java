package com.cloud.storage.datastore.provider;

import com.cloud.engine.subsystem.api.storage.DataStoreDriver;
import com.cloud.engine.subsystem.api.storage.DataStoreLifeCycle;
import com.cloud.engine.subsystem.api.storage.DataStoreProvider;
import com.cloud.engine.subsystem.api.storage.HypervisorHostListener;
import com.cloud.engine.subsystem.api.storage.ImageStoreProvider;
import com.cloud.storage.ScopeType;
import com.cloud.storage.datastore.driver.CloudStackImageStoreDriverImpl;
import com.cloud.storage.datastore.lifecycle.CloudStackImageStoreLifeCycleImpl;
import com.cloud.storage.image.ImageStoreDriver;
import com.cloud.storage.image.datastore.ImageStoreHelper;
import com.cloud.storage.image.datastore.ImageStoreProviderManager;
import com.cloud.storage.image.store.lifecycle.ImageStoreLifeCycle;
import com.cloud.utils.component.ComponentContext;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class CloudStackImageStoreProviderImpl implements ImageStoreProvider {

    private final String providerName = DataStoreProvider.NFS_IMAGE;
    protected ImageStoreLifeCycle lifeCycle;
    protected ImageStoreDriver driver;
    @Inject
    ImageStoreProviderManager storeMgr;
    @Inject
    ImageStoreHelper helper;

    @Override
    public DataStoreLifeCycle getDataStoreLifeCycle() {
        return lifeCycle;
    }

    @Override
    public DataStoreDriver getDataStoreDriver() {
        return this.driver;
    }

    @Override
    public HypervisorHostListener getHostListener() {
        return null;
    }

    @Override
    public String getName() {
        return this.providerName;
    }

    @Override
    public boolean configure(final Map<String, Object> params) {
        lifeCycle = ComponentContext.inject(CloudStackImageStoreLifeCycleImpl.class);
        driver = ComponentContext.inject(CloudStackImageStoreDriverImpl.class);

        storeMgr.registerDriver(this.getName(), driver);

        return true;
    }

    @Override
    public Set<DataStoreProviderType> getTypes() {
        final Set<DataStoreProviderType> types = new HashSet<>();
        types.add(DataStoreProviderType.IMAGE);
        types.add(DataStoreProviderType.ImageCache);
        return types;
    }

    @Override
    public boolean isScopeSupported(final ScopeType scope) {
        if (scope == ScopeType.ZONE) {
            return true;
        }
        return false;
    }

    @Override
    public boolean needDownloadSysTemplate() {
        return false;
    }
}
