package com.cloud.network.dao;

import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface NetworkExternalFirewallDao extends GenericDao<NetworkExternalFirewallVO, Long> {

    /**
     * find the network to firewall device mapping corresponding to a network
     *
     * @param lbDeviceId guest network Id
     * @return return NetworkExternalFirewallDao for the guest network
     */
    NetworkExternalFirewallVO findByNetworkId(long networkId);

    /**
     * list all network to firewall device mappings corresponding to a firewall device Id
     *
     * @param lbDeviceId firewall device Id
     * @return list of NetworkExternalFirewallVO mappings corresponding to the networks mapped to the firewall device
     */
    List<NetworkExternalFirewallVO> listByFirewallDeviceId(long lbDeviceId);
}
