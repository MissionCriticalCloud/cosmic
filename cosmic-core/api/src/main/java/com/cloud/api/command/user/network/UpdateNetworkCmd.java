package com.cloud.api.command.user.network;

import com.cloud.acl.RoleType;
import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.BaseAsyncCustomIdCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.NetworkOfferingResponse;
import com.cloud.api.response.NetworkResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.network.Network;
import com.cloud.offering.NetworkOffering;
import com.cloud.user.Account;
import com.cloud.user.User;
import com.cloud.utils.exception.InvalidParameterValueException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateNetwork", description = "Updates a network", responseObject = NetworkResponse.class, responseView = ResponseView.Restricted, entityType = {Network.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateNetworkCmd extends BaseAsyncCustomIdCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateNetworkCmd.class.getName());

    private static final String s_name = "updatenetworkresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = NetworkResponse.class,
            required = true, description = "the ID of the network")
    protected Long id;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "the new name for the network")
    private String name;

    @Parameter(name = ApiConstants.DISPLAY_TEXT, type = CommandType.STRING, description = "the new display text for the network")
    private String displayText;

    @Parameter(name = ApiConstants.NETWORK_DOMAIN, type = CommandType.STRING, description = "network domain")
    private String networkDomain;

    @Parameter(name = ApiConstants.CHANGE_CIDR, type = CommandType.BOOLEAN, description = "Force update even if CIDR type is different")
    private Boolean changeCidr;

    @Parameter(name = ApiConstants.NETWORK_OFFERING_ID, type = CommandType.UUID, entityType = NetworkOfferingResponse.class, description = "network offering ID")
    private Long networkOfferingId;

    @Parameter(name = ApiConstants.GUEST_VM_CIDR, type = CommandType.STRING, description = "CIDR for guest VMs, CloudStack allocates IPs to guest VMs only from this CIDR")
    private String guestVmCidr;

    @Parameter(name = ApiConstants.DISPLAY_NETWORK,
            type = CommandType.BOOLEAN,
            description = "an optional field, whether to the display the network to the end user or not.", authorized = {RoleType.Admin})
    private Boolean displayNetwork;

    @Parameter(name = ApiConstants.DNS1, type = CommandType.STRING, description = "The first DNS server of the network")
    private String dns1;

    @Parameter(name = ApiConstants.DNS2, type = CommandType.STRING, description = "The second DNS server of the network")
    private String dns2;

    @Parameter(name = ApiConstants.IP_EXCLUSION_LIST, type = CommandType.STRING, description = "IP exclusion list for private networks")
    private String ipExclusionList;


    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() throws InsufficientCapacityException, ConcurrentOperationException {
        final User callerUser = _accountService.getActiveUser(CallContext.current().getCallingUserId());
        final Account callerAccount = _accountService.getActiveAccountById(callerUser.getAccountId());
        final Network network = _networkService.getNetwork(id);
        if (network == null) {
            throw new InvalidParameterValueException("Couldn't find network by ID");
        }

        final Network result =
                _networkService.updateGuestNetwork(getId(), getNetworkName(), getDisplayText(), callerAccount, callerUser, getNetworkDomain(), getNetworkOfferingId(),
                        getChangeCidr(), getGuestVmCidr(), getDisplayNetwork(), getCustomId(), getDns1(), getDns2(), getIpExclusionList());

        if (result != null) {
            final NetworkResponse response = _responseGenerator.createNetworkResponse(ResponseView.Restricted, result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update network");
        }
    }

    public Long getId() {
        return id;
    }

    public String getNetworkName() {
        return name;
    }

    public String getDisplayText() {
        return displayText;
    }

    public String getNetworkDomain() {
        return networkDomain;
    }

    public Long getNetworkOfferingId() {
        return networkOfferingId;
    }

    public String getDns1() {
        return dns1;
    }

    public String getDns2() {
        return dns2;
    }

    public String getIpExclusionList() { return ipExclusionList; }

    public Boolean getChangeCidr() {
        if (changeCidr != null) {
            return changeCidr;
        }
        return false;
    }

    public String getGuestVmCidr() {
        return guestVmCidr;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public Boolean getDisplayNetwork() {
        return displayNetwork;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Network network = _networkService.getNetwork(id);
        if (network == null) {
            throw new InvalidParameterValueException("Networkd ID=" + id + " doesn't exist");
        } else {
            return _networkService.getNetwork(id).getAccountId();
        }
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_NETWORK_UPDATE;
    }

    @Override
    public String getEventDescription() {
        final StringBuilder eventMsg = new StringBuilder("Updating network: " + getId());
        if (getNetworkOfferingId() != null) {
            final Network network = _networkService.getNetwork(getId());
            if (network == null) {
                throw new InvalidParameterValueException("Networkd ID=" + id + " doesn't exist");
            }
            if (network.getNetworkOfferingId() != getNetworkOfferingId()) {
                final NetworkOffering oldOff = _entityMgr.findById(NetworkOffering.class, network.getNetworkOfferingId());
                final NetworkOffering newOff = _entityMgr.findById(NetworkOffering.class, getNetworkOfferingId());
                if (newOff == null) {
                    throw new InvalidParameterValueException("Networkd offering ID supplied is invalid");
                }

                eventMsg.append(". Original network offering ID: " + oldOff.getUuid() + ", new network offering ID: " + newOff.getUuid());
            }
        }

        return eventMsg.toString();
    }

    @Override
    public String getSyncObjType() {
        return BaseAsyncCmd.networkSyncObject;
    }

    @Override
    public Long getSyncObjId() {
        return id;
    }

    @Override
    public void checkUuid() {
        if (getCustomId() != null) {
            _uuidMgr.checkUuid(getCustomId(), Network.class);
        }
    }
}
