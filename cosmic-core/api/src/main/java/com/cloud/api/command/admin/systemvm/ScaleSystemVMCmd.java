package com.cloud.api.command.admin.systemvm;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.user.vm.UpgradeVMCmd;
import com.cloud.api.response.ServiceOfferingResponse;
import com.cloud.api.response.SystemVmResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ManagementServerException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.exception.VirtualMachineMigrationException;
import com.cloud.offering.ServiceOffering;
import com.cloud.user.Account;
import com.cloud.vm.VirtualMachine;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "scaleSystemVm", responseObject = SystemVmResponse.class, description = "Scale the service offering for a system vm (console proxy or secondary storage). "
        + "The system vm must be in a \"Stopped\" state for " + "this command to take effect.", entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ScaleSystemVMCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpgradeVMCmd.class.getName());
    private static final String s_name = "changeserviceforsystemvmresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = SystemVmResponse.class, required = true, description = "The ID of the system vm")
    private Long id;

    @Parameter(name = ApiConstants.SERVICE_OFFERING_ID, type = CommandType.UUID, entityType = ServiceOfferingResponse.class, required = true, description = "the service offering" +
            " ID to apply to the system vm")
    private Long serviceOfferingId;

    @Parameter(name = ApiConstants.DETAILS, type = CommandType.MAP, description = "name value pairs of custom parameters for cpu, memory and cpunumber. example details[i]" +
            ".name=value")
    private Map<String, String> details;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Map<String, String> getDetails() {
        return details;
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("SystemVm Id: " + getId());

        final ServiceOffering serviceOffering = _entityMgr.findById(ServiceOffering.class, serviceOfferingId);
        if (serviceOffering == null) {
            throw new InvalidParameterValueException("Unable to find service offering: " + serviceOfferingId);
        }

        VirtualMachine result = null;
        try {
            result = _mgr.upgradeSystemVM(this);
        } catch (final ResourceUnavailableException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.RESOURCE_UNAVAILABLE_ERROR, ex.getMessage());
        } catch (final ConcurrentOperationException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, ex.getMessage());
        } catch (final ManagementServerException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, ex.getMessage());
        } catch (final VirtualMachineMigrationException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, ex.getMessage());
        }
        if (result != null) {
            final SystemVmResponse response = _responseGenerator.createSystemVmResponse(result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to upgrade system vm");
        }
    }

    public Long getId() {
        return id;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

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

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VM_UPGRADE;
    }

    @Override
    public String getEventDescription() {
        return "Upgrading system vm: " + getId() + " to service offering: " + getServiceOfferingId();
    }

    public Long getServiceOfferingId() {
        return serviceOfferingId;
    }
}
