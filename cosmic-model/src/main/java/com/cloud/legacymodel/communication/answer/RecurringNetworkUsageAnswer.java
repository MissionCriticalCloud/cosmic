package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.Command;

public class RecurringNetworkUsageAnswer extends Answer {

    protected RecurringNetworkUsageAnswer() {
    }

    public RecurringNetworkUsageAnswer(final Command command) {
        super(command);
    }
}
