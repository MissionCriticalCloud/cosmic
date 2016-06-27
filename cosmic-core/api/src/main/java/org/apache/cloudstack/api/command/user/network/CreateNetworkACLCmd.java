package org.apache.cloudstack.api.command.user.network;

import com.cloud.event.EventTypes;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.vpc.NetworkACLItem;
import com.cloud.user.Account;
import com.cloud.utils.net.NetUtils;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCreateCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.NetworkACLItemResponse;
import org.apache.cloudstack.api.response.NetworkACLResponse;
import org.apache.cloudstack.api.response.NetworkResponse;
import org.apache.cloudstack.context.CallContext;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createNetworkACL",
        description = "Creates a ACL rule in the given network (the network has to belong to VPC)",
        responseObject = NetworkACLItemResponse.class,
        requestHasSensitiveInfo = false,
        responseHasSensitiveInfo = false)
public class CreateNetworkACLCmd extends BaseAsyncCreateCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateNetworkACLCmd.class.getName());

    private static final String s_name = "createnetworkaclresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.PROTOCOL,
            type = CommandType.STRING,
            required = true,
            description = "the protocol for the ACL rule. Valid values are TCP/UDP/ICMP/ALL or valid protocol number")
    private String protocol;

    @Parameter(name = ApiConstants.START_PORT, type = CommandType.INTEGER, description = "the starting port of ACL")
    private Integer publicStartPort;

    @Parameter(name = ApiConstants.END_PORT, type = CommandType.INTEGER, description = "the ending port of ACL")
    private Integer publicEndPort;

    @Parameter(name = ApiConstants.CIDR_LIST, type = CommandType.LIST, collectionType = CommandType.STRING, description = "the CIDR list to allow traffic from/to")
    private List<String> cidrlist;

    @Parameter(name = ApiConstants.ICMP_TYPE, type = CommandType.INTEGER, description = "type of the ICMP message being sent")
    private Integer icmpType;

    @Parameter(name = ApiConstants.ICMP_CODE, type = CommandType.INTEGER, description = "error code for this ICMP message")
    private Integer icmpCode;

    @Parameter(name = ApiConstants.NETWORK_ID,
            type = CommandType.UUID,
            entityType = NetworkResponse.class,
            description = "The network of the VM the ACL will be created for")
    private Long networkId;

    @Parameter(name = ApiConstants.ACL_ID,
            type = CommandType.UUID,
            entityType = NetworkACLResponse.class,
            description = "The network of the VM the ACL will be created for")
    private Long aclId;

    @Parameter(name = ApiConstants.TRAFFIC_TYPE, type = CommandType.STRING, description = "the traffic type for the ACL,"
            + "can be ingress or egress, defaulted to ingress if not specified")
    private String trafficType;

    @Parameter(name = ApiConstants.NUMBER, type = CommandType.INTEGER, description = "The network of the VM the ACL will be created for")
    private Integer number;

    @Parameter(name = ApiConstants.ACTION, type = CommandType.STRING, description = "scl entry action, allow or deny")
    private String action;

    @Parameter(name = ApiConstants.FOR_DISPLAY, type = CommandType.BOOLEAN, description = "an optional field, whether to the display the rule to the end user or not", since = "4" +
            ".4", authorized = {RoleType.Admin})
    private Boolean display;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////
    @Deprecated
    public Boolean getDisplay() {
        return display;
    }

    public List<String> getSourceCidrList() {
        if (cidrlist != null) {
            return cidrlist;
        } else {
            final List<String> oneCidrList = new ArrayList<>();
            oneCidrList.add(NetUtils.ALL_CIDRS);
            return oneCidrList;
        }
    }

    public NetworkACLItem.TrafficType getTrafficType() {
        if (trafficType == null) {
            return NetworkACLItem.TrafficType.Ingress;
        }
        for (final NetworkACLItem.TrafficType type : NetworkACLItem.TrafficType.values()) {
            if (type.toString().equalsIgnoreCase(trafficType)) {
                return type;
            }
        }
        throw new InvalidParameterValueException("Invalid traffic type " + trafficType);
    }

    public String getAction() {
        return action;
    }

    public Integer getNumber() {
        return number;
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    public Integer getSourcePortStart() {
        return publicStartPort;
    }

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

    public Long getNetworkId() {
        return networkId;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_NETWORK_ACL_ITEM_CREATE;
    }

    @Override
    public String getEventDescription() {
        return "Creating Network ACL Item";
    }

    public Integer getIcmpCode() {
        if (icmpCode != null) {
            return icmpCode;
        } else if (getProtocol().equalsIgnoreCase(NetUtils.ICMP_PROTO)) {
            return -1;
        }
        return null;
    }

    public String getProtocol() {
        String p = protocol.trim();
        // Deal with ICMP(protocol number 1) specially because it need to be paired with icmp type and code
        if (StringUtils.isNumeric(p)) {
            final int protoNumber = Integer.parseInt(p);
            if (protoNumber == 1) {
                p = "icmp";
            }
        }
        return p;
    }

    public Integer getIcmpType() {
        if (icmpType != null) {
            return icmpType;
        } else if (getProtocol().equalsIgnoreCase(NetUtils.ICMP_PROTO)) {
            return -1;
        }
        return null;
    }

    public Long getACLId() {
        return aclId;
    }

    @Override
    public void create() {
        final NetworkACLItem result = _networkACLService.createNetworkACLItem(this);
        setEntityId(result.getId());
        setEntityUuid(result.getUuid());
    }

    @Override
    public void execute() throws ResourceUnavailableException {
        boolean success = false;
        NetworkACLItem rule = _networkACLService.getNetworkACLItem(getEntityId());
        try {
            CallContext.current().setEventDetails("Rule ID: " + getEntityId());
            success = _networkACLService.applyNetworkACL(rule.getAclId());

            // State is different after the rule is applied, so get new object here
            rule = _networkACLService.getNetworkACLItem(getEntityId());
            NetworkACLItemResponse aclResponse = new NetworkACLItemResponse();
            if (rule != null) {
                aclResponse = _responseGenerator.createNetworkACLItemResponse(rule);
                setResponseObject(aclResponse);
            }
            aclResponse.setResponseName(getCommandName());
        } finally {
            if (!success || rule == null) {
                _networkACLService.revokeNetworkACLItem(getEntityId());
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create network ACL Item");
            }
        }
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

    @Override
    public boolean isDisplay() {
        if (display != null) {
            return display;
        } else {
            return true;
        }
    }
}
