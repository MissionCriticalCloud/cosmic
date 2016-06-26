package com.cloud.network.dao;

import com.cloud.network.rules.FirewallRule;
import com.cloud.network.rules.FirewallRuleVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

/*
 * Data Access Object for user_ip_address and ip_forwarding tables
 */
public interface FirewallRulesDao extends GenericDao<FirewallRuleVO, Long> {

    List<FirewallRuleVO> listByIpAndPurposeAndNotRevoked(long ipAddressId, FirewallRule.Purpose purpose);

    List<FirewallRuleVO> listByNetworkAndPurposeAndNotRevoked(long networkId, FirewallRule.Purpose purpose);

    boolean setStateToAdd(FirewallRuleVO rule);

    boolean revoke(FirewallRuleVO rule);

    boolean releasePorts(long ipAddressId, String protocol, FirewallRule.Purpose purpose, int[] ports);

    List<FirewallRuleVO> listByIpAndPurpose(long ipAddressId, FirewallRule.Purpose purpose);

    List<FirewallRuleVO> listByNetworkAndPurpose(long networkId, FirewallRule.Purpose purpose);

    List<FirewallRuleVO> listStaticNatByVmId(long vmId);

    List<FirewallRuleVO> listByIpPurposeAndProtocolAndNotRevoked(long ipAddressId, Integer startPort, Integer endPort, String protocol, FirewallRule.Purpose purpose);

    FirewallRuleVO findByRelatedId(long ruleId);

    List<FirewallRuleVO> listSystemRules();

    List<FirewallRuleVO> listByIp(long ipAddressId);

    List<FirewallRuleVO> listByIpAndNotRevoked(long ipAddressId);

    long countRulesByIpId(long sourceIpId);

    long countRulesByIpIdAndState(long sourceIpId, FirewallRule.State state);

    List<FirewallRuleVO> listByNetworkPurposeTrafficTypeAndNotRevoked(long networkId, FirewallRule.Purpose purpose, FirewallRule.TrafficType trafficType);

    List<FirewallRuleVO> listByNetworkPurposeTrafficType(long networkId, FirewallRule.Purpose purpose, FirewallRule.TrafficType trafficType);

    List<FirewallRuleVO> listByIpAndPurposeWithState(Long addressId, FirewallRule.Purpose purpose, FirewallRule.State state);

    void loadSourceCidrs(FirewallRuleVO rule);
}
