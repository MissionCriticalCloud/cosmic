package org.apache.cloudstack.api.command.admin.internallb;

import com.cloud.network.router.VirtualRouter.Role;
import com.cloud.vm.VirtualMachine;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListProjectAndAccountResourcesCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.DomainRouterResponse;
import org.apache.cloudstack.api.response.HostResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.NetworkResponse;
import org.apache.cloudstack.api.response.PodResponse;
import org.apache.cloudstack.api.response.UserVmResponse;
import org.apache.cloudstack.api.response.VpcResponse;
import org.apache.cloudstack.api.response.ZoneResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listInternalLoadBalancerVMs", description = "List internal LB VMs.", responseObject = DomainRouterResponse.class, entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListInternalLBVMsCmd extends BaseListProjectAndAccountResourcesCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListInternalLBVMsCmd.class.getName());

    private static final String s_name = "listinternallbvmsresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.HOST_ID, type = CommandType.UUID, entityType = HostResponse.class, description = "the host ID of the Internal LB VM")
    private Long hostId;

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = UserVmResponse.class, description = "the ID of the Internal LB VM")
    private Long id;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "the name of the Internal LB VM")
    private String routerName;

    @Parameter(name = ApiConstants.POD_ID, type = CommandType.UUID, entityType = PodResponse.class, description = "the Pod ID of the Internal LB VM")
    private Long podId;

    @Parameter(name = ApiConstants.STATE, type = CommandType.STRING, description = "the state of the Internal LB VM")
    private String state;

    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.UUID, entityType = ZoneResponse.class, description = "the Zone ID of the Internal LB VM")
    private Long zoneId;

    @Parameter(name = ApiConstants.NETWORK_ID, type = CommandType.UUID, entityType = NetworkResponse.class, description = "list by network id")
    private Long networkId;

    @Parameter(name = ApiConstants.VPC_ID, type = CommandType.UUID, entityType = VpcResponse.class, description = "List Internal LB VMs by VPC")
    private Long vpcId;

    @Parameter(name = ApiConstants.FOR_VPC, type = CommandType.BOOLEAN, description = "if true is passed for this parameter, list only VPC Internal LB VMs")
    private Boolean forVpc;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getHostId() {
        return hostId;
    }

    public Long getId() {
        return id;
    }

    public String getRouterName() {
        return routerName;
    }

    public Long getPodId() {
        return podId;
    }

    public String getState() {
        return state;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public Long getNetworkId() {
        return networkId;
    }

    public Long getVpcId() {
        return vpcId;
    }

    public Boolean getForVpc() {
        return forVpc;
    }

    public String getRole() {
        return Role.INTERNAL_LB_VM.toString();
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.DomainRouter;
    }

    @Override
    public void execute() {
        final ListResponse<DomainRouterResponse> response = _queryService.searchForInternalLbVms(this);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
