package com.cloud.storage.listener;

import com.cloud.agent.api.StartupRoutingCommand;
import com.cloud.host.HostVO;
import com.cloud.legacymodel.exceptions.ConnectionException;
import com.cloud.legacymodel.exceptions.StorageUnavailableException;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.model.enumeration.StoragePoolStatus;
import com.cloud.storage.ScopeType;
import com.cloud.storage.StorageManagerImpl;
import com.cloud.storage.datastore.db.PrimaryDataStoreDao;
import com.cloud.storage.datastore.db.StoragePoolVO;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class StoragePoolMonitorTest {

    private StorageManagerImpl storageManager;
    private PrimaryDataStoreDao poolDao;
    private StoragePoolMonitor storagePoolMonitor;
    private HostVO host;
    private StoragePoolVO pool;
    private StartupRoutingCommand cmd;

    @Before
    public void setUp() throws Exception {
        storageManager = Mockito.mock(StorageManagerImpl.class);
        poolDao = Mockito.mock(PrimaryDataStoreDao.class);

        storagePoolMonitor = new StoragePoolMonitor(storageManager, poolDao);
        host = new HostVO("some-uuid");
        pool = new StoragePoolVO();
        pool.setScope(ScopeType.CLUSTER);
        pool.setStatus(StoragePoolStatus.Up);
        pool.setId(123L);
        cmd = new StartupRoutingCommand();
        cmd.setHypervisorType(HypervisorType.KVM);
    }

    @Test
    public void testProcessConnectStoragePoolNormal() throws Exception {
        Mockito.when(poolDao.listBy(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), Mockito.any(ScopeType.class))).thenReturn(Collections.singletonList(pool));
        Mockito.when(poolDao.findZoneWideStoragePoolsByTags(Mockito.anyLong(), Mockito.any(String[].class))).thenReturn(Collections.<StoragePoolVO>emptyList());
        Mockito.when(poolDao.findZoneWideStoragePoolsByHypervisor(Mockito.anyLong(), Mockito.any(HypervisorType.class))).thenReturn(Collections.<StoragePoolVO>emptyList());

        storagePoolMonitor.processConnect(host, cmd, false);

        Mockito.verify(storageManager, Mockito.times(1)).connectHostToSharedPool(Mockito.eq(host.getId()), Mockito.eq(pool.getId()));
        Mockito.verify(storageManager, Mockito.times(1)).createCapacityEntry(Mockito.eq(pool.getId()));
    }

    @Test(expected = ConnectionException.class)
    public void testProcessConnectStoragePoolFailureOnHost() throws Exception {
        Mockito.when(poolDao.listBy(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), Mockito.any(ScopeType.class))).thenReturn(Collections.singletonList(pool));
        Mockito.when(poolDao.findZoneWideStoragePoolsByTags(Mockito.anyLong(), Mockito.any(String[].class))).thenReturn(Collections.<StoragePoolVO>emptyList());
        Mockito.when(poolDao.findZoneWideStoragePoolsByHypervisor(Mockito.anyLong(), Mockito.any(HypervisorType.class))).thenReturn(Collections.<StoragePoolVO>emptyList());
        Mockito.doThrow(new StorageUnavailableException("unable to mount storage", 123L)).when(storageManager).connectHostToSharedPool(Mockito.anyLong(), Mockito.anyLong());

        storagePoolMonitor.processConnect(host, cmd, false);
    }
}
