package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class ConfigurationResponse extends BaseResponse {
    @SerializedName(ApiConstants.CATEGORY)
    @Param(description = "the category of the configuration")
    private String category;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "the name of the configuration")
    private String name;

    @SerializedName(ApiConstants.VALUE)
    @Param(description = "the value of the configuration")
    private String value;

    @SerializedName(ApiConstants.SCOPE)
    @Param(description = "scope(zone/cluster/pool/account) of the parameter that needs to be updated")
    private String scope;

    @SerializedName(ApiConstants.ID)
    @Param(description = "the value of the configuration")
    private Long id;

    @SerializedName(ApiConstants.DESCRIPTION)
    @Param(description = "the description of the configuration")
    private String description;

    public String getCategory() {
        return category;
    }

    public void setCategory(final String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(final String scope) {
        this.scope = scope;
    }
}
