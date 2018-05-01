package com.cloud.api.command.admin.systemvm;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.HostResponse;
import com.cloud.api.response.SystemVmResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.host.Host;
import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.legacymodel.exceptions.ManagementServerException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.exceptions.VirtualMachineMigrationException;
import com.cloud.legacymodel.user.Account;
import com.cloud.vm.VirtualMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "migrateSystemVm", group = APICommandGroup.SystemVMService, description = "Attempts Migration of a system virtual machine to the host specified.", responseObject =
        SystemVmResponse.class, entityType
        = {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class MigrateSystemVMCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(MigrateSystemVMCmd.class.getName());

    private static final String s_name = "migratesystemvmresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.HOST_ID,
            type = CommandType.UUID,
            entityType = HostResponse.class,
            required = true,
            description = "destination Host ID to migrate VM to")
    private Long hostId;

    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.VIRTUAL_MACHINE_ID,
            type = CommandType.UUID,
            entityType = SystemVmResponse.class,
            required = true,
            description = "the ID of the virtual machine")
    private Long virtualMachineId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VM_MIGRATE;
    }

    @Override
    public String getEventDescription() {
        return "Attempting to migrate VM Id: " + getVirtualMachineId() + " to host Id: " + getHostId();
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public Long getVirtualMachineId() {
        return virtualMachineId;
    }

    public Long getHostId() {
        return hostId;
    }

    @Override
    public void execute() {

        final Host destinationHost = _resourceService.getHost(getHostId());
        if (destinationHost == null) {
            throw new InvalidParameterValueException("Unable to find the host to migrate the VM, host id=" + getHostId());
        }
        try {
            CallContext.current().setEventDetails("VM Id: " + getVirtualMachineId() + " to host Id: " + getHostId());
            //FIXME : Should not be calling UserVmService to migrate all types of VMs - need a generic VM layer
            final VirtualMachine migratedVm = _userVmService.migrateVirtualMachine(getVirtualMachineId(), destinationHost);
            if (migratedVm != null) {
                // return the generic system VM instance response
                final SystemVmResponse response = _responseGenerator.createSystemVmResponse(migratedVm);
                response.setResponseName(getCommandName());
                setResponseObject(response);
            } else {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to migrate the system vm");
            }
        } catch (final ResourceUnavailableException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.RESOURCE_UNAVAILABLE_ERROR, ex.getMessage());
        } catch (final ConcurrentOperationException e) {
            s_logger.warn("Exception: ", e);
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, e.getMessage());
        } catch (final ManagementServerException e) {
            s_logger.warn("Exception: ", e);
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, e.getMessage());
        } catch (final VirtualMachineMigrationException e) {
            s_logger.warn("Exception: ", e);
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, e.getMessage());
        }
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

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }
}
