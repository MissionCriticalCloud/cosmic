package com.cloud.api.command.admin.pod;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.PodResponse;
import com.cloud.api.response.ZoneResponse;
import com.cloud.legacymodel.dc.Pod;
import com.cloud.legacymodel.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createPod", group = APICommandGroup.PodService, description = "Creates a new Pod.", responseObject = PodResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreatePodCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CreatePodCmd.class.getName());

    private static final String s_name = "createpodresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, required = true, description = "the name of the Pod")
    private String podName;

    @Parameter(name = ApiConstants.ZONE_ID,
            type = CommandType.UUID,
            entityType = ZoneResponse.class,
            required = true,
            description = "the Zone ID in which the Pod will be created")
    private Long zoneId;

    @Parameter(name = ApiConstants.START_IP, type = CommandType.STRING, required = true, description = "the starting IP address for the Pod")
    private String startIp;

    @Parameter(name = ApiConstants.END_IP, type = CommandType.STRING, description = "the ending IP address for the Pod")
    private String endIp;

    @Parameter(name = ApiConstants.NETMASK, type = CommandType.STRING, required = true, description = "the netmask for the Pod")
    private String netmask;

    @Parameter(name = ApiConstants.GATEWAY, type = CommandType.STRING, required = true, description = "the gateway for the Pod")
    private String gateway;

    @Parameter(name = ApiConstants.ALLOCATION_STATE, type = CommandType.STRING, description = "Allocation state of this Pod for allocation of new resources")
    private String allocationState;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final Pod result = _configService.createPod(getZoneId(), getPodName(), getStartIp(), getEndIp(), getGateway(), getNetmask(), getAllocationState());
        if (result != null) {
            final PodResponse response = _responseGenerator.createPodResponse(result, false);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create pod");
        }
    }

    public Long getZoneId() {
        return zoneId;
    }

    public String getPodName() {
        return podName;
    }

    public String getStartIp() {
        return startIp;
    }

    public String getEndIp() {
        return endIp;
    }

    public String getGateway() {
        return gateway;
    }

    public String getNetmask() {
        return netmask;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public String getAllocationState() {
        return allocationState;
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
