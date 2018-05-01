package com.cloud.network;

import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.network.rules.FirewallRule;

import java.util.List;

public interface NetworkRuleApplier {
    public boolean applyRules(Network network, FirewallRule.Purpose purpose, List<? extends FirewallRule> rules) throws ResourceUnavailableException;
}
