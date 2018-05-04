package com.cloud.storage.helper;

import com.cloud.engine.subsystem.api.storage.DataMotionStrategy;
import com.cloud.engine.subsystem.api.storage.DataObject;
import com.cloud.engine.subsystem.api.storage.DataStore;
import com.cloud.engine.subsystem.api.storage.SnapshotStrategy;
import com.cloud.engine.subsystem.api.storage.SnapshotStrategy.SnapshotOperation;
import com.cloud.engine.subsystem.api.storage.StorageStrategyFactory;
import com.cloud.engine.subsystem.api.storage.StrategyPriority;
import com.cloud.engine.subsystem.api.storage.VMSnapshotStrategy;
import com.cloud.engine.subsystem.api.storage.VolumeInfo;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.storage.VMSnapshot;
import com.cloud.storage.Snapshot;

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
        return bestMatch(dataMotionStrategies, strategy -> strategy.canHandle(srcData, destData));
    }

    @Override
    public DataMotionStrategy getDataMotionStrategy(final Map<VolumeInfo, DataStore> volumeMap, final Host srcHost, final Host destHost) {
        return bestMatch(dataMotionStrategies, strategy -> strategy.canHandle(volumeMap, srcHost, destHost));
    }

    @Override
    public SnapshotStrategy getSnapshotStrategy(final Snapshot snapshot, final SnapshotOperation op) {
        return bestMatch(snapshotStrategies, strategy -> strategy.canHandle(snapshot, op));
    }

    @Override
    public VMSnapshotStrategy getVmSnapshotStrategy(final VMSnapshot vmSnapshot) {
        return bestMatch(vmSnapshotStrategies, strategy -> strategy.canHandle(vmSnapshot));
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

    @Inject
    public void setSnapshotStrategies(final List<SnapshotStrategy> snapshotStrategies) {
        this.snapshotStrategies = snapshotStrategies;
    }

    @Inject
    public void setDataMotionStrategies(final List<DataMotionStrategy> dataMotionStrategies) {
        this.dataMotionStrategies = dataMotionStrategies;
    }

    @Inject
    public void setVmSnapshotStrategies(final List<VMSnapshotStrategy> vmSnapshotStrategies) {
        this.vmSnapshotStrategies = vmSnapshotStrategies;
    }

    private interface CanHandle<T> {
        StrategyPriority canHandle(T strategy);
    }
}
