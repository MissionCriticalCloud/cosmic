package org.apache.cloudstack.storage.snapshot;

import com.cloud.storage.DataStoreRole;
import com.cloud.storage.SnapshotVO;
import com.cloud.storage.dao.SnapshotDao;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObject;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.engine.subsystem.api.storage.SnapshotDataFactory;
import org.apache.cloudstack.engine.subsystem.api.storage.SnapshotInfo;
import org.apache.cloudstack.engine.subsystem.api.storage.VolumeDataFactory;
import org.apache.cloudstack.storage.datastore.db.SnapshotDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.SnapshotDataStoreVO;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class SnapshotDataFactoryImpl implements SnapshotDataFactory {
    @Inject
    SnapshotDao snapshotDao;
    @Inject
    SnapshotDataStoreDao snapshotStoreDao;
    @Inject
    DataStoreManager storeMgr;
    @Inject
    VolumeDataFactory volumeFactory;

    @Override
    public SnapshotInfo getSnapshot(final long snapshotId, final DataStore store) {
        final SnapshotVO snapshot = snapshotDao.findById(snapshotId);
        final SnapshotObject so = SnapshotObject.getSnapshotObject(snapshot, store);
        return so;
    }

    @Override
    public SnapshotInfo getSnapshot(final DataObject obj, final DataStore store) {
        final SnapshotVO snapshot = snapshotDao.findById(obj.getId());
        if (snapshot == null) {
            throw new CloudRuntimeException("Can't find snapshot: " + obj.getId());
        }
        final SnapshotObject so = SnapshotObject.getSnapshotObject(snapshot, store);
        return so;
    }

    @Override
    public SnapshotInfo getSnapshot(final long snapshotId, final DataStoreRole role) {
        final SnapshotVO snapshot = snapshotDao.findById(snapshotId);
        SnapshotDataStoreVO snapshotStore = snapshotStoreDao.findBySnapshot(snapshotId, role);
        if (snapshotStore == null) {
            snapshotStore = snapshotStoreDao.findByVolume(snapshot.getVolumeId(), role);
            if (snapshotStore == null) {
                return null;
            }
        }
        final DataStore store = storeMgr.getDataStore(snapshotStore.getDataStoreId(), role);
        final SnapshotObject so = SnapshotObject.getSnapshotObject(snapshot, store);
        return so;
    }

    @Override
    public SnapshotInfo getReadySnapshotOnCache(final long snapshotId) {
        final SnapshotDataStoreVO snapStore = snapshotStoreDao.findReadyOnCache(snapshotId);
        if (snapStore != null) {
            final DataStore store = storeMgr.getDataStore(snapStore.getDataStoreId(), DataStoreRole.ImageCache);
            return getSnapshot(snapshotId, store);
        } else {
            return null;
        }
    }

    @Override
    public List<SnapshotInfo> listSnapshotOnCache(final long snapshotId) {
        final List<SnapshotDataStoreVO> cacheSnapshots = snapshotStoreDao.listOnCache(snapshotId);
        final List<SnapshotInfo> snapObjs = new ArrayList<>();
        for (final SnapshotDataStoreVO cacheSnap : cacheSnapshots) {
            final long storeId = cacheSnap.getDataStoreId();
            final DataStore store = storeMgr.getDataStore(storeId, DataStoreRole.ImageCache);
            final SnapshotInfo tmplObj = getSnapshot(snapshotId, store);
            snapObjs.add(tmplObj);
        }
        return snapObjs;
    }
}
