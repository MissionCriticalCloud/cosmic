package org.apache.cloudstack.api.command.admin.usage;

import com.cloud.network.PhysicalNetworkTrafficType;
import com.cloud.user.Account;
import com.cloud.utils.Pair;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.PhysicalNetworkResponse;
import org.apache.cloudstack.api.response.ProviderResponse;
import org.apache.cloudstack.api.response.TrafficTypeResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listTrafficTypes", description = "Lists traffic types of a given physical network.", responseObject = ProviderResponse.class, since = "3.0.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListTrafficTypesCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListTrafficTypesCmd.class.getName());
    private static final String s_name = "listtraffictypesresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.PHYSICAL_NETWORK_ID,
            type = CommandType.UUID,
            entityType = PhysicalNetworkResponse.class,
            required = true,
            description = "the Physical Network ID")
    private Long physicalNetworkId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }

    @Override
    public void execute() {
        final Pair<List<? extends PhysicalNetworkTrafficType>, Integer> trafficTypes = _networkService.listTrafficTypes(getPhysicalNetworkId());
        final ListResponse<TrafficTypeResponse> response = new ListResponse<>();
        final List<TrafficTypeResponse> trafficTypesResponses = new ArrayList<>();
        if (trafficTypes != null) {
            for (final PhysicalNetworkTrafficType trafficType : trafficTypes.first()) {
                final TrafficTypeResponse trafficTypeResponse = _responseGenerator.createTrafficTypeResponse(trafficType);
                trafficTypesResponses.add(trafficTypeResponse);
            }
            response.setResponses(trafficTypesResponses, trafficTypes.second());
            response.setResponseName(getCommandName());
        }
        this.setResponseObject(response);
    }

    public Long getPhysicalNetworkId() {
        return physicalNetworkId;
    }

    public void setPhysicalNetworkId(final Long physicalNetworkId) {
        this.physicalNetworkId = physicalNetworkId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////
    @Override
    public String getCommandName() {
        return s_name;
    }
}
