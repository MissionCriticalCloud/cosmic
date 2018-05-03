package com.cloud.network.rules;

import com.cloud.api.command.user.firewall.ListPortForwardingRulesCmd;
import com.cloud.legacymodel.exceptions.InsufficientAddressCapacityException;
import com.cloud.legacymodel.exceptions.NetworkRuleConflictException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.network.FirewallRule;
import com.cloud.legacymodel.network.Ip;
import com.cloud.legacymodel.network.PortForwardingRule;
import com.cloud.legacymodel.network.StaticNatRule;
import com.cloud.legacymodel.user.Account;
import com.cloud.legacymodel.utils.Pair;

import java.util.List;

public interface RulesService {
    Pair<List<? extends FirewallRule>, Integer> searchStaticNatRules(Long ipId, Long id, Long vmId, Long start, Long size, String accountName, Long domainId,
                                                                     Long projectId, boolean isRecursive, boolean listAll);

    /**
     * Creates a port forwarding rule between two ip addresses or between
     * an ip address and a virtual machine.
     *
     * @param rule         rule to be created.
     * @param vmId         vm to be linked to. If specified the destination ip address is ignored.
     * @param openFirewall TODO
     * @param forDisplay   TODO
     * @return PortForwardingRule if created.
     * @throws NetworkRuleConflictException if conflicts in the network rules are detected.
     */
    PortForwardingRule createPortForwardingRule(PortForwardingRule rule, Long vmId, Ip vmIp, boolean openFirewall, Boolean forDisplay) throws NetworkRuleConflictException;

    /**
     * Revokes a port forwarding rule
     *
     * @param ruleId the id of the rule to revoke.
     * @param caller
     * @return
     */
    boolean revokePortForwardingRule(long ruleId, boolean apply);

    /**
     * List port forwarding rules assigned to an ip address
     *
     * @param cmd the command object holding the criteria for listing port forwarding rules (the ipAddress)
     * @return list of port forwarding rules on the given address, empty list if no rules exist
     */
    public Pair<List<? extends PortForwardingRule>, Integer> listPortForwardingRules(ListPortForwardingRulesCmd cmd);

    boolean applyPortForwardingRules(long ipAdddressId, Account caller) throws ResourceUnavailableException;

    boolean enableStaticNat(long ipAddressId, long vmId, long networkId, String vmGuestIp) throws NetworkRuleConflictException, ResourceUnavailableException;

    StaticNatRule createStaticNatRule(StaticNatRule rule, boolean openFirewall) throws NetworkRuleConflictException;

    boolean revokeStaticNatRule(long ruleId, boolean apply);

    boolean applyStaticNatRules(long ipAdddressId, Account caller) throws ResourceUnavailableException;

    StaticNatRule buildStaticNatRule(FirewallRule rule, boolean forRevoke);

    boolean disableStaticNat(long ipId) throws ResourceUnavailableException, NetworkRuleConflictException, InsufficientAddressCapacityException;

    PortForwardingRule updatePortForwardingRule(long id, Integer privatePort, Long virtualMachineId, Ip vmGuestIp, String customId, Boolean forDisplay);
}
