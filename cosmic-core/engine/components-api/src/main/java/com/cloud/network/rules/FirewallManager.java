package com.cloud.network.rules;

import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.dao.IPAddressVO;
import com.cloud.network.firewall.FirewallService;
import com.cloud.network.rules.FirewallRule.FirewallRuleType;
import com.cloud.network.rules.FirewallRule.Purpose;
import com.cloud.user.Account;

import java.util.List;

public interface FirewallManager extends FirewallService {
    /**
     * detectRulesConflict finds conflicts in networking rules. It checks for
     * conflicts between the following types of netowrking rules;
     * 1. one to one nat ip forwarding
     * 2. port forwarding
     * 3. load balancing
     * <p>
     * and conflicts are detected between those two rules. In this case, it
     * is possible for both rules to be rolled back when, technically, we should
     * and the user can simply re-add one of the rules themselves.
     *
     * @param newRule the new rule created.
     * @throws NetworkRuleConflictException
     */
    void detectRulesConflict(FirewallRule newRule) throws NetworkRuleConflictException;

    void validateFirewallRule(Account caller, IPAddressVO ipAddress, Integer portStart, Integer portEnd, String proto, Purpose purpose, FirewallRuleType type,
                              Long networkid, FirewallRule.TrafficType trafficType);

    boolean applyRules(List<? extends FirewallRule> rules, boolean continueOnError, boolean updateRulesInDB) throws ResourceUnavailableException;

    boolean applyFirewallRules(List<FirewallRuleVO> rules, boolean continueOnError, Account caller);

    public void revokeRule(FirewallRuleVO rule, Account caller, long userId, boolean needUsageEvent);

    boolean revokeFirewallRulesForIp(long ipId, long userId, Account caller) throws ResourceUnavailableException;

    //    /**
    //     * Revokes a firewall rule
    //     *
    //     * @param ruleId
    //     *            the id of the rule to revoke.
    //     * @param caller
    //     *            TODO
    //     * @param userId
    //     *            TODO
    //     * @return
    //     */
    //    boolean revokeFirewallRule(long ruleId, boolean apply, Account caller, long userId);

    //    FirewallRule createFirewallRule(Long ipAddrId, Account caller, String xId, Integer portStart, Integer portEnd, String protocol, List<String> sourceCidrList, Integer
    // icmpCode, Integer icmpType, Long relatedRuleId,
    //            FirewallRule.FirewallRuleType type, Long networkId, FirewallRule.TrafficType traffictype)
    //            throws NetworkRuleConflictException;

    FirewallRule createRuleForAllCidrs(long ipAddrId, Account caller, Integer startPort, Integer endPort, String protocol, Integer icmpCode, Integer icmpType,
                                       Long relatedRuleId, long networkId) throws NetworkRuleConflictException;

    boolean revokeAllFirewallRulesForNetwork(long networkId, long userId, Account caller) throws ResourceUnavailableException;

    boolean revokeFirewallRulesForVm(long vmId);

    boolean addSystemFirewallRules(IPAddressVO ip, Account acct);

    /**
     * @param rule
     */
    void removeRule(FirewallRule rule);

    boolean applyDefaultEgressFirewallRule(Long networkId, boolean defaultPolicy, boolean add) throws ResourceUnavailableException;
}
