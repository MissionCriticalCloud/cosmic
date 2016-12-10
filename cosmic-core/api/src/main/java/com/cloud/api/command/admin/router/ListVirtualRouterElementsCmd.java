package com.cloud.api.command.admin.router;

import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.user.network.ListNetworkOfferingsCmd;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.ProviderResponse;
import com.cloud.api.response.VirtualRouterProviderResponse;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.VirtualRouterProvider;
import com.cloud.network.element.VirtualRouterElementService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listVirtualRouterElements", description = "Lists all available virtual router elements.", responseObject = VirtualRouterProviderResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListVirtualRouterElementsCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListNetworkOfferingsCmd.class.getName());
    private static final String s_name = "listvirtualrouterelementsresponse";

    // TODO, VirtualRouterElementServer is not singleton in system!
    @Inject
    private List<VirtualRouterElementService> _service;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = VirtualRouterProviderResponse.class, description = "list virtual router elements by id")
    private Long id;

    @Parameter(name = ApiConstants.NSP_ID,
            type = CommandType.UUID,
            entityType = ProviderResponse.class,
            description = "list virtual router elements by network service provider id")
    private Long nspId;

    @Parameter(name = ApiConstants.ENABLED, type = CommandType.BOOLEAN, description = "list network offerings by enabled state")
    private Boolean enabled;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getNspId() {
        return nspId;
    }

    public void setNspId(final Long nspId) {
        this.nspId = nspId;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(final Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException,
            ResourceAllocationException {
        final List<? extends VirtualRouterProvider> providers = _service.get(0).searchForVirtualRouterElement(this);
        final ListResponse<VirtualRouterProviderResponse> response = new ListResponse<>();
        final List<VirtualRouterProviderResponse> providerResponses = new ArrayList<>();
        for (final VirtualRouterProvider provider : providers) {
            final VirtualRouterProviderResponse providerResponse = _responseGenerator.createVirtualRouterProviderResponse(provider);
            providerResponses.add(providerResponse);
        }
        response.setResponses(providerResponses);
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
