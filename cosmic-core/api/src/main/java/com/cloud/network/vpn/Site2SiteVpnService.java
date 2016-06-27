package com.cloud.network.vpn;

import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Site2SiteCustomerGateway;
import com.cloud.network.Site2SiteVpnConnection;
import com.cloud.network.Site2SiteVpnGateway;
import com.cloud.utils.Pair;
import org.apache.cloudstack.api.command.user.vpn.CreateVpnConnectionCmd;
import org.apache.cloudstack.api.command.user.vpn.CreateVpnCustomerGatewayCmd;
import org.apache.cloudstack.api.command.user.vpn.CreateVpnGatewayCmd;
import org.apache.cloudstack.api.command.user.vpn.DeleteVpnConnectionCmd;
import org.apache.cloudstack.api.command.user.vpn.DeleteVpnCustomerGatewayCmd;
import org.apache.cloudstack.api.command.user.vpn.DeleteVpnGatewayCmd;
import org.apache.cloudstack.api.command.user.vpn.ListVpnConnectionsCmd;
import org.apache.cloudstack.api.command.user.vpn.ListVpnCustomerGatewaysCmd;
import org.apache.cloudstack.api.command.user.vpn.ListVpnGatewaysCmd;
import org.apache.cloudstack.api.command.user.vpn.ResetVpnConnectionCmd;
import org.apache.cloudstack.api.command.user.vpn.UpdateVpnCustomerGatewayCmd;

import java.util.List;

public interface Site2SiteVpnService {
    Site2SiteVpnGateway createVpnGateway(CreateVpnGatewayCmd cmd);

    Site2SiteCustomerGateway createCustomerGateway(CreateVpnCustomerGatewayCmd cmd);

    Site2SiteVpnConnection startVpnConnection(long id) throws ResourceUnavailableException;

    Site2SiteVpnGateway getVpnGateway(Long vpnGatewayId);

    Site2SiteVpnConnection createVpnConnection(CreateVpnConnectionCmd cmd) throws NetworkRuleConflictException;

    boolean deleteCustomerGateway(DeleteVpnCustomerGatewayCmd deleteVpnCustomerGatewayCmd);

    boolean deleteVpnGateway(DeleteVpnGatewayCmd deleteVpnGatewayCmd);

    boolean deleteVpnConnection(DeleteVpnConnectionCmd deleteVpnConnectionCmd) throws ResourceUnavailableException;

    Site2SiteVpnConnection resetVpnConnection(ResetVpnConnectionCmd resetVpnConnectionCmd) throws ResourceUnavailableException;

    Pair<List<? extends Site2SiteCustomerGateway>, Integer> searchForCustomerGateways(ListVpnCustomerGatewaysCmd listVpnCustomerGatewaysCmd);

    Pair<List<? extends Site2SiteVpnGateway>, Integer> searchForVpnGateways(ListVpnGatewaysCmd listVpnGatewaysCmd);

    Pair<List<? extends Site2SiteVpnConnection>, Integer> searchForVpnConnections(ListVpnConnectionsCmd listVpnConnectionsCmd);

    Site2SiteCustomerGateway updateCustomerGateway(UpdateVpnCustomerGatewayCmd updateVpnCustomerGatewayCmd);

    Site2SiteVpnConnection updateVpnConnection(long id, String customId, Boolean forDisplay);

    Site2SiteVpnGateway updateVpnGateway(Long id, String customId, Boolean forDisplay);
}
