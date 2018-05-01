package com.cloud.api.command.user.snapshot;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.SnapshotResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.legacymodel.user.Account;
import com.cloud.storage.Snapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "revertSnapshot", group = APICommandGroup.SnapshotService, description = "revert a volume snapshot.", responseObject = SnapshotResponse.class, entityType = {Snapshot.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class RevertSnapshotCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(RevertSnapshotCmd.class.getName());
    private static final String s_name = "revertsnapshotresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID, type = BaseCmd.CommandType.UUID, entityType = SnapshotResponse.class,
            required = true, description = "The ID of the snapshot")
    private Long id;

    @Override
    public String getEventType() {
        return EventTypes.EVENT_SNAPSHOT_REVERT;
    }

    @Override
    public String getEventDescription() {
        return "revert snapshot: " + getId();
    }

    @Override
    public Long getInstanceId() {
        return getId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.Snapshot;
    }

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////
    public Long getId() {
        return id;
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Snapshot Id: " + getId());
        final Snapshot snapshot = _snapshotService.revertSnapshot(getId());
        if (snapshot != null) {
            final SnapshotResponse response = _responseGenerator.createSnapshotResponse(snapshot);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to revert snapshot");
        }
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////
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
