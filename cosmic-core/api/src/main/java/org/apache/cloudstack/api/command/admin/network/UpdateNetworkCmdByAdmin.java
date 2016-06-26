package org.apache.cloudstack.api.command.admin.network;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.network.Network;
import com.cloud.user.Account;
import com.cloud.user.User;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.user.network.UpdateNetworkCmd;
import org.apache.cloudstack.api.response.NetworkResponse;
import org.apache.cloudstack.context.CallContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateNetwork", description = "Updates a network", responseObject = NetworkResponse.class, responseView = ResponseView.Full, entityType = {Network.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateNetworkCmdByAdmin extends UpdateNetworkCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateNetworkCmdByAdmin.class.getName());

    @Override
    public void execute() throws InsufficientCapacityException, ConcurrentOperationException {
        final User callerUser = _accountService.getActiveUser(CallContext.current().getCallingUserId());
        final Account callerAccount = _accountService.getActiveAccountById(callerUser.getAccountId());
        final Network network = _networkService.getNetwork(id);
        if (network == null) {
            throw new InvalidParameterValueException("Couldn't find network by id");
        }

        final Network result = _networkService.updateGuestNetwork(getId(), getNetworkName(), getDisplayText(), callerAccount,
                callerUser, getNetworkDomain(), getNetworkOfferingId(), getChangeCidr(), getGuestVmCidr(), getDisplayNetwork(), getCustomId());

        if (result != null) {
            final NetworkResponse response = _responseGenerator.createNetworkResponse(ResponseView.Full, result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update network");
        }
    }
}
