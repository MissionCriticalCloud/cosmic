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
import com.cloud.utils.component.ManagerBase;

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
    public void validateFirewallRule(final Account caller, final IPAddressVO ipAddress, final Integer portStart, final String proto, final Purpose purpose, final FirewallRuleType type,
                                     final Long networkid, final TrafficType trafficType) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean applyRules(final List<? extends FirewallRule> rules, final boolean continueOnError, final boolean updateRulesInDB) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void revokeRule(final FirewallRuleVO rule, final Account caller, final long userId, final boolean needUsageEvent) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeRule(final FirewallRule rule) {
        // TODO Auto-generated method stub

    }
}
