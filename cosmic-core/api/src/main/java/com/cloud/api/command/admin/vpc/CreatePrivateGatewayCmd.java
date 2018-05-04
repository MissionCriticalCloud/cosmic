package com.cloud.api.command.admin.vpc;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.BaseAsyncCreateCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.NetworkACLResponse;
import com.cloud.api.response.NetworkResponse;
import com.cloud.api.response.PrivateGatewayResponse;
import com.cloud.api.response.VpcResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.legacymodel.exceptions.InsufficientCapacityException;
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.legacymodel.exceptions.ResourceAllocationException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.network.vpc.PrivateGateway;
import com.cloud.legacymodel.network.vpc.Vpc;
import com.cloud.legacymodel.network.vpc.VpcGateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createPrivateGateway", group = APICommandGroup.VPCService, description = "Creates a private gateway", responseObject = PrivateGatewayResponse.class, entityType = {VpcGateway
        .class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreatePrivateGatewayCmd extends BaseAsyncCreateCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CreatePrivateGatewayCmd.class.getName());

    private static final String s_name = "createprivategatewayresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.GATEWAY, type = CommandType.STRING, description = "the gateway of the Private gateway (DEPRECATED!).")
    private String gateway;

    @Parameter(name = ApiConstants.NETMASK, type = CommandType.STRING, description = "the netmask of the Private gateway (DEPRECATED!).")
    private String netmask;

    @Parameter(name = ApiConstants.IP_ADDRESS, type = CommandType.STRING, required = true, description = "the IP address of the Private gateaway")
    private String ipAddress;

    @Parameter(name = ApiConstants.NETWORK_ID,
            type = CommandType.UUID,
            required = true,
            entityType = NetworkResponse.class,
            description = "the uuid of the private network to use for the private gateway")
    private Long networkId;

    @Parameter(name = ApiConstants.VPC_ID, type = CommandType.UUID, entityType = VpcResponse.class, required = true, description = "the VPC network belongs to")
    private Long vpcId;

    @Parameter(name = ApiConstants.SOURCE_NAT_SUPPORTED,
            type = CommandType.BOOLEAN,
            required = false,
            description = "source NAT supported value. Default value false. If 'true' source NAT is enabled on the private gateway"
                    + " 'false': sourcenat is not supported")
    private Boolean isSourceNat;

    @Parameter(name = ApiConstants.ACL_ID, type = CommandType.UUID, entityType = NetworkACLResponse.class, required = false, description = "the ID of the network ACL")
    private Long aclId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void create() throws ResourceAllocationException {
        PrivateGateway result;
        try {
            result =
                    _vpcService.createVpcPrivateGateway(getVpcId(), getStartIp(), getGateway(), getNetmask(), getEntityDomainId(),
                            getNetworkId(), getIsSourceNat(), getAclId());
        } catch (final InsufficientCapacityException ex) {
            s_logger.info(ex.toString());
            throw new ServerApiException(ApiErrorCode.INSUFFICIENT_CAPACITY_ERROR, ex.getMessage());
        } catch (final ConcurrentOperationException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, ex.getMessage());
        }

        if (result != null) {
            setEntityId(result.getId());
            setEntityUuid(result.getUuid());
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create private gateway");
        }
    }

    public Long getVpcId() {
        return vpcId;
    }

    public String getStartIp() {
        return ipAddress;
    }

    public String getGateway() {
        return gateway;
    }

    public String getNetmask() {
        return netmask;
    }

    private Long getNetworkId() {
        return networkId;
    }

    public Boolean getIsSourceNat() {
        if (isSourceNat == null) {
            return false;
        }
        return isSourceNat;
    }

    public Long getAclId() {
        return aclId;
    }

    @Override
    public void execute() throws InsufficientCapacityException, ConcurrentOperationException, ResourceAllocationException, ResourceUnavailableException {
        final PrivateGateway result = _vpcService.applyVpcPrivateGateway(getEntityId(), true);
        if (result != null) {
            final PrivateGatewayResponse response = _responseGenerator.createPrivateGatewayResponse(result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create private gateway");
        }
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
        return CallContext.current().getCallingAccount().getId();
    }

    private long getEntityDomainId() {
        return CallContext.current().getCallingAccount().getDomainId();
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_PRIVATE_GATEWAY_CREATE;
    }

    @Override
    public String getEventDescription() {
        return "Applying VPC private gateway. Private gateway Id: " + getEntityId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.PrivateGateway;
    }

    @Override
    public String getSyncObjType() {
        return BaseAsyncCmd.vpcSyncObject;
    }

    @Override
    public Long getSyncObjId() {
        final Vpc vpc = _entityMgr.findById(Vpc.class, vpcId);
        if (vpc == null) {
            throw new InvalidParameterValueException("Invalid id is specified for the vpc");
        }
        return vpc.getId();
    }
}
