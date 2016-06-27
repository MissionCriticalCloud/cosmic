package com.cloud.network;

import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.dao.IPAddressVO;
import com.cloud.network.firewall.FirewallService;
import com.cloud.network.rules.FirewallManager;
import com.cloud.network.rules.FirewallRule;
import com.cloud.network.rules.FirewallRule.FirewallRuleType;
import com.cloud.network.rules.FirewallRule.Purpose;
import com.cloud.network.rules.FirewallRule.TrafficType;
import com.cloud.network.rules.FirewallRuleVO;
import com.cloud.user.Account;
import com.cloud.utils.Pair;
import com.cloud.utils.component.ManagerBase;
import org.apache.cloudstack.api.command.user.firewall.IListFirewallRulesCmd;

import javax.naming.ConfigurationException;
import java.util.List;
import java.util.Map;

public class MockFirewallManagerImpl extends ManagerBase implements FirewallManager, FirewallService {

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void detectRulesConflict(final FirewallRule newRule) throws NetworkRuleConflictException {
        // TODO Auto-generated method stub

    }

    @Override
    public void validateFirewallRule(final Account caller, final IPAddressVO ipAddress, final Integer portStart, final Integer portEnd, final String proto, final Purpose
            purpose, final FirewallRuleType type,
                                     final Long networkid, final TrafficType trafficType) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean applyRules(final List<? extends FirewallRule> rules, final boolean continueOnError, final boolean updateRulesInDB) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean applyFirewallRules(final List<FirewallRuleVO> rules, final boolean continueOnError, final Account caller) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void revokeRule(final FirewallRuleVO rule, final Account caller, final long userId, final boolean needUsageEvent) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean revokeFirewallRulesForIp(final long ipId, final long userId, final Account caller) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public FirewallRule createRuleForAllCidrs(final long ipAddrId, final Account caller, final Integer startPort, final Integer endPort, final String protocol, final Integer
            icmpCode, final Integer icmpType,
                                              final Long relatedRuleId, final long networkId) throws NetworkRuleConflictException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean revokeAllFirewallRulesForNetwork(final long networkId, final long userId, final Account caller) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean revokeFirewallRulesForVm(final long vmId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean addSystemFirewallRules(final IPAddressVO ip, final Account acct) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void removeRule(final FirewallRule rule) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean applyDefaultEgressFirewallRule(final Long networkId, final boolean defaultPolicy, final boolean add) throws ResourceUnavailableException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public FirewallRule createIngressFirewallRule(final FirewallRule rule) throws NetworkRuleConflictException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FirewallRule createEgressFirewallRule(final FirewallRule rule) throws NetworkRuleConflictException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Pair<List<? extends FirewallRule>, Integer> listFirewallRules(final IListFirewallRulesCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean revokeIngressFirewallRule(final long ruleId, final boolean apply) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean revokeEgressFirewallRule(final long ruleId, final boolean apply) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean applyEgressFirewallRules(final FirewallRule rule, final Account caller) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean applyIngressFirewallRules(final long ipId, final Account caller) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public FirewallRule getFirewallRule(final long ruleId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean revokeRelatedFirewallRule(final long ruleId, final boolean apply) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public FirewallRule updateIngressFirewallRule(final long ruleId, final String customId, final Boolean forDisplay) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public FirewallRule updateEgressFirewallRule(final long ruleId, final String customId, final Boolean forDisplay) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean applyIngressFwRules(final long ipId, final Account caller) throws ResourceUnavailableException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean revokeIngressFwRule(final long ruleId, final boolean apply) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
