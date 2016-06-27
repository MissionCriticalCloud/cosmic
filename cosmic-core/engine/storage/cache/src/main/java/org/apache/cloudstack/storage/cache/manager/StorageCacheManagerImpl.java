package org.apache.cloudstack.storage.cache.manager;

import com.cloud.agent.api.to.DataObjectType;
import com.cloud.configuration.Config;
import com.cloud.storage.DataStoreRole;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.Manager;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.db.GlobalLock;
import com.cloud.utils.db.QueryBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.engine.subsystem.api.storage.CopyCommandResult;
import org.apache.cloudstack.engine.subsystem.api.storage.DataMotionService;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObject;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObjectInStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.Event;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.State;
import org.apache.cloudstack.engine.subsystem.api.storage.Scope;
import org.apache.cloudstack.engine.subsystem.api.storage.StorageCacheManager;
import org.apache.cloudstack.framework.async.AsyncCallFuture;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.managed.context.ManagedContextRunnable;
import org.apache.cloudstack.storage.cache.allocator.StorageCacheAllocator;
import org.apache.cloudstack.storage.datastore.ObjectInDataStoreManager;
import org.apache.cloudstack.storage.datastore.db.ImageStoreVO;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageCacheManagerImpl implements StorageCacheManager, Manager {
    private static final Logger s_logger = LoggerFactory.getLogger(StorageCacheManagerImpl.class);
    private static final Object templateLock = new Object();
    private static final Object volumeLock = new Object();
    private static final Object snapshotLock = new Object();
    @Inject
    List<StorageCacheAllocator> storageCacheAllocator;
    @Inject
    DataMotionService dataMotionSvr;
    @Inject
    ObjectInDataStoreManager objectInStoreMgr;
    @Inject
    DataStoreManager dataStoreManager;
    @Inject
    StorageCacheReplacementAlgorithm cacheReplacementAlgorithm;
    @Inject
    ConfigurationDao configDao;
    Boolean cacheReplacementEnabled = Boolean.TRUE;
    int workers;
    ScheduledExecutorService executors;
    int cacheReplaceMentInterval;

    protected List<DataStore> getCacheStores() {
        final QueryBuilder<ImageStoreVO> sc = QueryBuilder.create(ImageStoreVO.class);
        sc.and(sc.entity().getRole(), SearchCriteria.Op.EQ, DataStoreRole.ImageCache);
        final List<ImageStoreVO> imageStoreVOs = sc.list();
        final List<DataStore> stores = new ArrayList<>();
        for (final ImageStoreVO vo : imageStoreVOs) {
            stores.add(dataStoreManager.getDataStore(vo.getId(), vo.getRole()));
        }
        return stores;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DataStore getCacheStorage(final Scope scope) {
        for (final StorageCacheAllocator allocator : storageCacheAllocator) {
            final DataStore store = allocator.getCacheStore(scope);
            if (store != null) {
                return store;
            }
        }
        return null;
    }

    @Override
    public void setName(final String name) {
        // TODO Auto-generated method stub

    }

    @Override
    public Map<String, Object> getConfigParams() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DataStore getCacheStorage(final DataObject data, final Scope scope) {
        for (final StorageCacheAllocator allocator : storageCacheAllocator) {
            final DataStore store = allocator.getCacheStore(data, scope);
            if (store != null) {
                return store;
            }
        }
        return null;
    }

    @Override
    public void setConfigParams(final Map<String, Object> params) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getRunLevel() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setRunLevel(final int level) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        cacheReplacementEnabled = Boolean.parseBoolean(configDao.getValue(Config.StorageCacheReplacementEnabled.key()));
        cacheReplaceMentInterval = NumbersUtil.parseInt(configDao.getValue(Config.StorageCacheReplacementInterval.key()), 86400);
        workers = NumbersUtil.parseInt(configDao.getValue(Config.ExpungeWorkers.key()), 10);
        executors = Executors.newScheduledThreadPool(workers, new NamedThreadFactory("StorageCacheManager-cache-replacement"));
        return true;
    }

    @Override
    public boolean start() {
        if (cacheReplacementEnabled) {
            final Random generator = new Random();
            final int initalDelay = generator.nextInt(cacheReplaceMentInterval);
            executors.scheduleWithFixedDelay(new CacheReplacementRunner(), initalDelay, cacheReplaceMentInterval, TimeUnit.SECONDS);
        }
        return true;
    }

    @Override
    public boolean stop() {
        // TODO Auto-generated method stub
        return true;
    }

    protected class CacheReplacementRunner extends ManagedContextRunnable {

        @Override
        protected void runInContext() {
            GlobalLock replacementLock = null;
            try {
                replacementLock = GlobalLock.getInternLock("storageCacheMgr.replacement");
                if (replacementLock.lock(3)) {
                    final List<DataStore> stores = getCacheStores();
                    Collections.shuffle(stores);
                    DataObject object = null;
                    DataStore findAStore = null;
                    for (final DataStore store : stores) {
                        object = cacheReplacementAlgorithm.chooseOneToBeReplaced(store);
                        findAStore = store;
                        if (object != null) {
                            break;
                        }
                    }

                    if (object == null) {
                        return;
                    }

                    while (object != null) {
                        object.delete();
                        object = cacheReplacementAlgorithm.chooseOneToBeReplaced(findAStore);
                    }
                }
            } catch (final Exception e) {
                s_logger.debug("Failed to execute CacheReplacementRunner: " + e.toString());
            } finally {
                if (replacementLock != null) {
                    replacementLock.unlock();
                }
            }
        }
    }

    @Override
    public DataObject createCacheObject(final DataObject data, final DataStore store) {
        DataObject objOnCacheStore = null;
        final Object lock;
        final DataObjectType type = data.getType();
        final String typeName;
        final long storeId = store.getId();
        final long dataId = data.getId();

        /*
         * Make sure any thread knows own lock type.
         */
        if (type == DataObjectType.TEMPLATE) {
            lock = templateLock;
            typeName = "template";
        } else if (type == DataObjectType.VOLUME) {
            lock = volumeLock;
            typeName = "volume";
        } else if (type == DataObjectType.SNAPSHOT) {
            lock = snapshotLock;
            typeName = "snapshot";
        } else {
            final String msg = "unsupported DataObject comes, then can't acquire correct lock object";
            throw new CloudRuntimeException(msg);
        }
        s_logger.debug("check " + typeName + " cache entry(id: " + dataId + ") on store(id: " + storeId + ")");

        DataObject existingDataObj = null;
        synchronized (lock) {
            DataObjectInStore obj = objectInStoreMgr.findObject(data, store);
            if (obj != null) {
                State st = obj.getState();

                final long miliSeconds = 10000;
                final long timeoutSeconds = 3600;
                final long timeoutMiliSeconds = timeoutSeconds * 1000;
                Date now = new Date();
                final long expiredEpoch = now.getTime() + timeoutMiliSeconds;
                final Date expiredDate = new Date(expiredEpoch);

                /*
                 * Waiting for completion of cache copy.
                 */
                while (st == ObjectInDataStoreStateMachine.State.Allocated ||
                        st == ObjectInDataStoreStateMachine.State.Creating ||
                        st == ObjectInDataStoreStateMachine.State.Copying) {

                    /*
                     * Threads must release lock within waiting for cache copy and
                     * must be waken up at completion.
                     */
                    s_logger.debug("waiting cache copy completion type: " + typeName + ", id: " + obj.getObjectId() + ", lock: " + lock.hashCode());
                    try {
                        lock.wait(miliSeconds);
                    } catch (final InterruptedException e) {
                        s_logger.debug("[ignored] interupted while waiting for cache copy completion.");
                    }
                    s_logger.debug("waken up");

                    now = new Date();
                    if (now.after(expiredDate)) {
                        final String msg = "Waiting time exceeds timeout limit(" + timeoutSeconds + " s)";
                        throw new CloudRuntimeException(msg);
                    }

                    obj = objectInStoreMgr.findObject(data, store);
                    st = obj.getState();
                }

                if (st == ObjectInDataStoreStateMachine.State.Ready) {
                    s_logger.debug("there is already one in the cache store");
                    final DataObject dataObj = objectInStoreMgr.get(data, store);
                    dataObj.incRefCount();
                    existingDataObj = dataObj;
                }
            }

            if (existingDataObj == null) {
                s_logger.debug("create " + typeName + " cache entry(id: " + dataId + ") on store(id: " + storeId + ")");
                objOnCacheStore = store.create(data);
            }
            lock.notifyAll();
        }
        if (existingDataObj != null) {
            return existingDataObj;
        }
        if (objOnCacheStore == null) {
            s_logger.error("create " + typeName + " cache entry(id: " + dataId + ") on store(id: " + storeId + ") failed");
            return null;
        }

        final AsyncCallFuture<CopyCommandResult> future = new AsyncCallFuture<>();
        CopyCommandResult result = null;
        try {
            objOnCacheStore.processEvent(Event.CreateOnlyRequested);

            dataMotionSvr.copyAsync(data, objOnCacheStore, future);
            result = future.get();

            if (result.isFailed()) {
                objOnCacheStore.processEvent(Event.OperationFailed);
            } else {
                objOnCacheStore.processEvent(Event.OperationSuccessed, result.getAnswer());
                objOnCacheStore.incRefCount();
                return objOnCacheStore;
            }
        } catch (final InterruptedException e) {
            s_logger.debug("create cache storage failed: " + e.toString());
            throw new CloudRuntimeException(e);
        } catch (final ExecutionException e) {
            s_logger.debug("create cache storage failed: " + e.toString());
            throw new CloudRuntimeException(e);
        } finally {
            if (result == null) {
                objOnCacheStore.processEvent(Event.OperationFailed);
            }
            synchronized (lock) {
                /*
                 * Wake up all threads waiting for cache copy.
                 */
                s_logger.debug("wake up all waiting threads(lock: " + lock.hashCode() + ")");
                lock.notifyAll();
            }
        }
        return null;
    }

    @Override
    public DataObject createCacheObject(final DataObject data, final Scope scope) {
        final DataStore cacheStore = getCacheStorage(scope);

        if (cacheStore == null) {
            final String errMsg = "No cache DataStore in scope id " + scope.getScopeId() + " type " + scope.getScopeType().toString();
            throw new CloudRuntimeException(errMsg);
        }
        return this.createCacheObject(data, cacheStore);
    }

    @Override
    public DataObject getCacheObject(final DataObject data, final Scope scope) {
        final DataStore cacheStore = getCacheStorage(scope);
        final DataObject objOnCacheStore = cacheStore.create(data);
        objOnCacheStore.incRefCount();
        return objOnCacheStore;
    }

    @Override
    public boolean releaseCacheObject(final DataObject data) {
        data.decRefCount();
        return true;
    }

    @Override
    public boolean deleteCacheObject(final DataObject data) {
        return data.getDataStore().delete(data);
    }
}
