package com.cloud.api.response;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseResponse;
import com.cloud.serializer.Param;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

public class ApiResponseResponse extends BaseResponse {
    @SerializedName(ApiConstants.NAME)
    @Param(description = "the name of the api response field")
    private String name;

    @SerializedName(ApiConstants.DESCRIPTION)
    @Param(description = "description of the api response field")
    private String description;

    @SerializedName(ApiConstants.TYPE)
    @Param(description = "response field type")
    private String type;

    @SerializedName(ApiConstants.RESPONSE)
    @Param(description = "api response fields")
    private Set<ApiResponseResponse> apiResponse;

    public void setName(final String name) {
        this.name = name;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public void addApiResponse(final ApiResponseResponse childApiResponse) {
        if (this.apiResponse == null) {
            this.apiResponse = new HashSet<>();
        }
        this.apiResponse.add(childApiResponse);
    }
}
