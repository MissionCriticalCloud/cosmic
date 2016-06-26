package com.cloud.exception;

import com.cloud.utils.SerialVersionUID;

public class ConflictingNetworkSettingsException extends CloudException {

    private static final long serialVersionUID = SerialVersionUID.ConflictingNetworkSettingException;

    public ConflictingNetworkSettingsException() {
        super();
    }
}
