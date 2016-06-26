package com.cloud.agent.api.storage;

import com.cloud.agent.api.Answer;

public class CreateVolumeOVAAnswer extends Answer {
    public CreateVolumeOVAAnswer(final CreateVolumeOVACommand cmd, final boolean result, final String details) {
        super(cmd, result, details);
    }
}
