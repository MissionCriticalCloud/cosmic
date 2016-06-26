package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class HostTagResponse extends BaseResponse {
    @SerializedName("id")
    @Param(description = "the ID of the host tag")
    private String id;

    @SerializedName("hostid")
    @Param(description = "the host ID of the host tag")
    private long hostId;

    @SerializedName("name")
    @Param(description = "the name of the host tag")
    private String name;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public long getHostId() {
        return hostId;
    }

    public void setHostId(final long hostId) {
        this.hostId = hostId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
