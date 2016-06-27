package org.apache.cloudstack.api.command.admin.network;

import com.cloud.dc.StorageNetworkIpRange;
import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.PodResponse;
import org.apache.cloudstack.api.response.StorageNetworkIpRangeResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createStorageNetworkIpRange",
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
