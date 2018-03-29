package com.cloud.api.command.user.vm;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCreateCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.NicResponse;
import com.cloud.api.response.NicSecondaryIpResponse;
import com.cloud.context.CallContext;
import com.cloud.dc.DataCenter;
import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.model.enumeration.NetworkType;
import com.cloud.network.Network;
import com.cloud.utils.exception.InvalidParameterValueException;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.Nic;
import com.cloud.vm.NicSecondaryIp;
import com.cloud.vm.VirtualMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "addIpToNic", group = APICommandGroup.NicService, description = "Assigns secondary IP to NIC", responseObject = NicSecondaryIpResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class AddIpToVmNicCmd extends BaseAsyncCreateCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(AddIpToVmNicCmd.class.getName());
    private static final String s_name = "addiptovmnicresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.NIC_ID, type = CommandType.UUID, entityType = NicResponse.class, required = true, description = "the ID of the nic to which you want to assign" +
            " private IP")
    private Long nicId;

    @Parameter(name = ApiConstants.IP_ADDRESS, type = CommandType.STRING, required = false, description = "Secondary IP Address")
    private String ipAddr;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public static String getResultObjectName() {
        return "addressinfo";
    }

    public String getEntityTable() {
        return "nic_secondary_ips";
    }

    private NetworkType getNetworkType() {
        final Network ntwk = _entityMgr.findById(Network.class, getNetworkId());
        final DataCenter dc = _entityMgr.findById(DataCenter.class, ntwk.getDataCenterId());
        return dc.getNetworkType();
    }

    private long getNetworkId() {
        final Nic nic = _entityMgr.findById(Nic.class, nicId);
        if (nic == null) {
            throw new InvalidParameterValueException("Can't find network id for specified nic");
        }
        return nic.getNetworkId();
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_NIC_SECONDARY_IP_ASSIGN;
    }

    @Override
    public String getEventDescription() {
        return "associating ip to nic id=" + getNicId() + " belonging to network id=" + getNetworkId();
    }

    public long getNicId() {
        return nicId;
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.IpAddress;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public Long getSyncObjId() {
        return getNetworkId();
    }

    @Override
    public void execute() throws ResourceUnavailableException, ResourceAllocationException, ConcurrentOperationException, InsufficientCapacityException {

        CallContext.current().setEventDetails("Nic Id: " + getNicId());
        final NicSecondaryIp result = _entityMgr.findById(NicSecondaryIp.class, getEntityId());

        if (result != null) {
            CallContext.current().setEventDetails("secondary Ip Id: " + getEntityId());
            boolean success;
            success = _networkService.configureNicSecondaryIp(result);

            if (!success) {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to set security group rules for the secondary ip");
            }

            final NicSecondaryIpResponse response = _responseGenerator.createSecondaryIPToNicResponse(result);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to assign secondary ip to nic");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Nic nic = _entityMgr.findById(Nic.class, nicId);
        if (nic == null) {
            throw new InvalidParameterValueException("Can't find nic for id specified");
        }
        final long vmId = nic.getInstanceId();
        final VirtualMachine vm = _entityMgr.findById(VirtualMachine.class, vmId);

        return vm.getAccountId();
    }

    @Override
    public void create() {
        final String ip;
        final NicSecondaryIp result;
        if ((ip = getIpaddress()) != null) {
            if (!NetUtils.isValidIp4(ip)) {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Invalid ip address " + ip);
            }
        }

        try {
            result = _networkService.allocateSecondaryGuestIP(getNicId(), getIpaddress());
            if (result != null) {
                setEntityId(result.getId());
                setEntityUuid(result.getUuid());
            }
        } catch (final InsufficientAddressCapacityException e) {
            throw new InvalidParameterValueException("Allocating guest ip for nic failed : " + e.getMessage());
        }

        if (result == null) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to assign secondary ip to nic");
        }
    }

    private String getIpaddress() {
        if (ipAddr != null) {
            return ipAddr;
        } else {
            return null;
        }
    }
}
