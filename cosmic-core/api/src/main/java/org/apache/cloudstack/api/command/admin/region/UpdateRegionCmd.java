package org.apache.cloudstack.api.command.admin.region;

import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.RegionResponse;
import org.apache.cloudstack.region.Region;
import org.apache.cloudstack.region.RegionService;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateRegion", description = "Updates a region", responseObject = RegionResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateRegionCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateRegionCmd.class.getName());
    private static final String s_name = "updateregionresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Inject
    RegionService _regionService;
    @Parameter(name = ApiConstants.ID, type = CommandType.INTEGER, required = true, description = "Id of region to update")
    private Integer id;
    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "updates region with this name")
    private String regionName;
    @Parameter(name = ApiConstants.END_POINT, type = CommandType.STRING, description = "updates region with this end point")
    private String endPoint;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final Region region = _regionService.updateRegion(getId(), getRegionName(), getEndPoint());
        if (region != null) {
            final RegionResponse response = _responseGenerator.createRegionResponse(region);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update Region");
        }
    }

    public Integer getId() {
        return id;
    }

    public String getRegionName() {
        return regionName;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public String getEndPoint() {
        return endPoint;
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
