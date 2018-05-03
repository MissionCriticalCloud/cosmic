package com.cloud.api.command.user.vm;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiConstants.VMDetails;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.NicResponse;
import com.cloud.api.response.UserVmResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.legacymodel.dc.DataCenter;
import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.legacymodel.exceptions.InsufficientCapacityException;
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.legacymodel.exceptions.ResourceAllocationException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.network.Nic;
import com.cloud.model.enumeration.NetworkType;
import com.cloud.network.Network;
import com.cloud.uservm.UserVm;
import com.cloud.utils.net.NetUtils;

import java.util.ArrayList;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateVmNicIp", group = APICommandGroup.NicService, description = "Update the default Ip of a VM Nic", responseObject = UserVmResponse.class)
public class UpdateVmNicIpCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(AddIpToVmNicCmd.class.getName());
    private static final String s_name = "updatevmnicipresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.NIC_ID, type = CommandType.UUID, entityType = NicResponse.class, required = true,
            description = "the ID of the nic to which you want to assign private IP")
    private Long nicId;

    @Parameter(name = ApiConstants.IP_ADDRESS, type = CommandType.STRING, required = false,
            description = "Secondary IP Address")
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

    public String getAccountName() {
        return CallContext.current().getCallingAccount().getAccountName();
    }

    public long getDomainId() {
        return CallContext.current().getCallingAccount().getDomainId();
    }

    public NetworkType getNetworkType() {
        final Network ntwk = _entityMgr.findById(Network.class, getNetworkId());
        final DataCenter dc = _entityMgr.findById(DataCenter.class, ntwk.getDataCenterId());
        return dc.getNetworkType();
    }

    public Long getNetworkId() {
        final Nic nic = _entityMgr.findById(Nic.class, nicId);
        if (nic == null) {
            throw new InvalidParameterValueException("Can't find network id for specified nic");
        }
        final Long networkId = nic.getNetworkId();
        return networkId;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_NET_IP_ASSIGN;
    }

    @Override
    public String getEventDescription() {
        return "associating ip to nic id: " + getNetworkId() + " in zone " + getZoneId();
    }

    private long getZoneId() {
        final Network ntwk = _entityMgr.findById(Network.class, getNetworkId());
        if (ntwk == null) {
            throw new InvalidParameterValueException("Can't find zone id for specified");
        }
        return ntwk.getDataCenterId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.IpAddress;
    }

    @Override
    public String getSyncObjType() {
        return BaseAsyncCmd.networkSyncObject;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public Long getSyncObjId() {
        return getNetworkId();
    }

    @Override
    public void execute() throws ResourceUnavailableException, ResourceAllocationException,
            ConcurrentOperationException, InsufficientCapacityException {

        CallContext.current().setEventDetails("Nic Id: " + getNicId());
        final String ip;
        if ((ip = getIpaddress()) != null) {
            if (!NetUtils.isValidIp4(ip)) {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Invalid ip address " + ip);
            }
        }

        final UserVm vm = _userVmService.updateNicIpForVirtualMachine(this);
        final ArrayList<VMDetails> dc = new ArrayList<>();
        dc.add(VMDetails.valueOf("nics"));
        final EnumSet<VMDetails> details = EnumSet.copyOf(dc);
        if (vm != null) {
            final UserVmResponse response = _responseGenerator.createUserVmResponse(ResponseView.Restricted, "virtualmachine", details, vm).get(0);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update ip address on vm NIC. Refer to server logs for details.");
        }
    }

    public Long getNicId() {
        return nicId;
    }

    public String getIpaddress() {
        if (ipAddr != null) {
            return ipAddr;
        } else {
            return null;
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return CallContext.current().getCallingAccountId();
    }
}
