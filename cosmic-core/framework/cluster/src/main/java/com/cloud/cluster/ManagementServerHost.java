package com.cloud.cluster;

public interface ManagementServerHost {
    long getId();

    long getMsid();

    State getState();

    String getVersion();

    String getServiceIP();

    public static enum State {
        Up, Starting, Down
    }
}
