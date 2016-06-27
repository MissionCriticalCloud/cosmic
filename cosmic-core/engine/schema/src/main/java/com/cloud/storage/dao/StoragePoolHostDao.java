package com.cloud.storage.dao;

import com.cloud.host.Status;
import com.cloud.storage.StoragePoolHostVO;
import com.cloud.utils.Pair;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface StoragePoolHostDao extends GenericDao<StoragePoolHostVO, Long> {
    public List<StoragePoolHostVO> listByPoolId(long id);

    public List<StoragePoolHostVO> listByHostIdIncludingRemoved(long hostId);

    public StoragePoolHostVO findByPoolHost(long poolId, long hostId);

    List<StoragePoolHostVO> listByHostStatus(long poolId, Status hostStatus);

    List<Pair<Long, Integer>> getDatacenterStoragePoolHostInfo(long dcId, boolean sharedOnly);

    public void deletePrimaryRecordsForHost(long hostId);

    public void deleteStoragePoolHostDetails(long hostId, long poolId);

    List<StoragePoolHostVO> listByHostId(long hostId);
}
