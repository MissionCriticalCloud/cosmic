package com.cloud.legacymodel.exceptions;

/**
 * Used by the Task class to wrap-up its exceptions.
 */
public class TaskExecutionException extends Exception {
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
