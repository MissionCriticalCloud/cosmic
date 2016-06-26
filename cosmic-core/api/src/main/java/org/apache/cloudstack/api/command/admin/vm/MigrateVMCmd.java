package org.apache.cloudstack.api.command.admin.vm;

import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ManagementServerException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.exception.VirtualMachineMigrationException;
import com.cloud.host.Host;
import com.cloud.storage.StoragePool;
import com.cloud.user.Account;
import com.cloud.uservm.UserVm;
import com.cloud.vm.VirtualMachine;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.HostResponse;
import org.apache.cloudstack.api.response.StoragePoolResponse;
import org.apache.cloudstack.api.response.UserVmResponse;
import org.apache.cloudstack.context.CallContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "migrateVirtualMachine",
        description = "Attempts Migration of a VM to a different host or Root volume of the vm to a different storage pool",
        responseObject = UserVmResponse.class, entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false,
        responseHasSensitiveInfo = true)
public class MigrateVMCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(MigrateVMCmd.class.getName());

    private static final String s_name = "migratevirtualmachineresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.HOST_ID,
            type = CommandType.UUID,
            entityType = HostResponse.class,
            required = false,
            description = "Destination Host ID to migrate VM to. Required for live migrating a VM from host to host")
    private Long hostId;

    @Parameter(name = ApiConstants.VIRTUAL_MACHINE_ID,
            type = CommandType.UUID,
            entityType = UserVmResponse.class,
            required = true,
            description = "the ID of the virtual machine")
    private Long virtualMachineId;

    @Parameter(name = ApiConstants.STORAGE_ID,
            type = CommandType.UUID,
            entityType = StoragePoolResponse.class,
            required = false,
            description = "Destination storage pool ID to migrate VM volumes to. Required for migrating the root disk volume")
    private Long storageId;

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

    public Long getVirtualMachineId() {
        return virtualMachineId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public Long getHostId() {
        return hostId;
    }

    @Override
    public void execute() {
        if (getHostId() == null && getStoragePoolId() == null) {
            throw new InvalidParameterValueException("Either hostId or storageId must be specified");
        }

        if (getHostId() != null && getStoragePoolId() != null) {
            throw new InvalidParameterValueException("Only one of hostId and storageId can be specified");
        }

        final UserVm userVm = _userVmService.getUserVm(getVirtualMachineId());
        if (userVm == null) {
            throw new InvalidParameterValueException("Unable to find the VM by id=" + getVirtualMachineId());
        }

        Host destinationHost = null;
        if (getHostId() != null) {
            destinationHost = _resourceService.getHost(getHostId());
            if (destinationHost == null) {
                throw new InvalidParameterValueException("Unable to find the host to migrate the VM, host id=" + getHostId());
            }
            if (destinationHost.getType() != Host.Type.Routing) {
                throw new InvalidParameterValueException("The specified host(" + destinationHost.getName() + ") is not suitable to migrate the VM, please specify another one");
            }
            CallContext.current().setEventDetails("VM Id: " + getVirtualMachineId() + " to host Id: " + getHostId());
        }

        StoragePool destStoragePool = null;
        if (getStoragePoolId() != null) {
            destStoragePool = _storageService.getStoragePool(getStoragePoolId());
            if (destStoragePool == null) {
                throw new InvalidParameterValueException("Unable to find the storage pool to migrate the VM");
            }
            CallContext.current().setEventDetails("VM Id: " + getVirtualMachineId() + " to storage pool Id: " + getStoragePoolId());
        }

        try {
            VirtualMachine migratedVm = null;
            if (getHostId() != null) {
                migratedVm = _userVmService.migrateVirtualMachine(getVirtualMachineId(), destinationHost);
            } else if (getStoragePoolId() != null) {
                migratedVm = _userVmService.vmStorageMigration(getVirtualMachineId(), destStoragePool);
            }
            if (migratedVm != null) {
                final UserVmResponse response = _responseGenerator.createUserVmResponse(ResponseView.Full, "virtualmachine", (UserVm) migratedVm).get(0);
                response.setResponseName(getCommandName());
                setResponseObject(response);
            } else {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to migrate vm");
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

    public Long getStoragePoolId() {
        return storageId;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final UserVm userVm = _entityMgr.findById(UserVm.class, getVirtualMachineId());
        if (userVm != null) {
            return userVm.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }
}
