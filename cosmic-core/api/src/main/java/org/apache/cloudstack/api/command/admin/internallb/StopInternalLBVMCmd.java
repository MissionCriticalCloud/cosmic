package org.apache.cloudstack.api.command.admin.internallb;

import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.router.VirtualRouter;
import com.cloud.network.router.VirtualRouter.Role;
import com.cloud.vm.VirtualMachine;
import org.apache.cloudstack.acl.SecurityChecker.AccessType;
import org.apache.cloudstack.api.ACL;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.DomainRouterResponse;
import org.apache.cloudstack.context.CallContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "stopInternalLoadBalancerVM", description = "Stops an Internal LB vm.", responseObject = DomainRouterResponse.class, entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class StopInternalLBVMCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(StopInternalLBVMCmd.class.getName());
    private static final String s_name = "stopinternallbvmresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////
    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = DomainRouterResponse.class, required = true, description = "the ID of the internal lb vm")
    private Long id;

    @Parameter(name = ApiConstants.FORCED, type = CommandType.BOOLEAN, required = false, description = "Force stop the VM. The caller knows the VM is stopped.")
    private Boolean forced;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_INTERNAL_LB_VM_STOP;
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public String getEventDescription() {
        return "stopping internal lb vm: " + getId();
    }

    @Override
    public Long getInstanceId() {
        return getId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.InternalLbVm;
    }

    public Long getId() {
        return id;
    }

    @Override
    public void execute() throws ConcurrentOperationException, ResourceUnavailableException {
        CallContext.current().setEventDetails("Internal lb vm Id: " + getId());
        VirtualRouter result = null;
        final VirtualRouter vm = _routerService.findRouter(getId());
        if (vm == null || vm.getRole() != Role.INTERNAL_LB_VM) {
            throw new InvalidParameterValueException("Can't find internal lb vm by id");
        } else {
            result = _internalLbSvc.stopInternalLbVm(getId(), isForced(), CallContext.current().getCallingAccount(), CallContext.current().getCallingUserId());
        }

        if (result != null) {
            final DomainRouterResponse response = _responseGenerator.createDomainRouterResponse(result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to stop internal lb vm");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final VirtualRouter vm = _entityMgr.findById(VirtualRouter.class, getId());
        if (vm != null && vm.getRole() == Role.INTERNAL_LB_VM) {
            return vm.getAccountId();
        } else {
            throw new InvalidParameterValueException("Unable to find internal lb vm by id");
        }
    }

    public boolean isForced() {
        return (forced != null) ? forced : false;
    }
}
