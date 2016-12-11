package com.cloud.api.command.user.vpc;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.AccountResponse;
import com.cloud.api.response.StaticRouteResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.vpc.StaticRoute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteStaticRoute", description = "Deletes a static route", responseObject = SuccessResponse.class, entityType = {StaticRoute.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteStaticRouteCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteStaticRouteCmd.class.getName());
    private static final String s_name = "deletestaticrouteresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = StaticRouteResponse.class, required = true, description = "the ID of the static route")
    private Long id;

    // unexposed parameter needed for events logging
    @Parameter(name = ApiConstants.ACCOUNT_ID, type = CommandType.UUID, entityType = AccountResponse.class, expose = false)
    private Long ownerId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_STATIC_ROUTE_DELETE;
    }

    @Override
    public String getEventDescription() {
        return ("Deleting static route id=" + id);
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.StaticRoute;
    }

    @Override
    public String getSyncObjType() {
        return BaseAsyncCmd.vpcSyncObject;
    }

    @Override
    public Long getSyncObjId() {
        final StaticRoute route = _vpcService.getStaticRoute(id);
        if (route == null) {
            throw new InvalidParameterValueException("Invalid id is specified for the static route");
        }
        return route.getVpcId();
    }

    @Override
    public void execute() throws ResourceUnavailableException {
        CallContext.current().setEventDetails("Route Id: " + id);
        final boolean result = _vpcService.revokeStaticRoute(id);

        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete static route");
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
        if (ownerId == null) {
            final StaticRoute route = _entityMgr.findById(StaticRoute.class, id);
            if (route == null) {
                throw new InvalidParameterValueException("Unable to find static route by id=" + id);
            } else {
                ownerId = route.getAccountId();
            }
        }
        return ownerId;
    }
}
