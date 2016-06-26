package org.apache.cloudstack.api.command.user.loadbalancer;

import com.cloud.uservm.UserVm;
import com.cloud.utils.Pair;
import com.cloud.vm.VirtualMachine;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.response.FirewallRuleResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.LoadBalancerRuleVmMapResponse;
import org.apache.cloudstack.api.response.UserVmResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listLoadBalancerRuleInstances", description = "List all virtual machine instances that are assigned to a load balancer rule.", responseObject =
        LoadBalancerRuleVmMapResponse.class, responseView = ResponseView.Restricted,
        requestHasSensitiveInfo = false,
        responseHasSensitiveInfo = true)
public class ListLoadBalancerRuleInstancesCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListLoadBalancerRuleInstancesCmd.class.getName());

    private static final String s_name = "listloadbalancerruleinstancesresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.APPLIED,
            type = CommandType.BOOLEAN,
            description = "true if listing all virtual machines currently applied to the load balancer rule; default is true")
    private Boolean applied;

    @Parameter(name = ApiConstants.ID,
            type = CommandType.UUID,
            entityType = FirewallRuleResponse.class,
            required = true,
            description = "the ID of the load balancer rule")
    private Long id;

    @Parameter(name = ApiConstants.LIST_LB_VMIPS,
            type = CommandType.BOOLEAN,
            description = "true if load balancer rule VM IP information to be included; default is false")
    private boolean isListLbVmip;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Boolean isApplied() {
        return applied;
    }

    @Override
    public void execute() {
        final Pair<List<? extends UserVm>, List<String>> vmServiceMap = _lbService.listLoadBalancerInstances(this);
        final List<? extends UserVm> result = vmServiceMap.first();
        final List<String> serviceStates = vmServiceMap.second();

        if (!isListLbVmip()) {
            // list lb instances
            final ListResponse<UserVmResponse> response = new ListResponse<>();
            List<UserVmResponse> vmResponses = new ArrayList<>();
            if (result != null) {
                vmResponses = _responseGenerator.createUserVmResponse(ResponseView.Restricted, "loadbalancerruleinstance", result.toArray(new UserVm[result.size()]));

                for (int i = 0; i < result.size(); i++) {
                    vmResponses.get(i).setServiceState(serviceStates.get(i));
                }
            }
            response.setResponses(vmResponses);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            final ListResponse<LoadBalancerRuleVmMapResponse> lbRes = new ListResponse<>();

            List<UserVmResponse> vmResponses = new ArrayList<>();
            final List<LoadBalancerRuleVmMapResponse> listlbVmRes = new ArrayList<>();

            if (result != null) {
                vmResponses = _responseGenerator.createUserVmResponse(ResponseView.Full, "loadbalancerruleinstance", result.toArray(new UserVm[result.size()]));

                final List<String> ipaddr = null;

                for (int i = 0; i < result.size(); i++) {
                    final LoadBalancerRuleVmMapResponse lbRuleVmIpResponse = new LoadBalancerRuleVmMapResponse();
                    vmResponses.get(i).setServiceState(serviceStates.get(i));
                    lbRuleVmIpResponse.setUserVmResponse(vmResponses.get(i));
                    //get vm id from the uuid
                    final VirtualMachine lbvm = _entityMgr.findByUuid(VirtualMachine.class, vmResponses.get(i).getId());
                    lbRuleVmIpResponse.setIpAddr(_lbService.listLbVmIpAddress(getId(), lbvm.getId()));
                    lbRuleVmIpResponse.setObjectName("lbrulevmidip");
                    listlbVmRes.add(lbRuleVmIpResponse);
                }
            }

            lbRes.setResponseName(getCommandName());
            lbRes.setResponses(listlbVmRes);
            setResponseObject(lbRes);
        }
    }

    public boolean isListLbVmip() {
        return isListLbVmip;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
