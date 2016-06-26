package org.apache.cloudstack.api.command.user.firewall;

import com.cloud.event.EventTypes;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.IpAddress;
import com.cloud.network.rules.FirewallRule;
import com.cloud.network.rules.PortForwardingRule;
import com.cloud.user.Account;
import com.cloud.utils.net.Ip;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.VirtualMachine;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.acl.SecurityChecker.AccessType;
import org.apache.cloudstack.api.ACL;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.BaseAsyncCreateCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.FirewallRuleResponse;
import org.apache.cloudstack.api.response.IPAddressResponse;
import org.apache.cloudstack.api.response.NetworkResponse;
import org.apache.cloudstack.api.response.UserVmResponse;
import org.apache.cloudstack.context.CallContext;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createPortForwardingRule", description = "Creates a port forwarding rule", responseObject = FirewallRuleResponse.class, entityType = {FirewallRule.class,
        VirtualMachine.class, IpAddress.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreatePortForwardingRuleCmd extends BaseAsyncCreateCmd implements PortForwardingRule {
    public static final Logger s_logger = LoggerFactory.getLogger(CreatePortForwardingRuleCmd.class.getName());

    private static final String s_name = "createportforwardingruleresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.IP_ADDRESS_ID,
            type = CommandType.UUID,
            entityType = IPAddressResponse.class,
            required = true,
            description = "the IP address id of the port forwarding rule")
    private Long ipAddressId;

    @Parameter(name = ApiConstants.PRIVATE_START_PORT,
            type = CommandType.INTEGER,
            required = true,
            description = "the starting port of port forwarding rule's private port range")
    private Integer privateStartPort;

    @Parameter(name = ApiConstants.PROTOCOL,
            type = CommandType.STRING,
            required = true,
            description = "the protocol for the port forwarding rule. Valid values are TCP or UDP.")
    private String protocol;

    @Parameter(name = ApiConstants.PRIVATE_END_PORT,
            type = CommandType.INTEGER,
            required = false,
            description = "the ending port of port forwarding rule's private port range")
    private Integer privateEndPort;

    @Parameter(name = ApiConstants.PUBLIC_START_PORT,
            type = CommandType.INTEGER,
            required = true,
            description = "the starting port of port forwarding rule's public port range")
    private Integer publicStartPort;

    @Parameter(name = ApiConstants.PUBLIC_END_PORT,
            type = CommandType.INTEGER,
            required = false,
            description = "the ending port of port forwarding rule's private port range")
    private Integer publicEndPort;

    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.VIRTUAL_MACHINE_ID,
            type = CommandType.UUID,
            entityType = UserVmResponse.class,
            required = true,
            description = "the ID of the virtual machine for the port forwarding rule")
    private Long virtualMachineId;

    @Parameter(name = ApiConstants.CIDR_LIST, type = CommandType.LIST, collectionType = CommandType.STRING, description = "the cidr list to forward traffic from")
    private List<String> cidrlist;

    @Parameter(name = ApiConstants.OPEN_FIREWALL, type = CommandType.BOOLEAN, description = "if true, firewall rule for source/end public port is automatically created; "
            + "if false - firewall rule has to be created explicitly. If not specified 1) defaulted to false when PF"
            + " rule is being created for VPC guest network 2) in all other cases defaulted to true")
    private Boolean openFirewall;

    @Parameter(name = ApiConstants.NETWORK_ID,
            type = CommandType.UUID,
            entityType = NetworkResponse.class,
            description = "the network of the virtual machine the port forwarding rule will be created for. "
                    + "Required when public IP address is not associated with any guest network yet (VPC case).")
    private Long networkId;
    @Parameter(name = ApiConstants.VM_GUEST_IP,
            type = CommandType.STRING,
            required = false,
            description = "VM guest nic secondary IP address for the port forwarding rule")
    private String vmSecondaryIp;

    @Parameter(name = ApiConstants.FOR_DISPLAY, type = CommandType.BOOLEAN, description = "an optional field, whether to the display the rule to the end user or not", since = "4" +
            ".4", authorized = {RoleType.Admin})
    private Boolean display;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    @Override
    public void execute() throws ResourceUnavailableException {
        final CallContext callerContext = CallContext.current();
        boolean success = true;
        PortForwardingRule rule = null;
        try {
            CallContext.current().setEventDetails("Rule Id: " + getEntityId());

            if (getOpenFirewall()) {
                success = success && _firewallService.applyIngressFirewallRules(ipAddressId, callerContext.getCallingAccount());
            }

            success = success && _rulesService.applyPortForwardingRules(ipAddressId, callerContext.getCallingAccount());

            // State is different after the rule is applied, so get new object here
            rule = _entityMgr.findById(PortForwardingRule.class, getEntityId());
            FirewallRuleResponse fwResponse = new FirewallRuleResponse();
            if (rule != null) {
                fwResponse = _responseGenerator.createPortForwardingRuleResponse(rule);
                setResponseObject(fwResponse);
            }
            fwResponse.setResponseName(getCommandName());
        } finally {
            if (!success || rule == null) {

                if (getOpenFirewall()) {
                    _firewallService.revokeRelatedFirewallRule(getEntityId(), true);
                }

                try {
                    _rulesService.revokePortForwardingRule(getEntityId(), true);
                } catch (final Exception ex) {
                    //Ignore e.g. failed to apply rules to device error
                }

                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to apply port forwarding rule");
            }
        }
    }

    public Boolean getOpenFirewall() {
        final boolean isVpc = getVpcId() == null ? false : true;
        if (openFirewall != null) {
            if (isVpc && openFirewall) {
                throw new InvalidParameterValueException("Can't have openFirewall=true when IP address belongs to VPC");
            }
            return openFirewall;
        } else {
            if (isVpc) {
                return false;
            }
            return true;
        }
    }

    private Long getVpcId() {
        if (ipAddressId != null) {
            final IpAddress ipAddr = _networkService.getIp(ipAddressId);
            if (ipAddr == null || !ipAddr.readyToUse()) {
                throw new InvalidParameterValueException("Unable to create PF rule, invalid IP address id " + ipAddressId);
            } else {
                return ipAddr.getVpcId();
            }
        }
        return null;
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

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are
        // tracked
    }

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

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public Integer getSourcePortStart() {
        return publicStartPort.intValue();
    }

    @Override
    public Integer getSourcePortEnd() {
        return (publicEndPort == null) ? publicStartPort.intValue() : publicEndPort.intValue();
    }

    @Override
    public String getProtocol() {
        return protocol.trim();
    }

    @Override
    public Purpose getPurpose() {
        return Purpose.PortForwarding;
    }

    @Override
    public State getState() {
        throw new UnsupportedOperationException("Should never call me to find the state");
    }

    @Override
    public long getNetworkId() {
        final IpAddress ip = _entityMgr.findById(IpAddress.class, getIpAddressId());
        Long ntwkId = null;

        if (ip.getAssociatedWithNetworkId() != null) {
            ntwkId = ip.getAssociatedWithNetworkId();
        } else {
            ntwkId = networkId;
        }
        if (ntwkId == null) {
            throw new InvalidParameterValueException("Unable to create port forwarding rule for ip address with ID = " + getIpAddressUuid() +
                    " as this ip address is not associated with any network. Please add networkId parameter.");
        }
        return ntwkId;
    }

    public Long getIpAddressId() {
        return ipAddressId;
    }

    public String getIpAddressUuid() {
        final IpAddress ip = _networkService.getIp(ipAddressId);
        return ip.getUuid();
    }

    @Override
    public Long getSourceIpAddressId() {
        return ipAddressId;
    }

    @Override
    public Integer getIcmpCode() {
        return null;
    }

    @Override
    public Integer getIcmpType() {
        return null;
    }

    @Override
    public List<String> getSourceCidrList() {
        if (cidrlist != null) {
            throw new InvalidParameterValueException("Parameter cidrList is deprecated; if you need to open firewall "
                    + "rule for the specific cidr, please refer to createFirewallRule command");
        }
        return null;
    }

    @Override
    public Long getRelated() {
        return null;
    }

    @Override
    public FirewallRuleType getType() {
        return FirewallRuleType.User;
    }

    @Override
    public TrafficType getTrafficType() {
        return null;
    }

    @Override
    public String getUuid() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getDomainId() {
        final IpAddress ip = _networkService.getIp(ipAddressId);
        return ip.getDomainId();
    }

    @Override
    public Ip getDestinationIpAddress() {
        return null;
    }

    @Override
    public void setDestinationIpAddress(final Ip destinationIpAddress) {
        return;
    }

    @Override
    public int getDestinationPortStart() {
        return privateStartPort.intValue();
    }

    @Override
    public int getDestinationPortEnd() {
        return (privateEndPort == null) ? privateStartPort.intValue() : privateEndPort.intValue();
    }

    @Override
    public long getVirtualMachineId() {
        return virtualMachineId;
    }

    @Override
    public void create() {
        // cidr list parameter is deprecated
        if (cidrlist != null) {
            throw new InvalidParameterValueException(
                    "Parameter cidrList is deprecated; if you need to open firewall rule for the specific cidr, please refer to createFirewallRule command");
        }

        final Ip privateIp = getVmSecondaryIp();
        if (privateIp != null) {
            if (!NetUtils.isValidIp(privateIp.toString())) {
                throw new InvalidParameterValueException("Invalid vm ip address");
            }
        }

        try {
            final PortForwardingRule result = _rulesService.createPortForwardingRule(this, virtualMachineId, privateIp, getOpenFirewall(), isDisplay());
            setEntityId(result.getId());
            setEntityUuid(result.getUuid());
        } catch (final NetworkRuleConflictException ex) {
            s_logger.info("Network rule conflict: ", ex);
            s_logger.trace("Network Rule Conflict: ", ex);
            throw new ServerApiException(ApiErrorCode.NETWORK_RULE_CONFLICT_ERROR, ex.getMessage());
        }
    }

    public Ip getVmSecondaryIp() {
        if (vmSecondaryIp == null) {
            return null;
        }
        return new Ip(vmSecondaryIp);
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_NET_RULE_ADD;
    }

    @Override
    public String getEventDescription() {
        final IpAddress ip = _networkService.getIp(ipAddressId);
        return ("Applying port forwarding  rule for Ip: " + ip.getAddress() + " with virtual machine:" + virtualMachineId);
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
        return getIp().getAssociatedWithNetworkId();
    }

    private IpAddress getIp() {
        final IpAddress ip = _networkService.getIp(ipAddressId);
        if (ip == null) {
            throw new InvalidParameterValueException("Unable to find ip address by id " + ipAddressId);
        }
        return ip;
    }

    @Override
    public long getAccountId() {
        final IpAddress ip = _networkService.getIp(ipAddressId);
        return ip.getAccountId();
    }

    @Override
    public Class<?> getEntityType() {
        return FirewallRule.class;
    }
}
