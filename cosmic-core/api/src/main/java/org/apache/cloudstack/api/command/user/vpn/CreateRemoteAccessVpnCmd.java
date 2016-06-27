package org.apache.cloudstack.api.command.user.vpn;

import com.cloud.event.EventTypes;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.IpAddress;
import com.cloud.network.RemoteAccessVpn;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.BaseAsyncCreateCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.DomainResponse;
import org.apache.cloudstack.api.response.IPAddressResponse;
import org.apache.cloudstack.api.response.RemoteAccessVpnResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createRemoteAccessVpn", description = "Creates a l2tp/ipsec remote access vpn", responseObject = RemoteAccessVpnResponse.class, entityType = {RemoteAccessVpn
        .class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreateRemoteAccessVpnCmd extends BaseAsyncCreateCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateRemoteAccessVpnCmd.class.getName());

    private static final String s_name = "createremoteaccessvpnresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.PUBLIC_IP_ID,
            type = CommandType.UUID,
            entityType = IPAddressResponse.class,
            required = true,
            description = "public ip address id of the vpn server")
    private Long publicIpId;

    @Parameter(name = "iprange",
            type = CommandType.STRING,
            required = false,
            description = "the range of ip addresses to allocate to vpn clients. The first ip in the range will be taken by the vpn server")
    private String ipRange;

    @Deprecated
    @Parameter(name = ApiConstants.ACCOUNT, type = CommandType.STRING, description = "an optional account for the VPN. Must be used with domainId.")
    private String accountName;

    @Deprecated
    @Parameter(name = ApiConstants.DOMAIN_ID,
            type = CommandType.UUID,
            entityType = DomainResponse.class,
            description = "an optional domainId for the VPN. If the account parameter is used, domainId must also be used.")
    private Long domainId;

    @Parameter(name = ApiConstants.OPEN_FIREWALL,
            type = CommandType.BOOLEAN,
            description = "if true, firewall rule for source/end public port is automatically created; if false - firewall rule has to be created explicitely. Has value true by " +
                    "default")
    private Boolean openFirewall;

    @Parameter(name = ApiConstants.FOR_DISPLAY, type = CommandType.BOOLEAN, description = "an optional field, whether to the display the vpn to the end user or not", since = "4" +
            ".4", authorized = {RoleType.Admin})
    private Boolean display;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getPublicIpId() {
        return publicIpId;
    }

    public String getAccountName() {
        return accountName;
    }

    public Long getDomainId() {
        return domainId;
    }

    public String getIpRange() {
        return ipRange;
    }

    public void setIpRange(final String ipRange) {
        this.ipRange = ipRange;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_REMOTE_ACCESS_VPN_CREATE;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventDescription() {
        return "Create Remote Access VPN for account " + getEntityOwnerId() + " using public ip id=" + publicIpId;
    }

    @Override
    public String getSyncObjType() {
        return BaseAsyncCmd.networkSyncObject;
    }

    @Override
    public Long getSyncObjId() {
        return getIp().getAssociatedWithNetworkId();
    }

    private IpAddress getIp() {
        final IpAddress ip = _networkService.getIp(publicIpId);
        if (ip == null) {
            throw new InvalidParameterValueException("Unable to find ip address by id " + publicIpId);
        }
        return ip;
    }

    @Override
    public void create() {
        try {
            final RemoteAccessVpn vpn = _ravService.createRemoteAccessVpn(publicIpId, ipRange, getOpenFirewall(), isDisplay());
            if (vpn != null) {
                setEntityId(vpn.getId());
                setEntityUuid(vpn.getUuid());
            } else {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create remote access vpn");
            }
        } catch (final NetworkRuleConflictException e) {
            s_logger.info("Network rule conflict: " + e.getMessage());
            s_logger.trace("Network Rule Conflict: ", e);
            throw new ServerApiException(ApiErrorCode.NETWORK_RULE_CONFLICT_ERROR, e.getMessage());
        }
    }

    public Boolean getOpenFirewall() {
        if (openFirewall != null) {
            return openFirewall;
        } else {
            return true;
        }
    }

    @Override
    public void execute() {
        try {
            final RemoteAccessVpn result = _ravService.startRemoteAccessVpn(publicIpId, getOpenFirewall());
            if (result != null) {
                final RemoteAccessVpnResponse response = _responseGenerator.createRemoteAccessVpnResponse(result);
                response.setResponseName(getCommandName());
                setResponseObject(response);
            } else {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create remote access vpn");
            }
        } catch (final ResourceUnavailableException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.RESOURCE_UNAVAILABLE_ERROR, ex.getMessage());
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final IpAddress ip = _networkService.getIp(publicIpId);

        if (ip == null) {
            throw new InvalidParameterValueException("Unable to find ip address by id=" + publicIpId);
        }

        return ip.getAccountId();
    }

    @Override
    public boolean isDisplay() {
        if (display == null) {
            return true;
        } else {
            return display;
        }
    }
}
