package com.cloud.api.command.admin.network;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.NetworkDeviceResponse;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.legacymodel.exceptions.InsufficientCapacityException;
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.legacymodel.exceptions.ResourceAllocationException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.network.ExternalNetworkDeviceManager;

import javax.inject.Inject;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "addNetworkDevice", group = APICommandGroup.NetworkDeviceService,
        description = "Adds a network device of one of the following types: ExternalDhcp, ExternalLoadBalancer",
        responseObject = NetworkDeviceResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class AddNetworkDeviceCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(AddNetworkDeviceCmd.class);
    private static final String s_name = "addnetworkdeviceresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Inject
    ExternalNetworkDeviceManager nwDeviceMgr;
    @Parameter(name = ApiConstants.NETWORK_DEVICE_TYPE,
            type = CommandType.STRING,
            description = "Network device type, now supports ExternalDhcp, JuniperSRXFirewall, PaloAltoFirewall")
    private String type;

    @Parameter(name = ApiConstants.NETWORK_DEVICE_PARAMETER_LIST, type = CommandType.MAP, description = "parameters for network device")
    private Map paramList;

    public String getDeviceType() {
        return type;
    }

    public Map getParamList() {
        return paramList;
    }

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException,
            ResourceAllocationException {
        try {
            final Host device = nwDeviceMgr.addNetworkDevice(this);
            final NetworkDeviceResponse response = nwDeviceMgr.getApiResponse(device);
            response.setObjectName("networkdevice");
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } catch (final InvalidParameterValueException ipve) {
            throw new ServerApiException(ApiErrorCode.PARAM_ERROR, ipve.getMessage());
        } catch (final CloudRuntimeException cre) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, cre.getMessage());
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        // TODO Auto-generated method stub
        return 0;
    }
}
