package org.apache.cloudstack.api.command.user.vpn;

import com.cloud.network.Site2SiteCustomerGateway;
import com.cloud.utils.Pair;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListProjectAndAccountResourcesCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.Site2SiteCustomerGatewayResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listVpnCustomerGateways", description = "Lists site to site vpn customer gateways", responseObject = Site2SiteCustomerGatewayResponse.class, entityType =
        {Site2SiteCustomerGateway.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListVpnCustomerGatewaysCmd extends BaseListProjectAndAccountResourcesCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListVpnCustomerGatewaysCmd.class.getName());

    private static final String s_name = "listvpncustomergatewaysresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = Site2SiteCustomerGatewayResponse.class, description = "id of the customer gateway")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final Pair<List<? extends Site2SiteCustomerGateway>, Integer> gws = _s2sVpnService.searchForCustomerGateways(this);
        final ListResponse<Site2SiteCustomerGatewayResponse> response = new ListResponse<>();
        final List<Site2SiteCustomerGatewayResponse> gwResponses = new ArrayList<>();
        for (final Site2SiteCustomerGateway gw : gws.first()) {
            if (gw == null) {
                continue;
            }
            final Site2SiteCustomerGatewayResponse site2SiteCustomerGatewayRes = _responseGenerator.createSite2SiteCustomerGatewayResponse(gw);
            site2SiteCustomerGatewayRes.setObjectName("vpncustomergateway");
            gwResponses.add(site2SiteCustomerGatewayRes);
        }

        response.setResponses(gwResponses, gws.second());
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
