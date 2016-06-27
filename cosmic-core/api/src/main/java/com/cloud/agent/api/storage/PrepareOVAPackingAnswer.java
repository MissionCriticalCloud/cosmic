package com.cloud.agent.api.storage;

import com.cloud.agent.api.Answer;

public class PrepareOVAPackingAnswer extends Answer {
    public PrepareOVAPackingAnswer(final PrepareOVAPackingCommand cmd, final boolean result, final String details) {
        super(cmd, result, details);
    }
}
