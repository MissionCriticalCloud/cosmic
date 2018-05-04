package com.cloud.model.enumeration;

public enum ExitStatus {
    Normal(0), // Normal status = 0.
    Upgrade(65), // Exiting for upgrade.
    Configuration(66), // Exiting due to configuration problems.
    Error(67); // Exiting because of error.

    int value;

    ExitStatus(final int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
