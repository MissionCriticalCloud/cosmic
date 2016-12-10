package com.cloud.api.command.admin.loadbalancer;

import com.cloud.api.APICommand;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.command.user.loadbalancer.ListLoadBalancerRuleInstancesCmd;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.LoadBalancerRuleVmMapResponse;
import com.cloud.api.response.UserVmResponse;
import com.cloud.uservm.UserVm;
import com.cloud.utils.Pair;
import com.cloud.vm.VirtualMachine;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listLoadBalancerRuleInstances", description = "List all virtual machine instances that are assigned to a load balancer rule.", responseObject =
        LoadBalancerRuleVmMapResponse.class, responseView = ResponseView.Full,
        requestHasSensitiveInfo = false,
        responseHasSensitiveInfo = true)
public class ListLoadBalancerRuleInstancesCmdByAdmin extends ListLoadBalancerRuleInstancesCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListLoadBalancerRuleInstancesCmdByAdmin.class.getName());

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
}
