package com.cloud.vm.dao;

import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface NicSecondaryIpDao extends GenericDao<NicSecondaryIpVO, Long> {
    List<NicSecondaryIpVO> listByVmId(long instanceId);

    List<String> listSecondaryIpAddressInNetwork(long networkConfigId);

    List<NicSecondaryIpVO> listByNetworkId(long networkId);

    NicSecondaryIpVO findByInstanceIdAndNetworkId(long networkId, long instanceId);

    //    void removeNicsForInstance(long instanceId);
    //    void removeSecondaryIpForNic(long nicId);

    NicSecondaryIpVO findByIp4AddressAndNetworkId(String ip4Address, long networkId);

    /**
     * @param networkId
     * @param instanceId
     * @return
     */

    List<NicSecondaryIpVO> getSecondaryIpAddressesForVm(long vmId);

    List<NicSecondaryIpVO> listByNicId(long nicId);

    List<NicSecondaryIpVO> listByNicIdAndVmid(long nicId, long vmId);

    NicSecondaryIpVO findByIp4AddressAndNicId(String ip4Address, long nicId);

    NicSecondaryIpVO findByIp4AddressAndNetworkIdAndInstanceId(long networkId, Long vmId, String vmIp);

    List<String> getSecondaryIpAddressesForNic(long nicId);

    Long countByNicId(long nicId);
}
