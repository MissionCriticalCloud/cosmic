package com.cloud.storage.dao;

import com.cloud.legacymodel.storage.Volume;
import com.cloud.legacymodel.utils.Pair;
import com.cloud.model.enumeration.DiskControllerType;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.model.enumeration.ImageFormat;
import com.cloud.model.enumeration.VolumeType;
import com.cloud.storage.ScopeType;
import com.cloud.storage.VolumeVO;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.fsm.StateDao;

import java.util.Date;
import java.util.List;

public interface VolumeDao extends GenericDao<VolumeVO, Long>, StateDao<Volume.State, Volume.Event, Volume> {

    List<VolumeVO> findDetachedByAccount(long accountId);

    List<VolumeVO> findByAccount(long accountId);

    List<VolumeVO> findIncludingRemovedByAccount(long accountId);

    Pair<Long, Long> getCountAndTotalByPool(long poolId);

    Pair<Long, Long> getNonDestroyedCountAndTotalByPool(long poolId);

    long getVMSnapshotSizeByPool(long poolId);

    List<VolumeVO> findByInstance(long id);

    List<VolumeVO> findByInstanceAndType(long id, VolumeType vType);

    List<VolumeVO> findByInstanceIdDestroyed(long vmId);

    List<VolumeVO> findByPod(long podId);

    List<VolumeVO> findByDc(long dcId);

    List<VolumeVO> findByAccountAndPod(long accountId, long podId);

    List<VolumeVO> findByTemplateAndZone(long templateId, long zoneId);

    void deleteVolumesByInstance(long instanceId);

    void attachVolume(long volumeId, long vmId, long deviceId, DiskControllerType diskController);

    void detachVolume(long volumeId);

    boolean isAnyVolumeActivelyUsingTemplateOnPool(long templateId, long poolId);

    List<VolumeVO> findCreatedByInstance(long id);

    List<VolumeVO> findByPoolId(long poolId);

    VolumeVO findByPoolIdName(long poolId, String name);

    List<VolumeVO> findByPoolId(long poolId, VolumeType volumeType);

    List<VolumeVO> findByInstanceAndDeviceId(long instanceId, long deviceId);

    List<VolumeVO> findUsableVolumesForInstance(long instanceId);

    Long countAllocatedVolumesForAccount(long accountId);

    HypervisorType getHypervisorType(long volumeId);

    List<VolumeVO> listVolumesToBeDestroyed();

    List<VolumeVO> listNonRootVolumesToBeDestroyed(Date date);

    ImageFormat getImageFormat(Long volumeId);

    List<VolumeVO> findReadyRootVolumesByInstance(long instanceId);

    List<Long> listPoolIdsByVolumeCount(long dcId, Long podId, Long clusterId, long accountId);

    List<Long> listZoneWidePoolIdsByVolumeCount(long dcId, long accountId);

    /**
     * Gets the Total Primary Storage space allocated for an account
     *
     * @param account
     * @param list    of ids of virtual router VMs under this account
     * @return total Primary Storage space (in bytes) used
     */
    long primaryStorageUsedForAccount(long accountId, List<Long> virtualRouters);

    /**
     * Gets the Total Secondary Storage space used by volumes allocated for an
     * account
     *
     * @param account
     * @return total Secondary Storage space (in bytes) used
     */
    long secondaryStorageUsedForAccount(long accountId);

    /***
     * @param volumeId
     * @return the scope of the storage pool where the volume is present (ZONE/CLUSTER)
     */
    ScopeType getVolumeStoragePoolScope(long volumeId);

    /***
     * Updates the destVol uuid with srcVol uuid and sets the srcVol uuid as null.
     *
     * @param srcVolId
     * @param destVolId
     * @return returns true if transaction is successful.
     */
    boolean updateUuid(long srcVolId, long destVolId);
}
