//

//

package com.cloud.agent.api;

public class ConfigurePublicIpsOnLogicalRouterAnswer extends Answer {

    public ConfigurePublicIpsOnLogicalRouterAnswer(final Command command, final boolean success, final String details) {
        super(command, success, details);
    }

    public ConfigurePublicIpsOnLogicalRouterAnswer(final Command command, final Exception e) {
        super(command, e);
    }
}
