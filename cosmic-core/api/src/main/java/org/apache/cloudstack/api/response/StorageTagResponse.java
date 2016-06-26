package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class StorageTagResponse extends BaseResponse {
    @SerializedName("id")
    @Param(description = "the ID of the storage tag")
    private String id;

    @SerializedName("poolid")
    @Param(description = "the pool ID of the storage tag")
    private long poolId;

    @SerializedName("name")
    @Param(description = "the name of the storage tag")
    private String name;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public long getPoolId() {
        return poolId;
    }

    public void setPoolId(final long l) {
        this.poolId = l;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
