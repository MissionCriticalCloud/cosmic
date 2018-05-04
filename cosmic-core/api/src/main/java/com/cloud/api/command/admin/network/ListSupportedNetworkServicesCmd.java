package com.cloud.api.command.admin.network;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListCmd;
import com.cloud.api.Parameter;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.ServiceResponse;
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.legacymodel.network.Network;
import com.cloud.legacymodel.network.Network.Service;
import com.cloud.legacymodel.user.Account;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listSupportedNetworkServices", group = APICommandGroup.NetworkService,
        description = "Lists all network services provided by CloudStack or for the given Provider.",
        responseObject = ServiceResponse.class,
        since = "3.0.0",
        requestHasSensitiveInfo = false,
        responseHasSensitiveInfo = false)
public class ListSupportedNetworkServicesCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListSupportedNetworkServicesCmd.class.getName());
    private static final String s_name = "listsupportednetworkservicesresponse";

    @Parameter(name = ApiConstants.PROVIDER, type = CommandType.STRING, description = "network service provider name")
    private String providerName;

    @Parameter(name = ApiConstants.SERVICE, type = CommandType.STRING, description = "network service name to list providers and capabilities of")
    private String serviceName;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }

    @Override
    public void execute() {
        final List<? extends Network.Service> services;
        if (getServiceName() != null) {
            Network.Service service = null;
            if (serviceName != null) {
                service = Network.Service.getService(serviceName);
                if (service == null) {
                    throw new InvalidParameterValueException("Invalid Network Service=" + serviceName);
                }
            }
            final List<Network.Service> serviceList = new ArrayList<>();
            serviceList.add(service);
            services = serviceList;
        } else {
            services = _networkService.listNetworkServices(getProviderName());
        }

        final ListResponse<ServiceResponse> response = new ListResponse<>();
        final List<ServiceResponse> servicesResponses = new ArrayList<>();
        for (final Network.Service service : services) {
            //skip gateway service
            if (service == Service.Gateway) {
                continue;
            }
            final ServiceResponse serviceResponse = _responseGenerator.createNetworkServiceResponse(service);
            servicesResponses.add(serviceResponse);
        }

        response.setResponses(servicesResponses);
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(final String providerName) {
        this.providerName = providerName;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////
    @Override
    public String getCommandName() {
        return s_name;
    }
}
