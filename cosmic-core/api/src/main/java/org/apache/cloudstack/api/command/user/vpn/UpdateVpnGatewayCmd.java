package org.apache.cloudstack.api.command.user.vpn;

import com.cloud.event.EventTypes;
import com.cloud.network.Site2SiteVpnGateway;
import com.cloud.user.Account;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseAsyncCustomIdCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.Site2SiteVpnGatewayResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateVpnGateway", description = "Updates site to site vpn local gateway", responseObject = Site2SiteVpnGatewayResponse.class, since = "4.4",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateVpnGatewayCmd extends BaseAsyncCustomIdCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateVpnGatewayCmd.class.getName());

    private static final String s_name = "updatevpngatewayresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = Site2SiteVpnGatewayResponse.class, required = true, description = "id of customer gateway")
    private Long id;

    @Parameter(name = ApiConstants.FOR_DISPLAY, type = CommandType.BOOLEAN, description = "an optional field, whether to the display the vpn to the end user or not", since = "4" +
            ".4", authorized = {RoleType.Admin})
    private Boolean display;

    @Override
    public String getEventType() {
        return EventTypes.EVENT_S2S_VPN_GATEWAY_UPDATE;
    }

    @Override
    public String getEventDescription() {
        return "Update site-to-site VPN gateway id= " + id;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////
    @Override
    public void execute() {
        final Site2SiteVpnGateway result = _s2sVpnService.updateVpnGateway(id, this.getCustomId(), getDisplay());
        if (result != null) {
            final Site2SiteVpnGatewayResponse response = _responseGenerator.createSite2SiteVpnGatewayResponse(result);
            response.setResponseName(getCommandName());
        }
    }

    public Boolean getDisplay() {
        return display;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Site2SiteVpnGateway gw = _entityMgr.findById(Site2SiteVpnGateway.class, getId());
        if (gw != null) {
            return gw.getAccountId();
        }
        return Account.ACCOUNT_ID_SYSTEM;
    }

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////
    public Long getId() {
        return id;
    }

    @Override
    public void checkUuid() {
        if (this.getCustomId() != null) {
            _uuidMgr.checkUuid(this.getCustomId(), Site2SiteVpnGateway.class);
        }
    }
}
