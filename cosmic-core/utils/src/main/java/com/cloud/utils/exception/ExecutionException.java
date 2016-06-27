//

//

package com.cloud.utils.exception;

import com.cloud.utils.SerialVersionUID;

/**
 * a public method.
 */
public class ExecutionException extends Exception {
    private static final long serialVersionUID = SerialVersionUID.ExecutionException;

    protected int csErrorCode;

    public ExecutionException(final String msg, final Throwable cause) {
        super(msg, cause);
        setCSErrorCode(CSExceptionErrorCode.getCSErrCode(this.getClass().getName()));
    }

    public ExecutionException(final String msg) {
        super(msg);
    }

    public int getCSErrorCode() {
        return this.csErrorCode;
    }

    public void setCSErrorCode(final int cserrcode) {
        this.csErrorCode = cserrcode;
    }
}
