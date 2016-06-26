package org.apache.cloudstack.engine.subsystem.api.storage;

public interface ChapInfo {
    String getInitiatorUsername();

    String getInitiatorSecret();

    String getTargetUsername();

    String getTargetSecret();
}
