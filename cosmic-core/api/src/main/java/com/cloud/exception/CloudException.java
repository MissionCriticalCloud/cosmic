package com.cloud.exception;

import com.cloud.utils.exception.CSExceptionErrorCode;

import java.util.ArrayList;

/**
 * by the API response serializer. Any exceptions that are thrown by
 * class, which extends RuntimeException instead of Exception like this
 * class does.
 */

public class CloudException extends Exception {
    private static final long serialVersionUID = 8784427323859682503L;

    // This holds a list of uuids and their names. Add uuid:fieldname pairs
    protected ArrayList<String> idList = new ArrayList<>();

    protected Integer csErrorCode;

    public CloudException(final String message) {
        super(message);
        setCSErrorCode(CSExceptionErrorCode.getCSErrCode(this.getClass().getName()));
    }

    public CloudException(final String message, final Throwable cause) {
        super(message, cause);
        setCSErrorCode(CSExceptionErrorCode.getCSErrCode(this.getClass().getName()));
    }

    public CloudException() {
        super();
        setCSErrorCode(CSExceptionErrorCode.getCSErrCode(this.getClass().getName()));
    }

    public void addProxyObject(final String uuid) {
        idList.add(uuid);
        return;
    }

    public ArrayList<String> getIdProxyList() {
        return idList;
    }

    public int getCSErrorCode() {
        return csErrorCode;
    }

    public void setCSErrorCode(final int cserrcode) {
        csErrorCode = cserrcode;
    }
}
