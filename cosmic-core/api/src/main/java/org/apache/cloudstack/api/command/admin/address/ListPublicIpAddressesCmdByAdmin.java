package org.apache.cloudstack.api.command.admin.address;

import com.cloud.network.IpAddress;
import com.cloud.utils.Pair;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.command.user.address.ListPublicIpAddressesCmd;
import org.apache.cloudstack.api.response.IPAddressResponse;
import org.apache.cloudstack.api.response.ListResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listPublicIpAddresses", description = "Lists all public ip addresses", responseObject = IPAddressResponse.class, responseView = ResponseView.Full,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false, entityType = {IpAddress.class})
public class ListPublicIpAddressesCmdByAdmin extends ListPublicIpAddressesCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListPublicIpAddressesCmdByAdmin.class.getName());

    @Override
    public void execute() {
        final Pair<List<? extends IpAddress>, Integer> result = _mgr.searchForIPAddresses(this);
        final ListResponse<IPAddressResponse> response = new ListResponse<>();
        final List<IPAddressResponse> ipAddrResponses = new ArrayList<>();
        for (final IpAddress ipAddress : result.first()) {
            final IPAddressResponse ipResponse = _responseGenerator.createIPAddressResponse(ResponseView.Full, ipAddress);
            ipResponse.setObjectName("publicipaddress");
            ipAddrResponses.add(ipResponse);
        }

        response.setResponses(ipAddrResponses, result.second());
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }
}
