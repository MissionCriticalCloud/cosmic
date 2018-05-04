package com.cloud.api.command.admin.network;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
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

@APICommand(name = "updateStorageNetworkIpRange", group = APICommandGroup.NetworkService,
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
