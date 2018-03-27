package com.cloud.api.command.user.vm;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.NicSecondaryIpResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.context.CallContext;
import com.cloud.dc.DataCenter;
import com.cloud.event.EventTypes;
import com.cloud.model.enumeration.NetworkType;
import com.cloud.network.Network;
import com.cloud.user.Account;
import com.cloud.utils.exception.InvalidParameterValueException;
import com.cloud.vm.NicSecondaryIp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "removeIpFromNic", group = APICommandGroup.NicService, description = "Removes secondary IP from the NIC.", responseObject = SuccessResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class RemoveIpFromVmNicCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(RemoveIpFromVmNicCmd.class.getName());
    private static final String s_name = "removeipfromnicresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.ID,
            type = CommandType.UUID,
            required = true,
            entityType = NicSecondaryIpResponse.class,
            description = "the ID of the secondary ip address to nic")
    private Long id;

    // unexposed parameter needed for events logging
    @Parameter(name = ApiConstants.ACCOUNT_ID, type = CommandType.UUID, expose = false)
    private Long ownerId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public static String getResultObjectName() {
        return "addressinfo";
    }

    public String getEntityTable() {
        return "nic_secondary_ips";
    }

    public String getAccountName() {
        return CallContext.current().getCallingAccount().getAccountName();
    }

    public long getDomainId() {
        return CallContext.current().getCallingAccount().getDomainId();
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_NIC_SECONDARY_IP_UNASSIGN;
    }

    @Override
    public String getEventDescription() {
        return ("Disassociating ip address with id=" + id);
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.IpAddress;
    }

    @Override
    public String getSyncObjType() {
        return BaseAsyncCmd.networkSyncObject;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public NetworkType getNetworkType() {
        final Network ntwk = _entityMgr.findById(Network.class, getNetworkId());
        if (ntwk != null) {
            final DataCenter dc = _entityMgr.findById(DataCenter.class, ntwk.getDataCenterId());
            return dc.getNetworkType();
        }
        return null;
    }

    public Long getNetworkId() {
        final NicSecondaryIp nicSecIp = _entityMgr.findById(NicSecondaryIp.class, getIpAddressId());
        if (nicSecIp != null) {
            final Long networkId = nicSecIp.getNetworkId();
            return networkId;
        } else {
            return null;
        }
    }

    public Long getIpAddressId() {
        return id;
    }

    @Override
    public void execute() throws InvalidParameterValueException {
        CallContext.current().setEventDetails("Ip Id: " + id);
        final NicSecondaryIp nicSecIp = getIpEntry();

        if (nicSecIp == null) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Invalid IP id is passed");
        }

        try {
            final boolean result = _networkService.releaseSecondaryIpFromNic(id);
            if (result) {
                final SuccessResponse response = new SuccessResponse(getCommandName());
                setResponseObject(response);
            } else {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to remove secondary  ip address for the nic");
            }
        } catch (final InvalidParameterValueException e) {
            throw new InvalidParameterValueException("Removing guest ip from nic failed");
        }
    }

    public NicSecondaryIp getIpEntry() {
        final NicSecondaryIp nicSecIp = _entityMgr.findById(NicSecondaryIp.class, getIpAddressId());
        return nicSecIp;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Account caller = CallContext.current().getCallingAccount();
        return caller.getAccountId();
    }
}
