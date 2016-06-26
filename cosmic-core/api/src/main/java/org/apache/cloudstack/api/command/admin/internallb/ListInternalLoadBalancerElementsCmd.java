package org.apache.cloudstack.api.command.admin.internallb;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.VirtualRouterProvider;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.InternalLoadBalancerElementResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.ProviderResponse;
import org.apache.cloudstack.network.element.InternalLoadBalancerElementService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listInternalLoadBalancerElements",
        description = "Lists all available Internal Load Balancer elements.",
        responseObject = InternalLoadBalancerElementResponse.class,
        since = "4.2.0",
        requestHasSensitiveInfo = false,
        responseHasSensitiveInfo = false)
public class ListInternalLoadBalancerElementsCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListInternalLoadBalancerElementsCmd.class.getName());
    private static final String s_name = "listinternalloadbalancerelementsresponse";

    @Inject
    private InternalLoadBalancerElementService _service;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.ID,
            type = CommandType.UUID,
            entityType = InternalLoadBalancerElementResponse.class,
            description = "list internal load balancer elements by id")
    private Long id;

    @Parameter(name = ApiConstants.NSP_ID,
            type = CommandType.UUID,
            entityType = ProviderResponse.class,
            description = "list internal load balancer elements by network service provider id")
    private Long nspId;

    @Parameter(name = ApiConstants.ENABLED, type = CommandType.BOOLEAN, description = "list internal load balancer elements by enabled state")
    private Boolean enabled;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException,
            ResourceAllocationException {
        final List<? extends VirtualRouterProvider> providers = _service.searchForInternalLoadBalancerElements(getId(), getNspId(), getEnabled());
        final ListResponse<InternalLoadBalancerElementResponse> response = new ListResponse<>();
        final List<InternalLoadBalancerElementResponse> providerResponses = new ArrayList<>();
        for (final VirtualRouterProvider provider : providers) {
            final InternalLoadBalancerElementResponse providerResponse = _responseGenerator.createInternalLbElementResponse(provider);
            providerResponses.add(providerResponse);
        }
        response.setResponses(providerResponses);
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    public Long getId() {
        return id;
    }

    public Long getNspId() {
        return nspId;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
