package org.apache.cloudstack.api.command.admin.router;

import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.VirtualRouterProvider;
import com.cloud.network.element.VirtualRouterElementService;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.VirtualRouterProviderResponse;
import org.apache.cloudstack.context.CallContext;

import javax.inject.Inject;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "configureVirtualRouterElement", responseObject = VirtualRouterProviderResponse.class, description = "Configures a virtual router element.",
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
