//

//

package com.cloud.agent.api;

/**
 *
 */
public class ConfigurePortForwardingRulesOnLogicalRouterAnswer extends Answer {

    public ConfigurePortForwardingRulesOnLogicalRouterAnswer(final Command command, final boolean success, final String details) {
        super(command, success, details);
    }

    public ConfigurePortForwardingRulesOnLogicalRouterAnswer(final Command command, final Exception e) {
        super(command, e);
    }
}
