package com.cloud.agent.api;

import com.cloud.model.enumeration.HostType;

public class StartupExternalLoadBalancerCommand extends StartupCommand {
    public StartupExternalLoadBalancerCommand() {
        super(HostType.ExternalLoadBalancer);
    }
}
