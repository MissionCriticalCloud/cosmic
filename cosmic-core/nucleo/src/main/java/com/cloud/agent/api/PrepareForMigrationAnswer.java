//

//

package com.cloud.agent.api;

public class PrepareForMigrationAnswer extends Answer {
    protected PrepareForMigrationAnswer() {
    }

    public PrepareForMigrationAnswer(final PrepareForMigrationCommand cmd, final String detail) {
        super(cmd, false, detail);
    }

    public PrepareForMigrationAnswer(final PrepareForMigrationCommand cmd, final Exception ex) {
        super(cmd, ex);
    }

    public PrepareForMigrationAnswer(final PrepareForMigrationCommand cmd) {
        super(cmd, true, null);
    }
}
