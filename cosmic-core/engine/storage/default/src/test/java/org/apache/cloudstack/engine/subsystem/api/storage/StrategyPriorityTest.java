package org.apache.cloudstack.engine.subsystem.api.storage;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cloud.host.Host;
import com.cloud.storage.Snapshot;
import org.apache.cloudstack.engine.subsystem.api.storage.SnapshotStrategy.SnapshotOperation;
import org.apache.cloudstack.storage.helper.StorageStrategyFactoryImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class StrategyPriorityTest {

    @Test
    public void testSortSnapshotStrategies() {
        final SnapshotStrategy cantHandleStrategy = mock(SnapshotStrategy.class);
        final SnapshotStrategy defaultStrategy = mock(SnapshotStrategy.class);
        final SnapshotStrategy hyperStrategy = mock(SnapshotStrategy.class);
        final SnapshotStrategy pluginStrategy = mock(SnapshotStrategy.class);
        final SnapshotStrategy highestStrategy = mock(SnapshotStrategy.class);

        doReturn(StrategyPriority.CANT_HANDLE).when(cantHandleStrategy).canHandle(any(Snapshot.class), any(SnapshotOperation.class));
        doReturn(StrategyPriority.DEFAULT).when(defaultStrategy).canHandle(any(Snapshot.class), any(SnapshotOperation.class));
        doReturn(StrategyPriority.HYPERVISOR).when(hyperStrategy).canHandle(any(Snapshot.class), any(SnapshotOperation.class));
        doReturn(StrategyPriority.PLUGIN).when(pluginStrategy).canHandle(any(Snapshot.class), any(SnapshotOperation.class));
        doReturn(StrategyPriority.HIGHEST).when(highestStrategy).canHandle(any(Snapshot.class), any(SnapshotOperation.class));

        final List<SnapshotStrategy> strategies = new ArrayList<>(5);
        SnapshotStrategy strategy = null;

        final StorageStrategyFactoryImpl factory = new StorageStrategyFactoryImpl();
        factory.setSnapshotStrategies(strategies);

        strategies.add(cantHandleStrategy);
        strategy = factory.getSnapshotStrategy(mock(Snapshot.class), SnapshotOperation.TAKE);
        assertEquals("A strategy was found when it shouldn't have been.", null, strategy);

        strategies.add(defaultStrategy);
        strategy = factory.getSnapshotStrategy(mock(Snapshot.class), SnapshotOperation.TAKE);
        assertEquals("Default strategy was not picked.", defaultStrategy, strategy);

        strategies.add(hyperStrategy);
        strategy = factory.getSnapshotStrategy(mock(Snapshot.class), SnapshotOperation.TAKE);
        assertEquals("Hypervisor strategy was not picked.", hyperStrategy, strategy);

        strategies.add(pluginStrategy);
        strategy = factory.getSnapshotStrategy(mock(Snapshot.class), SnapshotOperation.TAKE);
        assertEquals("Plugin strategy was not picked.", pluginStrategy, strategy);

        strategies.add(highestStrategy);
        strategy = factory.getSnapshotStrategy(mock(Snapshot.class), SnapshotOperation.TAKE);
        assertEquals("Highest strategy was not picked.", highestStrategy, strategy);
    }

    @Test
    public void testSortDataMotionStrategies() {
        final DataMotionStrategy cantHandleStrategy = mock(DataMotionStrategy.class);
        final DataMotionStrategy defaultStrategy = mock(DataMotionStrategy.class);
        final DataMotionStrategy hyperStrategy = mock(DataMotionStrategy.class);
        final DataMotionStrategy pluginStrategy = mock(DataMotionStrategy.class);
        final DataMotionStrategy highestStrategy = mock(DataMotionStrategy.class);

        doReturn(StrategyPriority.CANT_HANDLE).when(cantHandleStrategy).canHandle(any(DataObject.class), any(DataObject.class));
        doReturn(StrategyPriority.DEFAULT).when(defaultStrategy).canHandle(any(DataObject.class), any(DataObject.class));
        doReturn(StrategyPriority.HYPERVISOR).when(hyperStrategy).canHandle(any(DataObject.class), any(DataObject.class));
        doReturn(StrategyPriority.PLUGIN).when(pluginStrategy).canHandle(any(DataObject.class), any(DataObject.class));
        doReturn(StrategyPriority.HIGHEST).when(highestStrategy).canHandle(any(DataObject.class), any(DataObject.class));

        final List<DataMotionStrategy> strategies = new ArrayList<>(5);
        DataMotionStrategy strategy = null;

        final StorageStrategyFactoryImpl factory = new StorageStrategyFactoryImpl();
        factory.setDataMotionStrategies(strategies);

        strategies.add(cantHandleStrategy);
        strategy = factory.getDataMotionStrategy(mock(DataObject.class), mock(DataObject.class));
        assertEquals("A strategy was found when it shouldn't have been.", null, strategy);

        strategies.add(defaultStrategy);
        strategy = factory.getDataMotionStrategy(mock(DataObject.class), mock(DataObject.class));
        assertEquals("Default strategy was not picked.", defaultStrategy, strategy);

        strategies.add(hyperStrategy);
        strategy = factory.getDataMotionStrategy(mock(DataObject.class), mock(DataObject.class));
        assertEquals("Hypervisor strategy was not picked.", hyperStrategy, strategy);

        strategies.add(pluginStrategy);
        strategy = factory.getDataMotionStrategy(mock(DataObject.class), mock(DataObject.class));
        assertEquals("Plugin strategy was not picked.", pluginStrategy, strategy);

        strategies.add(highestStrategy);
        strategy = factory.getDataMotionStrategy(mock(DataObject.class), mock(DataObject.class));
        assertEquals("Highest strategy was not picked.", highestStrategy, strategy);
    }

    @Test
    public void testSortDataMotionStrategies2() {
        final DataMotionStrategy cantHandleStrategy = mock(DataMotionStrategy.class);
        final DataMotionStrategy defaultStrategy = mock(DataMotionStrategy.class);
        final DataMotionStrategy hyperStrategy = mock(DataMotionStrategy.class);
        final DataMotionStrategy pluginStrategy = mock(DataMotionStrategy.class);
        final DataMotionStrategy highestStrategy = mock(DataMotionStrategy.class);

        doReturn(StrategyPriority.CANT_HANDLE).when(cantHandleStrategy).canHandle(any(Map.class), any(Host.class), any(Host.class));
        doReturn(StrategyPriority.DEFAULT).when(defaultStrategy).canHandle(any(Map.class), any(Host.class), any(Host.class));
        doReturn(StrategyPriority.HYPERVISOR).when(hyperStrategy).canHandle(any(Map.class), any(Host.class), any(Host.class));
        doReturn(StrategyPriority.PLUGIN).when(pluginStrategy).canHandle(any(Map.class), any(Host.class), any(Host.class));
        doReturn(StrategyPriority.HIGHEST).when(highestStrategy).canHandle(any(Map.class), any(Host.class), any(Host.class));

        final List<DataMotionStrategy> strategies = new ArrayList<>(5);
        DataMotionStrategy strategy = null;

        final StorageStrategyFactoryImpl factory = new StorageStrategyFactoryImpl();
        factory.setDataMotionStrategies(strategies);

        strategies.add(cantHandleStrategy);
        strategy = factory.getDataMotionStrategy(mock(Map.class), mock(Host.class), mock(Host.class));
        assertEquals("A strategy was found when it shouldn't have been.", null, strategy);

        strategies.add(defaultStrategy);
        strategy = factory.getDataMotionStrategy(mock(Map.class), mock(Host.class), mock(Host.class));
        assertEquals("Default strategy was not picked.", defaultStrategy, strategy);

        strategies.add(hyperStrategy);
        strategy = factory.getDataMotionStrategy(mock(Map.class), mock(Host.class), mock(Host.class));
        assertEquals("Hypervisor strategy was not picked.", hyperStrategy, strategy);

        strategies.add(pluginStrategy);
        strategy = factory.getDataMotionStrategy(mock(Map.class), mock(Host.class), mock(Host.class));
        assertEquals("Plugin strategy was not picked.", pluginStrategy, strategy);

        strategies.add(highestStrategy);
        strategy = factory.getDataMotionStrategy(mock(Map.class), mock(Host.class), mock(Host.class));
        assertEquals("Highest strategy was not picked.", highestStrategy, strategy);
    }
}
