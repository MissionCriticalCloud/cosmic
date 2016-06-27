package com.cloud.dc.dao;

import com.cloud.dc.DataCenterVnetVO;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.db.TransactionLegacy;

import java.util.List;

public interface DataCenterVnetDao extends GenericDao<DataCenterVnetVO, Long> {
    public List<DataCenterVnetVO> listAllocatedVnets(long physicalNetworkId);

    public List<DataCenterVnetVO> listAllocatedVnetsInRange(long dcId, long physicalNetworkId, Integer start, Integer end);

    public List<DataCenterVnetVO> findVnet(long dcId, String vnet);

    public int countZoneVlans(long dcId, boolean onlyCountAllocated);

    public List<DataCenterVnetVO> findVnet(long dcId, long physicalNetworkId, String vnet);

    public void add(long dcId, long physicalNetworkId, List<String> vnets);

    public void delete(long physicalNetworkId);

    public void deleteVnets(TransactionLegacy txn, long dcId, long physicalNetworkId, List<String> vnets);

    public void lockRange(long dcId, long physicalNetworkId, Integer start, Integer end);

    public DataCenterVnetVO take(long physicalNetworkId, long accountId, String reservationId, List<Long> vlanDbIds);

    public void release(String vnet, long physicalNetworkId, long accountId, String reservationId);

    public void releaseDedicatedGuestVlans(Long dedicatedGuestVlanRangeId);

    public int countVnetsAllocatedToAccount(long dcId, long accountId);

    public int countVnetsDedicatedToAccount(long dcId, long accountId);

    List<String> listVnetsByPhysicalNetworkAndDataCenter(long dcId, long physicalNetworkId);

    int countAllocatedVnets(long physicalNetworkId);
}
