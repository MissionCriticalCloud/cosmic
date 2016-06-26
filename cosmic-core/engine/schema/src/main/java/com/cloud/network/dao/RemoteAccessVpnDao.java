package com.cloud.network.dao;

import com.cloud.network.RemoteAccessVpn;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface RemoteAccessVpnDao extends GenericDao<RemoteAccessVpnVO, Long> {
    RemoteAccessVpnVO findByPublicIpAddress(long ipAddressId);

    RemoteAccessVpnVO findByPublicIpAddressAndState(long ipAddressId, RemoteAccessVpn.State state);

    RemoteAccessVpnVO findByAccountAndNetwork(Long accountId, Long networkId);

    RemoteAccessVpnVO findByAccountAndVpc(Long accountId, Long vpcId);

    List<RemoteAccessVpnVO> findByAccount(Long accountId);

    List<RemoteAccessVpnVO> listByNetworkId(Long networkId);
}
