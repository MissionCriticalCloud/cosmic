package com.cloud.legacymodel.exceptions;

public class UnsupportedVersionException extends CloudException {

    public static final String UnknownVersion = "unknown.version";
    public static final String IncompatibleVersion = "incompatible.version";
    String _reason;

    public UnsupportedVersionException(final String message, final String reason) {
        super(message);
        _reason = reason;
    }

    public String getReason() {
        return _reason;
    }
}
