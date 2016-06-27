package com.cloud.network.rules.dao;

import com.cloud.network.rules.PortForwardingRuleVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface PortForwardingRulesDao extends GenericDao<PortForwardingRuleVO, Long> {
    List<PortForwardingRuleVO> listForApplication(long ipId);

    /**
     * Find all port forwarding rules for the ip address that have not been revoked.
     *
     * @param ip ip address
     * @return List of PortForwardingRuleVO
     */
    List<PortForwardingRuleVO> listByIpAndNotRevoked(long ipId);

    List<PortForwardingRuleVO> listByNetworkAndNotRevoked(long networkId);

    List<PortForwardingRuleVO> listByIp(long ipId);

    List<PortForwardingRuleVO> listByVm(Long vmId);

    List<PortForwardingRuleVO> listByNetwork(long networkId);

    List<PortForwardingRuleVO> listByAccount(long accountId);

    List<PortForwardingRuleVO> listByDestIpAddr(String ip4Address);

    PortForwardingRuleVO findByIdAndIp(long id, String secondaryIp);

    List<PortForwardingRuleVO> listByNetworkAndDestIpAddr(String ip4Address, long networkId);
}
