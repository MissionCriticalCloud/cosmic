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
import org.apache.cloudstack.api.response.StorageNetworkIpRangeResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateStorageNetworkIpRange",
        description = "Update a Storage network IP range, only allowed when no IPs in this range have been allocated.",
        responseObject = StorageNetworkIpRangeResponse.class,
        since = "3.0.0",
        requestHasSensitiveInfo = false,
        responseHasSensitiveInfo = false)
public class UpdateStorageNetworkIpRangeCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateStorageNetworkIpRangeCmd.class);
    private static final String s_name = "updatestoragenetworkiprangeresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.ID,
            type = CommandType.UUID,
            entityType = StorageNetworkIpRangeResponse.class,
            required = true,
            description = "UUID of storage network ip range")
    private Long id;

    @Parameter(name = ApiConstants.START_IP, type = CommandType.STRING, description = "the beginning IP address")
    private String startIp;

    @Parameter(name = ApiConstants.END_IP, type = CommandType.STRING, description = "the ending IP address")
    private String endIp;

    @Parameter(name = ApiConstants.VLAN, type = CommandType.INTEGER, description = "Optional. the vlan the ip range sits on")
    private Integer vlan;

    @Parameter(name = ApiConstants.NETMASK, type = CommandType.STRING, description = "the netmask for storage network")
    private String netmask;

    @Override
    public String getEventType() {
        return EventTypes.EVENT_STORAGE_IP_RANGE_UPDATE;
    }

    @Override
    public String getEventDescription() {
        return "Update storage ip range " + getId() + " [StartIp=" + getStartIp() + ", EndIp=" + getEndIp() + ", vlan=" + getVlan() + ", netmask=" + getNetmask() + ']';
    }

    public Long getId() {
        return id;
    }

    public String getStartIp() {
        return startIp;
    }

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////
    public String getEndIp() {
        return endIp;
    }

    public Integer getVlan() {
        return vlan;
    }

    public String getNetmask() {
        return netmask;
    }

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException,
            ResourceAllocationException {
        try {
            final StorageNetworkIpRange result = _storageNetworkService.updateIpRange(this);
            final StorageNetworkIpRangeResponse response = _responseGenerator.createStorageNetworkIpRangeResponse(result);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } catch (final Exception e) {
            s_logger.warn("Update storage network IP range failed", e);
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
