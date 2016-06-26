package org.apache.cloudstack.storage.helper;

import com.cloud.host.Host;
import com.cloud.storage.Snapshot;
import com.cloud.vm.snapshot.VMSnapshot;
import org.apache.cloudstack.engine.subsystem.api.storage.DataMotionStrategy;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObject;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.SnapshotStrategy;
import org.apache.cloudstack.engine.subsystem.api.storage.SnapshotStrategy.SnapshotOperation;
import org.apache.cloudstack.engine.subsystem.api.storage.StorageStrategyFactory;
import org.apache.cloudstack.engine.subsystem.api.storage.StrategyPriority;
import org.apache.cloudstack.engine.subsystem.api.storage.VMSnapshotStrategy;
import org.apache.cloudstack.engine.subsystem.api.storage.VolumeInfo;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class StorageStrategyFactoryImpl implements StorageStrategyFactory {

    List<SnapshotStrategy> snapshotStrategies;
    List<DataMotionStrategy> dataMotionStrategies;
    List<VMSnapshotStrategy> vmSnapshotStrategies;

    @Override
    public DataMotionStrategy getDataMotionStrategy(final DataObject srcData, final DataObject destData) {
        return bestMatch(dataMotionStrategies, new CanHandle<DataMotionStrategy>() {
            @Override
            public StrategyPriority canHandle(final DataMotionStrategy strategy) {
                return strategy.canHandle(srcData, destData);
            }
        });
    }

    @Override
    public DataMotionStrategy getDataMotionStrategy(final Map<VolumeInfo, DataStore> volumeMap, final Host srcHost, final Host destHost) {
        return bestMatch(dataMotionStrategies, new CanHandle<DataMotionStrategy>() {
            @Override
            public StrategyPriority canHandle(final DataMotionStrategy strategy) {
                return strategy.canHandle(volumeMap, srcHost, destHost);
            }
        });
    }

    @Override
    public SnapshotStrategy getSnapshotStrategy(final Snapshot snapshot, final SnapshotOperation op) {
        return bestMatch(snapshotStrategies, new CanHandle<SnapshotStrategy>() {
            @Override
            public StrategyPriority canHandle(final SnapshotStrategy strategy) {
                return strategy.canHandle(snapshot, op);
            }
        });
    }

    @Override
    public VMSnapshotStrategy getVmSnapshotStrategy(final VMSnapshot vmSnapshot) {
        return bestMatch(vmSnapshotStrategies, new CanHandle<VMSnapshotStrategy>() {
            @Override
            public StrategyPriority canHandle(final VMSnapshotStrategy strategy) {
                return strategy.canHandle(vmSnapshot);
            }
        });
    }

    private static <T> T bestMatch(final Collection<T> collection, final CanHandle<T> canHandle) {
        if (collection.size() == 0) {
            return null;
        }

        StrategyPriority highestPriority = StrategyPriority.CANT_HANDLE;

        T strategyToUse = null;
        for (final T strategy : collection) {
            final StrategyPriority priority = canHandle.canHandle(strategy);
            if (priority.ordinal() > highestPriority.ordinal()) {
                highestPriority = priority;
                strategyToUse = strategy;
            }
        }

        return strategyToUse;
    }

    public List<SnapshotStrategy> getSnapshotStrategies() {
        return snapshotStrategies;
    }

    @Inject
    public void setSnapshotStrategies(final List<SnapshotStrategy> snapshotStrategies) {
        this.snapshotStrategies = snapshotStrategies;
    }

    public List<DataMotionStrategy> getDataMotionStrategies() {
        return dataMotionStrategies;
    }

    @Inject
    public void setDataMotionStrategies(final List<DataMotionStrategy> dataMotionStrategies) {
        this.dataMotionStrategies = dataMotionStrategies;
    }

    public List<VMSnapshotStrategy> getVmSnapshotStrategies() {
        return vmSnapshotStrategies;
    }

    @Inject
    public void setVmSnapshotStrategies(final List<VMSnapshotStrategy> vmSnapshotStrategies) {
        this.vmSnapshotStrategies = vmSnapshotStrategies;
    }

    private static interface CanHandle<T> {
        StrategyPriority canHandle(T strategy);
    }
}
