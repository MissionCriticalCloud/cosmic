package com.cloud.api.command.user.vm;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.ServiceOfferingResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.api.response.UserVmResponse;
import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.ManagementServerException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.exception.VirtualMachineMigrationException;
import com.cloud.user.Account;
import com.cloud.uservm.UserVm;
import com.cloud.vm.VirtualMachine;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "scaleVirtualMachine", group = APICommandGroup.VirtualMachineService, description = "Scales the virtual machine to a new service offering.", responseObject = SuccessResponse.class, responseView =
        ResponseView.Restricted, entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ScaleVMCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ScaleVMCmd.class.getName());
    private static final String s_name = "scalevirtualmachineresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = UserVmResponse.class,
            required = true, description = "The ID of the virtual machine")
    private Long id;

    @Parameter(name = ApiConstants.SERVICE_OFFERING_ID, type = CommandType.UUID, entityType = ServiceOfferingResponse.class,
            required = true, description = "the ID of the service offering for the virtual machine")
    private Long serviceOfferingId;

    @Parameter(name = ApiConstants.DETAILS, type = BaseCmd.CommandType.MAP, description = "name value pairs of custom parameters for cpu,memory and cpunumber. example details[i]" +
            ".name=value")
    private Map<String, String> details;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public static String getResultObjectName() {
        return "virtualmachine";
    }

    //instead of reading a map directly we are using collections.
    //it is because details.values() cannot be cast to a map.
    //it gives a exception
    public Map<String, String> getDetails() {
        final Map<String, String> customparameterMap = new HashMap<>();
        if (details != null && details.size() != 0) {
            final Collection parameterCollection = details.values();
            final Iterator iter = parameterCollection.iterator();
            while (iter.hasNext()) {
                final HashMap<String, String> value = (HashMap<String, String>) iter.next();
                for (final String key : value.keySet()) {
                    customparameterMap.put(key, value.get(key));
                }
            }
        }
        return customparameterMap;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VM_UPGRADE;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventDescription() {
        return "upgrading vm: " + getId() + " to service offering: " + getServiceOfferingId();
    }

    public Long getId() {
        return id;
    }

    public Long getServiceOfferingId() {
        return serviceOfferingId;
    }

    @Override
    public void execute() {
        final UserVm result;
        try {
            result = _userVmService.upgradeVirtualMachine(this);
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
            final List<UserVmResponse> responseList = _responseGenerator.createUserVmResponse(ResponseView.Restricted, "virtualmachine", result);
            final UserVmResponse response = responseList.get(0);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to scale vm");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final UserVm userVm = _entityMgr.findById(UserVm.class, getId());
        if (userVm != null) {
            return userVm.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }
}
