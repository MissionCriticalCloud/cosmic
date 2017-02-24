package com.cloud.api.command.admin.network;

import com.cloud.api.APICommand;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.user.network.UpdateNetworkCmd;
import com.cloud.api.response.NetworkResponse;
import com.cloud.context.CallContext;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.network.Network;
import com.cloud.user.Account;
import com.cloud.user.User;
import com.cloud.utils.exception.InvalidParameterValueException;

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

        final Network result =
                _networkService.updateGuestNetwork(getId(), getNetworkName(), getDisplayText(), callerAccount, callerUser, getNetworkDomain(), getNetworkOfferingId(),
                        getChangeCidr(), getGuestVmCidr(), getDisplayNetwork(), getCustomId(), getDns1(), getDns2(), getIpExclusionList());

        if (result != null) {
            final NetworkResponse response = _responseGenerator.createNetworkResponse(ResponseView.Full, result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update network");
        }
    }
}
