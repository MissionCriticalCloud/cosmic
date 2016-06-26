package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

public class ApiDiscoveryResponse extends BaseResponse {
    @SerializedName(ApiConstants.NAME)
    @Param(description = "the name of the api command")
    private String name;

    @SerializedName(ApiConstants.DESCRIPTION)
    @Param(description = "description of the api")
    private String description;

    @SerializedName(ApiConstants.SINCE)
    @Param(description = "version of CloudStack the api was introduced in")
    private String since;

    @SerializedName(ApiConstants.IS_ASYNC)
    @Param(description = "true if api is asynchronous")
    private Boolean isAsync;

    @SerializedName("related")
    @Param(description = "comma separated related apis")
    private String related;

    @SerializedName(ApiConstants.PARAMS)
    @Param(description = "the list params the api accepts", responseObject = ApiParameterResponse.class)
    private Set<ApiParameterResponse> params;

    @SerializedName(ApiConstants.RESPONSE)
    @Param(description = "api response fields", responseObject = ApiResponseResponse.class)
    private final Set<ApiResponseResponse> apiResponse;

    @SerializedName(ApiConstants.TYPE)
    @Param(description = "response field type")
    private String type;

    public ApiDiscoveryResponse() {
        params = new HashSet<>();
        apiResponse = new HashSet<>();
        isAsync = false;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getSince() {
        return since;
    }

    public void setSince(final String since) {
        this.since = since;
    }

    public boolean getAsync() {
        return isAsync;
    }

    public void setAsync(final Boolean isAsync) {
        this.isAsync = isAsync;
    }

    public String getRelated() {
        return related;
    }

    public void setRelated(final String related) {
        this.related = related;
    }

    public Set<ApiParameterResponse> getParams() {
        return params;
    }

    public void setParams(final Set<ApiParameterResponse> params) {
        this.params = params;
    }

    public void addParam(final ApiParameterResponse param) {
        this.params.add(param);
    }

    public void addApiResponse(final ApiResponseResponse apiResponse) {
        this.apiResponse.add(apiResponse);
    }
}
