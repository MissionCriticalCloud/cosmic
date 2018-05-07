package com.cloud.model.enumeration;

public enum WatchDogAction {
    RESET("reset"), SHUTDOWN("shutdown"), POWEROFF("poweroff"), PAUSE("pause"), NONE("none"), DUMP("dump");
    String action;

    WatchDogAction(final String action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return this.action;
    }
}
