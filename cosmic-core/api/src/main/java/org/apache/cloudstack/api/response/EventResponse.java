package org.apache.cloudstack.api.response;

import com.cloud.event.Event;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = Event.class)
public class EventResponse extends BaseResponse implements ControlledViewEntityResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "the ID of the event")
    private String id;

    @SerializedName(ApiConstants.USERNAME)
    @Param(description = "the name of the user who performed the action (can be different from the account if an admin is performing an action for a user, e.g. starting/stopping" +
            " a user's virtual machine)")
    private String username;

    @SerializedName(ApiConstants.TYPE)
    @Param(description = "the type of the event (see event types)")
    private String eventType;

    @SerializedName(ApiConstants.LEVEL)
    @Param(description = "the event level (INFO, WARN, ERROR)")
    private String level;

    @SerializedName(ApiConstants.DESCRIPTION)
    @Param(description = "a brief description of the event")
    private String description;

    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the account name for the account that owns the object being acted on in the event (e.g. the owner of the virtual machine, ip address, or security group)")
    private String accountName;

    @SerializedName(ApiConstants.PROJECT_ID)
    @Param(description = "the project id of the ipaddress")
    private String projectId;

    @SerializedName(ApiConstants.PROJECT)
    @Param(description = "the project name of the address")
    private String projectName;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the id of the account's domain")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the name of the account's domain")
    private String domainName;

    @SerializedName(ApiConstants.CREATED)
    @Param(description = "the date the event was created")
    private Date created;

    @SerializedName(ApiConstants.STATE)
    @Param(description = "the state of the event")
    private Event.State state;

    @SerializedName("parentid")
    @Param(description = "whether the event is parented")
    private String parentId;

    public void setId(final String id) {
        this.id = id;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public void setEventType(final String eventType) {
        this.eventType = eventType;
    }

    public void setLevel(final String level) {
        this.level = level;
    }

    public void setDescription(final String description) {
        this.description = description;
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

    @Override
    public void setDomainId(final String domainId) {
        this.domainId = domainId;
    }

    @Override
    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public void setState(final Event.State state) {
        this.state = state;
    }

    public void setParentId(final String parentId) {
        this.parentId = parentId;
    }
}
