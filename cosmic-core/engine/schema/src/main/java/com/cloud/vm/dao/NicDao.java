package com.cloud.vm.dao;

import com.cloud.utils.db.GenericDao;
import com.cloud.vm.NicVO;
import com.cloud.vm.VirtualMachine;

import java.net.URI;
import java.util.List;

public interface NicDao extends GenericDao<NicVO, Long> {
    List<NicVO> listByVmId(long instanceId);

    List<NicVO> listByIpAddress(final String ipAddress);

    List<NicVO> listByMacAddress(final String macAddress);

    List<String> listIpAddressInNetwork(long networkConfigId);

    List<NicVO> listByVmIdIncludingRemoved(long instanceId);

    List<NicVO> listByNetworkId(long networkId);

    NicVO findByNtwkIdAndInstanceId(long networkId, long instanceId);

    NicVO findByInstanceIdAndNetworkIdIncludingRemoved(long networkId, long instanceId);

    NicVO findByNetworkIdTypeAndGateway(long networkId, VirtualMachine.Type vmType, String gateway);

    void removeNicsForInstance(long instanceId);

    NicVO findByNetworkIdAndType(long networkId, VirtualMachine.Type vmType);

    NicVO findByIp4AddressAndNetworkId(String ip4Address, long networkId);

    NicVO findByNetworkIdAndMacAddress(long networkId, String mac);

    NicVO findDefaultNicForVM(long instanceId);

    /**
     * @param networkId
     * @param instanceId
     * @return
     */
    NicVO findNonReleasedByInstanceIdAndNetworkId(long networkId, long instanceId);

    String getIpAddress(long networkId, long instanceId);

    NicVO findByNetworkIdInstanceIdAndBroadcastUri(long networkId, long instanceId, String broadcastUri);

    NicVO findByIp4AddressAndNetworkIdAndInstanceId(long networkId, long instanceId, String ip4Address);

    List<NicVO> listByVmIdAndNicIdAndNtwkId(long vmId, Long nicId, Long networkId);

    NicVO findByIp4AddressAndVmId(String ip4Address, long instance);

    List<NicVO> listPlaceholderNicsByNetworkId(long networkId);

    List<NicVO> listPlaceholderNicsByNetworkIdAndVmType(long networkId, VirtualMachine.Type vmType);

    List<NicVO> listByNetworkIdAndVmType(long networkId, VirtualMachine.Type vmType);

    NicVO findByInstanceIdAndIpAddressAndVmtype(long instanceId, String ipaddress, VirtualMachine.Type type);

    List<NicVO> listByNetworkIdTypeAndGatewayAndBroadcastUri(long networkId, VirtualMachine.Type vmType, String gateway, URI broadcastUri);

    int countNicsForStartingVms(long networkId);
}
