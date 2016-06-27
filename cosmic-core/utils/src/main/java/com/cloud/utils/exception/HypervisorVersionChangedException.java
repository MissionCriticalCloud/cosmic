//

//

package com.cloud.utils.exception;

import com.cloud.utils.SerialVersionUID;

public class HypervisorVersionChangedException extends CloudRuntimeException {

    private static final long serialVersionUID = SerialVersionUID.CloudRuntimeException;

    public HypervisorVersionChangedException(final String message) {
        super(message);
    }

    protected HypervisorVersionChangedException() {
        super();
    }
}
