package org.apache.cloudstack.api.command.admin.network;

import com.cloud.network.PhysicalNetworkServiceProvider;
import com.cloud.user.Account;
import com.cloud.utils.Pair;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.PhysicalNetworkResponse;
import org.apache.cloudstack.api.response.ProviderResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listNetworkServiceProviders",
        description = "Lists network serviceproviders for a given physical network.",
        responseObject = ProviderResponse.class,
        since = "3.0.0",
        requestHasSensitiveInfo = false,
        responseHasSensitiveInfo = false)
public class ListNetworkServiceProvidersCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListNetworkServiceProvidersCmd.class.getName());
    private static final String Name = "listnetworkserviceprovidersresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.PHYSICAL_NETWORK_ID, type = CommandType.UUID, entityType = PhysicalNetworkResponse.class, description = "the Physical Network ID")
    private Long physicalNetworkId;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "list providers by name")
    private String name;

    @Parameter(name = ApiConstants.STATE, type = CommandType.STRING, description = "list providers by state")
    private String state;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }

    @Override
    public void execute() {
        final Pair<List<? extends PhysicalNetworkServiceProvider>, Integer> serviceProviders =
                _networkService.listNetworkServiceProviders(getPhysicalNetworkId(), getName(), getState(), this.getStartIndex(), this.getPageSizeVal());
        final ListResponse<ProviderResponse> response = new ListResponse<>();
        final List<ProviderResponse> serviceProvidersResponses = new ArrayList<>();
        for (final PhysicalNetworkServiceProvider serviceProvider : serviceProviders.first()) {
            final ProviderResponse serviceProviderResponse = _responseGenerator.createNetworkServiceProviderResponse(serviceProvider);
            serviceProvidersResponses.add(serviceProviderResponse);
        }

        response.setResponses(serviceProvidersResponses, serviceProviders.second());
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    public Long getPhysicalNetworkId() {
        return physicalNetworkId;
    }

    public void setPhysicalNetworkId(final Long physicalNetworkId) {
        this.physicalNetworkId = physicalNetworkId;
    }

    public String getName() {
        return name;
    }

    public String getState() {
        return state;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////
    @Override
    public String getCommandName() {
        return Name;
    }
}
