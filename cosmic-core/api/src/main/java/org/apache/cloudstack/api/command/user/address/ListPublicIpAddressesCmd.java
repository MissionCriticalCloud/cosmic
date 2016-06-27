package org.apache.cloudstack.api.command.user.address;

import com.cloud.network.IpAddress;
import com.cloud.utils.Pair;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListTaggedResourcesCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.response.IPAddressResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.NetworkResponse;
import org.apache.cloudstack.api.response.PhysicalNetworkResponse;
import org.apache.cloudstack.api.response.VlanIpRangeResponse;
import org.apache.cloudstack.api.response.VpcResponse;
import org.apache.cloudstack.api.response.ZoneResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listPublicIpAddresses", description = "Lists all public IP addresses", responseObject = IPAddressResponse.class, responseView = ResponseView.Restricted,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false, entityType = {IpAddress.class})
public class ListPublicIpAddressesCmd extends BaseListTaggedResourcesCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListPublicIpAddressesCmd.class.getName());

    private static final String s_name = "listpublicipaddressesresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ALLOCATED_ONLY, type = CommandType.BOOLEAN, description = "limits search results to allocated public IP addresses")
    private Boolean allocatedOnly;

    @Parameter(name = ApiConstants.STATE, type = CommandType.STRING, description = "lists all public IP addresses by state")
    private String state;

    @Parameter(name = ApiConstants.FOR_VIRTUAL_NETWORK, type = CommandType.BOOLEAN, description = "the virtual network for the IP address")
    private Boolean forVirtualNetwork;

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = IPAddressResponse.class, description = "lists IP address by ID")
    private Long id;

    @Parameter(name = ApiConstants.IP_ADDRESS, type = CommandType.STRING, description = "lists the specified IP address")
    private String ipAddress;

    @Parameter(name = ApiConstants.VLAN_ID, type = CommandType.UUID, entityType = VlanIpRangeResponse.class, description = "lists all public IP addresses by VLAN ID")
    private Long vlanId;

    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.UUID, entityType = ZoneResponse.class, description = "lists all public IP addresses by zone ID")
    private Long zoneId;

    @Parameter(name = ApiConstants.FOR_LOAD_BALANCING, type = CommandType.BOOLEAN, description = "list only IPs used for load balancing")
    private Boolean forLoadBalancing;

    @Parameter(name = ApiConstants.PHYSICAL_NETWORK_ID,
            type = CommandType.UUID,
            entityType = PhysicalNetworkResponse.class,
            description = "lists all public IP addresses by physical network ID")
    private Long physicalNetworkId;

    @Parameter(name = ApiConstants.ASSOCIATED_NETWORK_ID,
            type = CommandType.UUID,
            entityType = NetworkResponse.class,
            description = "lists all public IP addresses associated to the network specified")
    private Long associatedNetworkId;

    @Parameter(name = ApiConstants.IS_SOURCE_NAT, type = CommandType.BOOLEAN, description = "list only source NAT IP addresses")
    private Boolean isSourceNat;

    @Parameter(name = ApiConstants.IS_STATIC_NAT, type = CommandType.BOOLEAN, description = "list only static NAT IP addresses")
    private Boolean isStaticNat;

    @Parameter(name = ApiConstants.VPC_ID, type = CommandType.UUID, entityType = VpcResponse.class, description = "List IPs belonging to the VPC")
    private Long vpcId;

    @Parameter(name = ApiConstants.FOR_DISPLAY, type = CommandType.BOOLEAN, description = "list resources by display flag; only ROOT admin is eligible to pass this parameter",
            since = "4.4", authorized = {RoleType.Admin})
    private Boolean display;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////
    public Long getId() {
        return id;
    }

    public Boolean isAllocatedOnly() {
        return allocatedOnly;
    }

    public Boolean isForVirtualNetwork() {
        return forVirtualNetwork;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public Long getVlanId() {
        return vlanId;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public Long getPhysicalNetworkId() {
        return physicalNetworkId;
    }

    public Long getAssociatedNetworkId() {
        return associatedNetworkId;
    }

    public Boolean getIsSourceNat() {
        return isSourceNat;
    }

    public Boolean getIsStaticNat() {
        return isStaticNat;
    }

    public Long getVpcId() {
        return vpcId;
    }

    @Override
    public Boolean getDisplay() {
        if (display != null) {
            return display;
        }
        return super.getDisplay();
    }

    public Boolean isForLoadBalancing() {
        return forLoadBalancing;
    }

    public Boolean getForVirtualNetwork() {
        return forVirtualNetwork;
    }

    public Boolean getForLoadBalancing() {
        return forLoadBalancing;
    }

    public String getState() {
        return state;
    }

    @Override
    public void execute() {
        final Pair<List<? extends IpAddress>, Integer> result = _mgr.searchForIPAddresses(this);
        final ListResponse<IPAddressResponse> response = new ListResponse<>();
        final List<IPAddressResponse> ipAddrResponses = new ArrayList<>();
        for (final IpAddress ipAddress : result.first()) {
            final IPAddressResponse ipResponse = _responseGenerator.createIPAddressResponse(ResponseView.Restricted, ipAddress);
            ipResponse.setObjectName("publicipaddress");
            ipAddrResponses.add(ipResponse);
        }

        response.setResponses(ipAddrResponses, result.second());
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////
    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.IpAddress;
    }
}
