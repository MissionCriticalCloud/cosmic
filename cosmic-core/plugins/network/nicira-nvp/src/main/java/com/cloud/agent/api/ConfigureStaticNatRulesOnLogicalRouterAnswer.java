package com.cloud.agent.api;

import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.Command;

public class ConfigureStaticNatRulesOnLogicalRouterAnswer extends Answer {

    public ConfigureStaticNatRulesOnLogicalRouterAnswer(final Command command, final Exception e) {
        super(command, false, e.getMessage());
    }

    public ConfigureStaticNatRulesOnLogicalRouterAnswer(final Command command, final boolean success, final String details) {
        super(command, success, details);
    }
}
