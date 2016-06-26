package com.cloud.dc.dao;

import com.cloud.dc.DataCenterIpAddressVO;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.DataCenterVnetVO;
import com.cloud.utils.Pair;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface DataCenterDao extends GenericDao<DataCenterVO, Long> {
    DataCenterVO findByName(String name);

    /**
     * @param id data center id
     * @return a pair of mac address strings.  The first one is private and second is public.
     */
    String[] getNextAvailableMacAddressPair(long id);

    String[] getNextAvailableMacAddressPair(long id, long mask);

    Pair<String, Long> allocatePrivateIpAddress(long id, long podId, long instanceId, String reservationId);

    DataCenterIpAddressVO allocatePrivateIpAddress(long id, String reservationId);

    String allocateLinkLocalIpAddress(long id, long podId, long instanceId, String reservationId);

    String allocateVnet(long dcId, long physicalNetworkId, long accountId, String reservationId, boolean canUseSystemGuestVlans);

    void releaseVnet(String vnet, long dcId, long physicalNetworkId, long accountId, String reservationId);

    void releasePrivateIpAddress(String ipAddress, long dcId, Long instanceId);

    void releasePrivateIpAddress(long nicId, String reservationId);

    void releaseLinkLocalIpAddress(String ipAddress, long dcId, Long instanceId);

    void releaseLinkLocalIpAddress(long nicId, String reservationId);

    boolean deletePrivateIpAddressByPod(long podId);

    boolean deleteLinkLocalIpAddressByPod(long podId);

    void addPrivateIpAddress(long dcId, long podId, String start, String end);

    void addLinkLocalIpAddress(long dcId, long podId, String start, String end);

    List<DataCenterVnetVO> findVnet(long dcId, long physicalNetworkId, String vnet);

    String allocatePodVlan(long podId, long accountId);

    List<DataCenterVO> findZonesByDomainId(Long domainId);

    List<DataCenterVO> listPublicZones(String keyword);

    List<DataCenterVO> findChildZones(Object[] ids, String keyword);

    void loadDetails(DataCenterVO zone);

    void saveDetails(DataCenterVO zone);

    List<DataCenterVO> listDisabledZones();

    List<DataCenterVO> listEnabledZones();

    DataCenterVO findByToken(String zoneToken);

    DataCenterVO findByTokenOrIdOrName(String tokenIdOrName);

    int countZoneVlans(long dcId, boolean onlyCountAllocated);

    void addVnet(long dcId, long physicalNetworkId, List<String> vnets);

    void deleteVnet(long physicalNetworkId);

    List<DataCenterVnetVO> listAllocatedVnets(long physicalNetworkId);

    List<DataCenterVO> findZonesByDomainId(Long domainId, String keyword);

    List<DataCenterVO> findByKeyword(String keyword);

    List<DataCenterVO> listAllZones();
}
