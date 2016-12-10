package com.cloud.engine.subsystem.api.storage;

public interface ChapInfo {
    String getInitiatorUsername();

    String getInitiatorSecret();

    String getTargetUsername();

    String getTargetSecret();
}
