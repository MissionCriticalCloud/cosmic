package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.BaseResponse;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class SnapshotScheduleResponse extends BaseResponse {
    @SerializedName("id")
    @Param(description = "the ID of the snapshot schedule")
    private String id;

    @SerializedName("volumeid")
    @Param(description = "the volume ID the snapshot schedule applied for")
    private String volumeId;

    @SerializedName("snapshotpolicyid")
    @Param(description = "the snapshot policy ID used by the snapshot schedule")
    private String snapshotPolicyId;

    @SerializedName("scheduled")
    @Param(description = "time the snapshot is scheduled to be taken")
    private Date scheduled;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(final String volumeId) {
        this.volumeId = volumeId;
    }

    public String getSnapshotPolicyId() {
        return snapshotPolicyId;
    }

    public void setSnapshotPolicyId(final String snapshotPolicyId) {
        this.snapshotPolicyId = snapshotPolicyId;
    }

    public Date getScheduled() {
        return scheduled;
    }

    public void setScheduled(final Date scheduled) {
        this.scheduled = scheduled;
    }
}
