package com.cloud.api.command.admin.router;

import com.cloud.api.APICommand;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.BaseResponse;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.ClusterResponse;
import com.cloud.api.response.DomainResponse;
import com.cloud.api.response.DomainRouterResponse;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.PodResponse;
import com.cloud.api.response.UpgradeRouterTemplateResponse;
import com.cloud.api.response.ZoneResponse;
import com.cloud.context.CallContext;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.user.Account;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "upgradeRouterTemplate", description = "Upgrades router to use newer template", responseObject = BaseResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpgradeRouterTemplateCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpgradeRouterTemplateCmd.class.getName());
    private static final String s_name = "upgraderoutertemplateresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = DomainRouterResponse.class, description = "upgrades router with the specified Id")
    private Long id;

    @Parameter(name = ApiConstants.CLUSTER_ID,
            type = CommandType.UUID,
            entityType = ClusterResponse.class,
            description = "upgrades all routers within the specified cluster")
    private Long clusterId;

    @Parameter(name = ApiConstants.POD_ID, type = CommandType.UUID, entityType = PodResponse.class, description = "upgrades all routers within the specified pod")
    private Long podId;

    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.UUID, entityType = ZoneResponse.class, description = "upgrades all routers within the specified zone")
    private Long zoneId;

    @Parameter(name = ApiConstants.ACCOUNT, type = CommandType.STRING,
            description = "upgrades all routers owned by the specified account")
    private String account;

    @Parameter(name = ApiConstants.DOMAIN_ID,
            type = CommandType.UUID,
            entityType = DomainResponse.class,
            description = "upgrades all routers owned by the specified domain")
    private Long domainId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getClusterId() {
        return clusterId;
    }

    public Long getPodId() {
        return podId;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public String getAccount() {
        return account;
    }

    public Long getDomainId() {
        return domainId;
    }

    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.DomainRouter;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public Long getInstanceId() {
        return getId();
    }

    public Long getId() {
        return id;
    }

    @Override
    public void execute() throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException {
        CallContext.current().setEventDetails("Upgrading router template");
        final List<Long> result = _routerService.upgradeRouterTemplate(this);
        if (result != null) {
            final ListResponse<UpgradeRouterTemplateResponse> response = _responseGenerator.createUpgradeRouterTemplateResponse(result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to upgrade router template");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }
}
