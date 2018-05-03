package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.Command;

public class UpgradeDiskAnswer extends Answer {

    public UpgradeDiskAnswer() {
        super();
    }

    public UpgradeDiskAnswer(final Command cmd, final boolean success, final String details) {
        super(cmd, success, details);
    }
}
