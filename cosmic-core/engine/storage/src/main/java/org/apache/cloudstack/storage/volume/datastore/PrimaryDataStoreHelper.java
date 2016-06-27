package org.apache.cloudstack.storage.volume.datastore;

import com.cloud.agent.api.StoragePoolInfo;
import com.cloud.capacity.Capacity;
import com.cloud.capacity.CapacityVO;
import com.cloud.capacity.dao.CapacityDao;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.DataStoreRole;
import com.cloud.storage.ScopeType;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.storage.StorageManager;
import com.cloud.storage.StoragePoolHostVO;
import com.cloud.storage.StoragePoolStatus;
import com.cloud.storage.dao.StoragePoolHostDao;
import com.cloud.utils.crypt.DBEncryptionUtil;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.engine.subsystem.api.storage.HostScope;
import org.apache.cloudstack.engine.subsystem.api.storage.PrimaryDataStoreParameters;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.StoragePoolVO;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PrimaryDataStoreHelper {
    private static final Logger s_logger = LoggerFactory.getLogger(PrimaryDataStoreHelper.class);
    @Inject
    protected CapacityDao _capacityDao;
    @Inject
    protected StoragePoolHostDao storagePoolHostDao;
    @Inject
    DataStoreManager dataStoreMgr;
    @Inject
    StorageManager storageMgr;
    @Inject
    private PrimaryDataStoreDao dataStoreDao;

    public DataStore createPrimaryDataStore(final PrimaryDataStoreParameters params) {
        if (params == null) {
            throw new InvalidParameterValueException("createPrimaryDataStore: Input params is null, please check");
        }
        StoragePoolVO dataStoreVO = dataStoreDao.findPoolByUUID(params.getUuid());
        if (dataStoreVO != null) {
            throw new CloudRuntimeException("duplicate uuid: " + params.getUuid());
        }
        dataStoreVO = new StoragePoolVO();
        dataStoreVO.setStorageProviderName(params.getProviderName());
        dataStoreVO.setHostAddress(params.getHost());
        dataStoreVO.setPoolType(params.getType());
        dataStoreVO.setPath(params.getPath());
        dataStoreVO.setPort(params.getPort());
        dataStoreVO.setName(params.getName());
        dataStoreVO.setUuid(params.getUuid());
        dataStoreVO.setDataCenterId(params.getZoneId());
        dataStoreVO.setPodId(params.getPodId());
        dataStoreVO.setClusterId(params.getClusterId());
        dataStoreVO.setStatus(StoragePoolStatus.Initialized);
        dataStoreVO.setUserInfo(params.getUserInfo());
        dataStoreVO.setManaged(params.isManaged());
        dataStoreVO.setCapacityIops(params.getCapacityIops());
        dataStoreVO.setCapacityBytes(params.getCapacityBytes());
        dataStoreVO.setUsedBytes(params.getUsedBytes());
        dataStoreVO.setHypervisor(params.getHypervisorType());

        final Map<String, String> details = params.getDetails();
        if (params.getType() == StoragePoolType.SMB && details != null) {
            final String user = details.get("user");
            String password = details.get("password");
            final String domain = details.get("domain");
            String updatedPath = params.getPath();

            if (user == null || password == null) {
                final String errMsg = "Missing cifs user and password details. Add them as details parameter.";
                s_logger.warn(errMsg);
                throw new InvalidParameterValueException(errMsg);
            } else {
                try {
                    password = DBEncryptionUtil.encrypt(URLEncoder.encode(password, "UTF-8"));
                    details.put("password", password);
                    updatedPath += "?user=" + user + "&password=" + password + "&domain=" + domain;
                } catch (final UnsupportedEncodingException e) {
                    throw new CloudRuntimeException("Error while generating the cifs url. " + e.getMessage());
                }
            }

            dataStoreVO.setPath(updatedPath);
        }
        final String tags = params.getTags();
        if (tags != null) {
            final String[] tokens = tags.split(",");

            for (String tag : tokens) {
                tag = tag.trim();
                if (tag.length() == 0) {
                    continue;
                }
                details.put(tag, "true");
            }
        }
        dataStoreVO = dataStoreDao.persist(dataStoreVO, details);
        return dataStoreMgr.getDataStore(dataStoreVO.getId(), DataStoreRole.Primary);
    }

    public DataStore attachHost(final DataStore store, final HostScope scope, final StoragePoolInfo existingInfo) {
        StoragePoolHostVO poolHost = storagePoolHostDao.findByPoolHost(store.getId(), scope.getScopeId());
        if (poolHost == null) {
            poolHost = new StoragePoolHostVO(store.getId(), scope.getScopeId(), existingInfo.getLocalPath());
            storagePoolHostDao.persist(poolHost);
        }

        final StoragePoolVO pool = this.dataStoreDao.findById(store.getId());
        pool.setScope(scope.getScopeType());
        pool.setUsedBytes(existingInfo.getCapacityBytes() - existingInfo.getAvailableBytes());
        pool.setCapacityBytes(existingInfo.getCapacityBytes());
        pool.setStatus(StoragePoolStatus.Up);
        this.dataStoreDao.update(pool.getId(), pool);
        this.storageMgr.createCapacityEntry(pool, Capacity.CAPACITY_TYPE_LOCAL_STORAGE, pool.getUsedBytes());
        return dataStoreMgr.getDataStore(pool.getId(), DataStoreRole.Primary);
    }

    public DataStore attachCluster(final DataStore store) {
        final StoragePoolVO pool = this.dataStoreDao.findById(store.getId());

        storageMgr.createCapacityEntry(pool.getId());

        pool.setScope(ScopeType.CLUSTER);
        pool.setStatus(StoragePoolStatus.Up);
        this.dataStoreDao.update(pool.getId(), pool);
        return dataStoreMgr.getDataStore(store.getId(), DataStoreRole.Primary);
    }

    public DataStore attachZone(final DataStore store) {
        final StoragePoolVO pool = this.dataStoreDao.findById(store.getId());
        pool.setScope(ScopeType.ZONE);
        pool.setStatus(StoragePoolStatus.Up);
        this.dataStoreDao.update(pool.getId(), pool);
        return dataStoreMgr.getDataStore(store.getId(), DataStoreRole.Primary);
    }

    public DataStore attachZone(final DataStore store, final HypervisorType hypervisor) {
        final StoragePoolVO pool = this.dataStoreDao.findById(store.getId());
        pool.setScope(ScopeType.ZONE);
        pool.setHypervisor(hypervisor);
        pool.setStatus(StoragePoolStatus.Up);
        this.dataStoreDao.update(pool.getId(), pool);
        return dataStoreMgr.getDataStore(store.getId(), DataStoreRole.Primary);
    }

    public boolean maintain(final DataStore store) {
        final StoragePoolVO pool = this.dataStoreDao.findById(store.getId());
        pool.setStatus(StoragePoolStatus.Maintenance);
        this.dataStoreDao.update(pool.getId(), pool);
        return true;
    }

    public boolean cancelMaintain(final DataStore store) {
        final StoragePoolVO pool = this.dataStoreDao.findById(store.getId());
        pool.setStatus(StoragePoolStatus.Up);
        dataStoreDao.update(store.getId(), pool);
        return true;
    }

    public boolean disable(final DataStore store) {
        final StoragePoolVO pool = this.dataStoreDao.findById(store.getId());
        pool.setStatus(StoragePoolStatus.Disabled);
        this.dataStoreDao.update(pool.getId(), pool);
        return true;
    }

    public boolean enable(final DataStore store) {
        final StoragePoolVO pool = this.dataStoreDao.findById(store.getId());
        pool.setStatus(StoragePoolStatus.Up);
        dataStoreDao.update(pool.getId(), pool);
        return true;
    }

    public boolean deletePrimaryDataStore(final DataStore store) {
        final List<StoragePoolHostVO> hostPoolRecords = this.storagePoolHostDao.listByPoolId(store.getId());
        final StoragePoolVO poolVO = this.dataStoreDao.findById(store.getId());
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        for (final StoragePoolHostVO host : hostPoolRecords) {
            storagePoolHostDao.deleteStoragePoolHostDetails(host.getHostId(), host.getPoolId());
        }
        poolVO.setUuid(null);
        this.dataStoreDao.update(poolVO.getId(), poolVO);
        dataStoreDao.remove(poolVO.getId());
        deletePoolStats(poolVO.getId());
        // Delete op_host_capacity entries
        this._capacityDao.removeBy(Capacity.CAPACITY_TYPE_STORAGE_ALLOCATED, null, null, null, poolVO.getId());
        txn.commit();

        s_logger.debug("Storage pool id=" + poolVO.getId() + " is removed successfully");
        return true;
    }

    protected boolean deletePoolStats(final Long poolId) {
        final CapacityVO capacity1 = _capacityDao.findByHostIdType(poolId, Capacity.CAPACITY_TYPE_STORAGE);
        final CapacityVO capacity2 = _capacityDao.findByHostIdType(poolId, Capacity.CAPACITY_TYPE_STORAGE_ALLOCATED);
        if (capacity1 != null) {
            _capacityDao.remove(capacity1.getId());
        }

        if (capacity2 != null) {
            _capacityDao.remove(capacity2.getId());
        }

        return true;
    }
}
