package com.cloud.api.command.admin.network;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.PodResponse;
import com.cloud.api.response.StorageNetworkIpRangeResponse;
import com.cloud.event.EventTypes;
import com.cloud.legacymodel.dc.StorageNetworkIpRange;
import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.legacymodel.exceptions.InsufficientCapacityException;
import com.cloud.legacymodel.exceptions.ResourceAllocationException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createStorageNetworkIpRange", group = APICommandGroup.NetworkService,
        description = "Creates a Storage network IP range.",
        responseObject = StorageNetworkIpRangeResponse.class,
        since = "3.0.0",
        requestHasSensitiveInfo = false,
        responseHasSensitiveInfo = false)
public class CreateStorageNetworkIpRangeCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateStorageNetworkIpRangeCmd.class);

    private static final String s_name = "createstoragenetworkiprangeresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.POD_ID,
            type = CommandType.UUID,
            entityType = PodResponse.class,
            required = true,
            description = "UUID of pod where the ip range belongs to")
    private Long podId;

    @Parameter(name = ApiConstants.START_IP, type = CommandType.STRING, required = true, description = "the beginning IP address")
    private String startIp;

    @Parameter(name = ApiConstants.END_IP, type = CommandType.STRING, description = "the ending IP address")
    private String endIp;

    @Parameter(name = ApiConstants.VLAN,
            type = CommandType.INTEGER,
            description = "Optional. The vlan the ip range sits on, default to Null when it is not specificed which means you network is not on any Vlan.")
    private Integer vlan;

    @Parameter(name = ApiConstants.NETMASK, type = CommandType.STRING, required = true, description = "the netmask for storage network")
    private String netmask;

    @Parameter(name = ApiConstants.GATEWAY, type = CommandType.STRING, required = true, description = "the gateway for storage network")
    private String gateway;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getPodId() {
        return podId;
    }

    public String getNetmask() {
        return netmask;
    }

    public String getGateWay() {
        return gateway;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_STORAGE_IP_RANGE_CREATE;
    }

    @Override
    public String getEventDescription() {
        return "Creating storage ip range from " + getStartIp() + " to " + getEndIp() + " with vlan " + getVlan();
    }

    public String getStartIp() {
        return startIp;
    }

    public String getEndIp() {
        return endIp;
    }

    public Integer getVlan() {
        return vlan;
    }

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException,
            ResourceAllocationException {
        try {
            final StorageNetworkIpRange result = _storageNetworkService.createIpRange(this);
            final StorageNetworkIpRangeResponse response = _responseGenerator.createStorageNetworkIpRangeResponse(result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } catch (final Exception e) {
            s_logger.warn("Create storage network IP range failed", e);
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, e.getMessage());
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
