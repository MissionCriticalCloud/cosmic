package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import com.cloud.storage.GuestOS;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = GuestOS.class)
public class GuestOSResponse extends BaseResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "the ID of the OS type")
    private String id;

    @SerializedName(ApiConstants.OS_CATEGORY_ID)
    @Param(description = "the ID of the OS category")
    private String osCategoryId;

    @SerializedName(ApiConstants.DESCRIPTION)
    @Param(description = "the name/description of the OS type")
    private String description;

    @SerializedName(ApiConstants.IS_USER_DEFINED)
    @Param(description = "is the guest OS user defined")
    private String isUserDefined;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getOsCategoryId() {
        return osCategoryId;
    }

    public void setOsCategoryId(final String osCategoryId) {
        this.osCategoryId = osCategoryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getIsUserDefined() {
        return isUserDefined;
    }

    public void setIsUserDefined(final String isUserDefined) {
        this.isUserDefined = isUserDefined;
    }
}
