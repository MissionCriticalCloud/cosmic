package com.cloud.api.command.admin.router;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.DomainRouterResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.legacymodel.user.Account;
import com.cloud.network.router.VirtualRouter;
import com.cloud.network.router.VirtualRouter.Role;
import com.cloud.utils.exception.InvalidParameterValueException;
import com.cloud.vm.VirtualMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "startRouter", group = APICommandGroup.RouterService, responseObject = DomainRouterResponse.class, description = "Starts a router.", entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class StartRouterCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(StartRouterCmd.class.getName());
    private static final String s_name = "startrouterresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = DomainRouterResponse.class, required = true, description = "the ID of the router")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public static String getResultObjectName() {
        return "router";
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_ROUTER_START;
    }

    @Override
    public String getEventDescription() {
        return "starting router: " + getId();
    }

    @Override
    public Long getInstanceId() {
        return getId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.DomainRouter;
    }

    public Long getId() {
        return id;
    }

    @Override
    public void execute() throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException {
        CallContext.current().setEventDetails("Router Id: " + getId());
        VirtualRouter result = null;
        final VirtualRouter router = _routerService.findRouter(getId());
        if (router == null || router.getRole() != Role.VIRTUAL_ROUTER) {
            throw new InvalidParameterValueException("Can't find router by id");
        } else {
            result = _routerService.startRouter(getId());
        }
        if (result != null) {
            final DomainRouterResponse routerResponse = _responseGenerator.createDomainRouterResponse(result);
            routerResponse.setResponseName(getCommandName());
            setResponseObject(routerResponse);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to start router");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final VirtualRouter router = _entityMgr.findById(VirtualRouter.class, getId());
        if (router != null) {
            return router.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }
}
