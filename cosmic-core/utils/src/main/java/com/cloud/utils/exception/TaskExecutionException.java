//

//

package com.cloud.utils.exception;

import com.cloud.utils.SerialVersionUID;

/**
 * Used by the Task class to wrap-up its exceptions.
 */
public class TaskExecutionException extends Exception {
    private static final long serialVersionUID = SerialVersionUID.NioConnectionException;

    protected int csErrorCode;

    public TaskExecutionException(final String msg, final Throwable cause) {
        super(msg, cause);
        setCSErrorCode(CSExceptionErrorCode.getCSErrCode(this.getClass().getName()));
    }

    public TaskExecutionException(final String msg) {
        super(msg);
    }

    public int getCSErrorCode() {
        return csErrorCode;
    }

    public void setCSErrorCode(final int cserrcode) {
        csErrorCode = cserrcode;
    }
}
