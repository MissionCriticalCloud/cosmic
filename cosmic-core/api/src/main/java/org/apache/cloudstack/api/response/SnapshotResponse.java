package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import com.cloud.storage.Snapshot;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.Date;
import java.util.List;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = Snapshot.class)
public class SnapshotResponse extends BaseResponse implements ControlledEntityResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "ID of the snapshot")
    private String id;

    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the account associated with the snapshot")
    private String accountName;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain ID of the snapshot's account")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the domain name of the snapshot's account")
    private String domainName;

    @SerializedName(ApiConstants.PROJECT_ID)
    @Param(description = "the project id of the snapshot")
    private String projectId;

    @SerializedName(ApiConstants.PROJECT)
    @Param(description = "the project name of the snapshot")
    private String projectName;

    @SerializedName(ApiConstants.SNAPSHOT_TYPE)
    @Param(description = "the type of the snapshot")
    private String snapshotType;

    @SerializedName(ApiConstants.VOLUME_ID)
    @Param(description = "ID of the disk volume")
    private String volumeId;

    @SerializedName(ApiConstants.VOLUME_NAME)
    @Param(description = "name of the disk volume")
    private String volumeName;

    @SerializedName("volumetype")
    @Param(description = "type of the disk volume")
    private String volumeType;

    @SerializedName(ApiConstants.CREATED)
    @Param(description = "  the date the snapshot was created")
    private Date created;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "name of the snapshot")
    private String name;

    @SerializedName(ApiConstants.INTERVAL_TYPE)
    @Param(description = "valid types are hourly, daily, weekly, monthy, template, and none.")
    private String intervalType;

    @SerializedName(ApiConstants.STATE)
    @Param(description = "the state of the snapshot. BackedUp means that snapshot is ready to be used; Creating - the snapshot is being allocated on the primary storage; " +
            "BackingUp - the snapshot is being backed up on secondary storage")
    private Snapshot.State state;

    @SerializedName(ApiConstants.PHYSICAL_SIZE)
    @Param(description = "physical size of backedup snapshot on image store")
    private long physicalSize;

    @SerializedName(ApiConstants.ZONE_ID)
    @Param(description = "id of the availability zone")
    private String zoneId;

    @SerializedName(ApiConstants.TAGS)
    @Param(description = "the list of resource tags associated with snapshot", responseObject = ResourceTagResponse.class)
    private List<ResourceTagResponse> tags;

    @SerializedName(ApiConstants.REVERTABLE)
    @Param(description = "indicates whether the underlying storage supports reverting the volume to this snapshot")
    private boolean revertable;

    @Override
    public String getObjectId() {
        return this.getId();
    }

    private String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getAccountName() {
        return accountName;
    }

    @Override
    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    @Override
    public void setProjectId(final String projectId) {
        this.projectId = projectId;
    }

    @Override
    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    public String getDomainId() {
        return domainId;
    }

    @Override
    public void setDomainId(final String domainId) {
        this.domainId = domainId;
    }

    @Override
    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }

    public void setSnapshotType(final String snapshotType) {
        this.snapshotType = snapshotType;
    }

    public void setVolumeId(final String volumeId) {
        this.volumeId = volumeId;
    }

    public void setVolumeName(final String volumeName) {
        this.volumeName = volumeName;
    }

    public void setVolumeType(final String volumeType) {
        this.volumeType = volumeType;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setIntervalType(final String intervalType) {
        this.intervalType = intervalType;
    }

    public void setState(final Snapshot.State state) {
        this.state = state;
    }

    public void setPhysicaSize(final long physicalSize) {
        this.physicalSize = physicalSize;
    }

    public void setZoneId(final String zoneId) {
        this.zoneId = zoneId;
    }

    public void setTags(final List<ResourceTagResponse> tags) {
        this.tags = tags;
    }

    public boolean isRevertable() {
        return revertable;
    }

    public void setRevertable(final boolean revertable) {
        this.revertable = revertable;
    }
}
