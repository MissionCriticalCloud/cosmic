package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import com.cloud.utils.exception.ExceptionProxyObject;
import org.apache.cloudstack.api.BaseResponse;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ExceptionResponse extends BaseResponse {

    @SerializedName("uuidList")
    @Param(description = "List of uuids associated with this error")
    private final List<ExceptionProxyObject> idList;

    @SerializedName("errorcode")
    @Param(description = "numeric code associated with this error")
    private Integer errorCode;

    @SerializedName("cserrorcode")
    @Param(description = "cloudstack exception error code associated with this error")
    private Integer csErrorCode;

    @SerializedName("errortext")
    @Param(description = "the text associated with this error")
    private String errorText = "Command failed due to Internal Server Error";

    public ExceptionResponse() {
        idList = new ArrayList<>();
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(final Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(final String errorText) {
        this.errorText = errorText;
    }

    public void addProxyObject(final ExceptionProxyObject id) {
        idList.add(id);
        return;
    }

    public List<ExceptionProxyObject> getIdProxyList() {
        return idList;
    }

    public void setCSErrorCode(final int cserrcode) {
        this.csErrorCode = cserrcode;
    }

    @Override
    public String toString() {
        return ("Error Code: " + errorCode + " Error text: " + errorText);
    }
}
