package org.apache.cloudstack.api.response;

import com.cloud.network.as.Counter;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = Counter.class)
public class CounterResponse extends BaseResponse {
    @SerializedName("id")
    @Param(description = "the id of the Counter")
    private String id;

    @SerializedName(value = ApiConstants.NAME)
    @Param(description = "Name of the counter.")
    private String name;

    @SerializedName(value = ApiConstants.SOURCE)
    @Param(description = "Source of the counter.")
    private String source;

    @SerializedName(value = ApiConstants.VALUE)
    @Param(description = "Value in case of snmp or other specific counters.")
    private String value;

    @SerializedName(ApiConstants.ZONE_ID)
    @Param(description = "zone id of counter")
    private String zoneId;

    @Override
    public String getObjectId() {
        return this.id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setSource(final String source) {
        this.source = source;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
