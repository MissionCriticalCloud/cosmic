//

//

package com.cloud.agent.api;

/**
 *
 */
public class ConfigureStaticNatRulesOnLogicalRouterAnswer extends Answer {

    /**
     * @param command
     * @param success
     * @param details
     */
    public ConfigureStaticNatRulesOnLogicalRouterAnswer(final Command command, final boolean success, final String details) {
        super(command, success, details);
    }

    /**
     * @param command
     * @param e
     */
    public ConfigureStaticNatRulesOnLogicalRouterAnswer(final Command command, final Exception e) {
        super(command, e);
    }
}
