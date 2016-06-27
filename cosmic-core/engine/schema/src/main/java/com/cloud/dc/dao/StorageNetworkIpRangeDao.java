package com.cloud.dc.dao;

import com.cloud.dc.StorageNetworkIpRangeVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface StorageNetworkIpRangeDao extends GenericDao<StorageNetworkIpRangeVO, Long> {
    List<StorageNetworkIpRangeVO> listByRangeId(long rangeId);

    List<StorageNetworkIpRangeVO> listByPodId(long podId);

    List<StorageNetworkIpRangeVO> listByDataCenterId(long dcId);

    long countRanges();
}
