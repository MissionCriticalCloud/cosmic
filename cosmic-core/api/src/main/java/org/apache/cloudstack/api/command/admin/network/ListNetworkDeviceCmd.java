package org.apache.cloudstack.api.command.admin.network;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.host.Host;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.NetworkDeviceResponse;
import org.apache.cloudstack.network.ExternalNetworkDeviceManager;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@org.apache.cloudstack.api.APICommand(name = "listNetworkDevice", description = "List network devices", responseObject = NetworkDeviceResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListNetworkDeviceCmd extends org.apache.cloudstack.api.BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListNetworkDeviceCmd.class);
    private static final String s_name = "listnetworkdevice";

    @Inject
    ExternalNetworkDeviceManager nwDeviceMgr;
    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = org.apache.cloudstack.api.ApiConstants.NETWORK_DEVICE_TYPE,
            type = CommandType.STRING,
            description = "Network device type, now supports ExternalDhcp, JuniperSRXFirewall, PaloAltoFirewall")
    private String type;

    @Parameter(name = org.apache.cloudstack.api.ApiConstants.NETWORK_DEVICE_PARAMETER_LIST, type = CommandType.MAP, description = "parameters for network device")
    private Map paramList;

    public String getDeviceType() {
        return type;
    }

    public Map getParamList() {
        return paramList;
    }

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, org.apache.cloudstack.api.ServerApiException, ConcurrentOperationException,
            ResourceAllocationException {
        try {
            final List<Host> devices = nwDeviceMgr.listNetworkDevice(this);
            final List<NetworkDeviceResponse> nwdeviceResponses = new ArrayList<>();
            final ListResponse<NetworkDeviceResponse> listResponse = new ListResponse<>();
            for (final Host d : devices) {
                final NetworkDeviceResponse response = nwDeviceMgr.getApiResponse(d);
                response.setObjectName("networkdevice");
                response.setResponseName(getCommandName());
                nwdeviceResponses.add(response);
            }

            listResponse.setResponses(nwdeviceResponses);
            listResponse.setResponseName(getCommandName());
            this.setResponseObject(listResponse);
        } catch (final InvalidParameterValueException ipve) {
            throw new org.apache.cloudstack.api.ServerApiException(org.apache.cloudstack.api.ApiErrorCode.PARAM_ERROR, ipve.getMessage());
        } catch (final CloudRuntimeException cre) {
            throw new org.apache.cloudstack.api.ServerApiException(org.apache.cloudstack.api.ApiErrorCode.INTERNAL_ERROR, cre.getMessage());
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
