package org.apache.cloudstack.api.command.admin.address;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.IpAddress;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.user.address.AssociateIPAddrCmd;
import org.apache.cloudstack.api.response.IPAddressResponse;
import org.apache.cloudstack.context.CallContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "associateIpAddress", description = "Acquires and associates a public IP to an account.", responseObject = IPAddressResponse.class, responseView =
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
