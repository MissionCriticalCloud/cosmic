package com.cloud.api.command.admin.region;

import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.RegionResponse;
import com.cloud.region.Region;
import com.cloud.region.RegionService;
import com.cloud.user.Account;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "addRegion", description = "Adds a Region", responseObject = RegionResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class AddRegionCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(AddRegionCmd.class.getName());

    private static final String s_name = "addregionresponse";
    @Inject
    public RegionService _regionService;
    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.ID, type = CommandType.INTEGER, required = true, description = "Id of the Region")
    private Integer id;
    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, required = true, description = "Name of the region")
    private String regionName;
    @Parameter(name = ApiConstants.END_POINT, type = CommandType.STRING, required = true, description = "Region service endpoint")
    private String endPoint;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final Region region = _regionService.addRegion(getId(), getRegionName(), getEndPoint());
        if (region != null) {
            final RegionResponse response = _responseGenerator.createRegionResponse(region);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to add Region");
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
