package com.cloud.api.command.user.vpn;

import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.DomainResponse;
import com.cloud.api.response.Site2SiteCustomerGatewayResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.network.Site2SiteCustomerGateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateVpnCustomerGateway", description = "Update site to site vpn customer gateway", responseObject = Site2SiteCustomerGatewayResponse.class, entityType =
        {Site2SiteCustomerGateway.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateVpnCustomerGatewayCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateVpnCustomerGatewayCmd.class.getName());

    private static final String s_name = "updatevpncustomergatewayresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.ID,
            type = CommandType.UUID,
            entityType = Site2SiteCustomerGatewayResponse.class,
            required = true,
            description = "id of customer gateway")
    private Long id;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, required = false, description = "name of this customer gateway")
    private String name;

    @Parameter(name = ApiConstants.GATEWAY, type = CommandType.STRING, required = true, description = "public ip address id of the customer gateway")
    private String gatewayIp;

    @Parameter(name = ApiConstants.CIDR_LIST, type = CommandType.STRING, required = true, description = "guest cidr of the customer gateway")
    private String guestCidrList;

    @Parameter(name = ApiConstants.IPSEC_PSK, type = CommandType.STRING, required = true, description = "IPsec Preshared-Key of the customer gateway. Cannot contain newline or " +
            "double quotes.")
    private String ipsecPsk;

    @Parameter(name = ApiConstants.IKE_POLICY, type = CommandType.STRING, required = true, description = "IKE policy of the customer gateway")
    private String ikePolicy;

    @Parameter(name = ApiConstants.ESP_POLICY, type = CommandType.STRING, required = true, description = "ESP policy of the customer gateway")
    private String espPolicy;

    @Parameter(name = ApiConstants.IKE_LIFETIME,
            type = CommandType.LONG,
            required = false,
            description = "Lifetime of phase 1 VPN connection to the customer gateway, in seconds")
    private Long ikeLifetime;

    @Parameter(name = ApiConstants.ESP_LIFETIME,
            type = CommandType.LONG,
            required = false,
            description = "Lifetime of phase 2 VPN connection to the customer gateway, in seconds")
    private Long espLifetime;

    @Parameter(name = ApiConstants.DPD, type = CommandType.BOOLEAN, required = false, description = "If DPD is enabled for VPN connection")
    private Boolean dpd;

    @Parameter(name = ApiConstants.FORCE_ENCAP, type = CommandType.BOOLEAN, required = false, description = "Force encapsulation for Nat Traversal")
    private Boolean encap;

    @Parameter(name = ApiConstants.ACCOUNT, type = CommandType.STRING, description = "the account associated with the gateway. Must be used with the domainId parameter.")
    private String accountName;

    @Parameter(name = ApiConstants.DOMAIN_ID,
            type = CommandType.UUID,
            entityType = DomainResponse.class,
            description = "the domain ID associated with the gateway. If used with the account parameter returns the "
                    + "gateway associated with the account for the specified domain.")
    private Long domainId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIpsecPsk() {
        return ipsecPsk;
    }

    public String getGuestCidrList() {
        return guestCidrList;
    }

    public String getGatewayIp() {
        return gatewayIp;
    }

    public String getIkePolicy() {
        return ikePolicy;
    }

    public String getEspPolicy() {
        return espPolicy;
    }

    public Long getIkeLifetime() {
        return ikeLifetime;
    }

    public Long getEspLifetime() {
        return espLifetime;
    }

    public Boolean getDpd() {
        return dpd;
    }

    public Boolean getEncap() {
        return encap;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_S2S_VPN_CUSTOMER_GATEWAY_UPDATE;
    }

    @Override
    public String getEventDescription() {
        return "Update site-to-site VPN customer gateway";
    }

    @Override
    public void execute() {
        final Site2SiteCustomerGateway result = _s2sVpnService.updateCustomerGateway(this);
        if (result != null) {
            final Site2SiteCustomerGatewayResponse response = _responseGenerator.createSite2SiteCustomerGatewayResponse(result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update customer VPN gateway");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        Long accountId = _accountService.finalyzeAccountId(accountName, domainId, null, true);
        if (accountId == null) {
            accountId = CallContext.current().getCallingAccount().getId();
        }
        return accountId;
    }
}
