package com.cloud.api.command.user.loadbalancer;

import com.cloud.acl.RoleType;
import com.cloud.api.APICommand;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.BaseAsyncCreateCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.DomainResponse;
import com.cloud.api.response.IPAddressResponse;
import com.cloud.api.response.LoadBalancerResponse;
import com.cloud.api.response.NetworkResponse;
import com.cloud.api.response.ZoneResponse;
import com.cloud.context.CallContext;
import com.cloud.dc.DataCenter;
import com.cloud.dc.DataCenter.NetworkType;
import com.cloud.event.EventTypes;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.IpAddress;
import com.cloud.network.Network;
import com.cloud.network.rules.LoadBalancer;
import com.cloud.user.Account;
import com.cloud.utils.net.NetUtils;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createLoadBalancerRule", description = "Creates a load balancer rule", responseObject = LoadBalancerResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreateLoadBalancerRuleCmd extends BaseAsyncCreateCmd /*implements LoadBalancer */ {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateLoadBalancerRuleCmd.class.getName());

    private static final String s_name = "createloadbalancerruleresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ALGORITHM, type = CommandType.STRING, required = true, description = "load balancer algorithm (source, roundrobin, leastconn)")
    private String algorithm;

    @Parameter(name = ApiConstants.DESCRIPTION, type = CommandType.STRING, description = "the description of the load balancer rule", length = 4096)
    private String description;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, required = true, description = "name of the load balancer rule")
    private String loadBalancerRuleName;

    @Parameter(name = ApiConstants.PRIVATE_PORT,
            type = CommandType.INTEGER,
            required = true,
            description = "the private port of the private IP address/virtual machine where the network traffic will be load balanced to")
    private Integer privatePort;

    @Parameter(name = ApiConstants.PUBLIC_IP_ID,
            type = CommandType.UUID,
            entityType = IPAddressResponse.class,
            description = "public IP address ID from where the network traffic will be load balanced from")
    private Long publicIpId;

    @Parameter(name = ApiConstants.ZONE_ID,
            type = CommandType.UUID,
            entityType = ZoneResponse.class,
            required = false,
            description = "zone where the load balancer is going to be created. This parameter is required when LB service provider is ElasticLoadBalancerVm")
    private Long zoneId;

    @Parameter(name = ApiConstants.PUBLIC_PORT,
            type = CommandType.INTEGER,
            required = true,
            description = "the public port from where the network traffic will be load balanced from")
    private Integer publicPort;

    @Parameter(name = ApiConstants.OPEN_FIREWALL, type = CommandType.BOOLEAN, description = "if true, firewall rule for"
            + " source/end public port is automatically created; if false - firewall rule has to be created explicitely. If not specified 1) defaulted to false when LB"
            + " rule is being created for VPC guest network 2) in all other cases defaulted to true")
    private Boolean openFirewall;

    @Parameter(name = ApiConstants.ACCOUNT,
            type = CommandType.STRING,
            description = "the account associated with the load balancer. Must be used with the domainId parameter.")
    private String accountName;

    @Parameter(name = ApiConstants.DOMAIN_ID, type = CommandType.UUID, entityType = DomainResponse.class, description = "the domain ID associated with the load balancer")
    private Long domainId;

    @Parameter(name = ApiConstants.CIDR_LIST, type = CommandType.LIST, collectionType = CommandType.STRING, description = "the CIDR list to forward traffic from")
    private List<String> cidrlist;

    @Parameter(name = ApiConstants.NETWORK_ID, type = CommandType.UUID, entityType = NetworkResponse.class, description = "The guest network this "
            + "rule will be created for. Required when public Ip address is not associated with any Guest network yet (VPC case)")
    private Long networkId;

    @Parameter(name = ApiConstants.PROTOCOL, type = CommandType.STRING, description = "The protocol for the LB")
    private String lbProtocol;

    @Parameter(name = ApiConstants.FOR_DISPLAY, type = CommandType.BOOLEAN, description = "an optional field, whether to the display the rule to the end user or not", since = "4" +
            ".4", authorized = {RoleType.Admin})
    private Boolean display;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getAlgorithm() {
        return algorithm;
    }

    public String getDescription() {
        return description;
    }

    public String getLoadBalancerRuleName() {
        return loadBalancerRuleName;
    }

    public Integer getPrivatePort() {
        return privatePort;
    }

    public Integer getPublicPort() {
        return publicPort;
    }

    public List<String> getSourceCidrList() {
        if (cidrlist != null) {
            throw new InvalidParameterValueException(
                    "Parameter cidrList is deprecated; if you need to open firewall rule for the specific CIDR, please refer to createFirewallRule command");
        }
        return null;
    }

    public String getLbProtocol() {
        return lbProtocol;
    }

    @Override
    public void execute() throws ResourceAllocationException, ResourceUnavailableException {

        final CallContext callerContext = CallContext.current();
        boolean success = true;
        LoadBalancer rule = null;
        try {
            CallContext.current().setEventDetails("Rule Id: " + getEntityId());

            if (getOpenFirewall()) {
                success = success && _firewallService.applyIngressFirewallRules(getSourceIpAddressId(), callerContext.getCallingAccount());
            }

            // State might be different after the rule is applied, so get new object here
            rule = _entityMgr.findById(LoadBalancer.class, getEntityId());
            LoadBalancerResponse lbResponse = new LoadBalancerResponse();
            if (rule != null) {
                lbResponse = _responseGenerator.createLoadBalancerResponse(rule);
                setResponseObject(lbResponse);
            }
            lbResponse.setResponseName(getCommandName());
        } catch (final Exception ex) {
            s_logger.warn("Failed to create LB rule due to exception ", ex);
        }
        if (!success || rule == null) {

            if (getOpenFirewall()) {
                _firewallService.revokeRelatedFirewallRule(getEntityId(), true);
            }
            // no need to apply the rule on the backend as it exists in the db only
            _lbService.deleteLoadBalancerRule(getEntityId(), false);

            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create load balancer rule");
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

    public Long getSourceIpAddressId() {
        if (publicIpId != null) {
            final IpAddress ipAddr = _networkService.getIp(publicIpId);
            if (ipAddr == null || !ipAddr.readyToUse()) {
                throw new InvalidParameterValueException("Unable to create load balancer rule, invalid IP address ID " + publicIpId);
            }
        } else if (getEntityId() != null) {
            final LoadBalancer rule = _entityMgr.findById(LoadBalancer.class, getEntityId());
            return rule.getSourceIpAddressId();
        }

        return publicIpId;
    }

    private Long getVpcId() {
        if (publicIpId != null) {
            final IpAddress ipAddr = _networkService.getIp(publicIpId);
            if (ipAddr == null || !ipAddr.readyToUse()) {
                throw new InvalidParameterValueException("Unable to create load balancer rule, invalid IP address ID " + publicIpId);
            } else {
                return ipAddr.getVpcId();
            }
        }
        return null;
    }

    public void setSourceIpAddressId(final Long ipId) {
        this.publicIpId = ipId;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public long getEntityOwnerId() {
        return getAccountId();
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
    public void create() {
        //cidr list parameter is deprecated
        if (cidrlist != null) {
            throw new InvalidParameterValueException(
                    "Parameter cidrList is deprecated; if you need to open firewall rule for the specific CIDR, please refer to createFirewallRule command");
        }
        if (lbProtocol != null && !lbProtocol.toLowerCase().equals("tcp")) {
            throw new InvalidParameterValueException(
                    "Only TCP protocol is supported because HAProxy can only do TCP.");
        }
        try {
            final LoadBalancer result =
                    _lbService.createPublicLoadBalancerRule(getXid(), getName(), getDescription(), getSourcePortStart(), getSourcePortEnd(), getDefaultPortStart(),
                            getDefaultPortEnd(), getSourceIpAddressId(), getProtocol(), getAlgorithm(), getNetworkId(), getEntityOwnerId(), getOpenFirewall(), getLbProtocol(),
                            isDisplay());
            this.setEntityId(result.getId());
            this.setEntityUuid(result.getUuid());
        } catch (final NetworkRuleConflictException e) {
            s_logger.warn("Exception: ", e);
            throw new ServerApiException(ApiErrorCode.NETWORK_RULE_CONFLICT_ERROR, e.getMessage());
        } catch (final InsufficientAddressCapacityException e) {
            s_logger.warn("Exception: ", e);
            throw new ServerApiException(ApiErrorCode.INSUFFICIENT_CAPACITY_ERROR, e.getMessage());
        } catch (final InvalidParameterValueException e) {
            throw new ServerApiException(ApiErrorCode.PARAM_ERROR, e.getMessage());
        }
    }

    public Integer getSourcePortStart() {
        return publicPort.intValue();
    }

    public Integer getSourcePortEnd() {
        return publicPort.intValue();
    }

    public String getProtocol() {
        return NetUtils.TCP_PROTO;
    }

    public int getDefaultPortStart() {
        return privatePort.intValue();
    }

    public int getDefaultPortEnd() {
        return privatePort.intValue();
    }

    public void setPublicIpId(final Long publicIpId) {
        this.publicIpId = publicIpId;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_LOAD_BALANCER_CREATE;
    }

    @Override
    public String getEventDescription() {
        return "creating load balancer: " + getName() + " account: " + getAccountName();
    }

    public String getName() {
        return loadBalancerRuleName;
    }

    public String getAccountName() {
        return accountName;
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

    public long getNetworkId() {
        if (networkId != null) {
            return networkId;
        }
        final Long zoneId = getZoneId();

        if (zoneId == null) {
            final Long ipId = getSourceIpAddressId();
            if (ipId == null) {
                throw new InvalidParameterValueException("Either networkId or zoneId or publicIpId has to be specified");
            }
        }

        if (zoneId != null) {
            final DataCenter zone = _entityMgr.findById(DataCenter.class, zoneId);
            if (zone.getNetworkType() == NetworkType.Advanced) {
                final List<? extends Network> networks = _networkService.getIsolatedNetworksOwnedByAccountInZone(getZoneId(), _accountService.getAccount(getEntityOwnerId()));
                if (networks.size() == 0) {
                    final String domain = _domainService.getDomain(getDomainId()).getName();
                    throw new InvalidParameterValueException("Account name=" + getAccountName() + " domain=" + domain + " doesn't have virtual networks in zone=" +
                            zone.getName());
                }

                if (networks.size() < 1) {
                    throw new InvalidParameterValueException("Account doesn't have any isolated networks in the zone");
                } else if (networks.size() > 1) {
                    throw new InvalidParameterValueException("Account has more than one isolated network in the zone");
                }

                return networks.get(0).getId();
            } else {
                final Network defaultGuestNetwork = _networkService.getExclusiveGuestNetwork(zoneId);
                if (defaultGuestNetwork == null) {
                    throw new InvalidParameterValueException("Unable to find a default guest network for account " + getAccountName() + " in domain ID=" + getDomainId());
                } else {
                    return defaultGuestNetwork.getId();
                }
            }
        } else {
            final IpAddress ipAddr = _networkService.getIp(publicIpId);
            if (ipAddr.getAssociatedWithNetworkId() != null) {
                return ipAddr.getAssociatedWithNetworkId();
            } else {
                throw new InvalidParameterValueException("IP address ID=" + publicIpId + " is not associated with any network");
            }
        }
    }

    public Long getZoneId() {
        return zoneId;
    }

    public long getDomainId() {
        if (publicIpId != null) {
            return _networkService.getIp(getSourceIpAddressId()).getDomainId();
        }
        if (domainId != null) {
            return domainId;
        }
        return CallContext.current().getCallingAccount().getDomainId();
    }

    public long getAccountId() {
        if (publicIpId != null) {
            return _networkService.getIp(getSourceIpAddressId()).getAccountId();
        }

        Account account = null;
        if ((domainId != null) && (accountName != null)) {
            account = _responseGenerator.findAccountByNameDomain(accountName, domainId);
            if (account != null) {
                return account.getId();
            } else {
                throw new InvalidParameterValueException("Unable to find account " + accountName + " in domain ID=" + domainId);
            }
        } else {
            throw new InvalidParameterValueException("Can't define IP owner. Either specify account/domainId or publicIpId");
        }
    }

    public String getXid() {
        /*FIXME*/
        return null;
    }
}
