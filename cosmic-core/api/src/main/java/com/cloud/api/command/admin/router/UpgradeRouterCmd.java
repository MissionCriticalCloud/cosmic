package com.cloud.api.command.admin.router;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.DomainRouterResponse;
import com.cloud.api.response.ServiceOfferingResponse;
import com.cloud.legacymodel.network.VirtualRouter;
import com.cloud.legacymodel.user.Account;
import com.cloud.legacymodel.vm.VirtualMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "changeServiceForRouter", group = APICommandGroup.RouterService, description = "Upgrades domain router to a new service offering", responseObject = DomainRouterResponse.class,
        entityType =
                {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpgradeRouterCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpgradeRouterCmd.class.getName());
    private static final String s_name = "changeserviceforrouterresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = DomainRouterResponse.class, required = true, description = "The ID of the router")
    private Long id;

    @Parameter(name = ApiConstants.SERVICE_OFFERING_ID,
            type = CommandType.UUID,
            entityType = ServiceOfferingResponse.class,
            required = true,
            description = "the service offering ID to apply to the domain router")
    private Long serviceOfferingId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getServiceOfferingId() {
        return serviceOfferingId;
    }

    @Override
    public void execute() {
        final VirtualRouter router = _routerService.upgradeRouter(this);
        if (router != null) {
            final DomainRouterResponse routerResponse = _responseGenerator.createDomainRouterResponse(router);
            routerResponse.setResponseName(getCommandName());
            setResponseObject(routerResponse);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to upgrade router");
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
        final VirtualRouter router = _entityMgr.findById(VirtualRouter.class, getId());
        if (router != null) {
            return router.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }

    public Long getId() {
        return id;
    }
}
