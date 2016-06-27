package org.apache.cloudstack.api.command.admin.usage;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Networks.TrafficType;
import com.cloud.user.Account;
import com.cloud.utils.Pair;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.TrafficTypeImplementorResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listTrafficTypeImplementors",
        description = "Lists implementors of implementor of a network traffic type or implementors of all network traffic types",
        responseObject = TrafficTypeImplementorResponse.class,
        since = "3.0.0",
        requestHasSensitiveInfo = false,
        responseHasSensitiveInfo = false)
public class ListTrafficTypeImplementorsCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListTrafficTypeImplementorsCmd.class);
    private static final String s_name = "listtraffictypeimplementorsresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.TRAFFIC_TYPE,
            type = CommandType.STRING,
            description = "Optional. The network traffic type, if specified, return its implementor. Otherwise, return all traffic types with their implementor")
    private String trafficType;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getTrafficType() {
        return trafficType;
    }

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException,
            ResourceAllocationException {
        final List<Pair<TrafficType, String>> results = _networkService.listTrafficTypeImplementor(this);
        final ListResponse<TrafficTypeImplementorResponse> response = new ListResponse<>();
        final List<TrafficTypeImplementorResponse> responses = new ArrayList<>();
        for (final Pair<TrafficType, String> r : results) {
            final TrafficTypeImplementorResponse p = new TrafficTypeImplementorResponse();
            p.setTrafficType(r.first().toString());
            p.setImplementor(r.second());
            p.setObjectName("traffictypeimplementorresponse");
            responses.add(p);
        }

        response.setResponses(responses);
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
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
