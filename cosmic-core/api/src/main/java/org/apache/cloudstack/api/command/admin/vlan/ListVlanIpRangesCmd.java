package org.apache.cloudstack.api.command.admin.vlan;

import com.cloud.dc.Vlan;
import com.cloud.utils.Pair;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.DomainResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.NetworkResponse;
import org.apache.cloudstack.api.response.PhysicalNetworkResponse;
import org.apache.cloudstack.api.response.PodResponse;
import org.apache.cloudstack.api.response.ProjectResponse;
import org.apache.cloudstack.api.response.VlanIpRangeResponse;
import org.apache.cloudstack.api.response.ZoneResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listVlanIpRanges", description = "Lists all VLAN IP ranges.", responseObject = VlanIpRangeResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListVlanIpRangesCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListVlanIpRangesCmd.class.getName());

    private static final String s_name = "listvlaniprangesresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ACCOUNT,
            type = CommandType.STRING,
            description = "the account with which the VLAN IP range is associated. Must be used with the domainId parameter.")
    private String accountName;

    @Parameter(name = ApiConstants.PROJECT_ID, type = CommandType.UUID, entityType = ProjectResponse.class, description = "project who will own the VLAN")
    private Long projectId;

    @Parameter(name = ApiConstants.DOMAIN_ID,
            type = CommandType.UUID,
            entityType = DomainResponse.class,
            description = "the domain ID with which the VLAN IP range is associated.  If used with the account parameter, " +
                    "returns all VLAN IP ranges for that account in the specified domain.")
    private Long domainId;

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = VlanIpRangeResponse.class, required = false, description = "the ID of the VLAN IP range")
    private Long id;

    @Parameter(name = ApiConstants.POD_ID, type = CommandType.UUID, entityType = PodResponse.class, description = "the Pod ID of the VLAN IP range")
    private Long podId;

    @Parameter(name = ApiConstants.VLAN, type = CommandType.STRING, description = "the ID or VID of the VLAN. Default is an \"untagged\" VLAN.")
    private String vlan;

    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.UUID, entityType = ZoneResponse.class, description = "the Zone ID of the VLAN IP range")
    private Long zoneId;

    @Parameter(name = ApiConstants.NETWORK_ID, type = CommandType.UUID, entityType = NetworkResponse.class, description = "network id of the VLAN IP range")
    private Long networkId;

    @Parameter(name = ApiConstants.FOR_VIRTUAL_NETWORK, type = CommandType.BOOLEAN, description = "true if VLAN is of Virtual type, false if Direct")
    private Boolean forVirtualNetwork;

    @Parameter(name = ApiConstants.PHYSICAL_NETWORK_ID,
            type = CommandType.UUID,
            entityType = PhysicalNetworkResponse.class,
            description = "physical network id of the VLAN IP range")
    private Long physicalNetworkId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getAccountName() {
        return accountName;
    }

    public Long getDomainId() {
        return domainId;
    }

    public Long getId() {
        return id;
    }

    public Long getPodId() {
        return podId;
    }

    public String getVlan() {
        return vlan;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public Long getNetworkId() {
        return networkId;
    }

    public Boolean getForVirtualNetwork() {
        return forVirtualNetwork;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getPhysicalNetworkId() {
        return physicalNetworkId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final Pair<List<? extends Vlan>, Integer> vlans = _mgr.searchForVlans(this);
        final ListResponse<VlanIpRangeResponse> response = new ListResponse<>();
        final List<VlanIpRangeResponse> vlanResponses = new ArrayList<>();
        for (final Vlan vlan : vlans.first()) {
            final VlanIpRangeResponse vlanResponse = _responseGenerator.createVlanIpRangeResponse(vlan);
            vlanResponse.setObjectName("vlaniprange");
            vlanResponses.add(vlanResponse);
        }

        response.setResponses(vlanResponses, vlans.second());
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
