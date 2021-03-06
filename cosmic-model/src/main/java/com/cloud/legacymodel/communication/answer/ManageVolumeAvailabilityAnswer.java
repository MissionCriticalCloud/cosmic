package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.Command;

public class ManageVolumeAvailabilityAnswer extends Answer {

    protected ManageVolumeAvailabilityAnswer() {
        super();
    }

    public ManageVolumeAvailabilityAnswer(final Command command, final boolean success, final String details) {
        super(command, success, details);
    }
}
