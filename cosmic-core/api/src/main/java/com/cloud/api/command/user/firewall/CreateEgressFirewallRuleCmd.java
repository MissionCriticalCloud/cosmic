package com.cloud.api.command.user.firewall;

import com.cloud.acl.RoleType;
import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.BaseAsyncCreateCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.FirewallResponse;
import com.cloud.api.response.NetworkResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.legacymodel.exceptions.NetworkRuleConflictException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.network.FirewallRule;
import com.cloud.legacymodel.user.Account;
import com.cloud.network.Network;
import com.cloud.utils.net.NetUtils;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createEgressFirewallRule", group = APICommandGroup.FirewallService, description = "Creates a egress firewall rule for a given network ", responseObject = FirewallResponse.class,
        entityType =
                {FirewallRule.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreateEgressFirewallRuleCmd extends BaseAsyncCreateCmd implements FirewallRule {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateEgressFirewallRuleCmd.class.getName());

    private static final String s_name = "createegressfirewallruleresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.NETWORK_ID,
            type = CommandType.UUID,
            entityType = NetworkResponse.class,
            required = true,
            description = "the network id of the port forwarding rule")
    private Long networkId;

    @Parameter(name = ApiConstants.PROTOCOL,
            type = CommandType.STRING,
            required = true,
            description = "the protocol for the firewall rule. Valid values are TCP/UDP/ICMP.")
    private String protocol;

    @Parameter(name = ApiConstants.START_PORT, type = CommandType.INTEGER, description = "the starting port of firewall rule")
    private Integer publicStartPort;

    @Parameter(name = ApiConstants.END_PORT, type = CommandType.INTEGER, description = "the ending port of firewall rule")
    private Integer publicEndPort;

    @Parameter(name = ApiConstants.CIDR_LIST, type = CommandType.LIST, collectionType = CommandType.STRING, description = "the cidr list to forward traffic from")
    private List<String> cidrlist;

    @Parameter(name = ApiConstants.ICMP_TYPE, type = CommandType.INTEGER, description = "type of the icmp message being sent")
    private Integer icmpType;

    @Parameter(name = ApiConstants.ICMP_CODE, type = CommandType.INTEGER, description = "error code for this icmp message")
    private Integer icmpCode;

    @Parameter(name = ApiConstants.TYPE, type = CommandType.STRING, description = "type of firewallrule: system/user")
    private String type;

    @Parameter(name = ApiConstants.FOR_DISPLAY, type = CommandType.BOOLEAN, description = "an optional field, whether to the display the rule to the end user or not", since = "4" +
            ".4", authorized = {RoleType.Admin})
    private Boolean display;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    public Long getIpAddressId() {
        return null;
    }

    @Override
    public void execute() throws ResourceUnavailableException {
        final CallContext callerContext = CallContext.current();
        boolean success = false;
        FirewallRule rule = _entityMgr.findById(FirewallRule.class, getEntityId());
        try {
            CallContext.current().setEventDetails("Rule Id: " + getEntityId());
            success = _firewallService.applyEgressFirewallRules(rule, callerContext.getCallingAccount());
            // State is different after the rule is applied, so get new object here
            rule = _entityMgr.findById(FirewallRule.class, getEntityId());
            FirewallResponse fwResponse = new FirewallResponse();
            if (rule != null) {
                fwResponse = _responseGenerator.createFirewallResponse(rule);
                setResponseObject(fwResponse);
            }
            fwResponse.setResponseName(getCommandName());
        } finally {
            if (!success || rule == null) {
                _firewallService.revokeEgressFirewallRule(getEntityId(), true);
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create firewall rule");
            }
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Account account = CallContext.current().getCallingAccount();

        if (account != null) {
            return account.getId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public boolean isDisplay() {
        if (display != null) {
            return display;
        } else {
            return true;
        }
    }

    @Override
    public long getId() {
        throw new UnsupportedOperationException("database id can only provided by VO objects");
    }

    @Override
    public String getXid() {
        // FIXME: We should allow for end user to specify Xid.
        return null;
    }

    @Override
    public Integer getSourcePortStart() {
        if (publicStartPort != null) {
            return publicStartPort;
        }
        return null;
    }

    @Override
    public Integer getSourcePortEnd() {
        if (publicEndPort == null) {
            if (publicStartPort != null) {
                return publicStartPort;
            }
        } else {
            return publicEndPort;
        }
        return null;
    }

    @Override
    public String getProtocol() {
        return protocol.trim();
    }

    @Override
    public Purpose getPurpose() {
        return Purpose.Firewall;
    }

    @Override
    public State getState() {
        throw new UnsupportedOperationException("Should never call me to find the state");
    }

    @Override
    public long getNetworkId() {
        return networkId;
    }

    @Override
    public Long getSourceIpAddressId() {
        return null;
    }

    @Override
    public Integer getIcmpCode() {
        if (icmpCode != null) {
            return icmpCode;
        } else if (protocol.equalsIgnoreCase(NetUtils.ICMP_PROTO)) {
            return -1;
        }
        return null;
    }

    @Override
    public Integer getIcmpType() {
        if (icmpType != null) {
            return icmpType;
        } else if (protocol.equalsIgnoreCase(NetUtils.ICMP_PROTO)) {
            return -1;
        }
        return null;
    }

    @Override
    public List<String> getSourceCidrList() {
        if (cidrlist != null) {
            return cidrlist;
        } else {
            final List<String> oneCidrList = new ArrayList<>();
            oneCidrList.add(_networkService.getNetwork(networkId).getCidr());
            return oneCidrList;
        }
    }

    public void setSourceCidrList(final List<String> cidrs) {
        cidrlist = cidrs;
    }

    @Override
    public Long getRelated() {
        return null;
    }

    @Override
    public FirewallRuleType getType() {
        if (type != null && type.equalsIgnoreCase("system")) {
            return FirewallRuleType.System;
        } else {
            return FirewallRuleType.User;
        }
    }

    @Override
    public TrafficType getTrafficType() {
        return TrafficType.Egress;
    }

    @Override
    public long getDomainId() {
        final Network network = _networkService.getNetwork(networkId);
        return network.getDomainId();
    }

    @Override
    public void create() {
        if (getSourceCidrList() != null) {
            final String guestCidr = _networkService.getNetwork(getNetworkId()).getCidr();

            for (final String cidr : getSourceCidrList()) {
                if (!NetUtils.isValidIp4Cidr(cidr) && !NetUtils.isValidIp6Cidr(cidr)) {
                    throw new ServerApiException(ApiErrorCode.PARAM_ERROR, "Source cidrs formatting error " + cidr);
                }
                if (cidr.equals(NetUtils.ALL_IP4_CIDRS)) {
                    continue;
                }
                if (!NetUtils.isNetworkAWithinNetworkB(cidr, guestCidr)) {
                    throw new ServerApiException(ApiErrorCode.PARAM_ERROR, cidr + " is not within the guest cidr " + guestCidr);
                }
            }
        }
        if (getProtocol().equalsIgnoreCase(NetUtils.ALL_PROTO)) {
            if (getSourcePortStart() != null && getSourcePortEnd() != null) {
                throw new InvalidParameterValueException("Do not pass ports to protocol ALL, protocol ALL do not require ports. Unable to create " +
                        "firewall rule for the network id=" + networkId);
            }
        }

        if (getVpcId() != null) {
            throw new InvalidParameterValueException("Unable to create firewall rule for the network id=" + networkId +
                    " as firewall egress rule can be created only for non vpc networks.");
        }

        try {
            final FirewallRule result = _firewallService.createEgressFirewallRule(this);
            if (result != null) {
                setEntityId(result.getId());
                setEntityUuid(result.getUuid());
            }
        } catch (final NetworkRuleConflictException ex) {
            s_logger.info("Network rule conflict: " + ex.getMessage());
            s_logger.trace("Network Rule Conflict: ", ex);
            throw new ServerApiException(ApiErrorCode.NETWORK_RULE_CONFLICT_ERROR, ex.getMessage());
        }
    }

    public Long getVpcId() {
        final Network network = _networkService.getNetwork(getNetworkId());
        if (network == null) {
            throw new InvalidParameterValueException("Invalid networkId is given");
        }

        final Long vpcId = network.getVpcId();
        return vpcId;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_FIREWALL_EGRESS_OPEN;
    }

    @Override
    public String getEventDescription() {
        final Network network = _networkService.getNetwork(networkId);
        return ("Creating firewall rule for network: " + network + " for protocol:" + getProtocol());
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.FirewallRule;
    }

    @Override
    public String getSyncObjType() {
        return BaseAsyncCmd.networkSyncObject;
    }

    @Override
    public Long getSyncObjId() {
        return getNetworkId();
    }

    @Override
    public long getAccountId() {
        final Network network = _networkService.getNetwork(networkId);
        return network.getAccountId();
    }

    @Override
    public String getUuid() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<?> getEntityType() {
        return FirewallRule.class;
    }
}
