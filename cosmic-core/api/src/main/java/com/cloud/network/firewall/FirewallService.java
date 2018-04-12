package com.cloud.network.firewall;

import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.rules.FirewallRule;
import com.cloud.user.Account;

public interface FirewallService {
    FirewallRule createIngressFirewallRule(FirewallRule rule) throws NetworkRuleConflictException;

    FirewallRule createEgressFirewallRule(FirewallRule rule) throws NetworkRuleConflictException;

    /**
     * Revokes a firewall rule
     *
     * @param ruleId the id of the rule to revoke.
     * @return
     */
    boolean revokeIngressFirewallRule(long ruleId, boolean apply);

    boolean revokeEgressFirewallRule(long ruleId, boolean apply);

    boolean applyEgressFirewallRules(FirewallRule rule, Account caller) throws ResourceUnavailableException;

    boolean applyIngressFirewallRules(long ipId, Account caller) throws ResourceUnavailableException;

    FirewallRule getFirewallRule(long ruleId);

    boolean revokeRelatedFirewallRule(long ruleId, boolean apply);

    FirewallRule updateIngressFirewallRule(long ruleId, String customId, Boolean forDisplay);

    FirewallRule updateEgressFirewallRule(long ruleId, String customId, Boolean forDisplay);

    boolean applyIngressFwRules(long ipId, Account caller) throws ResourceUnavailableException;

    boolean revokeIngressFwRule(long ruleId, boolean apply);
}
