package com.cloud.dc.dao;

import com.cloud.dc.DataCenterIpAddressVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface DataCenterIpAddressDao extends GenericDao<DataCenterIpAddressVO, Long> {

    public DataCenterIpAddressVO takeIpAddress(long dcId, long podId, long instanceId, String reservationId);

    public DataCenterIpAddressVO takeDataCenterIpAddress(long dcId, String reservationId);

    public void addIpRange(long dcId, long podId, String start, String end);

    public void releaseIpAddress(String ipAddress, long dcId, Long instanceId);

    public void releaseIpAddress(long nicId, String reservationId);

    public void releaseIpAddress(long nicId);

    boolean mark(long dcId, long podId, String ip);

    List<DataCenterIpAddressVO> listByPodIdDcIdIpAddress(long podId, long dcId, String ipAddress);

    List<DataCenterIpAddressVO> listByPodIdDcId(long podId, long dcId);

    int countIPs(long podId, long dcId, boolean onlyCountAllocated);

    int countIPs(long dcId, boolean onlyCountAllocated);

    boolean deleteIpAddressByPod(long podId);
}
