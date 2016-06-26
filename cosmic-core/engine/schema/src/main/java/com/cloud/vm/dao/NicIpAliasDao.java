package com.cloud.vm.dao;

import com.cloud.utils.db.GenericDao;
import com.cloud.vm.NicIpAlias;

import java.util.List;

public interface NicIpAliasDao extends GenericDao<NicIpAliasVO, Long> {
    List<NicIpAliasVO> listByVmId(long instanceId);

    List<String> listAliasIpAddressInNetwork(long networkConfigId);

    List<NicIpAliasVO> listByNetworkId(long networkId);

    NicIpAliasVO findByInstanceIdAndNetworkId(long networkId, long instanceId);

    NicIpAliasVO findByIp4AddressAndNetworkId(String ip4Address, long networkId);

    /**
     * @param networkId
     * @param instanceId
     * @return
     */

    List<NicIpAliasVO> getAliasIpForVm(long vmId);

    List<NicIpAliasVO> listByNicId(long nicId);

    List<NicIpAliasVO> listByNicIdAndVmid(long nicId, long vmId);

    NicIpAliasVO findByIp4AddressAndNicId(String ip4Address, long nicId);

    NicIpAliasVO findByIp4AddressAndNetworkIdAndInstanceId(long networkId, Long vmId, String vmIp);

    List<String> getAliasIpAddressesForNic(long nicId);

    Integer countAliasIps(long nicId);

    public NicIpAliasVO findByIp4AddressAndVmId(String ip4Address, long vmId);

    NicIpAliasVO findByGatewayAndNetworkIdAndState(String gateway, long networkId, NicIpAlias.State state);

    List<NicIpAliasVO> listByNetworkIdAndState(long networkId, NicIpAlias.State state);
}
