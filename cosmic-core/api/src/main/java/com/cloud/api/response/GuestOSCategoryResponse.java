package com.cloud.api.response;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseResponse;
import com.cloud.api.EntityReference;
import com.cloud.serializer.Param;
import com.cloud.storage.GuestOsCategory;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = GuestOsCategory.class)
public class GuestOSCategoryResponse extends BaseResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "the ID of the OS category")
    private String id;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "the name of the OS category")
    private String name;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
