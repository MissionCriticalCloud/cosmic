package com.cloud.api.command.admin.address;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.user.address.AssociateIPAddrCmd;
import com.cloud.api.response.IPAddressResponse;
import com.cloud.context.CallContext;
import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.legacymodel.exceptions.InsufficientCapacityException;
import com.cloud.legacymodel.exceptions.ResourceAllocationException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.network.IpAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "associateIpAddress", group = APICommandGroup.PublicIPAddressService, description = "Acquires and associates a public IP to an account.", responseObject = IPAddressResponse
        .class, responseView =
        ResponseView.Full,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class AssociateIPAddrCmdByAdmin extends AssociateIPAddrCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(AssociateIPAddrCmdByAdmin.class.getName());

    @Override
    public void execute() throws ResourceUnavailableException, ResourceAllocationException,
            ConcurrentOperationException, InsufficientCapacityException {
        CallContext.current().setEventDetails("Ip Id: " + getEntityId());

        IpAddress result = null;

        if (getVpcId() != null) {
            result = _vpcService.associateIPToVpc(getEntityId(), getVpcId());
        } else if (getNetworkId() != null) {
            result = _networkService.associateIPToNetwork(getEntityId(), getNetworkId());
        }

        if (result != null) {
            final IPAddressResponse ipResponse = _responseGenerator.createIPAddressResponse(ResponseView.Full, result);
            ipResponse.setResponseName(getCommandName());
            setResponseObject(ipResponse);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to assign ip address");
        }
    }
}
