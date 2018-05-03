package com.cloud.api.command.user.loadbalancer;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.FirewallRuleResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.api.response.UserVmResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.legacymodel.network.LoadBalancer;
import com.cloud.legacymodel.user.Account;
import com.cloud.legacymodel.vm.VirtualMachine;
import com.cloud.utils.StringUtils;
import com.cloud.utils.net.NetUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "assignToLoadBalancerRule", group = APICommandGroup.LoadBalancerService,
        description = "Assigns virtual machine or a list of virtual machines to a load balancer rule.",
        responseObject = SuccessResponse.class,
        requestHasSensitiveInfo = false,
        responseHasSensitiveInfo = false)
public class AssignToLoadBalancerRuleCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(AssignToLoadBalancerRuleCmd.class.getName());

    private static final String s_name = "assigntoloadbalancerruleresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID,
            type = CommandType.UUID,
            entityType = FirewallRuleResponse.class,
            required = true,
            description = "the ID of the load balancer rule")
    private Long id;

    @Parameter(name = ApiConstants.VIRTUAL_MACHINE_IDS,
            type = CommandType.LIST,
            collectionType = CommandType.UUID,
            entityType = UserVmResponse.class,
            description = "the list of IDs of the virtual machine that are being assigned to the load balancer rule(i.e. virtualMachineIds=1,2,3)")
    private List<Long> virtualMachineIds;

    @Parameter(name = ApiConstants.VIRTUAL_MACHINE_ID_IP,
            type = CommandType.MAP,
            description = "VM ID and IP map, vmidipmap[0].vmid=1 vmidipmap[0].ip=10.1.1.75",
            since = "4.4")
    private Map vmIdIpMap;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Map<Long, String> getVmIdIpMap() {
        return vmIdIpMap;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_ASSIGN_TO_LOAD_BALANCER_RULE;
    }

    @Override
    public String getEventDescription() {
        return "applying instances for load balancer: " + getLoadBalancerId() + " (ids: " + StringUtils.join(getVirtualMachineIds(), ",") + ")";
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getSyncObjType() {
        return BaseAsyncCmd.networkSyncObject;
    }

    @Override
    public Long getSyncObjId() {
        final LoadBalancer lb = _lbService.findById(id);
        if (lb == null) {
            throw new InvalidParameterValueException("Unable to find load balancer rule: " + id);
        }
        return lb.getNetworkId();
    }

    public Long getLoadBalancerId() {
        return id;
    }

    public List<Long> getVirtualMachineIds() {
        return virtualMachineIds;
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Load balancer Id: " + getLoadBalancerId() + " VmIds: " + StringUtils.join(getVirtualMachineIds(), ","));

        final Map<Long, List<String>> vmIdIpsMap = getVmIdIpListMap();
        boolean result = false;

        try {
            result = _lbService.assignToLoadBalancer(getLoadBalancerId(), virtualMachineIds, vmIdIpsMap);
        } catch (final CloudRuntimeException ex) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to assign load balancer rule");
        }

        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to assign load balancer rule");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final LoadBalancer lb = _entityMgr.findById(LoadBalancer.class, getLoadBalancerId());
        if (lb == null) {
            return Account.ACCOUNT_ID_SYSTEM; // bad id given, parent this command to SYSTEM so ERROR events are tracked
        }
        return lb.getAccountId();
    }

    public Map<Long, List<String>> getVmIdIpListMap() {
        final Map<Long, List<String>> vmIdIpsMap = new HashMap<>();
        if (vmIdIpMap != null && !vmIdIpMap.isEmpty()) {
            final Collection idIpsCollection = vmIdIpMap.values();
            final Iterator iter = idIpsCollection.iterator();
            while (iter.hasNext()) {
                final HashMap<String, String> idIpsMap = (HashMap<String, String>) iter.next();
                final String vmId = idIpsMap.get("vmid");
                final String vmIp = idIpsMap.get("vmip");

                final VirtualMachine lbvm = _entityMgr.findByUuid(VirtualMachine.class, vmId);
                if (lbvm == null) {
                    throw new InvalidParameterValueException("Unable to find virtual machine ID: " + vmId);
                }

                //check wether the given ip is valid ip or not
                if (vmIp == null || !NetUtils.isValidIp4(vmIp)) {
                    throw new InvalidParameterValueException("Invalid ip address " + vmIp + " passed in vmidipmap for " +
                            "vmid " + vmId);
                }
                final Long longVmId = lbvm.getId();

                List<String> ipsList = null;
                if (vmIdIpsMap.containsKey(longVmId)) {
                    ipsList = vmIdIpsMap.get(longVmId);
                } else {
                    ipsList = new ArrayList<>();
                }
                ipsList.add(vmIp);
                vmIdIpsMap.put(longVmId, ipsList);
            }
        }

        return vmIdIpsMap;
    }
}
