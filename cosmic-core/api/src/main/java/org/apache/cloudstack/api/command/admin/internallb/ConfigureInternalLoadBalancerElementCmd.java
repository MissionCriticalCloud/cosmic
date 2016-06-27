package org.apache.cloudstack.api.command.admin.internallb;

import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.VirtualRouterProvider;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.InternalLoadBalancerElementResponse;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.network.element.InternalLoadBalancerElementService;

import javax.inject.Inject;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "configureInternalLoadBalancerElement",
        responseObject = InternalLoadBalancerElementResponse.class,
        description = "Configures an Internal Load Balancer element.",
        since = "4.2.0",
        requestHasSensitiveInfo = false,
        responseHasSensitiveInfo = false)
public class ConfigureInternalLoadBalancerElementCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ConfigureInternalLoadBalancerElementCmd.class.getName());
    private static final String s_name = "configureinternalloadbalancerelementresponse";

    @Inject
    private List<InternalLoadBalancerElementService> _service;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID,
            type = CommandType.UUID,
            entityType = InternalLoadBalancerElementResponse.class,
            required = true,
            description = "the ID of the internal lb provider")
    private Long id;

    @Parameter(name = ApiConstants.ENABLED, type = CommandType.BOOLEAN, required = true, description = "Enables/Disables the Internal Load Balancer element")
    private Boolean enabled;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_NETWORK_ELEMENT_CONFIGURE;
    }

    @Override
    public String getEventDescription() {
        return "configuring internal load balancer element: " + id;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException {
        CallContext.current().setEventDetails("Internal load balancer element: " + id);
        final VirtualRouterProvider result = _service.get(0).configureInternalLoadBalancerElement(getId(), getEnabled());
        if (result != null) {
            final InternalLoadBalancerElementResponse routerResponse = _responseGenerator.createInternalLbElementResponse(result);
            routerResponse.setResponseName(getCommandName());
            this.setResponseObject(routerResponse);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to configure the internal load balancer element");
        }
    }

    public Long getId() {
        return id;
    }

    public Boolean getEnabled() {
        return enabled;
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
