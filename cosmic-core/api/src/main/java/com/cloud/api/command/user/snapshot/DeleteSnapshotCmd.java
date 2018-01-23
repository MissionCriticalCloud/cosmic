package com.cloud.api.command.user.snapshot;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.SnapshotResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.storage.Snapshot;
import com.cloud.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteSnapshot", group = APICommandGroup.SnapshotService, description = "Deletes a snapshot of a disk volume.", responseObject = SuccessResponse.class, entityType = {Snapshot.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteSnapshotCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteSnapshotCmd.class.getName());
    private static final String s_name = "deletesnapshotresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = SnapshotResponse.class,
            required = true, description = "The ID of the snapshot")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_SNAPSHOT_DELETE;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventDescription() {
        return "deleting snapshot: " + getId();
    }

    @Override
    public Long getInstanceId() {
        return getId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.Snapshot;
    }

    public Long getId() {
        return id;
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Snapshot Id: " + getId());
        final boolean result = _snapshotService.deleteSnapshot(getId());
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete snapshot");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Snapshot snapshot = _entityMgr.findById(Snapshot.class, getId());
        if (snapshot != null) {
            return snapshot.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }
}
