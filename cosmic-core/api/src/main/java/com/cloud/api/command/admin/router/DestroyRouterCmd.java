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
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.legacymodel.user.Account;
import com.cloud.network.router.VirtualRouter;
import com.cloud.vm.VirtualMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "destroyRouter", group = APICommandGroup.RouterService, description = "Destroys a router.", responseObject = DomainRouterResponse.class, entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DestroyRouterCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DestroyRouterCmd.class.getName());
    private static final String s_name = "destroyrouterresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = DomainRouterResponse.class, required = true, description = "the ID of the router")
    private Long id;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_ROUTER_DESTROY;
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public String getEventDescription() {
        return "destroying router: " + getId();
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
    public void execute() throws ConcurrentOperationException, ResourceUnavailableException {
        final CallContext ctx = CallContext.current();
        ctx.setEventDetails("Router Id: " + getId());

        final VirtualRouter result = _routerService.destroyRouter(getId(), ctx.getCallingAccount(), ctx.getCallingUserId());
        if (result != null) {
            final DomainRouterResponse response = _responseGenerator.createDomainRouterResponse(result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to destroy router");
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
