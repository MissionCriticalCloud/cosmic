package com.cloud.storage;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.StoragePoolInfo;
import com.cloud.agent.manager.Commands;
import com.cloud.capacity.CapacityVO;
import com.cloud.engine.subsystem.api.storage.DataStore;
import com.cloud.engine.subsystem.api.storage.HypervisorHostListener;
import com.cloud.exception.ConnectionException;
import com.cloud.exception.StorageConflictException;
import com.cloud.exception.StorageUnavailableException;
import com.cloud.framework.config.ConfigKey;
import com.cloud.host.Host;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.datastore.db.StoragePoolVO;
import com.cloud.utils.Pair;
import com.cloud.vm.VMInstanceVO;

import java.math.BigDecimal;
import java.util.List;

public interface StorageManager extends StorageService {
    static final ConfigKey<Integer> StorageCleanupInterval = new ConfigKey<>(Integer.class, "storage.cleanup.interval", "Advanced", "86400",
            "The interval (in seconds) to wait before running the storage cleanup thread.", false, ConfigKey.Scope.Global, null);
    static final ConfigKey<Integer> StorageCleanupDelay = new ConfigKey<>(Integer.class, "storage.cleanup.delay", "Advanced", "86400",
            "Determines how long (in seconds) to wait before actually expunging destroyed volumes. The default value = the default value of storage.cleanup.interval.", false,
            ConfigKey.Scope.Global, null);
    static final ConfigKey<Boolean> StorageCleanupEnabled = new ConfigKey<>(Boolean.class, "storage.cleanup.enabled", "Advanced", "true",
            "Enables/disables the storage cleanup thread.", false, ConfigKey.Scope.Global, null);

    /**
     * Returns a comma separated list of tags for the specified storage pool
     *
     * @param poolId
     * @return comma separated list of tags
     */
    public String getStoragePoolTags(long poolId);

    Answer sendToPool(long poolId, Command cmd) throws StorageUnavailableException;

    Answer sendToPool(StoragePool pool, Command cmd) throws StorageUnavailableException;

    Answer[] sendToPool(long poolId, Commands cmd) throws StorageUnavailableException;

    Answer[] sendToPool(StoragePool pool, Commands cmds) throws StorageUnavailableException;

    Pair<Long, Answer[]> sendToPool(StoragePool pool, long[] hostIdsToTryFirst, List<Long> hostIdsToAvoid, Commands cmds) throws StorageUnavailableException;

    Pair<Long, Answer> sendToPool(StoragePool pool, long[] hostIdsToTryFirst, List<Long> hostIdsToAvoid, Command cmd) throws StorageUnavailableException;

    /**
     * Checks if a host has running VMs that are using its local storage pool.
     *
     * @return true if local storage is active on the host
     */
    boolean isLocalStorageActiveOnHost(Long hostId);

    /**
     * Cleans up storage pools by removing unused templates.
     *
     * @param recurring - true if this cleanup is part of a recurring garbage collection thread
     */
    void cleanupStorage(boolean recurring);

    String getPrimaryStorageNameLabel(VolumeVO volume);

    void createCapacityEntry(StoragePoolVO storagePool, short capacityType, long allocated);

    Answer sendToPool(StoragePool pool, long[] hostIdsToTryFirst, Command cmd) throws StorageUnavailableException;

    CapacityVO getSecondaryStorageUsedStats(Long hostId, Long zoneId);

    CapacityVO getStoragePoolUsedStats(Long poolId, Long clusterId, Long podId, Long zoneId);

    List<StoragePoolVO> ListByDataCenterHypervisor(long datacenterId, HypervisorType type);

    List<VMInstanceVO> listByStoragePool(long storagePoolId);

    StoragePoolVO findLocalStorageOnHost(long hostId);

    Host updateSecondaryStorage(long secStorageId, String newUrl);

    List<Long> getUpHostsInPool(long poolId);

    void cleanupSecondaryStorage(boolean recurring);

    HypervisorType getHypervisorTypeFromFormat(ImageFormat format);

    boolean storagePoolHasEnoughIops(List<Volume> volume, StoragePool pool);

    boolean storagePoolHasEnoughSpace(List<Volume> volume, StoragePool pool);

    boolean registerHostListener(String providerUuid, HypervisorHostListener listener);

    void connectHostToSharedPool(long hostId, long poolId) throws StorageUnavailableException, StorageConflictException;

    void createCapacityEntry(long poolId);

    DataStore createLocalStorage(Host host, StoragePoolInfo poolInfo) throws ConnectionException;

    BigDecimal getStorageOverProvisioningFactor(Long dcId);

    Long getDiskBytesReadRate(ServiceOfferingVO offering, DiskOfferingVO diskOffering);

    Long getDiskBytesWriteRate(ServiceOfferingVO offering, DiskOfferingVO diskOffering);

    Long getDiskIopsReadRate(ServiceOfferingVO offering, DiskOfferingVO diskOffering);

    Long getDiskIopsWriteRate(ServiceOfferingVO offering, DiskOfferingVO diskOffering);

    void cleanupDownloadUrls();
}
