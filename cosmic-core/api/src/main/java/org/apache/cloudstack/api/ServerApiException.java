package org.apache.cloudstack.api;

import com.cloud.exception.CloudException;
import com.cloud.utils.exception.CSExceptionErrorCode;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.exception.ExceptionProxyObject;

import java.util.ArrayList;

public class ServerApiException extends CloudRuntimeException {
    private ApiErrorCode _errorCode;
    private String _description;

    public ServerApiException() {
        _errorCode = ApiErrorCode.INTERNAL_ERROR;
        _description = null;
        setCSErrorCode(CSExceptionErrorCode.getCSErrCode(ServerApiException.class.getName()));
    }

    public ServerApiException(final ApiErrorCode errorCode, final String description) {
        _errorCode = errorCode;
        _description = description;
        setCSErrorCode(CSExceptionErrorCode.getCSErrCode(ServerApiException.class.getName()));
    }

    // wrap a specific CloudRuntimeException to a ServerApiException
    public ServerApiException(final ApiErrorCode errorCode, final String description, final Throwable cause) {
        super(description, cause);
        _errorCode = errorCode;
        _description = description;
        if (cause instanceof CloudRuntimeException) {
            final CloudRuntimeException rt = (CloudRuntimeException) cause;
            final ArrayList<ExceptionProxyObject> idList = rt.getIdProxyList();
            if (idList != null) {
                for (int i = 0; i < idList.size(); i++) {
                    addProxyObject(idList.get(i));
                }
            }
            setCSErrorCode(rt.getCSErrorCode());
        } else if (cause instanceof CloudException) {
            final CloudException rt = (CloudException) cause;
            final ArrayList<String> idList = rt.getIdProxyList();
            if (idList != null) {
                for (int i = 0; i < idList.size(); i++) {
                    addProxyObject(idList.get(i));
                }
            }
            setCSErrorCode(rt.getCSErrorCode());
        }
    }

    public ApiErrorCode getErrorCode() {
        return _errorCode;
    }

    public void setErrorCode(final ApiErrorCode errorCode) {
        _errorCode = errorCode;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(final String description) {
        _description = description;
    }

    @Override
    public String getMessage() {
        return _description;
    }
}
