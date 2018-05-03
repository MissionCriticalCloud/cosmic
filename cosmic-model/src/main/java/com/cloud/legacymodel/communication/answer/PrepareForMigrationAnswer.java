package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.PrepareForMigrationCommand;

public class PrepareForMigrationAnswer extends Answer {
    protected PrepareForMigrationAnswer() {
    }

    public PrepareForMigrationAnswer(final PrepareForMigrationCommand cmd, final String detail) {
        super(cmd, false, detail);
    }

    public PrepareForMigrationAnswer(final PrepareForMigrationCommand cmd, final Exception ex) {
        super(cmd, false, ex.getMessage());
    }

    public PrepareForMigrationAnswer(final PrepareForMigrationCommand cmd) {
        super(cmd, true, null);
    }
}
