package org.apache.cloudstack.api.response;

import com.cloud.alert.Alert;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = Alert.class)
public class AlertResponse extends BaseResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "the id of the alert")
    private String id;

    @SerializedName(ApiConstants.TYPE)
    @Param(description = "One of the following alert types: "
            + "MEMORY = 0, CPU = 1, STORAGE = 2, STORAGE_ALLOCATED = 3, PUBLIC_IP = 4, PRIVATE_IP = 5, HOST = 6, USERVM = 7, "
            + "DOMAIN_ROUTER = 8, CONSOLE_PROXY = 9, ROUTING = 10: lost connection to default route (to the gateway), "
            + "STORAGE_MISC = 11: lost connection to default route (to the gateway), " + "USAGE_SERVER = 12: lost connection to default route (to the gateway), "
            + "MANAGMENT_NODE = 13: lost connection to default route (to the gateway), "
            + "DOMAIN_ROUTER_MIGRATE = 14, CONSOLE_PROXY_MIGRATE = 15, USERVM_MIGRATE = 16, VLAN = 17, SSVM = 18, " + "USAGE_SERVER_RESULT = 19")
    private Short alertType;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "the name of the alert", since = "4.3")
    private String alertName;

    @SerializedName(ApiConstants.DESCRIPTION)
    @Param(description = "description of the alert")
    private String description;

    @SerializedName(ApiConstants.SENT)
    @Param(description = "the date and time the alert was sent")
    private Date lastSent;

    public void setId(final String id) {
        this.id = id;
    }

    public void setAlertType(final Short alertType) {
        this.alertType = alertType;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setLastSent(final Date lastSent) {
        this.lastSent = lastSent;
    }

    public void setName(final String name) {
        this.alertName = name;
    }
}
