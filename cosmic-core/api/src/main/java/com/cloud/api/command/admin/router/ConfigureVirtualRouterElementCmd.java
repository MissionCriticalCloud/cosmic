package com.cloud.api.command.admin.router;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.VirtualRouterProviderResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.legacymodel.user.Account;
import com.cloud.network.VirtualRouterProvider;
import com.cloud.network.element.VirtualRouterElementService;

import javax.inject.Inject;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "configureVirtualRouterElement", group = APICommandGroup.RouterService, responseObject = VirtualRouterProviderResponse.class, description = "Configures a virtual router element.",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ConfigureVirtualRouterElementCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ConfigureVirtualRouterElementCmd.class.getName());
    private static final String s_name = "configurevirtualrouterelementresponse";

    @Inject
    private List<VirtualRouterElementService> _service;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID,
            type = CommandType.UUID,
            entityType = VirtualRouterProviderResponse.class,
            required = true,
            description = "the ID of the virtual router provider")
    private Long id;

    @Parameter(name = ApiConstants.ENABLED, type = CommandType.BOOLEAN, required = true, description = "Enabled/Disabled the service provider")
    private Boolean enabled;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public static String getResultObjectName() {
        return "boolean";
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public void setEnabled(final Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_NETWORK_ELEMENT_CONFIGURE;
    }

    @Override
    public String getEventDescription() {
        return "configuring virtual router provider: " + id;
    }

    @Override
    public Long getInstanceId() {
        return id;
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.None;
    }

    @Override
    public void execute() throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException {
        CallContext.current().setEventDetails("Virtual router element: " + id);
        final VirtualRouterProvider result = _service.get(0).configure(this);
        if (result != null) {
            final VirtualRouterProviderResponse routerResponse = _responseGenerator.createVirtualRouterProviderResponse(result);
            if (routerResponse != null) {
                routerResponse.setResponseName(getCommandName());
                this.setResponseObject(routerResponse);
            }
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to configure the virtual router provider");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }
}
