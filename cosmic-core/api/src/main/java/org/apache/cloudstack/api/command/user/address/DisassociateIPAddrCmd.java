package org.apache.cloudstack.api.command.user.address;

import com.cloud.event.EventTypes;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.network.IpAddress;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.AccountResponse;
import org.apache.cloudstack.api.response.IPAddressResponse;
import org.apache.cloudstack.api.response.SuccessResponse;
import org.apache.cloudstack.context.CallContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "disassociateIpAddress", description = "Disassociates an IP address from the account.", responseObject = SuccessResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false, entityType = {IpAddress.class})
public class DisassociateIPAddrCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DisassociateIPAddrCmd.class.getName());

    private static final String s_name = "disassociateipaddressresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = IPAddressResponse.class, required = true, description = "the ID of the public IP address"
            + " to disassociate")
    private Long id;

    // unexposed parameter needed for events logging
    @Parameter(name = ApiConstants.ACCOUNT_ID, type = CommandType.UUID, entityType = AccountResponse.class, expose = false)
    private Long ownerId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() throws InsufficientAddressCapacityException {
        CallContext.current().setEventDetails("IP ID: " + getIpAddressId());
        boolean result = false;
        if (!isPortable(id)) {
            result = _networkService.releaseIpAddress(getIpAddressId());
        } else {
            result = _networkService.releasePortableIpAddress(getIpAddressId());
        }
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to disassociate IP address");
        }
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public Long getIpAddressId() {
        return id;
    }

    private boolean isPortable(final long id) {
        final IpAddress ip = getIpAddress(id);
        return ip.isPortable();
    }

    private IpAddress getIpAddress(final long id) {
        final IpAddress ip = _entityMgr.findById(IpAddress.class, id);

        if (ip == null) {
            throw new InvalidParameterValueException("Unable to find IP address by ID=" + id);
        } else {
            return ip;
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        if (ownerId == null) {
            final IpAddress ip = getIpAddress(id);
            if (ip == null) {
                throw new InvalidParameterValueException("Unable to find IP address by ID=" + id);
            }
            ownerId = ip.getAccountId();
        }

        if (ownerId == null) {
            return Account.ACCOUNT_ID_SYSTEM;
        }
        return ownerId;
    }

    @Override
    public String getEventType() {
        if (!isPortable(id)) {
            return EventTypes.EVENT_NET_IP_RELEASE;
        } else {
            return EventTypes.EVENT_PORTABLE_IP_RELEASE;
        }
    }

    @Override
    public String getEventDescription() {
        return ("Disassociating IP address with ID=" + id);
    }

    @Override
    public Long getInstanceId() {
        return getIpAddressId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.IpAddress;
    }

    @Override
    public String getSyncObjType() {
        return BaseAsyncCmd.networkSyncObject;
    }

    @Override
    public Long getSyncObjId() {
        final IpAddress ip = getIpAddress(id);
        return ip.getAssociatedWithNetworkId();
    }
}
