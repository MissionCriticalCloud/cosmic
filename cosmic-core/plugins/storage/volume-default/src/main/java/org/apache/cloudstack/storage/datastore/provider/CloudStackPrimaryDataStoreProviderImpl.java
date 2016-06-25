package org.apache.cloudstack.storage.datastore.provider;

import com.cloud.utils.component.ComponentContext;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreLifeCycle;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreProvider;
import org.apache.cloudstack.engine.subsystem.api.storage.HypervisorHostListener;
import org.apache.cloudstack.engine.subsystem.api.storage.PrimaryDataStoreDriver;
import org.apache.cloudstack.engine.subsystem.api.storage.PrimaryDataStoreProvider;
import org.apache.cloudstack.storage.datastore.driver.CloudStackPrimaryDataStoreDriverImpl;
import org.apache.cloudstack.storage.datastore.lifecycle.CloudStackPrimaryDataStoreLifeCycleImpl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CloudStackPrimaryDataStoreProviderImpl implements PrimaryDataStoreProvider {

    protected PrimaryDataStoreDriver driver;
    protected HypervisorHostListener listener;
    protected DataStoreLifeCycle lifecycle;

    CloudStackPrimaryDataStoreProviderImpl() {

    }

    @Override
    public DataStoreLifeCycle getDataStoreLifeCycle() {
        return lifecycle;
    }

    @Override
    public PrimaryDataStoreDriver getDataStoreDriver() {
        return driver;
    }

    @Override
    public HypervisorHostListener getHostListener() {
        return listener;
    }

    @Override
    public String getName() {
        return DataStoreProvider.DEFAULT_PRIMARY;
    }

    @Override
    public boolean configure(final Map<String, Object> params) {
        lifecycle = ComponentContext.inject(CloudStackPrimaryDataStoreLifeCycleImpl.class);
        driver = ComponentContext.inject(CloudStackPrimaryDataStoreDriverImpl.class);
        listener = ComponentContext.inject(DefaultHostListener.class);
        return true;
    }

    @Override
    public Set<DataStoreProviderType> getTypes() {
        final Set<DataStoreProviderType> types = new HashSet<>();
        types.add(DataStoreProviderType.PRIMARY);
        return types;
    }
}
