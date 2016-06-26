package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class ApiParameterResponse extends BaseResponse {
    @SerializedName(ApiConstants.NAME)
    @Param(description = "the name of the api parameter")
    private String name;

    @SerializedName(ApiConstants.DESCRIPTION)
    @Param(description = "description of the api parameter")
    private String description;

    @SerializedName(ApiConstants.TYPE)
    @Param(description = "parameter type")
    private String type;

    @SerializedName(ApiConstants.LENGTH)
    @Param(description = "length of the parameter")
    private int length;

    @SerializedName(ApiConstants.REQUIRED)
    @Param(description = "true if this parameter is required for the api request")
    private Boolean required;

    @SerializedName(ApiConstants.SINCE)
    @Param(description = "version of CloudStack the api was introduced in")
    private String since;

    @SerializedName("related")
    @Param(description = "comma separated related apis to get the parameter")
    private String related;

    public ApiParameterResponse() {
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public void setLength(final int length) {
        this.length = length;
    }

    public void setRequired(final Boolean required) {
        this.required = required;
    }

    public void setSince(final String since) {
        this.since = since;
    }

    public String getRelated() {
        return related;
    }

    public void setRelated(final String related) {
        this.related = related;
    }
}
