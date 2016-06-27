package org.apache.cloudstack.api.command.admin.router;

import com.cloud.event.EventTypes;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.network.VirtualRouterProvider;
import com.cloud.network.VirtualRouterProvider.Type;
import com.cloud.network.element.VirtualRouterElementService;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCreateCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.ProviderResponse;
import org.apache.cloudstack.api.response.VirtualRouterProviderResponse;
import org.apache.cloudstack.context.CallContext;

import javax.inject.Inject;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createVirtualRouterElement", responseObject = VirtualRouterProviderResponse.class, description = "Create a virtual router element.",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreateVirtualRouterElementCmd extends BaseAsyncCreateCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateVirtualRouterElementCmd.class.getName());
    private static final String s_name = "createvirtualrouterelementresponse";

    @Inject
    private List<VirtualRouterElementService> _service;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.NETWORK_SERVICE_PROVIDER_ID,
            type = CommandType.UUID,
            entityType = ProviderResponse.class,
            required = true,
            description = "the network service provider ID of the virtual router element")
    private Long nspId;

    @Parameter(name = ApiConstants.PROVIDER_TYPE,
            type = CommandType.UUID,
            entityType = ProviderResponse.class,
            description = "The provider type. Supported types are VirtualRouter (default) and VPCVirtualRouter")
    private String providerType;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Virtual router element Id: " + getEntityId());
        final VirtualRouterProvider result = _service.get(0).getCreatedElement(getEntityId());
        if (result != null) {
            final VirtualRouterProviderResponse response = _responseGenerator.createVirtualRouterProviderResponse(result);
            if (response != null) {
                response.setResponseName(getCommandName());
                this.setResponseObject(response);
            }
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to add Virtual Router entity to physical network");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void create() throws ResourceAllocationException {
        final VirtualRouterProvider result = _service.get(0).addElement(getNspId(), getProviderType());
        if (result != null) {
            setEntityId(result.getId());
            setEntityUuid(result.getUuid());
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to add Virtual Router entity to physical network");
        }
    }

    public Long getNspId() {
        return nspId;
    }

    public void setNspId(final Long nspId) {
        this.nspId = nspId;
    }

    public Type getProviderType() {
        if (providerType != null) {
            if (providerType.equalsIgnoreCase(Type.VirtualRouter.toString())) {
                return Type.VirtualRouter;
            } else if (providerType.equalsIgnoreCase(Type.VPCVirtualRouter.toString())) {
                return Type.VPCVirtualRouter;
            } else {
                throw new InvalidParameterValueException("Invalid providerType specified");
            }
        }
        return Type.VirtualRouter;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_SERVICE_PROVIDER_CREATE;
    }

    @Override
    public String getEventDescription() {
        return "Adding physical network ServiceProvider Virtual Router: " + getEntityId();
    }
}
