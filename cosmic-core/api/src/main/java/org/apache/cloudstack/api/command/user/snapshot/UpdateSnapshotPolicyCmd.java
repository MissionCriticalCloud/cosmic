package org.apache.cloudstack.api.command.user.snapshot;

import com.cloud.event.EventTypes;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.storage.Volume;
import com.cloud.storage.snapshot.SnapshotPolicy;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.acl.SecurityChecker;
import org.apache.cloudstack.api.ACL;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCustomIdCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.SnapshotPolicyResponse;
import org.apache.cloudstack.context.CallContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateSnapshotPolicy", description = "Updates the snapshot policy.", responseObject = SnapshotPolicyResponse.class, responseView = ResponseObject
        .ResponseView.Restricted, entityType = {Volume.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateSnapshotPolicyCmd extends BaseAsyncCustomIdCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateSnapshotPolicyCmd.class.getName());
    private static final String s_name = "updatesnapshotpolicyresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @ACL(accessType = SecurityChecker.AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = SnapshotPolicyResponse.class, description = "the ID of the snapshot policy")
    private Long id;

    @Parameter(name = ApiConstants.FOR_DISPLAY,
            type = CommandType.BOOLEAN,
            description = "an optional field, whether to the display the snapshot policy to the end user or not.",
            since = "4.4",
            authorized = {RoleType.Admin})
    private Boolean display;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Boolean getDisplay() {
        return display;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_SNAPSHOT_POLICY_UPDATE;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventDescription() {
        final StringBuilder desc = new StringBuilder("Updating snapshot policy: ");
        desc.append(getId());
        return desc.toString();
    }

    @Override
    public Long getInstanceId() {
        return getId();
    }

    public Long getId() {
        return id;
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("SnapshotPolicy Id: " + getId());
        final SnapshotPolicy result = _snapshotService.updateSnapshotPolicy(this);
        if (result != null) {
            final SnapshotPolicyResponse response = _responseGenerator.createSnapshotPolicyResponse(result);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update snapshot policy");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {

        final SnapshotPolicy policy = _entityMgr.findById(SnapshotPolicy.class, getId());
        if (policy == null) {
            throw new InvalidParameterValueException("Invalid snapshot policy id was provided");
        }
        final Volume volume = _responseGenerator.findVolumeById(policy.getVolumeId());
        if (volume == null) {
            throw new InvalidParameterValueException("Snapshot policy's volume id doesnt exist");
        } else {
            return volume.getAccountId();
        }
    }

    @Override
    public void checkUuid() {
        if (getCustomId() != null) {
            _uuidMgr.checkUuid(getCustomId(), SnapshotPolicy.class);
        }
    }
}
