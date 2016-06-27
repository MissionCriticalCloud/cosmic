package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class NicDetailResponse extends BaseResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "ID of the nic")
    private String id;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "name of the nic detail")
    private String name;

    @SerializedName(ApiConstants.VALUE)
    @Param(description = "value of the nic detail")
    private String value;

    @SerializedName(ApiConstants.DISPLAY_NIC)
    @Param(description = "an optional field whether to the display the nic to the end user or not.")
    private Boolean displayNic;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public String getName() {

        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Boolean getDisplayNic() {
        return displayNic;
    }

    public void setDisplayNic(final Boolean displayNic) {
        this.displayNic = displayNic;
    }
}
