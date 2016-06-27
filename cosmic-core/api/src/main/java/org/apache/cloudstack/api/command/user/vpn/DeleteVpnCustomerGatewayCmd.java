package org.apache.cloudstack.api.command.user.vpn;

import com.cloud.event.EventTypes;
import com.cloud.network.Site2SiteCustomerGateway;
import com.cloud.user.Account;
import org.apache.cloudstack.acl.SecurityChecker.AccessType;
import org.apache.cloudstack.api.ACL;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.Site2SiteCustomerGatewayResponse;
import org.apache.cloudstack.api.response.SuccessResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteVpnCustomerGateway", description = "Delete site to site vpn customer gateway", responseObject = SuccessResponse.class, entityType =
        {Site2SiteCustomerGateway.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteVpnCustomerGatewayCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteVpnCustomerGatewayCmd.class.getName());

    private static final String s_name = "deletevpncustomergatewayresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID,
            type = CommandType.UUID,
            entityType = Site2SiteCustomerGatewayResponse.class,
            required = true,
            description = "id of customer gateway")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_S2S_VPN_CUSTOMER_GATEWAY_DELETE;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventDescription() {
        return "Delete site-to-site VPN customer gateway for account " + getEntityOwnerId();
    }

    public Long getId() {
        return id;
    }

    @Override
    public void execute() {
        final boolean result = _s2sVpnService.deleteCustomerGateway(this);
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete customer VPN gateway");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Site2SiteCustomerGateway gw = _entityMgr.findById(Site2SiteCustomerGateway.class, getId());
        if (gw != null) {
            return gw.getAccountId();
        }
        return Account.ACCOUNT_ID_SYSTEM;
    }
}
