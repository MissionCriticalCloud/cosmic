package org.apache.cloudstack.api.command.admin.internallb;

import com.cloud.event.EventTypes;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.network.VirtualRouterProvider;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCreateCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.InternalLoadBalancerElementResponse;
import org.apache.cloudstack.api.response.ProviderResponse;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.network.element.InternalLoadBalancerElementService;

import javax.inject.Inject;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createInternalLoadBalancerElement",
        responseObject = InternalLoadBalancerElementResponse.class,
        description = "Create an Internal Load Balancer element.",
        since = "4.2.0",
        requestHasSensitiveInfo = false,
        responseHasSensitiveInfo = false)
public class CreateInternalLoadBalancerElementCmd extends BaseAsyncCreateCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateInternalLoadBalancerElementCmd.class.getName());
    private static final String s_name = "createinternalloadbalancerelementresponse";

    @Inject
    private List<InternalLoadBalancerElementService> _service;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.NETWORK_SERVICE_PROVIDER_ID,
            type = CommandType.UUID,
            entityType = ProviderResponse.class,
            required = true,
            description = "the network service provider ID of the internal load balancer element")
    private Long nspId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Virtual router element Id: " + getEntityId());
        final VirtualRouterProvider result = _service.get(0).getInternalLoadBalancerElement(getEntityId());
        if (result != null) {
            final InternalLoadBalancerElementResponse response = _responseGenerator.createInternalLbElementResponse(result);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to add Virtual Router entity to physical network");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }

    @Override
    public void create() throws ResourceAllocationException {
        final VirtualRouterProvider result = _service.get(0).addInternalLoadBalancerElement(getNspId());
        if (result != null) {
            setEntityId(result.getId());
            setEntityUuid(result.getUuid());
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to add Internal Load Balancer entity to physical network");
        }
    }

    public Long getNspId() {
        return nspId;
    }

    public void setNspId(final Long nspId) {
        this.nspId = nspId;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_SERVICE_PROVIDER_CREATE;
    }

    @Override
    public String getEventDescription() {
        return "Adding physical network element Internal Load Balancer: " + getEntityId();
    }
}
