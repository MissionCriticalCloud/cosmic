package org.apache.cloudstack.api.command.admin.network;

import com.cloud.dc.StorageNetworkIpRange;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.PodResponse;
import org.apache.cloudstack.api.response.StorageNetworkIpRangeResponse;
import org.apache.cloudstack.api.response.ZoneResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listStorageNetworkIpRange", description = "List a storage network IP range.", responseObject = StorageNetworkIpRangeResponse.class, since = "3.0.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListStorageNetworkIpRangeCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListStorageNetworkIpRangeCmd.class);

    String _name = "liststoragenetworkiprangeresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID,
            type = CommandType.UUID,
            entityType = StorageNetworkIpRangeResponse.class,
            description = "optional parameter. Storaget network IP range uuid, if specicied, using it to search the range.")
    private Long rangeId;

    @Parameter(name = ApiConstants.POD_ID,
            type = CommandType.UUID,
            entityType = PodResponse.class,
            description = "optional parameter. Pod uuid, if specicied and range uuid is absent, using it to search the range.")
    private Long podId;

    @Parameter(name = ApiConstants.ZONE_ID,
            type = CommandType.UUID,
            entityType = ZoneResponse.class,
            description = "optional parameter. Zone uuid, if specicied and both pod uuid and range uuid are absent, using it to search the range.")
    private Long zoneId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException,
            ResourceAllocationException {
        try {
            final List<StorageNetworkIpRange> results = _storageNetworkService.listIpRange(this);
            final ListResponse<StorageNetworkIpRangeResponse> response = new ListResponse<>();
            final List<StorageNetworkIpRangeResponse> resList = new ArrayList<>(results.size());
            for (final StorageNetworkIpRange r : results) {
                final StorageNetworkIpRangeResponse resp = _responseGenerator.createStorageNetworkIpRangeResponse(r);
                resList.add(resp);
            }
            response.setResponses(resList);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } catch (final Exception e) {
            s_logger.warn("Failed to list storage network ip range for rangeId=" + getRangeId() + " podId=" + getPodId() + " zoneId=" + getZoneId());
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, e.getMessage());
        }
    }

    public Long getRangeId() {
        return rangeId;
    }

    public Long getPodId() {
        return podId;
    }

    public Long getZoneId() {
        return zoneId;
    }

    @Override
    public String getCommandName() {
        return _name;
    }

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }
}
