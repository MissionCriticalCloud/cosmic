package com.cloud.network;

import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.network.FirewallRule;
import com.cloud.legacymodel.network.Network;

import java.util.List;

public interface NetworkRuleApplier {
    public boolean applyRules(Network network, FirewallRule.Purpose purpose, List<? extends FirewallRule> rules) throws ResourceUnavailableException;
}
