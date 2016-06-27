package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import com.cloud.storage.snapshot.SnapshotPolicy;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = SnapshotPolicy.class)
public class SnapshotPolicyResponse extends BaseResponse {
    @SerializedName("id")
    @Param(description = "the ID of the snapshot policy")
    private String id;

    @SerializedName("volumeid")
    @Param(description = "the ID of the disk volume")
    private String volumeId;

    @SerializedName("schedule")
    @Param(description = "time the snapshot is scheduled to be taken.")
    private String schedule;

    @SerializedName("intervaltype")
    @Param(description = "the interval type of the snapshot policy")
    private short intervalType;

    @SerializedName("maxsnaps")
    @Param(description = "maximum number of snapshots retained")
    private int maxSnaps;

    @SerializedName("timezone")
    @Param(description = "the time zone of the snapshot policy")
    private String timezone;

    @SerializedName(ApiConstants.FOR_DISPLAY)
    @Param(description = "is this policy for display to the regular user", since = "4.4", authorized = {RoleType.Admin})
    private Boolean forDisplay;

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

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(final String schedule) {
        this.schedule = schedule;
    }

    public short getIntervalType() {
        return intervalType;
    }

    public void setIntervalType(final short intervalType) {
        this.intervalType = intervalType;
    }

    public int getMaxSnaps() {
        return maxSnaps;
    }

    public void setMaxSnaps(final int maxSnaps) {
        this.maxSnaps = maxSnaps;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(final String timezone) {
        this.timezone = timezone;
    }

    public Boolean getForDisplay() {
        return forDisplay;
    }

    public void setForDisplay(final Boolean forDisplay) {
        this.forDisplay = forDisplay;
    }
}
