package com.cloud.dc.dao;

import com.cloud.dc.DataCenterLinkLocalIpAddressVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface DataCenterLinkLocalIpAddressDao extends GenericDao<DataCenterLinkLocalIpAddressVO, Long> {
    DataCenterLinkLocalIpAddressVO takeIpAddress(long dcId, long podId, long instanceId, String reservationId);

    boolean deleteIpAddressByPod(long podId);

    void addIpRange(long dcId, long podId, String start, String end);

    void releaseIpAddress(String ipAddress, long dcId, long instanceId);

    void releaseIpAddress(long nicId, String reservationId);

    List<DataCenterLinkLocalIpAddressVO> listByPodIdDcId(long podId, long dcId);

    int countIPs(long podId, long dcId, boolean onlyCountAllocated);
}
