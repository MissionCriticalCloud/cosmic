//

//

package com.cloud.exception;

import com.cloud.utils.SerialVersionUID;

/**
 */
public class UnsupportedVersionException extends CloudException {

    public static final String UnknownVersion = "unknown.version";
    public static final String IncompatibleVersion = "incompatible.version";
    private static final long serialVersionUID = SerialVersionUID.UnsupportedVersionException;
    String _reason;

    public UnsupportedVersionException(final String message, final String reason) {
        super(message);
        _reason = reason;
    }

    public String getReason() {
        return _reason;
    }
}
