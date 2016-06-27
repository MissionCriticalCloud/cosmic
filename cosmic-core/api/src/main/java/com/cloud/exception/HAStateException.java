package com.cloud.exception;

import com.cloud.utils.SerialVersionUID;

/**
 * states need to be properly cleaned up before anything special can be
 * done with it.  Hence this special state.
 */
public class HAStateException extends ManagementServerException {

    private static final long serialVersionUID = SerialVersionUID.HAStateException;

    public HAStateException(final String msg) {
        super(msg);
    }
}
