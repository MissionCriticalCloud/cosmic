package com.cloud.agent.api;

import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.Command;

public class RecurringNetworkUsageAnswer extends Answer {

    protected RecurringNetworkUsageAnswer() {
    }

    public RecurringNetworkUsageAnswer(final Command command) {
        super(command);
    }
}
