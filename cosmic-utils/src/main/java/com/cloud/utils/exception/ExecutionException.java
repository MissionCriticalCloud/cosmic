package com.cloud.utils.exception;

public class ExecutionException extends Exception {

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
